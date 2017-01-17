package fi.riista.mobile.models.srva;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class SrvaAuthorInfo implements Serializable {
    @JsonProperty("id")
    public Long id;

    @JsonProperty("rev")
    public Long rev;

    @JsonProperty("byName")
    public String byName;

    @JsonProperty("lastName")
    public String lastName;
}
