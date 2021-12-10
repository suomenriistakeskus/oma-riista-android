package fi.riista.mobile.models.shootingTest;

import android.content.Context;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import fi.riista.mobile.R;

public class ShootingTestCalendarEvent implements Serializable {
    private static final String EVENT_TYPE_DEFAULT = "AMPUMAKOE";
    private static final String EVENT_TYPE_BOW = "JOUSIAMPUMAKOE";

    @JsonProperty("rhyId")
    public Long rhyId;

    @JsonProperty("calendarEventId")
    public Long calendarEventId;

    @JsonProperty("shootingTestEventId")
    public Long shootingTestEventId;

    @JsonProperty("calendarEventType")
    public String calendarEventType;

    @JsonProperty("name")
    public String name;

    @JsonProperty("description")
    public String description;

    @JsonProperty("date")
    public String date;

    @JsonProperty("beginTime")
    public String beginTime;

    @JsonProperty("endTime")
    public String endTime;

    @JsonProperty("lockedTime")
    public String lockedTime;

    @JsonProperty("venue")
    public ShootingTestVenue venue;

    @JsonProperty("officials")
    public List<ShootingTestOfficial> officials;

    @JsonProperty("numberOfAllParticipants")
    public int numberOfAllParticipants;

    @JsonProperty("numberOfParticipantsWithNoAttempts")
    public int numberOfParticipantsWithNoAttempts;

    @JsonProperty("numberOfCompletedParticipants")
    public int numberOfCompletedParticipants;

    @JsonProperty("totalPaidAmount")
    public BigDecimal totalPaidAmount;

    public boolean isWaitingToStart() {
        return shootingTestEventId == null;
    }

    public boolean isOngoing() {
        return shootingTestEventId != null && !isClosed();
    }

    public boolean isClosed() {
        return shootingTestEventId != null && lockedTime != null && lockedTime.trim().length() > 0;
    }

    public boolean isReadyToClose() {
        return isOngoing() && numberOfAllParticipants == numberOfCompletedParticipants;
    }

    public static String localisedEventTypeText(Context context, String value) {
        String typeText = value;

        if (EVENT_TYPE_DEFAULT.equals(typeText)) {
            typeText = context.getString(R.string.shooting_test_event_type_defult);
        } else if (EVENT_TYPE_BOW.equals(typeText)) {
            typeText = context.getString(R.string.shooting_test_event_type_bow);
        }

        return typeText;
    }
}
