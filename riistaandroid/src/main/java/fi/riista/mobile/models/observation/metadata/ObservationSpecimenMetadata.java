package fi.riista.mobile.models.observation.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fi.riista.mobile.models.observation.ObservationCategory;
import fi.riista.mobile.models.observation.ObservationType;

public class ObservationSpecimenMetadata {

    @JsonProperty("gameSpeciesCode")
    public int gameSpeciesCode;

    @JsonProperty("maxLengthOfPaw")
    public Integer maxLengthOfPaw;

    @JsonProperty("minLengthOfPaw")
    public Integer minLengthOfPaw;

    @JsonProperty("maxWidthOfPaw")
    public Integer maxWidthOfPaw;

    @JsonProperty("minWidthOfPaw")
    public Integer minWidthOfPaw;

    @JsonProperty("baseFields")
    public Map<String, String> baseFields;

    @JsonProperty("specimenFields")
    public Map<String, String> specimenFields;

    @JsonProperty("contextSensitiveFieldSets")
    public List<ObservationContextSensitiveFieldSet> contextSensitiveFieldSets;

    public List<ObservationType> getObservationTypes(final ObservationCategory category) {
        final ArrayList<ObservationType> observationTypes = new ArrayList<>();

        for (final ObservationContextSensitiveFieldSet field : this.contextSensitiveFieldSets) {
            if (field.category == category) {
                observationTypes.add(field.type);
            }
        }

        return observationTypes;
    }

    public ObservationContextSensitiveFieldSet findFieldSetByType(final ObservationCategory category,
                                                                  final ObservationType type) {

        for (final ObservationContextSensitiveFieldSet set : contextSensitiveFieldSets) {
            if (set.category == category && set.type == type) {
                return set;
            }
        }

        return null;
    }

    public ObservationWithinHuntingCapability getMooseHuntingCapability() {
        return getWithinHuntingCapability("withinMooseHunting");
    }

    public ObservationWithinHuntingCapability getDeerHuntingCapability() {
        return getWithinHuntingCapability("withinDeerHunting");
    }

    private ObservationWithinHuntingCapability getWithinHuntingCapability(final String key) {
        final String value = baseFields.get(key);

        if (value == null) {
            return ObservationWithinHuntingCapability.NO;
        }

        // Try-catch block here to anticipate possible future changes.
        try {
            return ObservationWithinHuntingCapability.valueOf(value);
        } catch (final Exception e) {
            return ObservationWithinHuntingCapability.NO;
        }
    }
}
