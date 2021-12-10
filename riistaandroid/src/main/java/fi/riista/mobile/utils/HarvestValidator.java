package fi.riista.mobile.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.joda.time.LocalDate;

import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.riista.mobile.database.SpeciesResolver;
import fi.riista.mobile.gamelog.DeerHuntingFeatureAvailability;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.GreySealHuntingMethod;
import fi.riista.mobile.models.HarvestSpecimen;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.utils.RequiredHarvestFields.HarvestReportingType;
import fi.riista.mobile.utils.RequiredHarvestFields.Required;

import static fi.riista.mobile.utils.RequiredHarvestFields.Required.NO;
import static fi.riista.mobile.utils.RequiredHarvestFields.Required.YES;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Singleton
public class HarvestValidator {

    private static final String TAG = "HarvestValidator";

    private final SpeciesResolver speciesResolver;
    private final DeerHuntingFeatureAvailability deerHuntingFeatureAvailability;

    private boolean logEnabled;

    @Inject
    public HarvestValidator(@NonNull final SpeciesResolver speciesResolver,
                            @NonNull final DeerHuntingFeatureAvailability deerHuntingFeatureAvailability) {

        this.speciesResolver = requireNonNull(speciesResolver);
        this.deerHuntingFeatureAvailability = requireNonNull(deerHuntingFeatureAvailability);

        this.logEnabled = true;
    }

    public void setLogEnabled(final boolean enabled) {
        this.logEnabled = enabled;
    }

    @SuppressLint("DefaultLocale")
    public boolean validate(@NonNull final GameHarvest harvest) {
        requireNonNull(harvest);

        // Must have non-default datetime
        if (harvest.mTime == null || harvest.mTime.equals(Calendar.getInstance())) {
            logError(format("Invalid time: %s", formatHarvestTime(harvest)));
            return false;
        }

        if (!harvest.isLocationSet()) {
            logError(format("Invalid location: %s", harvest.mLocation != null ? harvest.mLocation.toString() : null));
            return false;
        }

        if (harvest.mSpeciesID == null) {
            logError("Missing species code");
            return false;
        }

        final Integer speciesCode = harvest.mSpeciesID;
        final Species species = speciesResolver.findSpecies(speciesCode);

        if (species == null) {
            logError(format("Invalid species code: %s", speciesCode));
            return false;
        }

        if (!harvest.isAmountWithinLegalRange()) {
            logError(format("Invalid amount: %d", harvest.mAmount));
            return false;
        }

        final int amount = harvest.mAmount;

        if (!species.mMultipleSpecimenAllowedOnHarvests && amount > 1) {
            logError(format("Amount must be 1 for species [%d]. Was: %d", speciesCode, amount));
            return false;
        }

        if (harvest.mSpecimen == null) {
            logError("Invalid specimens: null");
            return false;
        }

        final int numSpecimens = harvest.mSpecimen.size();

        if (!species.mMultipleSpecimenAllowedOnHarvests && numSpecimens > 1) {
            logError(format("Invalid specimen count: %d", numSpecimens));
            return false;
        }

        if (harvest.isMoose() || harvest.isDeer()) {
            for (final HarvestSpecimen specimen : harvest.mSpecimen) {
                if (specimen.getWeight() != null) {
                    logError("Moose weight must be null");
                    return false;
                }
            }
        }

        return validateSpeciesMandatoryFields(harvest, species);
    }

