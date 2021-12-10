package fi.riista.mobile.models.shootingTest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ShootingTestVenueAddress implements Serializable {
    @JsonProperty("id")
    public Long id;

    @JsonProperty("rev")
    public int rev;

    @JsonProperty("streetAddress")
    public String streetAddress;

    @JsonProperty("postalCode")
    public String postalCode;

    @JsonProperty("city")
    public String city;

    @JsonProperty("country")
    public String country;

    @JsonProperty("editable")
    public boolean editable;
}
