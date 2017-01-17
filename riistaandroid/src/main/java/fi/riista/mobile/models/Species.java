package fi.riista.mobile.models;

import java.io.Serializable;

public class Species implements Serializable {
    public int mCategory = 0;
    public int mId = 0;
    public String mName = "";
    public boolean mMultipleSpecimenAllowedOnHarvests = false;
}
