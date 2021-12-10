package fi.riista.mobile.models.shootingTest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ShootingTestParticipantDetailed {
    @JsonProperty("id")
    public long id;

    @JsonProperty("rev")
    public int rev;

    @JsonProperty("firstName")
    public String firstName;

    @JsonProperty("lastName")
    public String lastName;

    @JsonProperty("hunterNumber")
    public String hunterNumber;

    @JsonProperty("dateOfBirth")
    public String dateOfBirth;

    @JsonProperty("mooseTestIntended")
    public boolean mooseTestIntended;

    @JsonProperty("bearTestIntended")
    public boolean bearTestIntended;

    @JsonProperty("deerTestIntended")
    public boolean deerTestIntended;

    @JsonProperty("bowTestIntended")
    public boolean bowTestIntended;

    @JsonProperty("attempts")
    public List<ShootingTestAttemptDetailed> attempts = new ArrayList<>();
}
