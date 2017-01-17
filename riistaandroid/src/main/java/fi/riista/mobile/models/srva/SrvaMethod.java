package fi.riista.mobile.models.srva;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class SrvaMethod implements Serializable {
    @JsonProperty("name")
    public String name;

    @JsonProperty("isChecked")
    public boolean isChecked;
}
