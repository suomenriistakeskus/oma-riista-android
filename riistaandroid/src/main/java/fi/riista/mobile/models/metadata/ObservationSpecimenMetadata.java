package fi.riista.mobile.models.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ObservationSpecimenMetadata {
    @JsonProperty("gameSpeciesCode")
    public int gameSpeciesCode;

    @JsonProperty("baseFields")
    public Map<String, String> baseFields;

    @JsonProperty("specimenFields")
    public Map<String, String> specimenFields;

    @JsonProperty("contextSensitiveFieldSets")
    public List<ObservationContextSensitiveFieldSet> contextSensitiveFieldSets;

    public ObservationContextSensitiveFieldSet findFieldSetByType(String type, Boolean withinMooseHunting) {
        boolean mooseHuntingSet = (withinMooseHunting != null) && withinMooseHunting;
        for (ObservationContextSensitiveFieldSet set : contextSensitiveFieldSets) {
            if (set.type.equals(type) && set.withinMooseHunting == mooseHuntingSet) {
                return set;
            }
        }
        return null;
    }

    public boolean hasBaseFieldSet(String name) {
        String state = baseFields.get(name);
        if (state != null && state.equals("YES")) {
            return true;
        }
        return false;
    }
}
