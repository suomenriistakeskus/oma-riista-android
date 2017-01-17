package fi.riista.mobile.models.srva;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class SrvaSpecimen implements Serializable {
    @JsonProperty("gender")
    public String gender;

    @JsonProperty("age")
    public String age;
}
