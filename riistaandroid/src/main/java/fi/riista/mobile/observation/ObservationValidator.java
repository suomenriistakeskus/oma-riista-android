package fi.riista.mobile.observation;

import fi.riista.mobile.models.GameObservation;
import fi.riista.mobile.models.metadata.ObservationContextSensitiveFieldSet;
import fi.riista.mobile.models.metadata.ObservationSpecimenMetadata;

public class ObservationValidator {

    public static boolean validate(GameObservation observation) {
        ObservationSpecimenMetadata metadata = ObservationMetadataHelper.getInstance().getMetadataForSpecies(observation.gameSpeciesCode);
        if (metadata == null) {
            return false;
        }
        ObservationContextSensitiveFieldSet fieldSet = metadata.findFieldSetByType(observation.observationType, observation.withinMooseHunting);
        if (fieldSet == null) {
            return false;
        }

        boolean valid = observation.gameSpeciesCode != 0
                && notEmpty(observation.observationType)
                && observation.geoLocation != null
                && observation.pointOfTime != null;

        if (valid) {
            int mooselikeCount = observation.getMooselikeSpecimenCount();

            if (fieldSet.requiresMooselikeAmounts(fieldSet.baseFields)) {
                valid = mooselikeCount > 0
                        && observation.totalSpecimenAmount == null
                        && observation.specimens == null;
            } else if (mooselikeCount > 0) {
                valid = false;
            } else {
                if (fieldSet.hasFieldSet(fieldSet.baseFields, "amount")) {
                    valid = observation.totalSpecimenAmount != null
                            && observation.totalSpecimenAmount > 0
                            && observation.specimens != null
                            && (observation.specimens.size() <= observation.totalSpecimenAmount);
                } else if (fieldSet.hasFieldVoluntary(fieldSet.baseFields, "amount")) {
                    valid = (observation.totalSpecimenAmount == null || observation.totalSpecimenAmount > 0)
                            && observation.specimens != null
                            && observation.specimens.size() <= (observation.totalSpecimenAmount != null ? observation.totalSpecimenAmount : 0);
                } else {
                    valid = observation.totalSpecimenAmount == null
                            && observation.specimens == null;
                }
            }
        }
        return valid;
    }

    private static boolean notEmpty(String text) {
        return text != null && text.trim().length() > 0;
    }
}
