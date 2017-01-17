package fi.riista.mobile.models.user;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInfo {

    @JsonProperty("username")
    private String username;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("birthDate")
    private Date birthDate;
    @JsonProperty("address")
    private Address address;
    @JsonProperty("homeMunicipality")
    private Map<String, String> homeMunicipality;
    @JsonProperty("gameDiaryYears")
    private List<Integer> gameDiaryYears = new ArrayList<>();
    @JsonProperty("hunterNumber")
    private String hunterNumber;
    @JsonProperty("rhy")
    private Rhy rhy;
    @JsonProperty("hunterExamDate")
    private Date hunterExamDate;
    @JsonProperty("huntingCardStart")
    private Date huntingCardStart;
    @JsonProperty("huntingCardEnd")
    private Date huntingCardEnd;
    @JsonProperty("huntingBanStart")
    private Date huntingBanStart;
    @JsonProperty("huntingBanEnd")
    private Date huntingBanEnd;
    @JsonProperty("huntingCardValidNow")
    private Boolean huntingCardValidNow;
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("enableSrva")
    private boolean enableSrva;
    @JsonProperty("occupations")
    private List<Occupation> occupations = new ArrayList<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    @JsonProperty("firstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    @JsonProperty("lastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @JsonProperty("birthDate")
    public Date getBirthDate() {
        return birthDate;
    }

    @JsonProperty("birthDate")
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    @JsonProperty("address")
    public Address getAddress() {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(Address address) {
        this.address = address;
    }

    @JsonProperty("homeMunicipality")
    public Map<String, String> getHomeMunicipality() {
        return homeMunicipality;
    }

    @JsonProperty("homeMunicipality")
    public void setHomeMunicipality(String key, String value) {
        this.homeMunicipality.put(key, value);
    }

    @JsonProperty("gameDiaryYears")
    public List<Integer> getGameDiaryYears() {
        return gameDiaryYears;
    }

    @JsonProperty("gameDiaryYears")
    public void setGameDiaryYears(List<Integer> gameDiaryYears) {
        this.gameDiaryYears = gameDiaryYears;
    }

    @JsonProperty("hunterNumber")
    public String getHunterNumber() {
        return hunterNumber;
    }

    @JsonProperty("hunterNumber")
    public void setHunterNumber(String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    @JsonProperty("rhy")
    public Rhy getRhy() {
        return rhy;
    }

    @JsonProperty("rhy")
    public void setRhy(Rhy rhy) {
        this.rhy = rhy;
    }

    @JsonProperty("hunterExamDate")
    public Date getHunterExamDate() {
        return hunterExamDate;
    }

    @JsonProperty("hunterExamDate")
    public void setHunterExamDate(Date hunterExamDate) {
        this.hunterExamDate = hunterExamDate;
    }

    @JsonProperty("huntingCardStart")
    public Date getHuntingCardStart() {
        return huntingCardStart;
    }

    @JsonProperty("huntingCardStart")
    public void setHuntingCardStart(Date huntingCardStart) {
        this.huntingCardStart = huntingCardStart;
    }

    @JsonProperty("huntingCardEnd")
    public Date getHuntingCardEnd() {
        return huntingCardEnd;
    }

    @JsonProperty("huntingCardEnd")
    public void setHuntingCardEnd(Date huntingCardEnd) {
        this.huntingCardEnd = huntingCardEnd;
    }

    @JsonProperty("huntingBanStart")
    public Date getHuntingBanStart() {
        return huntingBanStart;
    }

    @JsonProperty("huntingBanStart")
    public void setHuntingBanStart(Date huntingBanStart) {
        this.huntingBanStart = huntingBanStart;
    }

    @JsonProperty("huntingBanEnd")
    public Date getHuntingBanEnd() {
        return huntingBanEnd;
    }

    @JsonProperty("huntingBanEnd")
    public void setHuntingBanEnd(Date huntingBanEnd) {
        this.huntingBanEnd = huntingBanEnd;
    }

    @JsonProperty("huntingCardValidNow")
    public Boolean getHuntingCardValidNow() {
        return huntingCardValidNow;
    }

    @JsonProperty("huntingCardValidNow")
    public void setHuntingCardValidNow(Boolean huntingCardValidNow) {
        this.huntingCardValidNow = huntingCardValidNow;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("enableSrva")
    public boolean getEnableSrva() {
        return enableSrva;
    }

    @JsonProperty("enableSrva")
    public void setEnableSrva(boolean enableSrva) {
        this.enableSrva = enableSrva;
    }

    @JsonProperty("occupations")
    public List<Occupation> getOccupations() {
        return occupations;
    }

    @JsonProperty("occupations")
    public void setOccupations(List<Occupation> occupations) {
        this.occupations = occupations;
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
