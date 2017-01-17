package fi.riista.mobile.models.announcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

public class AnnouncementSender implements Serializable {
    @JsonProperty("fullName")
    public String fullName;

    @JsonProperty("title")
    public Map<String, String> title;

    @JsonProperty("organisation")
    public Map<String, String> organisation;
}
