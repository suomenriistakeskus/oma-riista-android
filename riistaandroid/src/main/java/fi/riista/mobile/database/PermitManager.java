package fi.riista.mobile.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.HarvestSpecimen;
import fi.riista.mobile.models.Permit;
import fi.riista.mobile.models.PermitSpeciesAmount;
import fi.riista.mobile.network.PreloadPermitsTask;
import fi.vincit.androidutilslib.context.WorkContext;

import static fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME;
import static fi.riista.mobile.utils.DateTimeUtils.isDateInRange;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Singleton permit manager
 * Manages permit information associated with the user.
 */
@Singleton
public class PermitManager {

    private static final String TAG = "PermitManager";

    private static final String PRELOAD_PERMIT_FILE_PATH = "preloadPermits.json";
    private static final String MANUAL_PERMIT_FILE_PATH = "manualPermits.json";

    private final WorkContext mWorkContext;
    private final Context mContext;

    private final ObjectMapper mObjectMapper;

    // Permit list fetched from backend
    private Map<String, Permit> mPreloadPermits = new HashMap<>();

    // List of permits user has requested separately
    private Map<String, Permit> mManualPermits = new HashMap<>();

    @Inject
    public PermitManager(@NonNull @Named(APPLICATION_WORK_CONTEXT_NAME) final WorkContext workContext,
                         @NonNull final ObjectMapper objectMapper) {

        mWorkContext = requireNonNull(workContext);
        mContext = mWorkContext.getContext();

        mObjectMapper = requireNonNull(objectMapper);
    }

