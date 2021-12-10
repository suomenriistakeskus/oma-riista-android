package fi.riista.mobile.models.shootingTest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShootingTestAttemptSummary {

    // TODO Verify that enum type works flawlessly when Proguard minification is enabled.
    @JsonProperty("type")
    public ShootingTestType type;

    @JsonProperty("attemptCount")
    public int attemptCount;

    @JsonProperty("qualified")
    public boolean qualified;
}
