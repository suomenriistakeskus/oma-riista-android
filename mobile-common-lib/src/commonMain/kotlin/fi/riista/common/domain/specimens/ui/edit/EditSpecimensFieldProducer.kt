package fi.riista.common.domain.specimens.ui.edit

import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.localizationKey
import fi.riista.common.domain.observation.model.ObservationSpecimenMarking
import fi.riista.common.domain.observation.model.ObservationSpecimenState
import fi.riista.common.domain.specimens.ui.SpecimenFieldId
import fi.riista.common.domain.specimens.ui.SpecimenFieldSpecification
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.domain.specimens.ui.SpecimenSpecifications
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.StringWithId
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.toLocalizedStringWithId
import fi.riista.common.ui.dataField.AgeField
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.EnumStringListFieldFactory
import fi.riista.common.ui.dataField.GenderField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.Padding
import fi.riista.common.ui.dataField.StringListField
import kotlin.math.round

internal class EditSpecimensFieldProducer(
    val species: Species,
    private val speciesResolver: SpeciesResolver,
    private val stringProvider: StringProvider,
) {
    private val ageFactory = EnumStringListFieldFactory
        .createForNonLocalizableEnum<GameAge>(stringProvider) { age, stringProvider ->
            age.toLocalizedStringWithId {
                stringProvider.getString(it.localizationKey(species))
            }
        }
    private val markingFactory = EnumStringListFieldFactory.create<ObservationSpecimenMarking>(stringProvider)
    private val stateOrHealthFactory = EnumStringListFieldFactory.create<ObservationSpecimenState>(stringProvider)

    fun createSpeciesHeader(
        // the stable index of the specimen -> allows identifying the specimen at later point
        stableSpecimenIndex: Int,
        displayedSpecimenIndex: Int,
        specimenCount: Int,
    ): DataField<SpecimenFieldId> {
        val titleText = when (species) {
            is Species.Known ->
                speciesResolver.getSpeciesName(speciesCode = species.speciesCode)
                    ?: stringProvider.getString(RR.string.other_species)
            Species.Other ->
                stringProvider.getString(RR.string.other_species)
            Species.Unknown ->
                stringProvider.getString(RR.string.unknown_species)
        }

        val indexText = if (specimenCount > 1) {
            " ${displayedSpecimenIndex + 1} / $specimenCount"
        } else {
            ""
        }

        return LabelField(
            id = SpecimenFieldType.SPECIMEN_HEADER.toField(stableSpecimenIndex),
            text = "$titleText$indexText",
            type = LabelField.Type.CAPTION
        ) {
            paddingTop = if (displayedSpecimenIndex > 0) {
                Padding.LARGE
            } else {
                Padding.MEDIUM
            }
            paddingBottom = Padding.NONE
        }
    }

    fun createField(
        fieldSpecification: SpecimenFieldSpecification,
        specimen: CommonSpecimenData,
        stableSpecimenIndex: Int,
        specimenSpecifications: SpecimenSpecifications,
    ) : DataField<SpecimenFieldId>? {
        return when (fieldSpecification.fieldType) {
            SpecimenFieldType.SPECIMEN_HEADER -> null // header added separately

            SpecimenFieldType.GENDER ->
                GenderField(
                    id = fieldSpecification.fieldType.toField(stableSpecimenIndex),
                    gender = specimen.gender?.value,
                ) {
                    readOnly = false
                    showUnknown = true
                    paddingTop = Padding.SMALL_MEDIUM
                    paddingBottom = Padding.SMALL
                }
            SpecimenFieldType.AGE -> {
                if (!specimenSpecifications.containsOnlyAdultYoungOrUnknownAgeValues) {
                    return ageFactory.create(
                        fieldId = fieldSpecification.fieldType.toField(stableSpecimenIndex),
                        currentEnumValue = specimen.age ?: BackendEnum.create(null),
                        enumValues = specimenSpecifications.allowedAges,
                        allowEmptyValue = fieldSpecification.requirementStatus.isRequired().not(),
                    ) {
                        readOnly = false
                        label = stringProvider.getString(RR.string.age_label)
                    }
                }

                AgeField(
                    id = fieldSpecification.fieldType.toField(stableSpecimenIndex),
                    age = specimen.age?.value,
                ) {
                    readOnly = false
                    showUnknown = specimenSpecifications.allowedAges.contains(BackendEnum.create(GameAge.UNKNOWN))
                    paddingTop = Padding.SMALL_MEDIUM
                    paddingBottom = Padding.SMALL
                }
            }
            SpecimenFieldType.STATE_OF_HEALTH ->
                stateOrHealthFactory.create(
                    fieldId = fieldSpecification.fieldType.toField(stableSpecimenIndex),
                    currentEnumValue = specimen.stateOfHealth ?: BackendEnum.create(null),
                    enumValues = specimenSpecifications.allowedStatesOfHealth,
                    allowEmptyValue = fieldSpecification.requirementStatus.isRequired().not(),
                ) {
                    readOnly = false
                    label = stringProvider.getString(RR.string.specimen_label_state_of_health)
                }
            SpecimenFieldType.MARKING ->
                markingFactory.create(
                    fieldId = fieldSpecification.fieldType.toField(stableSpecimenIndex),
                    currentEnumValue = specimen.marking ?: BackendEnum.create(null),
                    enumValues = specimenSpecifications.allowedMarkings,
                    allowEmptyValue = fieldSpecification.requirementStatus.isRequired().not(),
                ) {
                    readOnly = false
                    label = stringProvider.getString(RR.string.specimen_label_marking)
                }
            SpecimenFieldType.WIDTH_OF_PAW ->
                createPawDimensionListField(
                    fieldSpecification = fieldSpecification,
                    stableSpecimenIndex = stableSpecimenIndex,
                    currentDimensionCentimeters = specimen.widthOfPaw,
                    possibleDimensionValuesInMillimeters = specimenSpecifications.possibleWidthsOfPawInMillimeters,
                    label = RR.string.specimen_label_width_of_paw
                )
            SpecimenFieldType.LENGTH_OF_PAW ->
                createPawDimensionListField(
                    fieldSpecification = fieldSpecification,
                    stableSpecimenIndex = stableSpecimenIndex,
                    currentDimensionCentimeters = specimen.lengthOfPaw,
                    possibleDimensionValuesInMillimeters = specimenSpecifications.possibleLengthsOfPawInMillimeters,
                    label = RR.string.specimen_label_length_of_paw
                )
        }
    }

    private fun createPawDimensionListField(
        fieldSpecification: SpecimenFieldSpecification,
        stableSpecimenIndex: Int,
        currentDimensionCentimeters: Double?,
        possibleDimensionValuesInMillimeters: IntProgression?,
        label: RR.string,
        configureSettings: (StringListField.DefaultStringListFieldSettings.() -> Unit)? = null
    ): StringListField<SpecimenFieldId>? {
        if (possibleDimensionValuesInMillimeters == null) {
            return null
        }

        val noDimensionValue = if (fieldSpecification.requirementStatus.isRequired().not()) {
            StringWithId(string = "", id = -1)
        } else {
            null
        }

        val selectedMillimeters = currentDimensionCentimeters
            ?.let { centimeters ->
                // figure out the current value rounded to 5mm accuracy
                // - split conversion in two phases: first multiply by 2 and then multiply by 5
                // -> round between to get 5mm accuracy in final result
                round(centimeters * 2).toInt() * 5
            }
            ?.coerceIn(possibleDimensionValuesInMillimeters.first, possibleDimensionValuesInMillimeters.last)
            ?.takeIf {
                possibleDimensionValuesInMillimeters.contains(it)
            }

        val selectedValue = (selectedMillimeters?.toLong() ?: noDimensionValue?.id)?.let {
            listOf(it)
        }

        val possibleValues = listOfNotNull(noDimensionValue) + possibleDimensionValuesInMillimeters.map {
            StringWithId(string = (it / 10f).toString(), id = it.toLong())
        }

        return StringListField(
            id = fieldSpecification.fieldType.toField(stableSpecimenIndex),
            values = possibleValues,
            selected = selectedValue
        ) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            this.label = stringProvider.getString(label)

            if (configureSettings != null) {
                configureSettings(this)
            }
        }
    }
}
