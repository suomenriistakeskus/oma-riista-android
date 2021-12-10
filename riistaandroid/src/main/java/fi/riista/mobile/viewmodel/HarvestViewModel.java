package fi.riista.mobile.viewmodel;

import android.location.Location;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import fi.riista.mobile.database.PermitManager;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.database.SpeciesResolver;
import fi.riista.mobile.gamelog.DeerHuntingFeatureAvailability;
import fi.riista.mobile.models.DeerHuntingType;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.GameLog;
import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.models.GreySealHuntingMethod;
import fi.riista.mobile.models.HarvestSpecimen;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.HarvestSeasonUtil;
import fi.riista.mobile.utils.HarvestValidator;
import fi.riista.mobile.utils.MapUtils;
import fi.riista.mobile.utils.RequiredHarvestFields;
import fi.riista.mobile.utils.RequiredHarvestFields.HarvestReportingType;
import fi.riista.mobile.utils.RequiredHarvestFields.Report;
import fi.riista.mobile.utils.RequiredHarvestFields.Required;
import fi.riista.mobile.utils.Utils;

import static androidx.lifecycle.Transformations.distinctUntilChanged;
import static androidx.lifecycle.Transformations.map;
import static fi.riista.mobile.utils.RequiredHarvestFields.Required.NO;
import static fi.riista.mobile.utils.RequiredHarvestFields.Required.YES;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

public class HarvestViewModel extends ViewModel {

    private static final String TAG = "HarvestViewModel";

    private final SpeciesResolver mSpeciesResolver;
    private final PermitManager mPermitManager;
    private final HarvestValidator mHarvestValidator;
    private final DeerHuntingFeatureAvailability deerHuntingFeatureAvailability;

    // region Harvest data field values

    // Harvest state is maintained in this instance along with all LiveData objects below.
    private GameHarvest mHarvest;

    // Last valid amount is cached in case a null value is inserted from UI.
    // A null value cannot be stored into GameHarvest entity while saving harvest state into disk.
    private int mLastValidAmount = 1;

    private final MutableLiveData<Calendar> mDateTime = new MutableLiveData<>();
    private final MutableLiveData<Integer> mSpeciesId = new MutableLiveData<>();
    private final MutableLiveData<Integer> mAmount = new MutableLiveData<>();
    private final MutableLiveData<String> mDescription = new MutableLiveData<>();
    private final MutableLiveData<Location> mLocation = new MutableLiveData<>();
    private final MutableLiveData<String> mLocationSource = new MutableLiveData<>();
    private final MutableLiveData<Pair<Long, Long>> mCoordinates = new MutableLiveData<>();

    private final MutableLiveData<List<HarvestSpecimen>> mSpecimens = new MutableLiveData<>();

    private final MutableLiveData<Pair<String, String>> mPermitNumberAndType = new MutableLiveData<>();

    // Extra information for specific species
    private final MutableLiveData<DeerHuntingType> mDeerHuntingType = new MutableLiveData<>();
    private final MutableLiveData<String> mDeerHuntingTypeDescription = new MutableLiveData<>();
    private final MutableLiveData<GreySealHuntingMethod> mGreySealHuntingMethod = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mFeedingPlace = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mTaigaBeanGoose = new MutableLiveData<>();

    // endregion

    // region Helpers and derived data values

    // Combines harvest report and permit states for display purposes
    private final MutableLiveData<String> mHarvestState = new MutableLiveData<>();

    private final LiveData<Species> mSpecies = distinctUntilChanged(map(mSpeciesId, this::resolveSpecies));

    // endregion

    // region App/view state

    private final MutableLiveData<Boolean> mEditEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mHasAllRequiredFields = new MutableLiveData<>();

    // endregion

    // region Dynamic field requirements

    private final MutableLiveData<Required> mPermitNumberRequired = new MutableLiveData<>();
    private final MutableLiveData<Required> mDeerHuntingTypeRequired = new MutableLiveData<>();
    private final MutableLiveData<Required> mGreySealHuntingMethodRequired = new MutableLiveData<>();
    private final MutableLiveData<Required> mFeedingPlaceRequired = new MutableLiveData<>();
    private final MutableLiveData<Required> mTaigaBeanGooseRequired = new MutableLiveData<>();

    public final MutableLiveData<Boolean> mSpecimenMandatoryAge = new MutableLiveData<>();
    public final MutableLiveData<Boolean> mSpecimenMandatoryGender = new MutableLiveData<>();
    public final MutableLiveData<Boolean> mSpecimenMandatoryWeight = new MutableLiveData<>();

