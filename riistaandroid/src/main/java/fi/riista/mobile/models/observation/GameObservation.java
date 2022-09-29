package fi.riista.mobile.models.observation;

import android.location.Location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.DeerHuntingType;
import fi.riista.mobile.models.GameLog;
import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.models.GeoLocation;
import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.utils.ConstantsKt;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.ModelUtils;

public class GameObservation implements Serializable {
    @JsonProperty("id")
    public Long remoteId;

    @JsonProperty("rev")
    public Long rev;

    @JsonProperty("type")
    public String type;

    @JsonProperty("observationSpecVersion")
    public int observationSpecVersion;

    @JsonProperty("gameSpeciesCode")
    public int gameSpeciesCode;

    @JsonProperty("observationCategory")
    public ObservationCategory observationCategory;

    @JsonProperty("observationType")
    public ObservationType observationType;

    @JsonProperty("deerHuntingType")
    public DeerHuntingType deerHuntingType;

    @JsonProperty("deerHuntingTypeDescription")
    public String deerHuntingTypeDescription;

    @JsonProperty("geoLocation")
    public GeoLocation geoLocation;

    @JsonProperty("pointOfTime")
    public String pointOfTime;

    @JsonProperty("description")
    public String description;

    @JsonProperty("imageIds")
    public List<String> imageIds = new ArrayList<>();

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

    @JsonProperty("mooselikeCalfAmount")
    public Integer mooselikeCalfAmount;

    @JsonProperty("mooselikeUnknownSpecimenAmount")
    public Integer mooselikeUnknownSpecimenAmount;

    @JsonProperty("observerName")
    public String observerName;

    @JsonProperty("observerPhoneNumber")
    public String observerPhoneNumber;

    @JsonProperty("officialAdditionalInfo")
    public String officialAdditionalInfo;

    @JsonProperty("verifiedByCarnivoreAuthority")
    public Boolean verifiedByCarnivoreAuthority;

    @JsonProperty("inYardDistanceToResidence")
    public Integer inYardDistanceToResidence;

    @JsonProperty("litter")
    public Boolean litter;

    @JsonProperty("pack")
    public Boolean pack;

    // Local client values

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

    @JsonIgnore
    public boolean observationCategorySelected = false;

    @JsonIgnore
    public boolean mooseOrDeerHuntingCapability = false;

    public static GameObservation createNew() {
        final GameObservation observation = new GameObservation();
        observation.setPointOfTime(DateTime.now());
        observation.type = GameLog.TYPE_OBSERVATION;
        observation.observationSpecVersion = AppConfig.OBSERVATION_SPEC_VERSION;
        observation.observationCategory = ObservationCategory.NORMAL;
        observation.canEdit = true;
        observation.modified = true;
        observation.mobileClientRefId = GameLog.generateMobileRefId();
        return observation;
    }

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
        if (mooselikeCalfAmount != null) {
            count += mooselikeCalfAmount;
        }
        if (mooselikeUnknownSpecimenAmount != null) {
            count += mooselikeUnknownSpecimenAmount;
        }
        return count;
    }

    public List<GameLogImage> getImages() {
        return ModelUtils.combineImages(imageIds, localImages);
    }

    public void setLocalImages(List<GameLogImage> images) {
        localImages.clear();

        for (GameLogImage image : images) {
            localImages.add(LocalImage.fromGameLogImage(image));
        }
    }

    public DateTime toDateTime() {
        return DateTimeUtils.parseDateTime(pointOfTime);
    }

    public void setPointOfTime(DateTime dateTime) {
        pointOfTime = dateTime.toString(ConstantsKt.ISO_8601);
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
}
