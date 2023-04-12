package fi.riista.mobile.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "rev",
        "permitType",
        "permitNumber",
        "speciesAmounts",
        "unavailable",
        "harvestsAsList"
})
public class Permit {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("rev")
    private Integer rev;
    @JsonProperty("permitType")
    private String permitType;
    @JsonProperty("permitNumber")
    private String permitNumber;
    @JsonProperty("speciesAmounts")
    private List<PermitSpeciesAmount> speciesAmounts = new ArrayList<>();
    @JsonProperty("unavailable")
    private Boolean unavailable;
    @JsonProperty("harvestsAsList")
    private Boolean harvestsAsList;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("rev")
    public Integer getRev() {
        return rev;
    }

    @JsonProperty("rev")
    public void setRev(Integer rev) {
        this.rev = rev;
    }

    @JsonProperty("permitType")
    public String getPermitType() {
        return permitType;
    }

    @JsonProperty("permitType")
    public void setPermitType(String permitType) {
        this.permitType = permitType;
    }

    @JsonProperty("permitNumber")
    public String getPermitNumber() {
        return permitNumber;
    }

    @JsonProperty("permitNumber")
    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    @JsonProperty("speciesAmounts")
    public List<PermitSpeciesAmount> getSpeciesAmounts() {
        return speciesAmounts;
    }

    @JsonProperty("speciesAmounts")
    public void setSpeciesAmounts(List<PermitSpeciesAmount> speciesAmounts) {
        this.speciesAmounts = speciesAmounts;
    }

    @JsonProperty("unavailable")
    public Boolean getUnavailable() {
        return unavailable;
    }

    @JsonProperty("unavailable")
    public void setUnavailable(Boolean unavailable) {
        this.unavailable = unavailable;
    }

    @JsonProperty("harvestsAsList")
    public Boolean getHarvestsAsList() {
        return harvestsAsList;
    }

    @JsonProperty("harvestsAsList")
    public void setHarvestsAsList(Boolean harvestsAsList) {
        this.harvestsAsList = harvestsAsList;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
