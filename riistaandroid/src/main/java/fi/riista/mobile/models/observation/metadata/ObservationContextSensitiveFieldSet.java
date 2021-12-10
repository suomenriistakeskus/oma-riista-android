package fi.riista.mobile.models.observation.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

import fi.riista.mobile.models.observation.ObservationCategory;
import fi.riista.mobile.models.observation.ObservationType;

public class ObservationContextSensitiveFieldSet {

    @JsonProperty("category")
    public ObservationCategory category;

    @JsonProperty("type")
    public ObservationType type;

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

    public boolean hasBaseField(final String name) {
        return hasField(baseFields, name);
    }

    public boolean hasSpecimenField(final String name) {
        return hasField(specimenFields, name);
    }

    private static boolean hasField(final Map<String, String> fields, final String name) {
        return hasRequiredField(fields, name) || hasVoluntaryField(fields, name);
    }

    public boolean hasRequiredBaseField(final String name) {
        return hasRequiredField(baseFields, name);
    }

    public boolean hasRequiredSpecimenField(final String name) {
        return hasRequiredField(specimenFields, name);
    }

    private static boolean hasRequiredField(final Map<String, String> fields, final String name) {
        final String value = fields.get(name);
        return "YES".equals(value);
    }

    public boolean hasVoluntaryBaseField(final String name) {
        return hasVoluntaryField(baseFields, name);
    }

    public boolean hasVoluntarySpecimenField(final String name) {
        return hasVoluntaryField(specimenFields, name);
    }

    private static boolean hasVoluntaryField(final Map<String, String> fields, final String name) {
        final String value = fields.get(name);
        return "VOLUNTARY".equals(value);
    }

    public boolean hasDeerPilotBaseField(final String name) {
        return hasRequiredDeerPilotBaseField(name) || hasVoluntaryDeerPilotBaseField(name);
    }

    public boolean hasRequiredDeerPilotBaseField(final String name) {
        final String value = baseFields.get(name);
        return "YES_DEER_PILOT".equals(value);
    }

    public boolean hasVoluntaryDeerPilotBaseField(final String name) {
        final String value = baseFields.get(name);
        return "VOLUNTARY_DEER_PILOT".equals(value);
    }

    public boolean hasVoluntaryCarnivoreAuthorityBaseField(final String name) {
        return hasVoluntaryCarnivoreAuthorityField(baseFields, name);
    }

    public boolean hasVoluntaryCarnivoreAuthoritySpecimenField(final String name) {
        return hasVoluntaryCarnivoreAuthorityField(specimenFields, name);
    }

    private static boolean hasVoluntaryCarnivoreAuthorityField(final Map<String, String> fields, final String name) {
        final String value = fields.get(name);
        return "VOLUNTARY_CARNIVORE_AUTHORITY".equals(value);
    }

    public boolean requiresMooselikeAmounts() {
        return hasRequiredField(baseFields, "mooselikeFemaleAmount");
    }
}
