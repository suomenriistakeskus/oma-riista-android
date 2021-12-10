package fi.riista.common.groupHunting.ui.groupHarvest

import fi.riista.common.groupHunting.model.GroupHuntingHarvestData
import fi.riista.common.groupHunting.model.GroupHuntingPerson
import fi.riista.common.groupHunting.ui.GroupHarvestField
import fi.riista.common.model.DeerHuntingType
import fi.riista.common.model.GameAge
import fi.riista.common.model.Gender
import fi.riista.common.model.SpeciesCodes
import fi.riista.common.ui.dataField.*

object GroupHuntingHarvestFields {

    /**
     * The context based on which the specifications for [GroupHuntingHarvestData] fields are determined.
     *
     * It is assumed that group harvest does not contain multiple specimens i.e.
     * gender, age and antlers existence can be determined based on the first specimen.
     */
    data class Context(
        val harvest: GroupHuntingHarvestData,
        val mode: Mode
    ) {
        enum class Mode {
            VIEW,
            EDIT,
        }

        val speciesCode = harvest.gameSpeciesCode

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



    fun getFieldsToBeDisplayed(context: Context): List<FieldSpecification<GroupHarvestField>> {
        return when (context.speciesCode) {
            SpeciesCodes.MOOSE_ID -> mooseFields(context)
            SpeciesCodes.FALLOW_DEER_ID -> fallowDeerFields(context)
            SpeciesCodes.ROE_DEER_ID -> roeDeerFields(context)
            SpeciesCodes.WHITE_TAILED_DEER_ID -> whiteTailedDeerFields(context)
            SpeciesCodes.WILD_FOREST_DEER_ID -> wildForestDeerFields(context)
            else -> throw RuntimeException("Unexpected species code ${context.speciesCode}")
        }
    }

    private fun mooseFields(context: Context): List<FieldSpecification<GroupHarvestField>> {
        require(context.speciesCode == SpeciesCodes.MOOSE_ID)

        return FieldSpecificationListBuilder<GroupHarvestField>()
            .add(
                    GroupHarvestField.LOCATION.required(),
                    GroupHarvestField.SPECIES_CODE.required(),

                    GroupHarvestField.DATE_AND_TIME.required().takeIf {
                        context.mode == Context.Mode.VIEW
                    },
                    GroupHarvestField.HUNTING_DAY_AND_TIME.required().takeIf {
                        context.mode == Context.Mode.EDIT
                    },

                    GroupHarvestField.HEADLINE_SHOOTER.noRequirement().takeIf {
                        context.mode == Context.Mode.EDIT
                    },
                    GroupHarvestField.ACTOR.required(),
                    GroupHarvestField.ACTOR_HUNTER_NUMBER
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
                    GroupHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR.voluntary().takeIf {
                        context.mode == Context.Mode.EDIT &&
                                context.harvest.actorInfo is GroupHuntingPerson.SearchingByHunterNumber
                    },
                    GroupHarvestField.AUTHOR.required().takeIf { context.mode == Context.Mode.VIEW },

                    GroupHarvestField.HEADLINE_SPECIMEN.noRequirement().takeIf {
                        context.mode == Context.Mode.EDIT
                    },
                    GroupHarvestField.GENDER.required(),
                    GroupHarvestField.AGE.required(),
                    GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                    GroupHarvestField.ALONE.required(indicateRequirementStatus = false).takeIf { context.young },
                    GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                    GroupHarvestField.WEIGHT_MEASURED.voluntary(),
                    GroupHarvestField.FITNESS_CLASS.voluntary(),
            )
            .conditionally(context.adultMale) {
                add(GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false))

                conditionally(!context.antlersLost) {
                    add(
                            GroupHarvestField.ANTLER_INSTRUCTIONS.noRequirement().takeIf {
                                context.mode == Context.Mode.EDIT
                            },
                            GroupHarvestField.ANTLERS_TYPE.voluntary(),
                            GroupHarvestField.ANTLERS_WIDTH.voluntary(),
                            GroupHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                            GroupHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                            GroupHarvestField.ANTLERS_GIRTH.voluntary(),
                    )
                }
            }
            .add(GroupHarvestField.ADDITIONAL_INFORMATION.voluntary())
            .add(
                GroupHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement().takeIf {
                    context.mode == Context.Mode.EDIT
                }
            )
            .toList()
    }

