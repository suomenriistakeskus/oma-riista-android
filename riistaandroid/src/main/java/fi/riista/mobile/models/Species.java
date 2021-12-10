package fi.riista.mobile.models;

import java.io.Serializable;

public class Species implements Serializable {

    public Integer mCategory = 0;
    public Integer mId = 0;
    public String mName = "";
    public boolean mMultipleSpecimenAllowedOnHarvests = false;

    public boolean hasSpeciesCode(final int speciesCode) {
        return mId != null && mId.intValue() == speciesCode;
    }
}
