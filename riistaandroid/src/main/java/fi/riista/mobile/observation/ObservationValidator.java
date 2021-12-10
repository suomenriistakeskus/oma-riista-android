package fi.riista.mobile.observation;

import androidx.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.riista.mobile.models.DeerHuntingType;
import fi.riista.mobile.models.observation.GameObservation;
import fi.riista.mobile.models.observation.metadata.ObservationContextSensitiveFieldSet;
import fi.riista.mobile.models.observation.metadata.ObservationSpecimenMetadata;
import fi.riista.mobile.models.user.UserInfo;
import fi.riista.mobile.utils.PhoneNumberValidator;
import fi.riista.mobile.utils.UserInfoStore;

import static java.util.Objects.requireNonNull;

@Singleton
public class ObservationValidator {

    private final UserInfoStore mUserInfoStore;
    private final ObservationMetadataHelper mObservationMetadataHelper;

    @Inject
    public ObservationValidator(@NonNull final UserInfoStore userInfoStore,
                                @NonNull final ObservationMetadataHelper observationMetadataHelper) {

        mUserInfoStore = requireNonNull(userInfoStore);
        mObservationMetadataHelper = requireNonNull(observationMetadataHelper);
    }

    public boolean validate(final GameObservation observation) {

        final boolean alwaysMandatoryFieldsPresent = observation.gameSpeciesCode != 0
                && observation.observationCategory != null
                && observation.observationType != null
                && observation.geoLocation != null
                && observation.pointOfTime != null;

        if (!alwaysMandatoryFieldsPresent) {
            return false;
        }

        final ObservationSpecimenMetadata metadata =
                mObservationMetadataHelper.getMetadataForSpecies(observation.gameSpeciesCode);

        if (metadata == null) {
            return false;
        }

        final ObservationContextSensitiveFieldSet fieldSet =
                metadata.findFieldSetByType(observation.observationCategory, observation.observationType);

        if (fieldSet == null) {
            return false;
        }

        if (!isAmountValid(observation, fieldSet, isCarnivoreAuthority())) {
            return false;
        }

        if (!isDeerHuntingTypeValid(observation, fieldSet, isDeerPilotUser())) {
            return false;
        }

        if (!PhoneNumberValidator.isValid(observation.observerPhoneNumber)) {
            return false;
        }

        if (observation.mooseOrDeerHuntingCapability && !observation.observationCategorySelected) {
            return false;
        }

        return true;
    }

    private static boolean isAmountValid(final GameObservation observation,
                                         final ObservationContextSensitiveFieldSet fieldSet,
                                         final boolean isCarnivoreAuthority) {

        final Integer amount = observation.totalSpecimenAmount;
        final int mooselikeCount = observation.getMooselikeSpecimenCount();

        if (fieldSet.requiresMooselikeAmounts()) {
            return mooselikeCount > 0 && amount == null && observation.specimens == null;

        } else if (mooselikeCount > 0) {
            return false;
        }

        if (fieldSet.hasRequiredBaseField("amount")) {
            return amount != null
                    && amount > 0
                    && observation.specimens != null
                    && observation.specimens.size() <= amount;

        } else if (fieldSet.hasVoluntaryBaseField("amount")
                || isCarnivoreAuthority && fieldSet.hasVoluntaryCarnivoreAuthorityBaseField("amount")) {

            return (amount == null || amount > 0)
                    && observation.specimens != null
                    && observation.specimens.size() <= (amount != null ? amount : 0);

        } else {
            return amount == null && observation.specimens == null;
        }
    }

    private static boolean isDeerHuntingTypeValid(final GameObservation observation,
                                                  final ObservationContextSensitiveFieldSet fieldSet,
                                                  final boolean isDeerPilotUser) {

        final DeerHuntingType deerHuntingType = observation.deerHuntingType;

        final boolean isDeerHuntingTypeRequired = fieldSet.hasRequiredBaseField("deerHuntingType")
                || isDeerPilotUser && fieldSet.hasRequiredDeerPilotBaseField("deerHuntingType");

        final boolean isDeerHuntingTypeVoluntary = fieldSet.hasVoluntaryBaseField("deerHuntingType")
                || isDeerPilotUser && fieldSet.hasVoluntaryDeerPilotBaseField("deerHuntingType");

        if (isDeerHuntingTypeRequired) {
            if (deerHuntingType == null) {
                return false;
            }
        } else if (!isDeerHuntingTypeVoluntary && deerHuntingType != null) {
            return false;
        }

        final String description = observation.deerHuntingTypeDescription;

        final boolean isDescriptionRequired = fieldSet.hasRequiredBaseField("deerHuntingTypeDescription")
                || isDeerPilotUser && fieldSet.hasRequiredDeerPilotBaseField("deerHuntingTypeDescription");

        final boolean isDescriptionVoluntary = fieldSet.hasVoluntaryBaseField("deerHuntingTypeDescription")
                || isDeerPilotUser && fieldSet.hasVoluntaryDeerPilotBaseField("deerHuntingTypeDescription");

        if (isDescriptionRequired) {
            if (description == null) {
                return false;
            }
        } else if (!isDescriptionVoluntary && description != null) {
            return false;
        }

        // Description must not be present if deerHuntingType is NOT OTHER (cannot be expressed by metadata).
        return description == null || deerHuntingType == DeerHuntingType.OTHER;
    }

    private boolean isCarnivoreAuthority() {
        final UserInfo userInfo = mUserInfoStore.getUserInfo();
        return userInfo != null && userInfo.isCarnivoreAuthority();
    }

    private boolean isDeerPilotUser() {
        final UserInfo userInfo = mUserInfoStore.getUserInfo();
        return userInfo != null && userInfo.isDeerPilotUser();
    }
}
