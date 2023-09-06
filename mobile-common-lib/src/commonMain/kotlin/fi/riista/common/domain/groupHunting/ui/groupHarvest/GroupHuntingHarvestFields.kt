package fi.riista.common.domain.groupHunting.ui.groupHarvest

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.ui.dataField.*

object GroupHuntingHarvestFields {

    /**
     * The context based on which the specifications for [CommonHarvestData] fields are determined.
     *
     * It is assumed that group harvest does not contain multiple specimens i.e.
     * gender, age and antlers existence can be determined based on the first specimen.
     */
    data class Context internal constructor(
        internal val harvest: CommonHarvestData,
        val mode: Mode
    ) {
        enum class Mode {
            VIEW,
            EDIT,
        }

        val speciesCode = harvest.species.knownSpeciesCodeOrNull()

        val adultMale: Boolean by lazy {
            harvest.specimens.getOrNull(0)
                ?.let {
                    it.age?.value == GameAge.ADULT && it.gender?.value == Gender.MALE
                } ?:false
        }

        val young: Boolean by lazy {
            harvest.specimens.getOrNull(0)
                ?.let {
                    it.age?.value == GameAge.YOUNG
                }
                    ?:false
        }

        val antlersLost: Boolean by lazy {
            harvest.specimens.getOrNull(0)?.antlersLost ?: false
        }
    }



    fun getFieldsToBeDisplayed(context: Context): List<FieldSpecification<CommonHarvestField>> {
        return when (context.speciesCode) {
            SpeciesCodes.MOOSE_ID -> mooseFields(context)
            SpeciesCodes.FALLOW_DEER_ID -> fallowDeerFields(context)
            SpeciesCodes.ROE_DEER_ID -> roeDeerFields(context)
            SpeciesCodes.WHITE_TAILED_DEER_ID -> whiteTailedDeerFields(context)
            SpeciesCodes.WILD_FOREST_DEER_ID -> wildForestDeerFields(context)
            else -> throw RuntimeException("Unexpected species code ${context.speciesCode}")
        }
    }

    private fun mooseFields(context: Context): List<FieldSpecification<CommonHarvestField>> {
        require(context.speciesCode == SpeciesCodes.MOOSE_ID)

        return FieldSpecificationListBuilder<CommonHarvestField>()
            .add(
                    CommonHarvestField.LOCATION.required(),
                    CommonHarvestField.SPECIES_CODE.required(),

                    CommonHarvestField.DATE_AND_TIME.required().takeIf {
                        context.mode == Context.Mode.VIEW
                    },
                    CommonHarvestField.HUNTING_DAY_AND_TIME.required().takeIf {
                        context.mode == Context.Mode.EDIT
                    },

                    CommonHarvestField.HEADLINE_SHOOTER.noRequirement().takeIf {
                        context.mode == Context.Mode.EDIT
                    },
                    CommonHarvestField.ACTOR.required(),
                    CommonHarvestField.ACTOR_HUNTER_NUMBER
                        .withRequirement {
                            if (context.harvest.actorInfo is GroupHuntingPerson.SearchingByHunterNumber) {
                                FieldRequirement.required()
                            } else {
                                FieldRequirement.voluntary()
                            }
                        }
                        .takeIf {
                            context.mode == Context.Mode.EDIT &&
                                    context.harvest.actorInfo !is GroupHuntingPerson.Unknown
                    },
                    CommonHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR.voluntary().takeIf {
                        context.mode == Context.Mode.EDIT &&
                                context.harvest.actorInfo is GroupHuntingPerson.SearchingByHunterNumber
                    },
                    CommonHarvestField.AUTHOR.required().takeIf { context.mode == Context.Mode.VIEW },

                    CommonHarvestField.HEADLINE_SPECIMEN.noRequirement().takeIf {
                        context.mode == Context.Mode.EDIT
                    },
                    CommonHarvestField.GENDER.required(),
                    CommonHarvestField.AGE.required(),
                    CommonHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                    CommonHarvestField.ALONE.required(indicateRequirementStatus = false).takeIf { context.young },
                    CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                    CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                    CommonHarvestField.FITNESS_CLASS.voluntary(),
            )
            .conditionally(context.adultMale) {
                add(CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false))

                conditionally(!context.antlersLost) {
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
                add(CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false))

                conditionally(!context.antlersLost) {
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
                add(CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false))

                conditionally(!context.antlersLost) {
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
                    CommonHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                    CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                    CommonHarvestField.WEIGHT_MEASURED.voluntary(),
            )
            .conditionally(context.adultMale) {
                add(CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false))

                conditionally(!context.antlersLost) {
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
                    CommonHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                    CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                    CommonHarvestField.WEIGHT_MEASURED.voluntary(),
            )
            .conditionally(context.adultMale) {
                add(CommonHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false))

                conditionally(!context.antlersLost) {
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

    private fun commonDeerFields(
        context: Context,
        addDeerHuntingTypeFields: Boolean,
    ): Array<FieldSpecification<CommonHarvestField>> {

        return listOfNotNull(
                CommonHarvestField.LOCATION.required(),
                CommonHarvestField.SPECIES_CODE.required(),
                CommonHarvestField.DATE_AND_TIME.required(),

                CommonHarvestField.HEADLINE_SHOOTER.noRequirement()
                    .takeIf { context.mode == Context.Mode.EDIT },
                CommonHarvestField.ACTOR.required(),
                CommonHarvestField.ACTOR_HUNTER_NUMBER
                    .withRequirement {
                        if (context.harvest.actorInfo is GroupHuntingPerson.SearchingByHunterNumber) {
                            FieldRequirement.required()
                        } else {
                            FieldRequirement.voluntary()
                        }
                    }
                    .takeIf {
                        context.mode == Context.Mode.EDIT &&
                                context.harvest.actorInfo !is GroupHuntingPerson.Unknown
                    },
                CommonHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR.voluntary().takeIf {
                    context.mode == Context.Mode.EDIT &&
                            context.harvest.actorInfo is GroupHuntingPerson.SearchingByHunterNumber
                },
                CommonHarvestField.AUTHOR.required().takeIf { context.mode == Context.Mode.VIEW },

                CommonHarvestField.DEER_HUNTING_TYPE.voluntary()
                    .takeIf { addDeerHuntingTypeFields },
                CommonHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION.voluntary()
                    .takeIf {
                        addDeerHuntingTypeFields &&
                                context.harvest.deerHuntingType.value == DeerHuntingType.OTHER
                            },

                CommonHarvestField.HEADLINE_SPECIMEN.noRequirement()
                    .takeIf { context.mode == Context.Mode.EDIT },
                CommonHarvestField.GENDER.required(),
                CommonHarvestField.AGE.required(),
        ).toTypedArray()
    }
}