    private fun fallowDeerFields(context: Context): List<FieldSpecification<GroupHarvestField>> {
        require(context.speciesCode == SpeciesCodes.FALLOW_DEER_ID)

        return FieldSpecificationListBuilder<GroupHarvestField>()
            .add(
                    *commonDeerFields(context, addDeerHuntingTypeFields = false),
                    GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                    GroupHarvestField.WEIGHT_MEASURED.voluntary(),
            )
            .conditionally(context.adultMale) {
                add(GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false))

                conditionally(!context.antlersLost) {
                    add(
                            GroupHarvestField.ANTLERS_WIDTH.voluntary(),
                            GroupHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                            GroupHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                    )
                }
            }
            .add(GroupHarvestField.ADDITIONAL_INFORMATION.voluntary())
            .add(
                GroupHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement().takeIf {
                    context.mode == Context.Mode.EDIT
                }
            )
            .toList()
    }

    private fun roeDeerFields(context: Context): List<FieldSpecification<GroupHarvestField>> {
        require(context.speciesCode == SpeciesCodes.ROE_DEER_ID)

        return FieldSpecificationListBuilder<GroupHarvestField>()
            .add(
                    *commonDeerFields(context, addDeerHuntingTypeFields = false),
                    GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                    GroupHarvestField.WEIGHT_MEASURED.voluntary(),
            )
            .conditionally(context.adultMale) {
                add(GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false))

                conditionally(!context.antlersLost) {
                    add(
                            GroupHarvestField.ANTLER_INSTRUCTIONS.noRequirement().takeIf {
                                context.mode == Context.Mode.EDIT
                            },
                            GroupHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                            GroupHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                            GroupHarvestField.ANTLERS_LENGTH.voluntary(),
                            GroupHarvestField.ANTLER_SHAFT_WIDTH.voluntary(),
                    )
                }
            }
            .toList()
    }

    private fun whiteTailedDeerFields(context: Context): List<FieldSpecification<GroupHarvestField>> {
        require(context.speciesCode == SpeciesCodes.WHITE_TAILED_DEER_ID)

        return FieldSpecificationListBuilder<GroupHarvestField>()
            .add(
                    *commonDeerFields(context, addDeerHuntingTypeFields = true),
                    GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                    GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                    GroupHarvestField.WEIGHT_MEASURED.voluntary(),
            )
            .conditionally(context.adultMale) {
                add(GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false))

                conditionally(!context.antlersLost) {
                    add(
                            GroupHarvestField.ANTLER_INSTRUCTIONS.noRequirement().takeIf {
                                context.mode == Context.Mode.EDIT
                            },
                            GroupHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                            GroupHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                            GroupHarvestField.ANTLERS_GIRTH.voluntary(),
                            GroupHarvestField.ANTLERS_LENGTH.voluntary(),
                            GroupHarvestField.ANTLERS_INNER_WIDTH.voluntary(),
                    )
                }
            }
            .add(GroupHarvestField.ADDITIONAL_INFORMATION.voluntary())
            .add(
                GroupHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement().takeIf {
                    context.mode == Context.Mode.EDIT
                }
            )
            .toList()
    }

    private fun wildForestDeerFields(context: Context): List<FieldSpecification<GroupHarvestField>> {
        require(context.speciesCode == SpeciesCodes.WILD_FOREST_DEER_ID)

        return FieldSpecificationListBuilder<GroupHarvestField>()
            .add(
                    *commonDeerFields(context, addDeerHuntingTypeFields = false),
                    GroupHarvestField.NOT_EDIBLE.required(indicateRequirementStatus = false),
                    GroupHarvestField.WEIGHT_ESTIMATED.voluntary(),
                    GroupHarvestField.WEIGHT_MEASURED.voluntary(),
            )
            .conditionally(context.adultMale) {
                add(GroupHarvestField.ANTLERS_LOST.required(indicateRequirementStatus = false))

                conditionally(!context.antlersLost) {
                    add(
                            GroupHarvestField.ANTLERS_WIDTH.voluntary(),
                            GroupHarvestField.ANTLER_POINTS_LEFT.voluntary(),
                            GroupHarvestField.ANTLER_POINTS_RIGHT.voluntary(),
                    )
                }
            }
            .add(GroupHarvestField.ADDITIONAL_INFORMATION.voluntary())
            .add(
                GroupHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS.noRequirement().takeIf {
                    context.mode == Context.Mode.EDIT
                }
            )
            .toList()
    }

    private fun commonDeerFields(
        context: Context,
        addDeerHuntingTypeFields: Boolean,
    ): Array<FieldSpecification<GroupHarvestField>> {

        return listOfNotNull(
                GroupHarvestField.LOCATION.required(),
                GroupHarvestField.SPECIES_CODE.required(),
                GroupHarvestField.DATE_AND_TIME.required(),

                GroupHarvestField.HEADLINE_SHOOTER.noRequirement()
                    .takeIf { context.mode == Context.Mode.EDIT },
                GroupHarvestField.ACTOR.required(),
                GroupHarvestField.AUTHOR.required().takeIf { context.mode == Context.Mode.VIEW },

                GroupHarvestField.DEER_HUNTING_TYPE.voluntary()
                    .takeIf { addDeerHuntingTypeFields },
                GroupHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION.voluntary()
                    .takeIf {
                        addDeerHuntingTypeFields &&
                                context.harvest.deerHuntingType.value == DeerHuntingType.OTHER
                            },

                GroupHarvestField.HEADLINE_SPECIMEN.noRequirement()
                    .takeIf { context.mode == Context.Mode.EDIT },
                GroupHarvestField.GENDER.required(),
                GroupHarvestField.AGE.required(),
        ).toTypedArray()
    }
}
