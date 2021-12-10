package fi.riista.mobile.models.shootingTest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SelectedShootingTestTypes {
    @JsonProperty("mooseTestIntended")
    public boolean mooseTestIntended;

    @JsonProperty("bearTestIntended")
    public boolean bearTestIntended;

    @JsonProperty("roeDeerTestIntended")
    public boolean roeDeerTestIntended;

    @JsonProperty("bowTestIntended")
    public boolean bowTestIntended;
}
