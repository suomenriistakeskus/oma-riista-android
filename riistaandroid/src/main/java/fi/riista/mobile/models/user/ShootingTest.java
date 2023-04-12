package fi.riista.mobile.models.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import fi.riista.common.domain.model.ShootingTestType;

public class ShootingTest {
    @JsonProperty("rhyName")
    private String rhyName;

    // TODO Verify that enum type works flawlessly when Proguard minification is enabled.
    @JsonProperty("type")
    private ShootingTestType type;

    @JsonProperty("begin")
    private String begin;

    @JsonProperty("end")
    private String end;

    @JsonProperty("expired")
    private boolean expired;

    @JsonProperty("rhyName")
    public String getRhyName() {
        return rhyName;
    }

    @JsonProperty("rhyName")
    public void setRhyName(String rhyName) {
        this.rhyName = rhyName;
    }

    @JsonProperty("type")
    public ShootingTestType getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(ShootingTestType type) {
        this.type = type;
    }

    @JsonProperty("begin")
    public String getBegin() {
        return begin;
    }

    @JsonProperty("begin")
    public void setBegin(String begin) {
        this.begin = begin;
    }

    @JsonProperty("end")
    public String getEnd() {
        return end;
    }

    @JsonProperty("end")
    public void setEnd(String end) {
        this.end = end;
    }

    @JsonProperty("expired")
    public boolean isExpired() {
        return expired;
    }

    @JsonProperty("expired")
    public void setExpired(boolean expired) {
        this.expired = expired;
    }
}
