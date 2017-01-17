package fi.riista.mobile.models.srva;

import android.location.Location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.GeoLocation;
import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.models.LogEventBase;
import fi.riista.mobile.models.LogImage;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.ModelUtils;

public class SrvaEvent extends LogEventBase implements Serializable {

    public static final String STATE_UNFINISHED = "UNFINISHED";
    public static final String STATE_APPROVED = "APPROVED";
    public static final String STATE_REJECTED = "REJECTED";

    @JsonProperty("id")
    public Long remoteId;

    @JsonProperty("rev")
    public Long rev;

    @JsonProperty("type")
    public String type;

    @JsonProperty("geoLocation")
    public GeoLocation geoLocation;

    @JsonProperty("pointOfTime")
    public String pointOfTime;

    @JsonProperty("gameSpeciesCode")
    public Integer gameSpeciesCode;

    @JsonProperty("description")
    public String description;

    @JsonProperty("canEdit")
    public boolean canEdit;

    @JsonProperty("imageIds")
    public List<String> imageIds = new ArrayList<>();

    @JsonProperty("eventName")
    public String eventName;

    @JsonProperty("eventType")
    public String eventType;

    @JsonProperty("totalSpecimenAmount")
    public int totalSpecimenAmount;

    @JsonProperty("otherMethodDescription")
    public String otherMethodDescription;

    @JsonProperty("otherTypeDescription")
    public String otherTypeDescription;

    @JsonProperty("methods")
    public List<SrvaMethod> methods = new ArrayList<>();

    @JsonProperty("personCount")
    public int personCount;

    @JsonProperty("timeSpent")
    public int timeSpent;

    @JsonProperty("eventResult")
    public String eventResult;

    @JsonProperty("authorInfo")
    public SrvaAuthorInfo authorInfo;

    @JsonProperty("specimens")
    public List<SrvaSpecimen> specimens = new ArrayList<>();

    @JsonProperty("rhyId")
    public Integer rhyId;

    @JsonProperty("state")
    public String state;

    @JsonProperty("otherSpeciesDescription")
    public String otherSpeciesDescription;

    @JsonProperty("approverInfo")
    public SrvaApproverInfo approverInfo;

    @JsonProperty("mobileClientRefId")
    public Long mobileClientRefId;

    @JsonProperty("srvaEventSpecVersion")
    public int srvaEventSpecVersion;

    @JsonIgnore
    public Long localId;

    @JsonIgnore
    public boolean deleted = false;

    @JsonIgnore
    public boolean modified = false;

    @JsonIgnore
    public List<LocalImage> localImages = new ArrayList<>();

    @JsonIgnore
    public String username;


    public DateTime toDateTime() {
        return DateTimeUtils.parseDate(pointOfTime);
    }

    public void setPointOfTime(DateTime dateTime) {
        pointOfTime = dateTime.toString(AppConfig.SERVER_DATE_FORMAT);
    }

    public Location toLocation() {
        if (geoLocation != null) {
            return geoLocation.toLocation();
        }
        return null;
    }

    public List<LogImage> getAllImages() {
        return ModelUtils.combineImages(localId, imageIds, localImages);
    }

    public void setLocalImages(List<LogImage> images) {
        localImages.clear();

        for (LogImage image : images) {
            localImages.add(LocalImage.fromLogImage(image));
        }
    }

    public void copyLocalAttributes(SrvaEvent from) {
        localId = from.localId;
        deleted = from.deleted;
        modified = from.modified;
        localImages = new ArrayList<>(from.localImages);
        username = from.username;
    }

    public static SrvaEvent createNew() {
        SrvaEvent event = new SrvaEvent();
        event.setPointOfTime(DateTime.now());
        event.type = TYPE_SRVA;
        event.srvaEventSpecVersion = AppConfig.SRVA_SPEC_VERSION;
        event.mobileClientRefId = GameDatabase.generateMobileRefId(new Random());
        event.state = STATE_UNFINISHED;
        event.canEdit = true;
        event.modified = true;
        return event;
    }
}
