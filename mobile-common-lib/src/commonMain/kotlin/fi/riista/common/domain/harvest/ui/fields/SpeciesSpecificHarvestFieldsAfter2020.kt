package fi.riista.common.domain.harvest.ui.fields

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.fields.CommonHarvestFields.Context
import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.logging.getLogger
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.FieldSpecificationListBuilder
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.voluntary

internal class SpeciesSpecificHarvestFieldsAfter2020: SpeciesSpecificHarvestFields {
    override fun getSpeciesSpecificFields(context: Context): List<FieldSpecification<CommonHarvestField>>? {
        return when (context.speciesCode) {
            SpeciesCodes.MOOSE_ID -> mooseFields(context)
            SpeciesCodes.FALLOW_DEER_ID -> fallowDeerFields(context)
            SpeciesCodes.ROE_DEER_ID -> roeDeerFields(context)
            SpeciesCodes.WHITE_TAILED_DEER_ID -> whiteTailedDeerFields(context)
            SpeciesCodes.WILD_FOREST_DEER_ID -> wildForestDeerFields(context)
            SpeciesCodes.WILD_BOAR_ID -> wildBoardSpecificFields(context)
            else -> null
        }.also { speciesSpecificFields ->
            if (speciesSpecificFields != null) {
                logger.v { "Determined fields for species ${context.speciesCode}" }
            }
        }
    }

