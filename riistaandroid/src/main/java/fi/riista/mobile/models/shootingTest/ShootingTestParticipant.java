package fi.riista.mobile.models.shootingTest;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShootingTestParticipant implements Serializable {
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

    @JsonProperty("mooseTestIntended")
    public boolean mooseTestIntended;

    @JsonProperty("bearTestIntended")
    public boolean bearTestIntended;

    @JsonProperty("deerTestIntended")
    public boolean deerTestIntended;

    @JsonProperty("bowTestIntended")
    public boolean bowTestIntended;

    @JsonProperty("attempts")
    public List<ShootingTestAttemptSummary> attempts = new ArrayList<>();

    @JsonProperty("totalDueAmount")
    public int totalDueAmount;

    @JsonProperty("paidAmount")
    public int paidAmount;

    @JsonProperty("remainingAmount")
    public int remainingAmount;

    @JsonProperty("registrationTime")
    public String registrationTime;

    @JsonProperty("completed")
    public boolean completed;

    @Nullable
    public ShootingTestAttemptSummary getAttemptSummaryFor(@Nullable final ShootingTestType type) {
        if (type != null) {
            for (final ShootingTestAttemptSummary item : attempts) {
                if (type == item.type) {
                    return item;
                }
            }
        }
        return null;
    }
}