    // endregion

    @Inject
    public HarvestViewModel(@NonNull final SpeciesResolver speciesResolver,
                            @NonNull final PermitManager permitManager,
                            @NonNull final HarvestValidator harvestValidator,
                            @NonNull final DeerHuntingFeatureAvailability deerHuntingFeatureAvailability) {

        this.mSpeciesResolver = requireNonNull(speciesResolver);
        this.mPermitManager = requireNonNull(permitManager);
        this.mHarvestValidator = requireNonNull(harvestValidator);
        this.deerHuntingFeatureAvailability = requireNonNull(deerHuntingFeatureAvailability);
    }

    public void initWith(@NonNull final GameHarvest source, final boolean edit) {
        requireNonNull(source);

        final GameHarvest harvest = source.deepClone();

        // mHarvest field needs to be assigned before populating LiveData instances.
        mHarvest = harvest;

        mDateTime.setValue(harvest.mTime);
        mSpeciesId.setValue(harvest.mSpeciesID);

        final int amount = harvest.mAmount;
        mAmount.setValue(amount);
        this.mLastValidAmount = amount;

        mDescription.setValue(harvest.mDescription);

        mLocation.setValue(harvest.mLocation);
        mLocationSource.setValue(harvest.mLocationSource);
        mCoordinates.setValue(harvest.mCoordinates);

        setSpecimens(harvest.mSpecimen);
        setPermitNumberAndType(harvest.mPermitNumber, harvest.mPermitType);

        mDeerHuntingType.setValue(harvest.mDeerHuntingType);
        mDeerHuntingTypeDescription.setValue(harvest.mDeerHuntingOtherTypeDescription);
        mGreySealHuntingMethod.setValue(harvest.mHuntingMethod);
        mFeedingPlace.setValue(harvest.mFeedingPlace);
        mTaigaBeanGoose.setValue(harvest.mTaigaBeanGoose);

        mHarvestState.setValue(combinedHarvestState(
                harvest.mHarvestReportState, harvest.mStateAcceptedToHarvestPermit, harvest.mHarvestReportRequired));
        mEditEnabled.setValue(edit);

        refreshSubmitReadyState();
    }

    public LiveData<Calendar> getDateTime() {
        return mDateTime;
    }

    public LiveData<Integer> getAmount() {
        return mAmount;
    }

    public LiveData<String> getDescription() {
        return mDescription;
    }

    public LiveData<Location> getLocation() {
        return mLocation;
    }

    public LiveData<String> getLocationSource() {
        return mLocationSource;
    }

    public LiveData<Pair<Long, Long>> getCoordinates() {
        return mCoordinates;
    }

    public LiveData<List<HarvestSpecimen>> getSpecimens() {
        return mSpecimens;
    }

    public LiveData<Pair<String, String>> getPermitNumberAndType() {
        return mPermitNumberAndType;
    }

    public LiveData<Species> getSpecies() {
        return mSpecies;
    }

    public LiveData<String> getHarvestState() {
        return mHarvestState;
    }

    public LiveData<DeerHuntingType> getDeerHuntingType() {
        return mDeerHuntingType;
    }

    public LiveData<String> getDeerHuntingTypeDescription() {
        return mDeerHuntingTypeDescription;
    }

    public LiveData<GreySealHuntingMethod> getGreySealHuntingMethod() {
        return mGreySealHuntingMethod;
    }

    public LiveData<Boolean> getFeedingPlace() {
        return mFeedingPlace;
    }

    public LiveData<Boolean> getTaigaBeanGoose() {
        return mTaigaBeanGoose;
    }

    public LiveData<Boolean> getEditEnabled() {
        return mEditEnabled;
    }

    public LiveData<Required> getPermitNumberMandatory() {
        return mPermitNumberRequired;
    }

    public LiveData<Required> getDeerHuntingTypeRequired() {
        return mDeerHuntingTypeRequired;
    }

    public LiveData<Required> getGreySealHuntingMethodRequired() {
        return mGreySealHuntingMethodRequired;
    }

    public LiveData<Required> getFeedingPlaceRequired() {
        return mFeedingPlaceRequired;
    }

    public LiveData<Required> getTaigaBeanGooseRequired() {
        return mTaigaBeanGooseRequired;
    }

