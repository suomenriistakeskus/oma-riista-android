package fi.riista.mobile.models.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ObservationContextSensitiveFieldSet {
    @JsonProperty("withinMooseHunting")
    public boolean withinMooseHunting;

    @JsonProperty("type")
    public String type;

    @JsonProperty("baseFields")
    public Map<String, String> baseFields;

    @JsonProperty("specimenFields")
    public Map<String, String> specimenFields;

    @JsonProperty("allowedAges")
    public List<String> allowedAges;

    @JsonProperty("allowedStates")
    public List<String> allowedStates;

    @JsonProperty("allowedMarkings")
    public List<String> allowedMarkings;

    public boolean hasField(Map<String, String> fields, String name) {
        return hasFieldSet(fields, name) || hasFieldVoluntary(fields, name);
    }

    public boolean hasFieldSet(Map<String, String> fields, String name) {
        String value = fields.get(name);
        if (value != null) {
            return value.equals("YES");
        }
        return false;
    }

    public boolean hasFieldVoluntary(Map<String, String> fields, String name) {
        String value = fields.get(name);
        if (value != null) {
            return value.equals("VOLUNTARY");
        }
        return false;
    }

    public boolean requiresMooselikeAmounts(Map<String, String> fields) {
        return hasFieldSet(fields, "mooselikeFemaleAmount");
    }
}
