package fi.riista.mobile.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ObservationSpecimen implements Serializable {
    @JsonProperty("id")
    public Long id;

    @JsonProperty("rev")
    public Long rev;

    @JsonProperty("gender")
    public String gender;

    @JsonProperty("age")
    public String age;

    @JsonProperty("state")
    public String state;

    @JsonProperty("marking")
    public String marking;
}
