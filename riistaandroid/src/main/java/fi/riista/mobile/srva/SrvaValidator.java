package fi.riista.mobile.srva;

import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.srva.SrvaEvent;

public class SrvaValidator {

    public static boolean validate(SrvaEvent event) {
        if (isEmpty(event.eventName) ||
                isEmpty(event.eventType) ||
                event.geoLocation == null ||
                event.totalSpecimenAmount <= 0 ||
                event.specimens == null) {
            return false;
        }

        if (event.gameSpeciesCode == null && isEmpty(event.otherSpeciesDescription)) {
            return false;
        }
        if (event.gameSpeciesCode != null && !isEmpty(event.otherSpeciesDescription)) {
            return false;
        }
        if (event.gameSpeciesCode != null) {
            Species species = SpeciesInformation.getSpecies(event.gameSpeciesCode);
            if (species == null) {
                //Not a valid species code
                return false;
            }
        }

        return true;
    }

    private static boolean isEmpty(String text) {
        return text == null || text.trim().length() == 0;
    }
}