    private fun mooseFields(context: Context): List<FieldSpecification<CommonHarvestField>> {
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
                add(CommonHarvestField.ANTLERS_LOST.required(
                    indicateRequirementStatus = (context.antlersLost == null)
                ))

                conditionally(context.antlersLost == false) {
                    add(
                        CommonHarvestField.ANTLER_INSTRUCTIONS.noRequirement().takeIf {
                            context.mode == Context.Mode.EDIT
                        },
                        CommonHarvestField.ANTLERS_TYPE.voluntary(),
                        CommonHarvestField.ANTLERS_WIDTH.voluntary(),
                        CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                        CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                        CommonHarvestField.ANTLERS_GIRTH.voluntary(),
                    )
                }
            }
            .add(CommonHarvestField.ADDITIONAL_INFORMATION.voluntary())
            .add(
                CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement().takeIf {
                    context.mode == Context.Mode.EDIT
                }
            )
            .toList()
    }

    private fun fallowDeerFields(context: Context): List<FieldSpecification<CommonHarvestField>> {
        require(context.speciesCode == SpeciesCodes.FALLOW_DEER_ID)

        return FieldSpecificationListBuilder<CommonHarvestField>()
            .add(
                *commonDeerFields(context, addDeerHuntingTypeFields = false),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
            )
            .conditionally(context.adultMale) {
                add(CommonHarvestField.ANTLERS_LOST.required(
                    indicateRequirementStatus = (context.antlersLost == null)
                ))

                conditionally(context.antlersLost == false) {
                    add(
                        CommonHarvestField.ANTLERS_WIDTH.voluntary(),
                        CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                        CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                    )
                }
            }
            .add(CommonHarvestField.ADDITIONAL_INFORMATION.voluntary())
            .add(
                CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement().takeIf {
                    context.mode == Context.Mode.EDIT
                }
            )
            .toList()
    }

    private fun roeDeerFields(context: Context): List<FieldSpecification<CommonHarvestField>> {
        require(context.speciesCode == SpeciesCodes.ROE_DEER_ID)

        return FieldSpecificationListBuilder<CommonHarvestField>()
            .add(
                *commonDeerFields(context, addDeerHuntingTypeFields = false),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
            )
            .conditionally(context.adultMale) {
                add(CommonHarvestField.ANTLERS_LOST.required(
                    indicateRequirementStatus = (context.antlersLost == null)
                ))

                conditionally(context.antlersLost == false) {
                    add(
                        CommonHarvestField.ANTLER_INSTRUCTIONS.noRequirement().takeIf {
                            context.mode == Context.Mode.EDIT
                        },
                        CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                        CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                        CommonHarvestField.ANTLERS_LENGTH.voluntary(),
                        CommonHarvestField.ANTLER_SHAFT_WIDTH.voluntary(),
                    )
                }
            }
            .toList()
    }

    private fun whiteTailedDeerFields(context: Context): List<FieldSpecification<CommonHarvestField>> {
        require(context.speciesCode == SpeciesCodes.WHITE_TAILED_DEER_ID)

        return FieldSpecificationListBuilder<CommonHarvestField>()
            .add(
                *commonDeerFields(context, addDeerHuntingTypeFields = true),
                CommonHarvestField.NOT_EDIBLE.voluntary(),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
            )
            .conditionally(context.adultMale) {
                add(CommonHarvestField.ANTLERS_LOST.required(
                    indicateRequirementStatus = (context.antlersLost == null)
                ))

                conditionally(context.antlersLost == false) {
                    add(
                        CommonHarvestField.ANTLER_INSTRUCTIONS.noRequirement().takeIf {
                            context.mode == Context.Mode.EDIT
                        },
                        CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                        CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                        CommonHarvestField.ANTLERS_GIRTH.voluntary(),
                        CommonHarvestField.ANTLERS_LENGTH.voluntary(),
                        CommonHarvestField.ANTLERS_INNER_WIDTH.voluntary(),
                    )
                }
            }
            .add(CommonHarvestField.ADDITIONAL_INFORMATION.voluntary())
            .add(
                CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement().takeIf {
                    context.mode == Context.Mode.EDIT
                }
            )
            .toList()
    }

    private fun wildForestDeerFields(context: Context): List<FieldSpecification<CommonHarvestField>> {
        require(context.speciesCode == SpeciesCodes.WILD_FOREST_DEER_ID)

        return FieldSpecificationListBuilder<CommonHarvestField>()
            .add(
                *commonDeerFields(context, addDeerHuntingTypeFields = false),
                CommonHarvestField.NOT_EDIBLE.voluntary(),
                CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                CommonHarvestField.WEIGHT_MEASURED.voluntary(),
            )
            .conditionally(context.adultMale) {
                add(CommonHarvestField.ANTLERS_LOST.required(
                    indicateRequirementStatus = (context.antlersLost == null)
                ))

                conditionally(context.antlersLost == false) {
                    add(
                        CommonHarvestField.ANTLERS_WIDTH.voluntary(),
                        CommonHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                        CommonHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                    )
                }
            }
            .add(CommonHarvestField.ADDITIONAL_INFORMATION.voluntary())
            .add(
                CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement().takeIf {
                    context.mode == Context.Mode.EDIT
                }
            )
            .toList()
    }

    private fun wildBoardSpecificFields(context: Context): List<FieldSpecification<CommonHarvestField>> {
        require(context.speciesCode == SpeciesCodes.WILD_BOAR_ID)

        return listOfNotNull(
            CommonHarvestField.WILD_BOAR_FEEDING_PLACE.voluntary(),
            CommonHarvestField.GENDER.resolveRequirement(context),
            CommonHarvestField.AGE.resolveRequirement(context),
            CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
            CommonHarvestField.WEIGHT_MEASURED.voluntary(),
        )
    }

    private fun commonDeerFields(
        context: Context,
        addDeerHuntingTypeFields: Boolean,
    ): Array<FieldSpecification<CommonHarvestField>> {

        return listOfNotNull(
            CommonHarvestField.DEER_HUNTING_TYPE.voluntary()
                .takeIf { addDeerHuntingTypeFields },
            CommonHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION.voluntary()
                .takeIf {
                    addDeerHuntingTypeFields &&
                            context.harvest.deerHuntingType.value == DeerHuntingType.OTHER
                },

            CommonHarvestField.GENDER.required(),
            CommonHarvestField.AGE.required(),
        ).toTypedArray()
    }

    companion object {
        private val logger by getLogger(SpeciesSpecificHarvestFieldsAfter2020::class)
    }
}
