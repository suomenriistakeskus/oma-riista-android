package fi.riista.common.domain.harvest.ui.fields

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.harvest.model.HarvestReportingType
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.ui.dataField.FieldRequirement
import fi.riista.common.ui.dataField.FieldSpecification

internal object HarvestSpecimenFieldRequirementResolver {

    fun isFieldRequired(
        specimenField: CommonHarvestField,
        speciesCode: SpeciesCode,
        harvestReportingType: HarvestReportingType,
    ): Boolean {
        val permitMandatorySpecies = specimenField.mandatorySpecies
        if (permitMandatorySpecies.isEmpty()) {
            return false
        }

        return getFieldRequirement(speciesCode, permitMandatorySpecies, harvestReportingType)
    }

    fun resolveRequirementType(
        specimenField: CommonHarvestField,
        speciesCode: SpeciesCode,
        harvestReportingType: HarvestReportingType,
        fallbackRequirement: FieldRequirement.Type = FieldRequirement.Type.VOLUNTARY,
    ): FieldRequirement.Type {
        return if (isFieldRequired(specimenField, speciesCode, harvestReportingType)) {
            FieldRequirement.Type.REQUIRED
        } else {
            fallbackRequirement
        }
    }

    fun isFieldRequired(
        specimenField: SpecimenFieldType,
        speciesCode: SpeciesCode,
        harvestReportingType: HarvestReportingType,
    ): Boolean {
        val permitMandatorySpecies = specimenField.mandatorySpecies
        if (permitMandatorySpecies.isEmpty()) {
            return false
        }

        return getFieldRequirement(speciesCode, permitMandatorySpecies, harvestReportingType)
    }

    fun resolveRequirementType(
        specimenField: SpecimenFieldType,
        speciesCode: SpeciesCode,
        harvestReportingType: HarvestReportingType,
        fallbackRequirement: FieldRequirement.Type = FieldRequirement.Type.VOLUNTARY,
    ): FieldRequirement.Type {
        return if (isFieldRequired(specimenField, speciesCode, harvestReportingType)) {
            FieldRequirement.Type.REQUIRED
        } else {
            fallbackRequirement
        }
    }

    fun resolveRequirementType(
        specimenField: CommonHarvestField,
        species: Species,
        harvestReportingType: HarvestReportingType,
        fallbackRequirement: FieldRequirement.Type = FieldRequirement.Type.VOLUNTARY,
    ): FieldRequirement.Type {
        return species.knownSpeciesCodeOrNull()?.let { speciesCode ->
            resolveRequirementType(specimenField, speciesCode, harvestReportingType)
        } ?: fallbackRequirement
    }

    private fun getFieldRequirement(
        speciesCode: SpeciesCode,
        permitMandatorySpecies: Set<SpeciesCode>,
        harvestReportingType: HarvestReportingType,
    ): Boolean {
        return when (harvestReportingType) {
            HarvestReportingType.BASIC -> false
            HarvestReportingType.SEASON -> SEASON_COMMON_MANDATORY.contains(speciesCode)
            HarvestReportingType.PERMIT -> permitMandatorySpecies.contains(speciesCode)
        }
    }

    // {mufloni,saksanhirvi,japaninpeura,halli,susi,ahma,karhu,hirvi,kuusipeura,valkohäntäpeura,metsäpeura,
    // villisika,saukko,ilves}
    internal val PERMIT_MANDATORY_AGE = setOf(
        47774, 47476, 47479, 47282, 46549, 47212, 47348, 47503, 47484, 47629, 200556, 47926, 47169, 46615
    )

    // {villisika,saukko,ilves,piisami,rämemajava,"tarhattu naali",pesukarhu,hilleri,kirjohylje,mufloni,
    // saksanhirvi,japaninpeura,halli,susi,"villiintynyt kissa",metsäjänis,rusakko,orava,kanadanmajava,kettu,
    // kärppä,näätä,minkki,villikani,supikoira,mäyrä,itämerennorppa,euroopanmajava,ahma,karhu,metsäkauris,
    // hirvi,kuusipeura,valkohäntäpeura,metsäpeura}
    internal val PERMIT_MANDATORY_GENDER = setOf(
        47926, 47169, 46615, 48537, 50336, 46542, 47329, 47240, 47305, 47774, 47476, 47479, 47282, 46549, 53004,
        50106, 50386, 48089, 48250, 46587, 47230, 47223, 47243, 50114, 46564, 47180, 200555, 48251, 47212, 47348,
        47507, 47503, 47484, 47629, 200556
    )

    // {halli,susi,saukko,ilves,ahma,karhu}
    internal val PERMIT_MANDATORY_WEIGHT = setOf(47282, 46549, 47169, 46615, 47212, 47348)

    // {karhu,metsäkauris,halli,villisika,norppa}
    internal val SEASON_COMMON_MANDATORY = setOf(47348, 47507, 47282, 47926, 200555)

    private val CommonHarvestField.mandatorySpecies: Set<SpeciesCode>
        get() {
            return when (this) {
                CommonHarvestField.AGE -> PERMIT_MANDATORY_AGE
                CommonHarvestField.GENDER -> PERMIT_MANDATORY_GENDER
                CommonHarvestField.WEIGHT -> PERMIT_MANDATORY_WEIGHT
                else -> emptySet()
            }
        }

    private val SpecimenFieldType.mandatorySpecies: Set<SpeciesCode>
        get() {
            return when (this) {
                SpecimenFieldType.AGE -> PERMIT_MANDATORY_AGE
                SpecimenFieldType.GENDER -> PERMIT_MANDATORY_GENDER
                SpecimenFieldType.WEIGHT -> PERMIT_MANDATORY_WEIGHT
                else -> emptySet()
            }
        }
}

/**
 * Resolves the field requirement.
 *
 * Returns the field as required or with given fallback requirement type.
 */
internal fun CommonHarvestField.resolveRequirement(
    speciesCode: SpeciesCode,
    harvestReportingType: HarvestReportingType,
    fallbackRequirement: FieldRequirement.Type = FieldRequirement.Type.VOLUNTARY,
    indicateRequirementStatus: Boolean = true,
): FieldSpecification<CommonHarvestField> {
    return FieldSpecification(
        fieldId = this,
        requirementStatus = FieldRequirement(
            type = HarvestSpecimenFieldRequirementResolver.resolveRequirementType(
                specimenField = this,
                speciesCode = speciesCode,
                harvestReportingType = harvestReportingType,
                fallbackRequirement = fallbackRequirement,
            ),
            indicateRequirement = indicateRequirementStatus
        )
    )
}