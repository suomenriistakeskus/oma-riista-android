package fi.riista.mobile.models.announcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Announcement implements Serializable {
    @JsonProperty("id")
    public Long remoteId;

    @JsonProperty("rev")
    public int rev;

    @JsonProperty("pointOfTime")
    public String pointOfTime;

    @JsonProperty("sender")
    public AnnouncementSender sender;

    @JsonProperty("subject")
    public String subject;

    @JsonProperty("body")
    public String body;
}
