package fi.riista.mobile.models.srva;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class SrvaApproverInfo implements Serializable {
    @JsonProperty("firstName")
    public String firstName;

    @JsonProperty("lastName")
    public String lastName;
}
