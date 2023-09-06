package fi.riista.common.domain.harvest.ui.fields

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.harvest.model.HarvestReportingType
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.fields.CommonHarvestFields.Context
import fi.riista.common.domain.model.GreySealHuntingMethod
import fi.riista.common.domain.model.isBeanGoose
import fi.riista.common.domain.model.isDeer
import fi.riista.common.domain.model.isGreySeal
import fi.riista.common.domain.model.isMoose
import fi.riista.common.domain.model.isWildBoar
import fi.riista.common.logging.getLogger
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.FieldSpecificationListBuilder
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.requiredIf
import fi.riista.common.ui.dataField.voluntary

internal class SpeciesSpecificHarvestFieldsPre2020(
    private val speciesResolver: SpeciesResolver,
): SpeciesSpecificHarvestFields {
    override fun getSpeciesSpecificFields(context: Context): List<FieldSpecification<CommonHarvestField>> {
        val species = context.harvest.species
        return when {
            species.isMoose() -> mooseSpecificFields(context)
            species.isDeer() -> deerSpecificFields(context)
            species.isWildBoar() -> wildBoarSpecificFields(context)
            species.isGreySeal() -> greySealSpecificFields(context)
            species.isBeanGoose() -> beanGooseSpecificFields(context)
            else -> defaultSpeciesSpecificFields(context)
        }.also {
            logger.v { "Determined fields for species ${context.speciesCode}" }
        }
    }

    private fun mooseSpecificFields(context: Context): List<FieldSpecification<CommonHarvestField>> {
        require(context.speciesCode == SpeciesCodes.MOOSE_ID)

        return FieldSpecificationListBuilder<CommonHarvestField>()
            .add(
                CommonHarvestField.GENDER.required(),
                CommonHarvestField.AGE.required(),
                CommonHarvestField.NOT_EDIBLE.voluntary(),
                CommonHarvestField.ALONE.voluntary().takeIf { context.young },
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                CommonHarvestField.FITNESS_CLASS.voluntary(),
            )
            .conditionally(context.adultMale) {
                add(
                    CommonHarvestField.ANTLERS_TYPE.voluntary(),
                    CommonHarvestField.ANTLERS_WIDTH.voluntary(),
                    CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                    CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                )
            }
            .add(CommonHarvestField.ADDITIONAL_INFORMATION.voluntary())
            .add(
                CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement().takeIf {
                    context.mode == Context.Mode.EDIT
                }
            )
            .toList()
    }

    private fun deerSpecificFields(context: Context): List<FieldSpecification<CommonHarvestField>> {
        require(context.harvest.species.isDeer())

        return FieldSpecificationListBuilder<CommonHarvestField>()
            .add(
                CommonHarvestField.GENDER.required(),
                CommonHarvestField.AGE.required(),
                CommonHarvestField.NOT_EDIBLE.voluntary(),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
            )
            .conditionally(context.adultMale) {
                add(
                    CommonHarvestField.ANTLERS_WIDTH.voluntary(),
                    CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                    CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                )
            }
            .add(CommonHarvestField.ADDITIONAL_INFORMATION.voluntary())
            .add(
                CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement().takeIf {
                    context.mode == Context.Mode.EDIT
                }
            )
            .toList()
    }

    private fun wildBoarSpecificFields(context: Context): List<FieldSpecification<CommonHarvestField>> {
        require(context.speciesCode == SpeciesCodes.WILD_BOAR_ID)

        return listOfNotNull(
            CommonHarvestField.WILD_BOAR_FEEDING_PLACE.voluntary(),
            *defaultSpeciesSpecificFields(context).toTypedArray()
        )
    }

    private fun greySealSpecificFields(context: Context): List<FieldSpecification<CommonHarvestField>> {
        require(context.harvest.species.isGreySeal())

        val includeWeight: Boolean = when (context.harvest.greySealHuntingMethod.value) {
            GreySealHuntingMethod.SHOT_BUT_LOST -> false
            else -> true
        }

        return listOfNotNull(
            CommonHarvestField.GREY_SEAL_HUNTING_METHOD
                .requiredIf(context.harvestReportingType != HarvestReportingType.BASIC),
            *defaultSpeciesSpecificFields(
                context = context,
                includeWeight = includeWeight,
            ).toTypedArray()
        )
    }

    private fun beanGooseSpecificFields(context: Context): List<FieldSpecification<CommonHarvestField>> {
        require(context.harvest.species.isBeanGoose())

        return listOfNotNull(
            CommonHarvestField.IS_TAIGA_BEAN_GOOSE.voluntary().takeIf {
                context.harvestReportingType != HarvestReportingType.BASIC
            },
            *defaultSpeciesSpecificFields(context).toTypedArray()
        )
    }

    internal fun defaultSpeciesSpecificFields(
        context: Context,
        includeWeight: Boolean = true
    ): List<FieldSpecification<CommonHarvestField>> {
        if (context.speciesCode == null) {
            // No need to display specimen amount, specimens, gender or age if species is not known.
            // The presence of those fields depends on selected species after all
            return emptyList()
        }

        val multipleSpecimenAllowed = speciesResolver.getMultipleSpecimensAllowedOnHarvests(
            speciesCode = context.speciesCode,
        )

        return if (multipleSpecimenAllowed) {
            listOf(
                CommonHarvestField.SPECIMEN_AMOUNT.required(),
                CommonHarvestField.SPECIMENS.required(),
            )
        } else {
            listOfNotNull(
                CommonHarvestField.GENDER.resolveRequirement(context),
                CommonHarvestField.AGE.resolveRequirement(context),
                CommonHarvestField.WEIGHT.resolveRequirement(context).takeIf { includeWeight },
            )
        }
    }

    companion object {
        private val logger by getLogger(SpeciesSpecificHarvestFieldsPre2020::class)
    }
}
