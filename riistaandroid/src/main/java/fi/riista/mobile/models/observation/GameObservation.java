package fi.riista.mobile.models.observation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fi.riista.mobile.models.DeerHuntingType;
import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.models.GeoLocation;
import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.utils.ConstantsKt;
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

    public List<GameLogImage> getImages() {
        return ModelUtils.combineImages(imageIds, localImages);
    }

    public void setPointOfTime(DateTime dateTime) {
        pointOfTime = dateTime.toString(ConstantsKt.ISO_8601);
    }
}
