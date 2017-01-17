package fi.riista.mobile.models.srva;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SrvaParameters {
    @JsonProperty("ages")
    public List<String> ages;

    @JsonProperty("genders")
    public List<String> genders;

    @JsonProperty("species")
    public List<SrvaSpecies> species;

    @JsonProperty("events")
    public List<SrvaEventParameters> events;
}
