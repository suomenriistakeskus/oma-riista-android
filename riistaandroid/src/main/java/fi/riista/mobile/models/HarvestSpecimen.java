package fi.riista.mobile.models;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HarvestSpecimen implements Serializable {

    public static ArrayList<HarvestSpecimen> withEmptyRemoved(@NonNull final List<HarvestSpecimen> specimens) {
        final ArrayList<HarvestSpecimen> result = new ArrayList<>(specimens.size());

        for (final HarvestSpecimen specimen : specimens) {
            if (!specimen.isEmpty()) {
                result.add(specimen);
            }
        }

        return result;
    }

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("rev")
    private Integer rev;

    @JsonProperty("age")
    private String age;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("weight")
    private Double weight;

    // Additional moose/deer fields -->

    @JsonProperty("weightEstimated")
    private Double weightEstimated;

    @JsonProperty("weightMeasured")
    private Double weightMeasured;

    // Moose only
    @JsonProperty("fitnessClass")
    private String fitnessClass;

    @JsonProperty("notEdible")
    private Boolean notEdible;

    @JsonProperty("additionalInfo")
    private String additionalInfo;

    @JsonProperty("antlersLost")
    private Boolean antlersLost;

    // Moose only
    @JsonProperty("antlersType")
    private String antlersType;

    @JsonProperty("antlersWidth")
    private Integer antlersWidth;

    @JsonProperty("antlerPointsLeft")
    private Integer antlerPointsLeft;

    @JsonProperty("antlerPointsRight")
    private Integer antlerPointsRight;

    @JsonProperty("antlersGirth")
    private Integer antlersGirth;

    @JsonProperty("antlersLength")
    private Integer antlersLength;

    @JsonProperty("antlersInnerWidth")
    private Integer antlersInnerWidth;

    @JsonProperty("antlerShaftWidth")
    private Integer antlerShaftWidth;

    // Moose only
    @JsonProperty("alone")
    private Boolean alone;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public HarvestSpecimen() {
    }

    // region Object-oriented helpers

    public boolean isEmpty() {
        return id == null &&
                age == null &&
                gender == null &&

                weight == null &&
                weightEstimated == null &&
                weightMeasured == null &&

                fitnessClass == null &&
                notEdible == null &&
                additionalInfo == null &&

                antlersLost == null &&
                antlersType == null &&
                antlersWidth == null &&
                antlerPointsLeft == null &&
                antlerPointsRight == null &&
                antlersGirth == null &&
                antlersLength == null &&
                antlersInnerWidth == null &&
                antlerShaftWidth == null &&

                alone == null;
    }

    public boolean isNotEdible() {
        return Boolean.TRUE.equals(notEdible);
    }

    public boolean isAntlersLost() {
        return Boolean.TRUE.equals(antlersLost);
    }

    public boolean isAlone() {
        return Boolean.TRUE.equals(alone);
    }

    public void clearExtensionFields() {
        weightEstimated = null;
        weightMeasured = null;
        fitnessClass = null;
        notEdible = null;
        additionalInfo = null;
        alone = null;

        clearAllAntlerFields();
    }

    public void clearAllAntlerFields() {
        antlersLost = null;
        clearAntlerDetailFields();
    }

    public void clearAntlerDetailFields() {
        antlersType = null;
        antlersWidth = null;
        antlerPointsLeft = null;
        antlerPointsRight = null;
        antlersGirth = null;
        antlersLength = null;
        antlersInnerWidth = null;
        antlerShaftWidth = null;
    }

    public HarvestSpecimen createCopy() {
        final HarvestSpecimen copy = new HarvestSpecimen();
        copy.setId(this.getId());
        copy.setRev(this.getRev());

        copy.setAge(this.getAge());
        copy.setGender(this.getGender());

        copy.setWeight(this.getWeight());
        copy.setWeightEstimated(this.getWeightEstimated());
        copy.setWeightMeasured(this.getWeightMeasured());

        copy.setFitnessClass(this.getFitnessClass());
        copy.setNotEdible(this.getNotEdible());
        copy.setAdditionalInfo(this.getAdditionalInfo());

        copy.setAntlersLost(this.getAntlersLost());
        copy.setAntlersType(this.getAntlersType());
        copy.setAntlersWidth(this.getAntlersWidth());
        copy.setAntlerPointsLeft(this.getAntlerPointsLeft());
        copy.setAntlerPointsRight(this.getAntlerPointsRight());
        copy.setAntlersGirth(this.getAntlersGirth());
        copy.setAntlersLength(this.getAntlersLength());
        copy.setAntlersInnerWidth(this.getAntlersInnerWidth());
        copy.setAntlerShaftWidth(this.getAntlerShaftWidth());

        copy.setAlone(this.getAlone());

        if (this.getAdditionalProperties() != null) {
            copy.getAdditionalProperties().putAll(this.getAdditionalProperties());
        }

        return copy;
    }

    // endregion

    // region Accessors

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRev() {
        return rev;
    }

    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getWeightEstimated() {
        return weightEstimated;
    }

    public void setWeightEstimated(Double weightEstimated) {
        this.weightEstimated = weightEstimated;
    }

    public Double getWeightMeasured() {
        return weightMeasured;
    }

    public void setWeightMeasured(Double weightMeasured) {
        this.weightMeasured = weightMeasured;
    }

    public String getFitnessClass() {
        return fitnessClass;
    }

    public void setFitnessClass(String fitnessClass) {
        this.fitnessClass = fitnessClass;
    }

    public Boolean getNotEdible() {
        return notEdible;
    }

    public void setNotEdible(Boolean notEdible) {
        this.notEdible = notEdible;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Boolean getAntlersLost() {
        return antlersLost;
    }

    public void setAntlersLost(Boolean antlersLost) {
        this.antlersLost = antlersLost;
    }

    public String getAntlersType() {
        return antlersType;
    }

    public void setAntlersType(String antlersType) {
        this.antlersType = antlersType;
    }

    public Integer getAntlersWidth() {
        return antlersWidth;
    }

    public void setAntlersWidth(Integer antlersWidth) {
        this.antlersWidth = antlersWidth;
    }

    public Integer getAntlerPointsLeft() {
        return antlerPointsLeft;
    }

    public void setAntlerPointsLeft(Integer antlerPointsLeft) {
        this.antlerPointsLeft = antlerPointsLeft;
    }

    public Integer getAntlerPointsRight() {
        return antlerPointsRight;
    }

    public void setAntlerPointsRight(Integer antlerPointsRight) {
        this.antlerPointsRight = antlerPointsRight;
    }

    public Integer getAntlersGirth() {
        return antlersGirth;
    }

    public void setAntlersGirth(Integer antlersGirth) {
        this.antlersGirth = antlersGirth;
    }

    public Integer getAntlersLength() {
        return antlersLength;
    }

    public void setAntlersLength(Integer antlersLength) {
        this.antlersLength = antlersLength;
    }

    public Integer getAntlersInnerWidth() {
        return antlersInnerWidth;
    }

    public void setAntlersInnerWidth(Integer antlersInnerWidth) {
        this.antlersInnerWidth = antlersInnerWidth;
    }

    public Integer getAntlerShaftWidth() {
        return antlerShaftWidth;
    }

    public void setAntlerShaftWidth(Integer antlerShaftWidth) {
        this.antlerShaftWidth = antlerShaftWidth;
    }

    public Boolean getAlone() {
        return alone;
    }

    public void setAlone(Boolean alone) {
        this.alone = alone;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    // endregion
}