    public LiveData<Boolean> getSpecimenMandatoryAge() {
        return mSpecimenMandatoryAge;
    }

    public LiveData<Boolean> getSpecimenMandatoryGender() {
        return mSpecimenMandatoryGender;
    }

    public LiveData<Boolean> getSpecimenMandatoryWeight() {
        return mSpecimenMandatoryWeight;
    }

    public LiveData<Boolean> getHasAllRequiredFields() {
        return mHasAllRequiredFields;
    }

    public boolean isLocallyPersisted() {
        return requireHarvest().isPersistedLocally();
    }

    public boolean notLocked() {
        return requireHarvest().mCanEdit;
    }

    public List<GameLogImage> getImages() {
        return requireHarvest().mImages;
    }

    // Not exposed as LiveData because this does not change.
    public int requireLocalId() {
        return requireHarvest().mLocalId;
    }

    public GameHarvest getResultHarvest() {
        if (mAmount.getValue() == null) {
            setAmountInternal(mLastValidAmount);
        }

        // Create a copy because specimen list will be altered by removing empty items.
        final GameHarvest result = requireHarvest().deepClone();
        result.setSpecimens(HarvestSpecimen.withEmptyRemoved(getSpecimens().getValue()));
        return result;
    }

    private GameHarvest requireHarvest() {
        return requireNonNull(mHarvest);
    }

    public void setAmount(@Nullable final Integer amount) {
        boolean triggerUserInputChange = true;

        if (amount != null) {
            GameHarvest.assertAmountWithinLegalRange(amount);
            final int validatedAmount = getAmountAfterValidationAgainstSelectedSpecies(amount);

            if (setAmountInternal(validatedAmount)) {
                triggerUserInputChange = false;
            }
        } else {
            mHarvest.mAmount = 0;
            mAmount.setValue(null);
        }

        if (triggerUserInputChange) {
            onUserInputChanged();
        }
    }

    // Returns boolean indicating whether specimens were updated.
    private boolean setAmountInternal(final int amount) {
        final GameHarvest harvest = requireHarvest();
        harvest.mAmount = amount;
        mLastValidAmount = amount;
        mAmount.setValue(amount);

        final List<HarvestSpecimen> specimens = mSpecimens.getValue();

        if (specimens != null && specimens.size() > amount) {
            final ArrayList<HarvestSpecimen> truncatedSpecimens = new ArrayList<>(amount);

            for (int i = 0; i < amount; i++) {
                truncatedSpecimens.add(specimens.get(i));
            }

            setSpecimensInternal(truncatedSpecimens);
            return true;
        }

        return false;
    }

    public void setDescription(@Nullable final String description) {
        final GameHarvest harvest = requireHarvest();
        final String previousValue = mDescription.getValue();

        if (!TextUtils.equals(description, previousValue)) {
            harvest.mDescription = description;
            mDescription.setValue(description);
        }

        onUserInputChanged();
    }

    public void setSpecimens(@NonNull final List<HarvestSpecimen> specimens) {
        requireNonNull(specimens);

        final Integer amount = mAmount.getValue();

        if (amount == null) {
            throw new IllegalStateException("Cannot set specimens when amount is set to null");
        }

        final int numInputSpecimens = specimens.size();

        if (numInputSpecimens > amount) {
            throw new IllegalArgumentException(format(
                    "Number of specimens is greater than amount: %d > %d", numInputSpecimens, amount));
        }

        final List<HarvestSpecimen> trimmedSpecimens;

        if (numInputSpecimens > 0) {
            trimmedSpecimens = HarvestSpecimen.withEmptyRemoved(specimens);

            // Always retain at least one specimen in the list in order to not result in empty list.
            if (trimmedSpecimens.isEmpty()) {
                trimmedSpecimens.add(specimens.get(0));
            }

        } else {
            // Ensure that the specimen list always contains at least one empty specimen in the view model.
            trimmedSpecimens = singletonList(new HarvestSpecimen());
        }

        setSpecimensInternal(trimmedSpecimens);

        onUserInputChanged();
    }

    private void setSpecimensInternal(@NonNull final List<HarvestSpecimen> specimens) {
        if (specimens.isEmpty()) {
            throw new IllegalStateException("Must always have non-empty specimen list in view model");
        }

        requireHarvest().setSpecimens(specimens);
        mSpecimens.setValue(specimens);
    }

