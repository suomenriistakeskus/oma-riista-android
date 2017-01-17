package fi.riista.mobile.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.Permit;
import fi.riista.mobile.models.PermitSpeciesAmount;
import fi.riista.mobile.models.Specimen;
import fi.riista.mobile.network.PreloadPermitsTask;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.util.JsonSerializator;

/**
 * Singleton permit manager
 * Manages permit information associated with the user.
 */
public class PermitManager {

    private static final String PRELOAD_PERMIT_FILE_PATH = "preloadPermits.json";
    private static final String MANUAL_PERMIT_FILE_PATH = "manualPermits.json";

    private static PermitManager sInstance;

    // Permit list fetched from backend
    private Map<String, Permit> mPreloadPermits = new HashMap<>();

    // List of permits user has requested separately
    private Map<String, Permit> mManualPermits = new HashMap<>();

    private Context mContext;

    private static ObjectMapper sMapper = JsonSerializator.createDefaultMapper();

    public static PermitManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PermitManager(context.getApplicationContext());
        }

        return sInstance;
    }

    private PermitManager(Context context) {
        mContext = context;

        sMapper.registerModule(new JodaModule());
    }

    /**
     * List of all known permits associated with user
     * Combines list from preloaded permits and permits manually fetched with permit number
     *
     * @return All known permits
     */
    private List<Permit> getAllPermits() {
        List<Permit> results = new ArrayList<>();

        reloadPermits();

        // Preloaded permits override manual ones
        Map<String, Permit> combinedPermits = new HashMap<>(mManualPermits);
        combinedPermits.putAll(mPreloadPermits);

        results.addAll(combinedPermits.values());
        sortPermitList(results);

        return results;
    }

    private void reloadPermits() {
        if (mPreloadPermits == null || mPreloadPermits.isEmpty()) {
            mPreloadPermits = readPermitFile(PRELOAD_PERMIT_FILE_PATH);
        }

        if (mManualPermits == null || mManualPermits.isEmpty()) {
            mManualPermits = readPermitFile(MANUAL_PERMIT_FILE_PATH);
        }
    }

    private Map<String, Permit> readPermitFile(String filePath) {
        List<Permit> results = new ArrayList<>();

        File permitFile = new File(mContext.getFilesDir(), filePath);
        if (permitFile.exists()) {
            try {
                results = sMapper.readValue(new File(mContext.getFilesDir(), filePath), sMapper.getTypeFactory().constructCollectionType(List.class, Permit.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Map<String, Permit> retVal = new HashMap<>();
        for (Permit item : results) {
            retVal.put(item.getPermitNumber(), item);
        }

        return retVal;
    }

    /**
     * Get list of all available permits
     *
     * @return Permits matching criteria
     */
    public List<Permit> getAvailablePermits() {
        List<Permit> results = new ArrayList<>();

        for (Permit item : getAllPermits()) {
            if (!item.getUnavailable()) {
                results.add(item);
            }
        }

        return results;
    }

    /**
     * Sort list alphapetically based on permit number
     *
     * @param permitList List to sort
     */
    private void sortPermitList(List<Permit> permitList) {
        Comparator<Permit> comparator = new Comparator<Permit>() {
            @Override
            public int compare(Permit permit1, Permit permit2) {
                return permit1.getPermitNumber().compareTo(permit2.getPermitNumber());
            }
        };
        Collections.sort(permitList, comparator);
    }

    /**
     * Get permit with number.
     *
     * @param permitNumber Permit number to search with
     * @return Permit or null
     */
    public Permit getPermit(String permitNumber) {
        reloadPermits();

        if (mPreloadPermits.containsKey(permitNumber)) {
            return mPreloadPermits.get(permitNumber);
        }

        if (mManualPermits.containsKey(permitNumber)) {
            return mManualPermits.get(permitNumber);
        }

        return null;
    }

    /**
     * Get species amount details from permit.
     * Return first matching species item in case there are multiple items for same species
     *
     * @param permit      Permit containing specimen amount details
     * @param speciesCode Selected species
     * @return Specimen amount details matching parameters or null if no match
     */
    public PermitSpeciesAmount getSpeciesAmountFromPermit(Permit permit, int speciesCode) {
        if (permit != null) {
            for (PermitSpeciesAmount speciesAmount : permit.getSpeciesAmounts()) {
                // Same animal may be included multiple times in list. Select first one where date matches.
                if (speciesAmount.getGameSpeciesCode() == speciesCode) {
                    return speciesAmount;
                }
            }
        }

        return null;
    }

    /**
     * Clear stored permit data.
     */
    public void clearPermits() {
        mPreloadPermits.clear();
        mManualPermits.clear();

        File file = new File(mContext.getFilesDir(), PRELOAD_PERMIT_FILE_PATH);
        if (file.exists()) {
            if (!file.delete()) {
                Log.d(Utils.LOG_TAG, "PermitManager: Failed to delete " + file.getAbsolutePath());
            }
        }

        file = new File(mContext.getFilesDir(), MANUAL_PERMIT_FILE_PATH);
        if (file.exists()) {
            if (!file.delete()) {
                Log.d(Utils.LOG_TAG, "PermitManager: Failed to delete " + file.getAbsolutePath());
            }
        }
    }

    /**
     * Save new permit details locally.
     * If permit with same number exist do nothing
     *
     * @param permit New permit to add
     */
    public void addManualPermit(Permit permit) {
        reloadPermits();

        if (mPreloadPermits.containsKey(permit.getPermitNumber())) {
            return;
        }

        // Overwrite if exists
        mManualPermits.put(permit.getPermitNumber(), permit);

        // Persist to file
        try {
            sMapper.writeValue(new File(mContext.getFilesDir(), MANUAL_PERMIT_FILE_PATH), mManualPermits.values());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void preloadPermits(final WorkContext context) {
        PreloadPermitsTask task = new PreloadPermitsTask(context) {

            @Override
            protected void onFinishText(String text) {
                try {
                    FileOutputStream file = mContext.openFileOutput(PRELOAD_PERMIT_FILE_PATH, Context.MODE_PRIVATE);
                    OutputStreamWriter outputWriter = new OutputStreamWriter(file);
                    outputWriter.write(text);
                    outputWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Forces reload the next time permits are accessed.
                mPreloadPermits.clear();
            }

            @Override
            protected void onError() {
                Log.e(Utils.LOG_TAG, String.format("Failed to preload permits [%d]", getHttpStatusCode()));
            }
        };
        task.start();
    }

    /**
     * Validate that entry information matches permit.
     * Checks:
     * * Permit allows harvest of selected species.
     * * Permit allows harvest of species at selected time.
     *
     * @param entry  Entry to check
     * @param permit Permit to check against
     * @return True if entry information is set according to permit
     */
    @SuppressLint("SimpleDateFormat")
    private boolean validateEntryWithPermit(GameHarvest entry, Permit permit) {
        boolean speciesOk = false;
        boolean dateOk = false;
        boolean specimenOk;

        if (entry == null || permit == null) {
            return false;
        }

        // Validate species and date
        PermitSpeciesAmount speciesAmountMatch = null;
        for (PermitSpeciesAmount speciesAmount : permit.getSpeciesAmounts()) {
            if (speciesAmount.getGameSpeciesCode() != null && speciesAmount.getGameSpeciesCode().equals(entry.mSpeciesID)) {
                speciesOk = true;

                if (DateTimeUtils.isDateInRange(LocalDate.fromCalendarFields(entry.mTime), speciesAmount.getBeginDate(), speciesAmount.getEndDate())
                        || DateTimeUtils.isDateInRange(LocalDate.fromCalendarFields(entry.mTime), speciesAmount.getBeginDate2(), speciesAmount.getEndDate2())) {
                    dateOk = true;

                    speciesAmountMatch = speciesAmount;
                    break;
                }
            }
        }

        if (speciesAmountMatch == null) {
            Log.d(Utils.LOG_TAG, String.format("Date %S is not valid for [%d]", new SimpleDateFormat("dd-MM-yyy").format(entry.mTime.getTime()), entry.mSpeciesID));
            return false;
        }

        specimenOk = validateEntrySpecimenForPermit(entry, speciesAmountMatch);

        return speciesOk && dateOk && specimenOk;
    }

    private boolean validateEntrySpecimenForPermit(GameHarvest entry, PermitSpeciesAmount speciesAmount) {
        if (speciesAmount.getGenderRequired() || speciesAmount.getAgeRequired() || speciesAmount.getWeightRequired()) {
            if (entry.mAmount != entry.mSpecimen.size()) {
                Log.d(Utils.LOG_TAG, "validateEntrySpecimenForPermit: Specimen count does not match");
                return false;
            }

            boolean isOk = true;

            for (Specimen specimen : entry.mSpecimen) {
                if (speciesAmount.getGenderRequired() && TextUtils.isEmpty(specimen.getGender())) {
                    Log.d(Utils.LOG_TAG, "validateEntrySpecimenForPermit: Gender required");
                    isOk = false;
                }

                if (speciesAmount.getAgeRequired() && TextUtils.isEmpty(specimen.getAge())) {
                    Log.d(Utils.LOG_TAG, "validateEntrySpecimenForPermit: Age required");
                    isOk = false;
                }

                if (speciesAmount.getWeightRequired() && specimen.getWeight() == null) {
                    Log.d(Utils.LOG_TAG, "validateEntrySpecimenForPermit: Weight required");
                    isOk = false;
                }
            }

            return isOk;
        }
        return true;
    }

    /**
     * Validate entry details against permit requirements.
     *
     * @param entry Entry to check.
     * @return True if no permit number set or entry contains all required information.
     */
    public boolean validateEntryPermitInformation(GameHarvest entry) {
        if (entry == null) {
            return false;
        } else if (TextUtils.isEmpty(entry.mPermitNumber)) {
            return true;
        }

        Permit permit = getPermit(entry.mPermitNumber);
        return permit != null && validateEntryWithPermit(entry, permit);
    }

    /**
     * Is current date during species item season
     *
     * @param speciesItem   Species item containing season dates
     * @param daysTolerance Days before and after season where permit is visible
     * @return Is it current season
     */
    public boolean isSpeciesSeasonActive(PermitSpeciesAmount speciesItem, int daysTolerance) {
        if (DateTimeUtils.isDateInRange(LocalDate.now(), speciesItem.getBeginDate().minusDays(daysTolerance), speciesItem.getEndDate().plusDays(daysTolerance))) {
            return true;
        } else if (speciesItem.getBeginDate2() != null && speciesItem.getEndDate2() != null) {
            return DateTimeUtils.isDateInRange(LocalDate.now(), speciesItem.getBeginDate2().minusDays(daysTolerance), speciesItem.getEndDate2().plusDays(daysTolerance));
        }

        return false;
    }
}
