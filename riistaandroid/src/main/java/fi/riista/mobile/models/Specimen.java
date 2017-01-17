package fi.riista.mobile.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Specimen implements Serializable {

    public enum SpecimenGender {
        FEMALE("FEMALE"),
        MALE("MALE"),
        UNKNOWN("UNKNOWN");

        private final String mValue;

        SpecimenGender(String value) {
            this.mValue = value;
        }

        @Override
        public String toString() {
            return this.mValue;
        }
    }

    public enum SpecimenAge {
        ADULT("ADULT"),
        YOUNG("YOUNG"),
        UNKNOWN("UNKNOWN");

        private final String mValue;

        SpecimenAge(String value) {
            this.mValue = value;
        }

        @Override
        public String toString() {
            return this.mValue;
        }
    }

    public enum MooseFitnessClass {
        EXCELLENT("ERINOMAINEN"),
        NORMAL("NORMAALI"),
        THIN("LAIHA"),
        STARVED("NAANTYNYT");

        private final String mValue;

        MooseFitnessClass(String value) {
            this.mValue = value;
        }

        @Override
        public String toString() {
            return this.mValue;
        }
    }

    public enum MooseAntlersType {
        HANKO("HANKO"),
        LAPIO("LAPIO"),
        SEKA("SEKA");

        private final String mValue;

        MooseAntlersType(String value) {
            this.mValue = value;
        }

        @Override
        public String toString() {
            return this.mValue;
        }
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

    //Additional moose and/or deer fields
    @JsonProperty("weightEstimated")
    private Double weightEstimated;

    @JsonProperty("weightMeasured")
    private Double weightMeasured;

    //Moose only
    @JsonProperty("fitnessClass")
    private String fitnessClass;

    //Moose only
    @JsonProperty("antlersType")
    private String antlersType;

    @JsonProperty("antlersWidth")
    private Integer antlersWidth;

    @JsonProperty("antlerPointsLeft")
    private Integer antlerPointsLeft;

    @JsonProperty("antlerPointsRight")
    private Integer antlerPointsRight;

    @JsonProperty("notEdible")
    private Boolean notEdible;

    @JsonProperty("additionalInfo")
    private String additionalInfo;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    public Specimen() {
    }

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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public boolean isEmpty() {
        return id == null &&
                age == null &&
                gender == null &&
                weight == null &&
                weightEstimated == null &&
                weightMeasured == null &&
                fitnessClass == null &&
                antlersType == null &&
                antlersWidth == null &&
                antlerPointsLeft == null &&
                antlerPointsRight == null &&
                notEdible == null &&
                additionalInfo == null;

    }
}
