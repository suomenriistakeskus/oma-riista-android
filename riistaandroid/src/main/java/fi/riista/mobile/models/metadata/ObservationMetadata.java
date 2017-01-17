package fi.riista.mobile.models.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ObservationMetadata {
    @JsonProperty("observationSpecVersion")
    public int observationSpecVersion;

    @JsonProperty("lastModified")
    public String lastModified;

    @JsonProperty("speciesList")
    public List<ObservationSpecimenMetadata> speciesList;
}
