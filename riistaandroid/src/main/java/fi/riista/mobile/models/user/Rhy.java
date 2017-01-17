package fi.riista.mobile.models.user;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class Rhy {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("officialCode")
    private String officialCode;
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

    @JsonProperty("officialCode")
    public String getOfficialCode() {
        return officialCode;
    }

    @JsonProperty("officialCode")
    public void setOfficialCode(String officialCode) {
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
