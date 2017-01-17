package fi.riista.mobile.models.srva;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SrvaEventParameters {
    @JsonProperty("name")
    public String name;

    @JsonProperty("types")
    public List<String> types;

    @JsonProperty("results")
    public List<String> results;

    @JsonProperty("methods")
    public List<SrvaMethod> methods;
}