    private void setDeerHuntingType(@Nullable final DeerHuntingType huntingType) {
        final GameHarvest harvest = requireHarvest();
        harvest.mDeerHuntingType = huntingType;
        mDeerHuntingType.setValue(huntingType);
    }

    public void setDeerHuntingTypeDescription(@Nullable final String huntingTypeDescription) {
        final GameHarvest harvest = requireHarvest();
        harvest.mDeerHuntingOtherTypeDescription = huntingTypeDescription;
        mDeerHuntingTypeDescription.setValue(huntingTypeDescription);
    }

    private void setGreySealHuntingMethod(@Nullable final GreySealHuntingMethod huntingMethod) {
        final GameHarvest harvest = requireHarvest();
        harvest.mHuntingMethod = huntingMethod;
        mGreySealHuntingMethod.setValue(huntingMethod);
    }

    private void setFeedingPlace(@Nullable final Boolean value) {
        final GameHarvest harvest = requireHarvest();
        harvest.mFeedingPlace = value;
        mFeedingPlace.setValue(value);
    }

    private void setTaigaBeanGoose(@Nullable final Boolean value) {
        final GameHarvest harvest = requireHarvest();
        harvest.mTaigaBeanGoose = value;
        mTaigaBeanGoose.setValue(value);
    }

    public void setEditEnabled(@NonNull final Boolean enabled) {
        mEditEnabled.setValue(enabled);

        onUserInputChanged();
    }

    // region User action handlers

    public void selectDateTime(@NonNull final Calendar dateTime) {
        final GameHarvest harvest = requireHarvest();
        harvest.mTime = dateTime;
        mDateTime.setValue(dateTime);

        onUserInputChanged();
    }

    public void selectLocationManual(@NonNull final Location location) {
        final GameHarvest harvest = requireHarvest();
        harvest.mLocation = location;
        harvest.mAccuracy = location.getAccuracy();
        harvest.mHasAltitude = location.hasAltitude();
        harvest.mAltitude = location.getAltitude();
        harvest.mCoordinates = MapUtils.WGS84toETRSTM35FIN(location.getLatitude(), location.getLongitude());
        harvest.mLocationSource = GameLog.LOCATION_SOURCE_MANUAL;

        mLocation.setValue(location);
        mCoordinates.setValue(MapUtils.WGS84toETRSTM35FIN(location.getLatitude(), location.getLongitude()));
        mLocationSource.setValue(GameLog.LOCATION_SOURCE_MANUAL);

        onUserInputChanged();
    }

    public void selectLocationGps(@NonNull final Location location) {
        final GameHarvest harvest = requireHarvest();
        harvest.mLocation = location;
        harvest.mAccuracy = location.getAccuracy();
        harvest.mHasAltitude = location.hasAltitude();
        harvest.mAltitude = location.getAltitude();
        harvest.mCoordinates = MapUtils.WGS84toETRSTM35FIN(location.getLatitude(), location.getLongitude());
        harvest.mLocationSource = GameLog.LOCATION_SOURCE_GPS;

        mLocation.setValue(location);
        mCoordinates.setValue(MapUtils.WGS84toETRSTM35FIN(location.getLatitude(), location.getLongitude()));
        mLocationSource.setValue(GameLog.LOCATION_SOURCE_GPS);

        onUserInputChanged();
    }

    public void selectSpecies(@Nullable final Integer speciesCode) {
        final Species species = resolveSpecies(speciesCode);
        final GameHarvest harvest = requireHarvest();

        final Integer previous = mSpeciesId.getValue();
        boolean triggerUserInputChange = true;

        if (species == null) {
            if (previous != null) {
                harvest.mSpeciesID = null;
                mSpeciesId.setValue(null);
                setAmountInternal(1);
            }
        } else {
            harvest.mSpeciesID = speciesCode;
            mSpeciesId.setValue(speciesCode);

            final Integer amount = getAmount().getValue();

            if (isMultipleSpecimensDisallowed(species) && amount != null && amount > 1) {
                setAmountInternal(1);
                triggerUserInputChange = false;
            }
        }

        resetHarvestFieldsAfterSpeciesChanged(species);

        if (triggerUserInputChange) {
            onUserInputChanged();
        }
    }

