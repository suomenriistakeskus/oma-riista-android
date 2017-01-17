package fi.riista.mobile.utils;

import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.Specimen;

public class HarvestValidator {

    public static boolean validate(GameHarvest entry) {
        boolean ok = true;

        if (entry.mSpecimen == null) {
            ok = false;
        }

        if (entry.mAmount < 1 || entry.mAmount > 999) {
            ok = false;
        }

        if (entry.mSpecimen == null) {
            ok = false;
        }

        if (entry.mLocation == null || entry.mLocation.getLatitude() == 0 || entry.mLocation.getLongitude() == 0) {
            ok = false;
        }

        if (entry.isMoose() || entry.isDeer()) {
            for (Specimen specimen : entry.mSpecimen) {
                if (specimen.getWeight() != null) {
                    Utils.LogMessage("HarvestValidator: Moose weight must be null");
                    ok = false;
                }
            }
        }
        return ok;
    }
}
