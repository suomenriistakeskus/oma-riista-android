package fi.riista.mobile.models.user;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class Organization {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("rev")
    private Integer rev;
    @JsonProperty("officialCode")
    private Integer officialCode;
    @JsonProperty("name")
    private Map<String, String> name;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

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

    @JsonProperty("officialCode")
    public Integer getOfficialCode() {
        return officialCode;
    }

    @JsonProperty("officialCode")
    public void setOfficialCode(Integer officialCode) {
        this.officialCode = officialCode;
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