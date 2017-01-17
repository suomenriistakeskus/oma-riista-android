package fi.riista.mobile.models.srva;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class SrvaSpecies {
    @JsonProperty("code")
    public int code;

    @JsonProperty("categoryId")
    public int categoryId;

    @JsonProperty("name")
    public Map<String, String> name;

    @JsonProperty("multipleSpecimenAllowedOnHarvests")
    public boolean multipleSpecimenAllowedOnHarvests;
}
