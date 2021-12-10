package fi.riista.mobile.models.shootingTest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ShootingTestVenue implements Serializable {
    @JsonProperty("id")
    public Long id;

    @JsonProperty("rev")
    public int rev;

    @JsonProperty("name")
    public String name;

    @JsonProperty("address")
    public ShootingTestVenueAddress address;

    @JsonProperty("info")
    public String info;
}
