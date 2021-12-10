package fi.riista.mobile.models.shootingTest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShootingTestSearchPersonResult {
    public static final String REGISTRATION_STATUS_COMPLETED = "COMPLETED";
    public static final String REGISTRATION_STATUS_OFFICIAL = "DISQUALIFIED_AS_OFFICIAL";
    public static final String REGISTRATION_STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String REGISTRATION_STATUS_HUNTING_PAYMENT_DONE = "HUNTING_PAYMENT_DONE";
    public static final String REGISTRATION_STATUS_HUNTING_PAYMENT_NOT_DONE = "HUNTING_PAYMENT_NOT_DONE";
    public static final String REGISTRATION_STATUS_HUNTING_BAN = "HUNTING_BAN";
    public static final String REGISTRATION_STATUS_NOT_HUNTER = "NO_HUNTER_NUMBER";
    public static final String REGISTRATION_STATUS_FOREIGN_HUNTER = "FOREIGN_HUNTER";

    @JsonProperty("id")
    public long id;

    @JsonProperty("firstName")
    public String firstName;

    @JsonProperty("lastName")
    public String lastName;

    @JsonProperty("hunterNumber")
    public String hunterNumber;

    @JsonProperty("dateOfBirth")
    public String dateOfBirth;

    @JsonProperty("registrationStatus")
    public String registrationStatus;

    @JsonProperty("selectedShootingTestTypes")
    public SelectedShootingTestTypes selectedShootingTestTypes;
}
