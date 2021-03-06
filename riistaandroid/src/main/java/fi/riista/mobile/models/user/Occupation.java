package fi.riista.mobile.models.user;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Occupation {

    @JsonProperty("organisation")
    private Organization organisation;
    @JsonProperty("beginDate")
    private Date beginDate;
    @JsonProperty("endDate")
    private Date endDate;
    @JsonProperty("occupationType")
    private String occupationType;
    @JsonProperty("name")
    private Map<String, String> name;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("organisation")
    public Organization getOrganisation() {
        return organisation;
    }

    @JsonProperty("organisation")
    public void setOrganisation(Organization organisation) {
        this.organisation = organisation;
    }

    @JsonProperty("beginDate")
    public Date getBeginDate() {
        return beginDate;
    }

    @JsonProperty("beginDate")
    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    @JsonProperty("endDate")
    public Date getEndDate() {
        return endDate;
    }

    @JsonProperty("endDate")
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @JsonProperty("occupationType")
    public String getOccupationType() {
        return occupationType;
    }

    @JsonProperty("occupationType")
    public void setOccupationType(String occupationType) {
        this.occupationType = occupationType;
    }

    @JsonProperty("name")
    public Map<String, String> getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String key, String value) {
        this.name.put(key, value);
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
