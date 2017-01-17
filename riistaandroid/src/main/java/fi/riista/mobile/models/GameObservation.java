package fi.riista.mobile.models;

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
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.ModelUtils;


public class GameObservation extends LogEventBase implements Serializable {
    @JsonProperty("id")
    public Long remoteId;

    @JsonProperty("rev")
    public Long rev;

    @JsonProperty("type")
    public String type;

    @JsonProperty("observationSpecVersion")
    public int observationSpecVersion;

    @JsonProperty("geoLocation")
    public GeoLocation geoLocation;

    @JsonProperty("pointOfTime")
    public String pointOfTime;

    @JsonProperty("gameSpeciesCode")
    public int gameSpeciesCode;

    @JsonProperty("description")
    public String description;

    @JsonProperty("imageIds")
    public List<String> imageIds = new ArrayList<>();

    @JsonProperty("observationType")
    public String observationType;

    @JsonProperty("withinMooseHunting")
    public Boolean withinMooseHunting;

    @JsonProperty("specimens")
    public List<ObservationSpecimen> specimens = new ArrayList<>();

    @JsonProperty("canEdit")
    public boolean canEdit;

    @JsonProperty("mobileClientRefId")
    public Long mobileClientRefId;

    @JsonProperty("totalSpecimenAmount")
    public Integer totalSpecimenAmount;

    @JsonProperty("mooselikeMaleAmount")
    public Integer mooselikeMaleAmount;

    @JsonProperty("mooselikeFemaleAmount")
    public Integer mooselikeFemaleAmount;

    @JsonProperty("mooselikeFemale1CalfAmount")
    public Integer mooselikeFemale1CalfAmount;

    @JsonProperty("mooselikeFemale2CalfsAmount")
    public Integer mooselikeFemale2CalfsAmount;

    @JsonProperty("mooselikeFemale3CalfsAmount")
    public Integer mooselikeFemale3CalfsAmount;

    @JsonProperty("mooselikeFemale4CalfsAmount")
    public Integer mooselikeFemale4CalfsAmount;

    @JsonProperty("mooselikeUnknownSpecimenAmount")
    public Integer mooselikeUnknownSpecimenAmount;

    //Local client values

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

    public int getMooselikeSpecimenCount() {
        int count = 0;
        if (mooselikeMaleAmount != null) {
            count += mooselikeMaleAmount;
        }
        if (mooselikeFemaleAmount != null) {
            count += mooselikeFemaleAmount;
        }
        if (mooselikeFemale1CalfAmount != null) {
            count += mooselikeFemale1CalfAmount * (1 + 1);
        }
        if (mooselikeFemale2CalfsAmount != null) {
            count += mooselikeFemale2CalfsAmount * (1 + 2);
        }
        if (mooselikeFemale3CalfsAmount != null) {
            count += mooselikeFemale3CalfsAmount * (1 + 3);
        }
        if (mooselikeFemale4CalfsAmount != null) {
            count += mooselikeFemale4CalfsAmount * (1 + 4);
        }
        if (mooselikeUnknownSpecimenAmount != null) {
            count += mooselikeUnknownSpecimenAmount;
        }
        return count;
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

    public void copyLocalAttributes(GameObservation from) {
        localId = from.localId;
        deleted = from.deleted;
        modified = from.modified;
        localImages = new ArrayList<>(from.localImages);
        username = from.username;
    }

    public static GameObservation createNew() {
        GameObservation observation = new GameObservation();
        observation.setPointOfTime(DateTime.now());
        observation.type = TYPE_OBSERVATION;
        observation.observationSpecVersion = AppConfig.OBSERVATION_SPEC_VERSION;
        observation.canEdit = true;
        observation.modified = true;
        observation.mobileClientRefId = GameDatabase.generateMobileRefId(new Random());
        return observation;
    }
}
