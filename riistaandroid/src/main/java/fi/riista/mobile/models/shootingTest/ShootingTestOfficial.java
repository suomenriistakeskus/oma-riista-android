package fi.riista.mobile.models.shootingTest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ShootingTestOfficial implements Serializable {
    @JsonProperty("id")
    public Long id;

    @JsonProperty("shootingTestEventId")
    public Long shootingTestEventId;

    @JsonProperty("occupationId")
    public Long occupationId;

    @JsonProperty("personId")
    public Long personId;

    @JsonProperty("firstName")
    public String firstName;

    @JsonProperty("lastName")
    public String lastName;
}
