package fi.riista.mobile.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class ClubAreaMap {
    @JsonProperty("huntingYear")
    public int huntingYear;

    @JsonProperty("type")
    public String type;

    @JsonProperty("name")
    public HashMap<String, String> name;

    @JsonProperty("clubName")
    public HashMap<String, String> clubName;

    @JsonProperty("externalId")
    public String externalId;

    @JsonProperty("modificationTime")
    public String modificationTime;

    //Local value
    @JsonProperty("manuallyAdded")
    public boolean manuallyAdded;
}
