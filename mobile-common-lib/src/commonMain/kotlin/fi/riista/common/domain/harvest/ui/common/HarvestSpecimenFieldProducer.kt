package fi.riista.common.domain.harvest.ui.common

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.isPermitBasedMooselike
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.model.HarvestReportingType
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.fields.HarvestSpecimenFieldRequirementResolver
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.domain.specimens.ui.SpecimenFieldSpecification
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.dataField.FieldRequirement
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.SpecimenField

internal class HarvestSpecimenFieldProducer(
    stringProvider: StringProvider,
) {
    private val specimenFieldTypes = mapOf(
        SpecimenFieldType.GENDER to SpecimenFieldTypeRelations(
            harvestField = CommonHarvestField.GENDER,
            label = stringProvider.getString(RR.string.gender_label)
        ),
        SpecimenFieldType.AGE to SpecimenFieldTypeRelations(
            harvestField = CommonHarvestField.AGE,
            label = stringProvider.getString(RR.string.age_label)
        ),
        SpecimenFieldType.WEIGHT to SpecimenFieldTypeRelations(
            harvestField = CommonHarvestField.WEIGHT,
            label = stringProvider.getString(RR.string.harvest_label_weight)
        ),
    )

    fun createSpecimenField(
        fieldSpecification: FieldSpecification<CommonHarvestField>,
        harvest: CommonHarvestData,
        harvestReportingType: HarvestReportingType?,
        configureSettings: (SpecimenField.DefaultSpecimenFieldSettings.() -> Unit)? = null
    ): SpecimenField<CommonHarvestField> {
        val speciesCode = harvest.species.knownSpeciesCodeOrNull()
        val specimenFieldSpecifications = getSpecimenFieldTypes(speciesCode = speciesCode).map { specimenFieldType ->
            createSpecimenFieldSpecification(specimenFieldType, harvest, harvestReportingType)
        }

        val specimenAmount = harvest.amount ?: harvest.specimens.size

        return SpecimenField(
            id = fieldSpecification.fieldId,
            specimenData = SpecimenFieldDataContainer.createForHarvest(
                species = harvest.species,
                specimenAmount = specimenAmount,
                specimens = harvest.specimens,
                fieldSpecifications = specimenFieldSpecifications,
            ),
            configureSettings = configureSettings,
        )
    }

    private fun createSpecimenFieldSpecification(
        specimenFieldType: SpecimenFieldType,
        harvest: CommonHarvestData,
        harvestReportingType: HarvestReportingType?,
    ): SpecimenFieldSpecification {
        val specimenFieldData = specimenFieldTypes[specimenFieldType] ?: kotlin.run {
            throw RuntimeException("Unexpected specimen field type $specimenFieldType")
        }

        val requirementStatus = if (harvestReportingType != null) {
            FieldRequirement(
                type = HarvestSpecimenFieldRequirementResolver.resolveRequirementType(
                    specimenField = specimenFieldData.harvestField,
                    species = harvest.species,
                    harvestReportingType = harvestReportingType
                ),
                indicateRequirement = true,
            )
        } else {
            FieldRequirement.noRequirement()
        }

        return SpecimenFieldSpecification(
            fieldType = specimenFieldType,
            label = specimenFieldData.label,
            requirementStatus = requirementStatus
        )
    }

    companion object {
        fun getSpecimenFieldTypes(speciesCode: SpeciesCode?): List<SpecimenFieldType> {
            return if (speciesCode?.isPermitBasedMooselike() == true) {
                listOf(
                    SpecimenFieldType.GENDER,
                    SpecimenFieldType.AGE,
                )
            } else {
                listOf(
                    SpecimenFieldType.GENDER,
                    SpecimenFieldType.AGE,
                    SpecimenFieldType.WEIGHT,
                )
            }
        }
    }
}

private class SpecimenFieldTypeRelations(
    val harvestField: CommonHarvestField,
    val label: String,
)