    private boolean validateSpeciesMandatoryFields(final GameHarvest harvest, final Species species) {
        final Integer speciesCode = species.mId;
        final int huntingYear = DateTimeUtils.getHuntingYearForCalendar(harvest.mTime);
        final boolean insideSeason =
                HarvestSeasonUtil.isInsideHuntingSeason(LocalDate.fromCalendarFields(harvest.mTime), speciesCode);

        final String permitNumber = harvest.mPermitNumber;
        final HarvestReportingType reportingType;

        if (permitNumber != null) {
            reportingType = HarvestReportingType.PERMIT;
        } else if (insideSeason) {
            reportingType = HarvestReportingType.SEASON;
        } else {
            reportingType = HarvestReportingType.BASIC;
        }

        final boolean isDeerHuntingFeatureEnabled = deerHuntingFeatureAvailability.getEnabled();

        final RequiredHarvestFields.Report reportFields = RequiredHarvestFields.getFormFields(
                huntingYear, species.mId, reportingType, isDeerHuntingFeatureEnabled);

        final Required permitNumberReq = reportFields.getPermitNumber();

        if (permitNumberReq == YES && permitNumber == null) {
            logError(format("Permit number required. Species: %s time: %s", speciesCode, formatHarvestTime(harvest)));
            return false;
        }

        final Required deerHuntingTypeReq = reportFields.getDeerHuntingType();

        if (deerHuntingTypeReq == YES && harvest.mDeerHuntingType == null ||
                deerHuntingTypeReq == NO && harvest.mDeerHuntingType != null) {

            logError(format("Invalid deer hunting type. Required: %s value: %s", deerHuntingTypeReq, harvest.mDeerHuntingType));
            return false;
        }

        if (deerHuntingTypeReq == NO && harvest.mDeerHuntingOtherTypeDescription != null) {
            logError(format("Invalid deer hunting type description. Required: %s value: %s",
                    deerHuntingTypeReq, harvest.mDeerHuntingOtherTypeDescription));
            return false;
        }

        final Required greySealHuntingMethodReq = reportFields.getGreySealHuntingMethod();
        final GreySealHuntingMethod sealHuntingMethod = harvest.mHuntingMethod;

        if (greySealHuntingMethodReq == YES && sealHuntingMethod == null ||
                greySealHuntingMethodReq == NO && sealHuntingMethod != null) {

            logError(format("Invalid hunting method. Required: %s value: %s", greySealHuntingMethodReq, sealHuntingMethod));
            return false;
        }

        final Required feedingPlaceReq = reportFields.getFeedingPlace();
        final Boolean isFeedingPlace = harvest.mFeedingPlace;

        if (feedingPlaceReq == YES && null == isFeedingPlace || feedingPlaceReq == NO && null != isFeedingPlace) {
            logError(format("Invalid feeding place. Required: %s value: %s", feedingPlaceReq, isFeedingPlace));
            return false;
        }

        final Required taigaBeanGooseReq = reportFields.getTaigaBeanGoose();
        final Boolean isTaigaBeanMoose = harvest.mTaigaBeanGoose;

        if (taigaBeanGooseReq == YES && null == isTaigaBeanMoose ||
                taigaBeanGooseReq == NO && null != isTaigaBeanMoose) {

            logError(format("Invalid taiga bean goose. Required: %s value: %s", taigaBeanGooseReq, isTaigaBeanMoose));
            return false;
        }

        final RequiredHarvestFields.Specimen specimenFields =
                RequiredHarvestFields.getSpecimenFields(huntingYear, speciesCode, sealHuntingMethod, reportingType);

        final int numSpecimens = harvest.mSpecimen.size();

        if (!species.mMultipleSpecimenAllowedOnHarvests && numSpecimens > 1) {
            logError(format("Invalid specimen count for %s: %d", species.mName, numSpecimens));
            return false;
        }

        for (final HarvestSpecimen specimen : harvest.mSpecimen) {
            final Required genderRequirement = specimenFields.getGender();
            final String genderValue = specimen.getGender();

            if (genderRequirement == YES && TextUtils.isEmpty(genderValue) ||
                    genderRequirement == NO && genderValue != null) {

                logError(format("Invalid gender. Required: %s value: %s", genderRequirement, genderValue));
                return false;
            }

            final Required ageRequirement = specimenFields.getAge();
            final String ageValue = specimen.getAge();

            if (ageRequirement == YES && TextUtils.isEmpty(ageValue) || ageRequirement == NO && ageValue != null) {
                logError(format("Invalid age. Required: %s value: %s", ageRequirement, ageValue));
                return false;
            }

            final Required weightRequirement = specimenFields.getWeight();
            final Double weightValue = specimen.getWeight();

            if (weightRequirement == YES && weightValue == null || weightRequirement == NO && weightValue != null) {
                logError(format("Invalid weight. Required: %s value: %s", weightRequirement, weightValue));
                return false;
            }
        }

        return true;
    }

    private void logError(final String errMsg) {
        if (logEnabled) {
            Utils.LogMessage(TAG, errMsg);
        }
    }

    private static String formatHarvestTime(final GameHarvest harvest) {
        return harvest.mTime != null
                ? DateTimeUtils.formatDateUsingFinnishFormat(harvest.mTime.getTime())
                : null;
    }
}