    /**
     * List of all known permits associated with user
     * Combines list from preloaded permits and permits manually fetched with permit number
     *
     * @return All known permits
     */
    private List<Permit> getAllPermits() {
        reloadPermits();

        // Preloaded permits override manual ones
        final Map<String, Permit> combinedPermits = new HashMap<>(mManualPermits);
        combinedPermits.putAll(mPreloadPermits);

        final List<Permit> results = new ArrayList<>(combinedPermits.values());
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

    private Map<String, Permit> readPermitFile(final String filePath) {
        final File permitFile = new File(mContext.getFilesDir(), filePath);
        final Map<String, Permit> results = new HashMap<>();

        if (permitFile.exists()) {
            try {
                final List<Permit> permits = mObjectMapper.readValue(
                        permitFile, mObjectMapper.getTypeFactory().constructCollectionType(List.class, Permit.class));

                for (final Permit permit : permits) {
                    results.put(permit.getPermitNumber(), permit);
                }

            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        return results;
    }

    /**
     * Get list of all available permits.
     *
     * @return Permits matching criteria
     */
    public List<Permit> getAvailablePermits() {
        final List<Permit> allPermits = getAllPermits();
        final List<Permit> availablePermits = new ArrayList<>(allPermits.size());

        for (final Permit permit : allPermits) {
            if (!permit.getUnavailable()) {
                availablePermits.add(permit);
            }
        }

        return availablePermits;
    }

    /**
     * Sort list alphapetically based on permit number
     *
     * @param permitList List to sort
     */
    private void sortPermitList(final List<Permit> permitList) {
        Collections.sort(permitList, (p1, p2) -> p1.getPermitNumber().compareTo(p2.getPermitNumber()));
    }

    /**
     * Get permit with number.
     *
     * @param permitNumber Permit number to search with
     * @return Permit or null
     */
    public Permit getPermit(final String permitNumber) {
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
     * @return Species amount details matching parameters or null if no match
     */
    public PermitSpeciesAmount getSpeciesAmountFromPermit(@Nullable final Permit permit, final int speciesCode) {
        if (permit != null) {
            for (final PermitSpeciesAmount speciesAmount : permit.getSpeciesAmounts()) {
                // Same animal may be included multiple times in list. Select first one where date matches.
                if (Objects.equals(speciesAmount.getGameSpeciesCode(), speciesCode)) {
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
                Log.d(TAG, "Failed to delete " + file.getAbsolutePath());
            }
        }

        file = new File(mContext.getFilesDir(), MANUAL_PERMIT_FILE_PATH);

        if (file.exists()) {
            if (!file.delete()) {
                Log.d(TAG, "Failed to delete " + file.getAbsolutePath());
            }
        }
    }

    /**
     * Save new permit details locally.
     * If permit with same number exist do nothing.
     *
     * @param permit New permit to add
     */
    public void addManualPermit(@NonNull final Permit permit) {
        reloadPermits();

        if (!mPreloadPermits.containsKey(permit.getPermitNumber())) {
            // Overwrite if exists
            mManualPermits.put(permit.getPermitNumber(), permit);

            // Persist to file
            try {
                mObjectMapper.writeValue(
                        new File(mContext.getFilesDir(), MANUAL_PERMIT_FILE_PATH), mManualPermits.values());
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void preloadPermits() {
        final PreloadPermitsTask task = new PreloadPermitsTask(mWorkContext) {

            @Override
            protected void onFinishText(final String text) {
                try (final FileOutputStream file = mContext.openFileOutput(PRELOAD_PERMIT_FILE_PATH, Context.MODE_PRIVATE);
                     final OutputStreamWriter outputWriter = new OutputStreamWriter(file)) {

                    outputWriter.write(text);

                } catch (final IOException e) {
                    e.printStackTrace();
                }

                // Forces reload the next time permits are accessed.
                mPreloadPermits.clear();
            }

            @Override
            protected void onError() {
                Log.e(TAG, format("Failed to preload permits [%d]", getHttpStatusCode()));
            }
        };
        task.start();
    }

    /**
     * Validate that harvest information matches permit.
     * Checks:
     * * Permit allows harvest of selected species.
     * * Permit allows harvest of species at selected time.
     *
     * @param harvest Harvest to check
     * @param permit  Permit to check against
     * @return True if harvest information is set according to permit
     */
    @SuppressLint("SimpleDateFormat")
    private boolean validateHarvestWithPermit(final GameHarvest harvest, final Permit permit) {
        if (harvest == null || permit == null) {
            return false;
        }

        // Validate species and date.

        PermitSpeciesAmount speciesAmountMatch = null;

        for (final PermitSpeciesAmount speciesAmount : permit.getSpeciesAmounts()) {
            final Integer speciesCode = speciesAmount.getGameSpeciesCode();

            if (speciesCode != null && speciesCode.equals(harvest.mSpeciesID)) {
                final LocalDate harvestDate = LocalDate.fromCalendarFields(harvest.mTime);

                if (isDateInRange(harvestDate, speciesAmount.getBeginDate(), speciesAmount.getEndDate())
                        || isDateInRange(harvestDate, speciesAmount.getBeginDate2(), speciesAmount.getEndDate2())) {

                    speciesAmountMatch = speciesAmount;
                    break;
                }
            }
        }

        if (speciesAmountMatch == null) {
            Log.d(TAG, format("Date %S is not valid for [%d]", new SimpleDateFormat("dd-MM-yyy").format(harvest.mTime.getTime()), harvest.mSpeciesID));
            return false;
        }

        return validateHarvestSpecimenForPermit(harvest, speciesAmountMatch);
    }

    private boolean validateHarvestSpecimenForPermit(final GameHarvest harvest,
                                                     final PermitSpeciesAmount speciesAmount) {

        if (speciesAmount.getGenderRequired() || speciesAmount.getAgeRequired() || speciesAmount.getWeightRequired()) {
            if (harvest.mAmount != harvest.mSpecimen.size()) {
                Log.d(TAG, "validateHarvestSpecimenForPermit: Specimen count does not match");
                return false;
            }

            for (final HarvestSpecimen specimen : harvest.mSpecimen) {
                if (speciesAmount.getGenderRequired() && TextUtils.isEmpty(specimen.getGender())) {
                    Log.d(TAG, "validateHarvestSpecimenForPermit: Gender required");
                    return false;
                }

                if (speciesAmount.getAgeRequired() && TextUtils.isEmpty(specimen.getAge())) {
                    Log.d(TAG, "validateHarvestSpecimenForPermit: Age required");
                    return false;
                }

                if (speciesAmount.getWeightRequired() && specimen.getWeight() == null) {
                    Log.d(TAG, "validateHarvestSpecimenForPermit: Weight required");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Validate harvest details against permit requirements.
     *
     * @param harvest Harvest to check.
     * @return True if no permit number set or harvest contains all required information.
     */
    public boolean validateHarvestPermitInformation(final GameHarvest harvest) {
        if (harvest == null) {
            return false;
        }
        if (TextUtils.isEmpty(harvest.mPermitNumber)) {
            return true;
        }

        final Permit permit = getPermit(harvest.mPermitNumber);
        return permit != null && validateHarvestWithPermit(harvest, permit);
    }

    /**
     * Is current date during species item season
     *
     * @param speciesItem   Species item containing season dates
     * @param daysTolerance Days before and after season where permit is visible
     * @return Is it current season
     */
    public boolean isSpeciesSeasonActive(final PermitSpeciesAmount speciesItem, final int daysTolerance) {
        final LocalDate today = LocalDate.now();

        if (isDateInRange(today, speciesItem.getBeginDate().minusDays(daysTolerance), speciesItem.getEndDate().plusDays(daysTolerance))) {
            return true;
        } else if (speciesItem.getBeginDate2() != null && speciesItem.getEndDate2() != null) {
            return isDateInRange(today, speciesItem.getBeginDate2().minusDays(daysTolerance), speciesItem.getEndDate2().plusDays(daysTolerance));
        }

        return false;
    }
}