    private void resetHarvestFieldsAfterSpeciesChanged(@Nullable final Species species) {
        if (mGreySealHuntingMethod.getValue() != null && speciesNotHavingCode(species, SpeciesInformation.GREY_SEAL_ID)) {
            setGreySealHuntingMethod(null);
        }
        if (mFeedingPlace.getValue() != null && speciesNotHavingCode(species, SpeciesInformation.WILD_BOAR_ID)) {
            setFeedingPlace(null);
        }
        if (mTaigaBeanGoose.getValue() != null && speciesNotHavingCode(species, SpeciesInformation.BEAN_GOOSE_ID)) {
            setTaigaBeanGoose(null);
        }
    }

    private static boolean speciesNotHavingCode(@Nullable final Species species, final int speciesCode) {
        return species == null || !species.hasSpeciesCode(speciesCode);
    }

    public void selectPermitNumber(@Nullable final String permitNumber,
                                   @Nullable final String permitType,
                                   @Nullable final Species species) {

        final GameHarvest harvest = requireHarvest();
        harvest.mPermitNumber = permitNumber;
        harvest.mPermitType = permitType;

        setPermitNumberAndType(permitNumber, permitType);

        if (species != null) {
            selectSpecies(species.mId);
        }

        onUserInputChanged();
    }

    private void setPermitNumberAndType(@Nullable final String permitNumber, @Nullable final String permitType) {
        // Do not replace invoking Pair constructor with Pair.create() because of local unit tests!
        mPermitNumberAndType.setValue(new Pair<>(permitNumber, permitType));
    }

    public void selectDeerHuntingType(@Nullable final DeerHuntingType huntingType) {
        final Integer speciesCode = mSpeciesId.getValue();

        if (speciesCode != null && SpeciesInformation.WHITE_TAILED_DEER_ID == speciesCode) {
            setDeerHuntingType(huntingType);
        } else {
            Utils.LogMessage(TAG, format(Locale.getDefault(), "Illegal value for deerHuntingType %s [species: %d]",
                    huntingType, speciesCode));
            setDeerHuntingType(null);
        }

        onUserInputChanged();
    }

    public void selectGreySealHuntingMethod(@Nullable final GreySealHuntingMethod huntingMethod) {
        final Integer speciesCode = mSpeciesId.getValue();

        if (speciesCode != null && SpeciesInformation.GREY_SEAL_ID == speciesCode) {
            setGreySealHuntingMethod(huntingMethod);
        } else {
            Utils.LogMessage(TAG, format(Locale.getDefault(), "Illegal value for huntingType %s [species: %d]", huntingMethod, speciesCode));
            setGreySealHuntingMethod(null);
        }

        onUserInputChanged();
    }

    public void selectFeedingPlace(@Nullable final Boolean value) {
        final Integer speciesCode = mSpeciesId.getValue();

        if (speciesCode != null && SpeciesInformation.WILD_BOAR_ID == speciesCode) {
            setFeedingPlace(value);
        } else {
            Utils.LogMessage(TAG, format(Locale.getDefault(), "Illegal value for feedingPlace %s [species: %d]", value, speciesCode));
            setFeedingPlace(null);
        }

        onUserInputChanged();
    }

    public void selectTaigaBeanGoose(final Boolean value) {
        final Integer speciesCode = mSpeciesId.getValue();

        if (speciesCode != null && SpeciesInformation.BEAN_GOOSE_ID == speciesCode) {
            setTaigaBeanGoose(value);
        } else {
            Utils.LogMessage(TAG, format(Locale.getDefault(), "Illegal value for taigaBeanGoose %s [species: %d]", value, speciesCode));
            setTaigaBeanGoose(null);
        }

        onUserInputChanged();
    }

    // endregion

    // region Refreshing state and resolving conditions

    private void onUserInputChanged() {
        refreshMandatoryFields();
        refreshSubmitReadyState();
    }

    public void refreshSubmitReadyState() {
        mHasAllRequiredFields.setValue(isInputValid());
    }

