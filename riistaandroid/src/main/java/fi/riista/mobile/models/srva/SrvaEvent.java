package fi.riista.mobile.models.srva;

import android.location.Location;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.models.GeoLocation;
import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.ModelUtils;

public class SrvaEvent implements Serializable {

    //public static final String STATE_UNFINISHED = "UNFINISHED";
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

    @Nullable
    @JsonProperty("deportationOrderNumber")
    public String deportationOrderNumber;

    @JsonProperty("eventType")
    public String eventType;

    @Nullable
    @JsonProperty("eventTypeDetail")
    public String eventTypeDetail;

    @Nullable
    @JsonProperty("otherEventTypeDetailDescription")
    public String otherEventTypeDetailDescription;

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

    @Nullable
    @JsonProperty("eventResultDetail")
    public String eventResultDetail;

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
        return DateTimeUtils.parseDateTime(pointOfTime);
    }

    public Location toLocation() {
        if (geoLocation != null) {
            return geoLocation.toLocation();
        }
        return null;
    }

    public List<GameLogImage> getImages() {
        // todo: change the order of image ids if backend implementation ever changes so that
        //       last image no longer is the first item

        // For SRVA the imageIds seem to be ordered so that the image that was last added
        // is actually the first one in the list. Reverse the list so that combineImages
        // organizes images correctly
        List<String> reversedImageIds = new ArrayList<>(imageIds);
        Collections.reverse(reversedImageIds);
        return ModelUtils.combineImages(reversedImageIds, localImages);
    }

    public void copyLocalAttributes(SrvaEvent from) {
        localId = from.localId;
        deleted = from.deleted;
        modified = from.modified;
        localImages = new ArrayList<>(from.localImages);
        username = from.username;
    }
}
