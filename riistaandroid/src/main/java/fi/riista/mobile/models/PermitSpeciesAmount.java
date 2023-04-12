package fi.riista.mobile.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.joda.time.LocalDate;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "gameSpeciesCode",
        "amount",
        "beginDate",
        "endDate",
        "beginDate2",
        "endDate2",
        "ageRequired",
        "genderRequired",
        "weightRequired"
})
public class PermitSpeciesAmount {

    @JsonProperty("gameSpeciesCode")
    private Integer gameSpeciesCode;
    @JsonProperty("amount")
    // Note! Amount type is double
    // Young animal may count as half for calculating allowed harvest amounts.
    private Double amount;
    @JsonProperty("beginDate")
    private LocalDate beginDate;
    @JsonProperty("endDate")
    private LocalDate endDate;
    @JsonProperty("beginDate2")
    private LocalDate beginDate2;
    @JsonProperty("endDate2")
    private LocalDate endDate2;
    @JsonProperty("ageRequired")
    private Boolean ageRequired;
    @JsonProperty("genderRequired")
    private Boolean genderRequired;
    @JsonProperty("weightRequired")
    private Boolean weightRequired;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("gameSpeciesCode")
    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    @JsonProperty("gameSpeciesCode")
    public void setGameSpeciesCode(Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    @JsonProperty("amount")
    public Double getAmount() {
        return amount;
    }

    @JsonProperty("amount")
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @JsonProperty("beginDate")
    public LocalDate getBeginDate() {
        return beginDate;
    }

    @JsonProperty("beginDate")
    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @JsonProperty("endDate")
    public LocalDate getEndDate() {
        return endDate;
    }

    @JsonProperty("endDate")
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @JsonProperty("beginDate2")
    public LocalDate getBeginDate2() {
        return beginDate2;
    }

    @JsonProperty("beginDate2")
    public void setBeginDate2(LocalDate beginDate2) {
        this.beginDate2 = beginDate2;
    }

    @JsonProperty("endDate2")
    public LocalDate getEndDate2() {
        return endDate2;
    }

    @JsonProperty("endDate2")
    public void setEndDate2(LocalDate endDate2) {
        this.endDate2 = endDate2;
    }

    @JsonProperty("ageRequired")
    public Boolean getAgeRequired() {
        return ageRequired;
    }

    @JsonProperty("ageRequired")
    public void setAgeRequired(Boolean ageRequired) {
        this.ageRequired = ageRequired;
    }

    @JsonProperty("genderRequired")
    public Boolean getGenderRequired() {
        return genderRequired;
    }

    @JsonProperty("genderRequired")
    public void setGenderRequired(Boolean genderRequired) {
        this.genderRequired = genderRequired;
    }

    @JsonProperty("weightRequired")
    public Boolean getWeightRequired() {
        return weightRequired;
    }

    @JsonProperty("weightRequired")
    public void setWeightRequired(Boolean weightRequired) {
        this.weightRequired = weightRequired;
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
