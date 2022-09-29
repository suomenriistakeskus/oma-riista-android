package fi.riista.common.domain.groupHunting.ui.groupObservation.modify

import fi.riista.common.domain.constants.SpeciesConstants
import fi.riista.common.domain.constants.isDeer
import fi.riista.common.domain.groupHunting.model.GroupHuntingObservationData
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.groupHunting.model.HuntingGroupMember
import fi.riista.common.domain.groupHunting.model.isMember
import fi.riista.common.domain.groupHunting.ui.GroupObservationField
import fi.riista.common.domain.groupHunting.ui.groupHarvest.modify.toStringWithId
import fi.riista.common.domain.groupHunting.ui.groupHarvest.modify.toStringWithIdList
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.model.StringWithId
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.dataField.*

internal class ModifyGroupHuntingObservationFieldProducer(
    private val stringProvider: StringProvider,
) {
    fun createField(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
        groupMembers: List<HuntingGroupMember>,
    ) : DataField<GroupObservationField> {
        return when (fieldSpecification.fieldId) {
            GroupObservationField.SPECIES_CODE -> speciesCode(fieldSpecification, observation)
            GroupObservationField.HUNTING_DAY_AND_TIME -> huntingDayAndTime(fieldSpecification, observation)
            GroupObservationField.ERROR_TIME_NOT_WITHIN_HUNTING_DAY -> errorTimeNotWithinHuntingDay(fieldSpecification)
            GroupObservationField.DATE_AND_TIME -> dateAndTime(fieldSpecification, observation)
            GroupObservationField.LOCATION -> location(fieldSpecification, observation)
            GroupObservationField.OBSERVATION_TYPE -> observationType(fieldSpecification, observation)
            GroupObservationField.ACTOR -> actor(fieldSpecification, observation, groupMembers)
            GroupObservationField.ACTOR_HUNTER_NUMBER -> actorHunterNumber(fieldSpecification, observation)
            GroupObservationField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR -> actorHunterNumberInfoOrError(fieldSpecification, observation)
            GroupObservationField.HEADLINE_SPECIMEN_DETAILS -> specimenDetails(fieldSpecification)
            GroupObservationField.MOOSELIKE_MALE_AMOUNT -> mooseLikeMaleAmount(fieldSpecification, observation)
            GroupObservationField.MOOSELIKE_FEMALE_AMOUNT -> mooseLikeFemaleAmount(fieldSpecification, observation)
            GroupObservationField.MOOSELIKE_FEMALE_1CALF_AMOUNT -> mooseLikeFemale1CalfAmount(fieldSpecification, observation)
            GroupObservationField.MOOSELIKE_FEMALE_2CALF_AMOUNT -> mooseLikeFemale2CalfAmount(fieldSpecification, observation)
            GroupObservationField.MOOSELIKE_FEMALE_3CALF_AMOUNT -> mooseLikeFemale3CalfAmount(fieldSpecification, observation)
            GroupObservationField.MOOSELIKE_CALF_AMOUNT -> mooseLikeCalfAmount(fieldSpecification, observation)
            GroupObservationField.MOOSELIKE_UNKNOWN_SPECIMEN_AMOUNT -> mooseLikeUnknownSpecimenAmount(fieldSpecification, observation)
            // explicitly list unexpected fields, don't use else here!
            GroupObservationField.MOOSELIKE_FEMALE_4CALF_AMOUNT,
            GroupObservationField.AUTHOR -> {
                throw RuntimeException("Was not expecting $fieldSpecification to be displayed")
            }
        }
    }

    private fun speciesCode(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
    ): SpeciesField<GroupObservationField> {
        return SpeciesField(fieldSpecification.fieldId, observation.gameSpeciesCode) {
            readOnly = true
            requirementStatus = fieldSpecification.requirementStatus
        }
    }

    private fun huntingDayAndTime(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
    ): HuntingDayAndTimeField<GroupObservationField> {
        return HuntingDayAndTimeField(
            id = fieldSpecification.fieldId,
            huntingDayId = observation.huntingDayId,
            dateAndTime = observation.pointOfTime
        ) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            label = stringProvider.getString(RR.string.group_hunting_observation_field_hunting_day_and_time)
            paddingBottom = Padding.MEDIUM_LARGE
        }
    }

    private fun dateAndTime(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
    ): DateAndTimeField<GroupObservationField> {
        return DateAndTimeField(fieldSpecification.fieldId, observation.pointOfTime) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
        }
    }

    private fun errorTimeNotWithinHuntingDay(
        fieldSpecification: FieldSpecification<GroupObservationField>,
    ): LabelField<GroupObservationField> {
        return LabelField(
            id = fieldSpecification.fieldId,
            text = stringProvider.getString(RR.string.group_hunting_error_time_not_within_hunting_day),
            type = LabelField.Type.ERROR,
        ) {
            paddingTop = Padding.NONE
        }
    }

    private fun location(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
    ): LocationField<GroupObservationField> {
        return LocationField(fieldSpecification.fieldId, observation.geoLocation.asKnownLocation()) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
        }
    }

    private fun observationType(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
    ): StringField<GroupObservationField> {
        // TODO: For moose only possible selection is ObservationType.NAKO, when implementing e.g. bear, add other selections.
        val observationTypeValue = observation.observationType.value
        val observationType = if (observationTypeValue != null) {
            stringProvider.getString(observationTypeValue.resourcesStringId)
        } else {
            ""
        }
        return StringField(fieldSpecification.fieldId, observationType) {
            requirementStatus = fieldSpecification.requirementStatus
            readOnly = true
            singleLine = true
            paddingTop = Padding.MEDIUM
            paddingBottom = Padding.SMALL
            label = stringProvider.getString(RR.string.group_hunting_observation_field_observation_type)
        }
    }


    private fun actor(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
        groupMembers: List<HuntingGroupMember>
    ): StringListField<GroupObservationField> {
        val actorInfo = observation.actorInfo

        // Possible values:
        //   Other
        //   Other - FirstName LastName (HunterNumber) - if currently selected valid guest observer
        //   <all group member>

        val otherString = stringProvider.getString(RR.string.group_hunting_other_observer)
        val guestHunterChoice = StringWithId(
            id = GroupHuntingPerson.SearchingByHunterNumber.ID,
            string = otherString
        )

        val listOfHunters = listOfNotNull(
            guestHunterChoice,
            observation.actorInfo.personWithHunterNumber
                ?.takeIf { !groupMembers.isMember(it) }
                ?.toStringWithId(prefix = "$otherString - ", includeHunterNumber = false)
        ) + groupMembers.toStringWithIdList(includeHunterNumber = false)

        val detailedListOfHunters = listOfNotNull(
            guestHunterChoice,
            observation.actorInfo.personWithHunterNumber
                ?.takeIf { !groupMembers.isMember(it) }
                ?.toStringWithId(prefix = "$otherString - ", includeHunterNumber = true)
        ) + groupMembers.toStringWithIdList(includeHunterNumber = true)

        val selectedValueId = when (actorInfo) {
            is GroupHuntingPerson.GroupMember -> actorInfo.personInformation.id
            is GroupHuntingPerson.Guest -> actorInfo.personInformation.id
            is GroupHuntingPerson.SearchingByHunterNumber -> guestHunterChoice.id
            GroupHuntingPerson.Unknown -> null
        }

        val selected = selectedValueId?.let { listOf(it) }
        return StringListField(
            id = fieldSpecification.fieldId,
            values = listOfHunters,
            detailedValues = detailedListOfHunters,
            selected = selected,
        ) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            preferExternalViewForSelection = true
            externalViewConfiguration = StringListField.ExternalViewConfiguration(
                title = stringProvider.getString(RR.string.group_member_selection_select_observer),
                filterLabelText = stringProvider.getString(RR.string.group_member_selection_search_by_name),
                filterTextHint = stringProvider.getString(RR.string.group_member_selection_name_hint),
            )
            label = stringProvider.getString(RR.string.group_hunting_observation_field_actor)
            paddingTop = Padding.SMALL_MEDIUM
        }
    }

    private fun actorHunterNumber(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
    ): IntField<GroupObservationField> {
        val actorInfo = observation.actorInfo
        val hunterNumber = actorInfo.personWithHunterNumber?.hunterNumber
            ?: (actorInfo as? GroupHuntingPerson.SearchingByHunterNumber)?.hunterNumber

        val shouldBeReadOnly = when (actorInfo) {
            is GroupHuntingPerson.GroupMember,
            is GroupHuntingPerson.Guest,
            GroupHuntingPerson.Unknown -> true
            is GroupHuntingPerson.SearchingByHunterNumber -> {
                when (actorInfo.status) {
                    GroupHuntingPerson.SearchingByHunterNumber.Status.ENTERING_HUNTER_NUMBER,
                    GroupHuntingPerson.SearchingByHunterNumber.Status.INVALID_HUNTER_NUMBER,
                    GroupHuntingPerson.SearchingByHunterNumber.Status.SEARCH_FAILED -> false
                    GroupHuntingPerson.SearchingByHunterNumber.Status.VALID_HUNTER_NUMBER_ENTERED,
                    GroupHuntingPerson.SearchingByHunterNumber.Status.SEARCHING_PERSON_BY_HUNTER_NUMBER -> true
                }
            }
        }

        // max Int value is 2,147,483,647 which more than enough
        // for holding hunter number (8 numbers -> max value 99 999 999)
        return IntField(
            id = fieldSpecification.fieldId,
            value = hunterNumber?.toIntOrNull()
        ) {
            // not allowed to edit for known hunters
            readOnly = shouldBeReadOnly
            maxValue = 99999999 // hunternumber is 8 digits
            requirementStatus = fieldSpecification.requirementStatus

            label = stringProvider.getString(RR.string.group_hunting_hunter_id)
            paddingTop = Padding.NONE
            paddingBottom = Padding.NONE
        }
    }

    private fun actorHunterNumberInfoOrError(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
    ): LabelField<GroupObservationField> {
        val actorInfo = observation.actorInfo as? GroupHuntingPerson.SearchingByHunterNumber

        val type: LabelField.Type = when (actorInfo?.status) {
            GroupHuntingPerson.SearchingByHunterNumber.Status.ENTERING_HUNTER_NUMBER,
            GroupHuntingPerson.SearchingByHunterNumber.Status.VALID_HUNTER_NUMBER_ENTERED,
            GroupHuntingPerson.SearchingByHunterNumber.Status.SEARCHING_PERSON_BY_HUNTER_NUMBER,
            null -> LabelField.Type.INFO
            GroupHuntingPerson.SearchingByHunterNumber.Status.INVALID_HUNTER_NUMBER,
            GroupHuntingPerson.SearchingByHunterNumber.Status.SEARCH_FAILED -> LabelField.Type.ERROR
        }

        val text: String = when (actorInfo?.status) {
            GroupHuntingPerson.SearchingByHunterNumber.Status.ENTERING_HUNTER_NUMBER ->
                stringProvider.getString(RR.string.group_hunting_enter_hunter_id)
            GroupHuntingPerson.SearchingByHunterNumber.Status.INVALID_HUNTER_NUMBER ->
                stringProvider.getString(RR.string.group_hunting_invalid_hunter_id)
            GroupHuntingPerson.SearchingByHunterNumber.Status.VALID_HUNTER_NUMBER_ENTERED ->
                stringProvider.getString(RR.string.group_hunting_searching_observer_by_id)
            GroupHuntingPerson.SearchingByHunterNumber.Status.SEARCHING_PERSON_BY_HUNTER_NUMBER ->
                stringProvider.getString(RR.string.group_hunting_searching_observer_by_id)
            GroupHuntingPerson.SearchingByHunterNumber.Status.SEARCH_FAILED ->
                stringProvider.getString(RR.string.group_hunting_observer_search_failed)
            null -> ""
        }
        return LabelField(
            id = fieldSpecification.fieldId,
            text = text,
            type = type
        ) {
            paddingTop = Padding.NONE
            paddingBottom = Padding.LARGE
        }
    }

    private fun specimenDetails(
        fieldSpecification: FieldSpecification<GroupObservationField>,
    ): LabelField<GroupObservationField> {
        return LabelField(
            id = fieldSpecification.fieldId,
            text = stringProvider.getString(RR.string.group_hunting_observation_field_headline_specimen_details),
            type = LabelField.Type.CAPTION
        ) {
            paddingBottom = Padding.SMALL // content right below this item
        }
    }

    private fun mooseLikeMaleAmount(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
    ): IntField<GroupObservationField> {
        return amountField(
            fieldSpecification = fieldSpecification,
            amount = observation.mooselikeMaleAmount,
            labelId = if (observation.gameSpeciesCode.isDeer()) {
                RR.string.group_hunting_observation_field_mooselike_male_amount_within_deer_hunting
            } else {
                RR.string.group_hunting_observation_field_mooselike_male_amount
            },
        )
    }

    private fun mooseLikeFemaleAmount(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
    ): IntField<GroupObservationField> {
        return amountField(
            fieldSpecification = fieldSpecification,
            amount = observation.mooselikeFemaleAmount,
            labelId = if (observation.gameSpeciesCode.isDeer()) {
                RR.string.group_hunting_observation_field_mooselike_female_amount_within_deer_hunting
            } else {
                RR.string.group_hunting_observation_field_mooselike_female_amount
            },
        )
    }

    private fun mooseLikeFemale1CalfAmount(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
    ): IntField<GroupObservationField> {
        return amountField(
            fieldSpecification = fieldSpecification,
            amount = observation.mooselikeFemale1CalfAmount,
            labelId = if (observation.gameSpeciesCode.isDeer()) {
                RR.string.group_hunting_observation_field_mooselike_female_1calf_amount_within_deer_hunting
            } else {
                RR.string.group_hunting_observation_field_mooselike_female_1calf_amount
            },
        )
    }

    private fun mooseLikeFemale2CalfAmount(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
    ): IntField<GroupObservationField> {
        return amountField(
            fieldSpecification = fieldSpecification,
            amount = observation.mooselikeFemale2CalfsAmount,
            labelId = if (observation.gameSpeciesCode.isDeer()) {
                RR.string.group_hunting_observation_field_mooselike_female_2calf_amount_within_deer_hunting
            } else {
                RR.string.group_hunting_observation_field_mooselike_female_2calf_amount
            },
        )
    }

    private fun mooseLikeFemale3CalfAmount(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
    ): IntField<GroupObservationField> {
        return amountField(
            fieldSpecification = fieldSpecification,
            amount = observation.mooselikeFemale3CalfsAmount,
            labelId = if (observation.gameSpeciesCode.isDeer()) {
                RR.string.group_hunting_observation_field_mooselike_female_3calf_amount_within_deer_hunting
            } else {
                RR.string.group_hunting_observation_field_mooselike_female_3calf_amount
            },
        )
    }

    private fun mooseLikeCalfAmount(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
    ): IntField<GroupObservationField> {
        return amountField(
            fieldSpecification = fieldSpecification,
            amount = observation.mooselikeCalfAmount,
            labelId = if (observation.gameSpeciesCode.isDeer()) {
                RR.string.group_hunting_observation_field_mooselike_calf_amount_within_deer_hunting
            } else {
                RR.string.group_hunting_observation_field_mooselike_calf_amount
            },
        )
    }

    private fun mooseLikeUnknownSpecimenAmount(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        observation: GroupHuntingObservationData,
    ): IntField<GroupObservationField> {
        return amountField(
            fieldSpecification = fieldSpecification,
            amount = observation.mooselikeUnknownSpecimenAmount,
            labelId = if (observation.gameSpeciesCode.isDeer()) {
                RR.string.group_hunting_observation_field_mooselike_unknown_specimen_amount_within_deer_hunting
            } else {
                RR.string.group_hunting_observation_field_mooselike_unknown_specimen_amount
            },
        )
    }

    private fun amountField(
        fieldSpecification: FieldSpecification<GroupObservationField>,
        amount: Int?,
        labelId: RR.string,
    ): IntField<GroupObservationField> {
        return IntField(fieldSpecification.fieldId, amount) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            maxValue = SpeciesConstants.MAX_OBSERVATION_MOOSELIKE_AMOUNT
            label = stringProvider.getString(labelId)
        }
    }
}