    private void refreshMandatoryFields() {
        final Calendar calendar = mDateTime.getValue();
        final Integer speciesId = mSpeciesId.getValue();
        final Location location = mLocation.getValue();
        final String permitNumber =
                mPermitNumberAndType.getValue() != null ? mPermitNumberAndType.getValue().first : null;

        if (calendar != null && speciesId != null && location != null) {
            final int huntingYear = DateTimeUtils.getHuntingYearForCalendar(calendar);
            final boolean insideSeason =
                    HarvestSeasonUtil.isInsideHuntingSeason(LocalDate.fromCalendarFields(calendar), speciesId);

            final HarvestReportingType reportingType;
            if (permitNumber != null) {
                reportingType = HarvestReportingType.PERMIT;
            } else if (insideSeason) {
                reportingType = HarvestReportingType.SEASON;
            } else {
                reportingType = HarvestReportingType.BASIC;
            }

            final boolean isDeerHuntingFeatureEnabled = deerHuntingFeatureAvailability.getEnabled();

            final Report reportFields = RequiredHarvestFields.getFormFields(
                    huntingYear, speciesId, reportingType, isDeerHuntingFeatureEnabled);

            mPermitNumberRequired.setValue(reportFields.getPermitNumber());

            mDeerHuntingTypeRequired.setValue(reportFields.getDeerHuntingType());
            mGreySealHuntingMethodRequired.setValue(reportFields.getGreySealHuntingMethod());
            mFeedingPlaceRequired.setValue(reportFields.getFeedingPlace());
            mTaigaBeanGooseRequired.setValue(reportFields.getTaigaBeanGoose());

            final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFields.getSpecimenFields(
                    huntingYear,
                    speciesId,
                    mGreySealHuntingMethod.getValue(),
                    reportingType);

            mSpecimenMandatoryGender.setValue(YES == specimenFields.getGender());
            mSpecimenMandatoryAge.setValue(YES == specimenFields.getAge());
            mSpecimenMandatoryWeight.setValue(YES == specimenFields.getWeight());
        } else {
            mPermitNumberRequired.setValue(NO);

            mDeerHuntingTypeRequired.setValue(NO);
            mGreySealHuntingMethodRequired.setValue(NO);
            mFeedingPlaceRequired.setValue(NO);
            mTaigaBeanGooseRequired.setValue(NO);

            mSpecimenMandatoryGender.setValue(false);
            mSpecimenMandatoryAge.setValue(false);
            mSpecimenMandatoryWeight.setValue(false);
        }
    }

    private boolean isInputValid() {
        if (mHarvest == null) {
            Utils.LogMessage(TAG, "Harvest null");
            return false;
        }

        return mPermitManager.validateHarvestPermitInformation(mHarvest) && mHarvestValidator.validate(mHarvest);
    }

    // endregion

    // region Utility and helpers

    private Species resolveSpecies(@Nullable final Integer speciesId) {
        return mSpeciesResolver.findSpecies(speciesId);
    }

    private int getAmountAfterValidationAgainstSelectedSpecies(final int amount) {
        return isMultipleSpecimensDisallowed(mSpeciesId.getValue()) ? 1 : amount;
    }

    private boolean isMultipleSpecimensDisallowed(@Nullable final Integer speciesId) {
        return speciesId != null && isMultipleSpecimensDisallowed(mSpeciesResolver.findSpecies(speciesId));
    }

    private boolean isMultipleSpecimensDisallowed(@Nullable final Species species) {
        return species != null && !species.mMultipleSpecimenAllowedOnHarvests;
    }

    private String combinedHarvestState(@Nullable final String reportState,
                                        @Nullable final String permitState,
                                        final boolean harvestReportRequired) {
        String state = null;

        if (GameHarvest.HARVEST_PROPOSED.equals(reportState)) {
            state = GameHarvest.HARVEST_PROPOSED;
        } else if (GameHarvest.HARVEST_SENT_FOR_APPROVAL.equals(reportState)) {
            state = GameHarvest.HARVEST_SENT_FOR_APPROVAL;
        } else if (GameHarvest.HARVEST_APPROVED.equals(reportState)) {
            state = GameHarvest.HARVEST_APPROVED;
        } else if (GameHarvest.HARVEST_REJECTED.equals(reportState)) {
            state = GameHarvest.HARVEST_REJECTED;
        } else if (GameHarvest.HARVEST_CREATEREPORT.equals(reportState)) {
            state = GameHarvest.HARVEST_CREATEREPORT;
        } else if (GameHarvest.PERMIT_PROPOSED.equals(permitState)) {
            state = GameHarvest.PERMIT_PROPOSED;
        } else if (GameHarvest.PERMIT_ACCEPTED.equals(permitState)) {
            state = GameHarvest.PERMIT_ACCEPTED;
        } else if (GameHarvest.PERMIT_REJECTED.equals(permitState)) {
            state = GameHarvest.PERMIT_REJECTED;
        } else if (harvestReportRequired) {
            state = GameHarvest.HARVEST_CREATEREPORT;
        }

        return state;
    }

    // endregion
}
