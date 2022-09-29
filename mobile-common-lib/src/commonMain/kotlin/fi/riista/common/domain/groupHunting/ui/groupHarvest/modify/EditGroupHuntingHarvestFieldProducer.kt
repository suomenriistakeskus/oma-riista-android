package fi.riista.common.domain.groupHunting.ui.groupHarvest.modify

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.constants.SpeciesConstants
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvestData
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.groupHunting.model.HuntingGroupMember
import fi.riista.common.domain.groupHunting.model.isMember
import fi.riista.common.domain.groupHunting.ui.GroupHarvestField
import fi.riista.common.domain.model.*
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.StringWithId
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.dataField.*

internal class EditGroupHuntingHarvestFieldProducer(
    private val stringProvider: StringProvider,
) {

    private val deerHuntingTypeFieldFactory = EnumStringListFieldFactory.create<DeerHuntingType>(stringProvider)
    private val antlersTypeFieldFactory = EnumStringListFieldFactory.create<GameAntlersType>(stringProvider)
    private val fitnessClassFieldFactory = EnumStringListFieldFactory.create<GameFitnessClass>(stringProvider)

    fun createField(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData,
        groupMembers: List<HuntingGroupMember>,
    ) : DataField<GroupHarvestField> {
        return when (fieldSpecification.fieldId) {
            GroupHarvestField.SPECIES_CODE -> speciesCode(fieldSpecification, harvest)
            GroupHarvestField.DATE_AND_TIME -> dateAndTime(fieldSpecification, harvest)
            GroupHarvestField.ERROR_DATE_NOT_WITHIN_GROUP_PERMIT -> errorDateNotWithinPermit(fieldSpecification)
            GroupHarvestField.HUNTING_DAY_AND_TIME -> huntingDayAndTime(fieldSpecification, harvest)
            GroupHarvestField.ERROR_TIME_NOT_WITHIN_HUNTING_DAY -> errorTimeNotWithinHuntingDay(fieldSpecification)
            GroupHarvestField.LOCATION -> location(fieldSpecification, harvest)
            GroupHarvestField.HEADLINE_SHOOTER -> shooter(fieldSpecification)
            GroupHarvestField.ACTOR -> actor(fieldSpecification, harvest, groupMembers)
            GroupHarvestField.ACTOR_HUNTER_NUMBER -> actorHunterNumber(fieldSpecification, harvest)
            GroupHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR ->
                actorHunterNumberInfoOrError(fieldSpecification, harvest)
            GroupHarvestField.DEER_HUNTING_TYPE -> deerHuntingType(fieldSpecification, harvest)
            GroupHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION -> deerHuntingOtherTypeDescription(fieldSpecification, harvest)
            GroupHarvestField.HEADLINE_SPECIMEN -> specimenDetails(fieldSpecification)
            GroupHarvestField.GENDER -> gender(fieldSpecification, harvest)
            GroupHarvestField.AGE -> age(fieldSpecification, harvest)
            GroupHarvestField.NOT_EDIBLE -> notEdible(fieldSpecification, harvest)
            GroupHarvestField.WEIGHT_ESTIMATED -> weightEstimated(fieldSpecification, harvest)
            GroupHarvestField.WEIGHT_MEASURED -> weightMeasured(fieldSpecification, harvest)
            GroupHarvestField.FITNESS_CLASS -> fitnessClass(fieldSpecification, harvest)
            GroupHarvestField.ANTLERS_TYPE -> antlersType(fieldSpecification, harvest)
            GroupHarvestField.ANTLERS_WIDTH -> antlersWidth(fieldSpecification, harvest)
            GroupHarvestField.ANTLER_POINTS_LEFT -> antlerPointsLeft(fieldSpecification, harvest)
            GroupHarvestField.ANTLER_POINTS_RIGHT -> antlerPointsRight(fieldSpecification, harvest)
            GroupHarvestField.ANTLERS_LOST -> antlersLost(fieldSpecification, harvest)
            GroupHarvestField.ANTLERS_GIRTH -> antlersGirth(fieldSpecification, harvest)
            GroupHarvestField.ANTLER_SHAFT_WIDTH -> antlerShaftWidth(fieldSpecification, harvest)
            GroupHarvestField.ANTLERS_LENGTH -> antlersLength(fieldSpecification, harvest)
            GroupHarvestField.ANTLERS_INNER_WIDTH -> antlersInnerWidth(fieldSpecification, harvest)
            GroupHarvestField.ALONE -> alone(fieldSpecification, harvest)
            GroupHarvestField.ADDITIONAL_INFORMATION -> additionalInformation(fieldSpecification, harvest)
            GroupHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS -> additionalInformationInstructions(fieldSpecification, harvest)
            GroupHarvestField.ANTLER_INSTRUCTIONS -> antlerInstructions(fieldSpecification, harvest)
            // explicitly list unexpected fields, don't use else here!
            GroupHarvestField.WEIGHT,
            GroupHarvestField.AUTHOR -> {
                throw RuntimeException("Was not expecting $fieldSpecification to be displayed")
            }
        }
    }

    private fun speciesCode(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): SpeciesField<GroupHarvestField> {
        return SpeciesField(fieldSpecification.fieldId, harvest.gameSpeciesCode) {
            requirementStatus = fieldSpecification.requirementStatus
        }
    }

    private fun dateAndTime(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): DateAndTimeField<GroupHarvestField> {
        return DateAndTimeField(fieldSpecification.fieldId, harvest.pointOfTime) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
        }
    }

    private fun errorDateNotWithinPermit(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
    ): LabelField<GroupHarvestField> {
        return LabelField(
                id = fieldSpecification.fieldId,
                text = stringProvider.getString(RR.string.group_hunting_day_error_dates_not_within_permit),
                type = LabelField.Type.ERROR,
        ) {
            paddingTop = Padding.NONE
        }
    }

    private fun huntingDayAndTime(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): HuntingDayAndTimeField<GroupHarvestField> {
        return HuntingDayAndTimeField(
                id = fieldSpecification.fieldId,
                huntingDayId = harvest.huntingDayId,
                dateAndTime = harvest.pointOfTime
        ) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            label = stringProvider.getString(RR.string.group_hunting_harvest_field_hunting_day_and_time)
            paddingBottom = Padding.MEDIUM_LARGE
        }
    }

    private fun errorTimeNotWithinHuntingDay(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
    ): LabelField<GroupHarvestField> {
        return LabelField(
            id = fieldSpecification.fieldId,
            text = stringProvider.getString(RR.string.group_hunting_error_time_not_within_hunting_day),
            type = LabelField.Type.ERROR,
        ) {
            paddingTop = Padding.NONE
        }
    }

    private fun deerHuntingType(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): StringListField<GroupHarvestField> {
        return deerHuntingTypeFieldFactory.create(
                fieldId = fieldSpecification.fieldId,
                currentEnumValue = harvest.deerHuntingType,
                allowEmptyValue = fieldSpecification.requirementStatus.isRequired().not()
        ) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            label = stringProvider.getString(RR.string.group_hunting_harvest_field_deer_hunting_type)
        }
    }

    private fun shooter(fieldSpecification: FieldSpecification<GroupHarvestField>): LabelField<GroupHarvestField> {
        return LabelField(
                id = fieldSpecification.fieldId,
                text = stringProvider.getString(RR.string.group_hunting_proposed_group_harvest_shooter),
                type = LabelField.Type.CAPTION
        ) {
            paddingBottom = Padding.SMALL // content right below this item
        }
    }

    private fun specimenDetails(fieldSpecification: FieldSpecification<GroupHarvestField>): LabelField<GroupHarvestField> {
        return LabelField(
                id = fieldSpecification.fieldId,
                text = stringProvider.getString(RR.string.group_hunting_proposed_group_harvest_specimen),
                type = LabelField.Type.CAPTION
        ) {
            paddingBottom = Padding.SMALL // content right below this item
        }
    }

    private fun actor(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData,
        groupMembers: List<HuntingGroupMember>
    ): StringListField<GroupHarvestField> {
        val actorInfo = harvest.actorInfo

        // Possible values:
        //   Other
        //   Other - FirstName LastName (HunterNumber) - if currently selected valid guest hunter
        //   <all group member>

        val otherString = stringProvider.getString(RR.string.group_hunting_other_hunter)
        val guestHunterChoice = StringWithId(
            id = GroupHuntingPerson.SearchingByHunterNumber.ID,
            string = otherString
        )

        val listOfHunters = listOfNotNull(
            guestHunterChoice,
            harvest.actorInfo.personWithHunterNumber
                ?.takeIf { !groupMembers.isMember(it) }
                ?.toStringWithId(prefix = "$otherString - ", includeHunterNumber = false)
        ) + groupMembers.toStringWithIdList(includeHunterNumber = false)

        val detailedListOfHunters = listOfNotNull(
            guestHunterChoice,
            harvest.actorInfo.personWithHunterNumber
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
            mode = StringListField.Mode.SINGLE
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            preferExternalViewForSelection = true
            externalViewConfiguration = StringListField.ExternalViewConfiguration(
                title = stringProvider.getString(RR.string.group_member_selection_select_hunter),
                filterLabelText = stringProvider.getString(RR.string.group_member_selection_search_by_name),
                filterTextHint = stringProvider.getString(RR.string.group_member_selection_name_hint),
            )
            label = stringProvider.getString(RR.string.group_hunting_proposed_group_harvest_actor)
            paddingTop = Padding.SMALL_MEDIUM
        }
    }

    private fun actorHunterNumber(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData,
    ): IntField<GroupHarvestField> {
        val actorInfo = harvest.actorInfo
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
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData,
    ): LabelField<GroupHarvestField> {
        val actorInfo = harvest.actorInfo as? GroupHuntingPerson.SearchingByHunterNumber

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
                stringProvider.getString(RR.string.group_hunting_searching_hunter_by_id)
            GroupHuntingPerson.SearchingByHunterNumber.Status.SEARCHING_PERSON_BY_HUNTER_NUMBER ->
                stringProvider.getString(RR.string.group_hunting_searching_hunter_by_id)
            GroupHuntingPerson.SearchingByHunterNumber.Status.SEARCH_FAILED ->
                stringProvider.getString(RR.string.group_hunting_hunter_search_failed)
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

    private fun deerHuntingOtherTypeDescription(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): StringField<GroupHarvestField> {
        return StringField(fieldSpecification.fieldId, harvest.deerHuntingOtherTypeDescription ?: "") {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            label = stringProvider.getString(RR.string.group_hunting_harvest_field_deer_hunting_other_type_description)
            paddingTop = Padding.NONE
        }
    }

    private fun location(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): LocationField<GroupHarvestField> {
        return LocationField(fieldSpecification.fieldId, harvest.geoLocation.asKnownLocation()) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
        }
    }

    private fun gender(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): GenderField<GroupHarvestField> {
        val gender = harvest.specimens.firstOrNull()?.gender?.value ?: Gender.UNKNOWN
        return GenderField(fieldSpecification.fieldId, gender) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            paddingTop = Padding.SMALL_MEDIUM
        }
    }

    private fun age(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): AgeField<GroupHarvestField> {
        val age = harvest.specimens.firstOrNull()?.age?.value ?: GameAge.UNKNOWN
        return AgeField(fieldSpecification.fieldId, age) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
        }
    }

    private fun notEdible(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): BooleanField<GroupHarvestField> {
        val notEdible = harvest.specimens.firstOrNull()?.notEdible
        return BooleanField(fieldSpecification.fieldId, notEdible) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            label = stringProvider.getString(RR.string.group_hunting_harvest_field_not_edible)
            // Todo: preferred view style (checkbox, radio toggle)
        }
    }

    private fun weightEstimated(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): DoubleField<GroupHarvestField> {
        val weightEstimated = harvest.specimens.firstOrNull()?.weightEstimated
        return DoubleField(fieldSpecification.fieldId, weightEstimated) {
            readOnly = false
            decimals = 0
            requirementStatus = fieldSpecification.requirementStatus
            maxValue = SpeciesConstants.MAX_WEIGHT
            label = stringProvider.getString(RR.string.group_hunting_harvest_field_weight_estimated)
        }
    }

    private fun weightMeasured(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): DoubleField<GroupHarvestField> {
        val weightMeasured = harvest.specimens.firstOrNull()?.weightMeasured
        return DoubleField(fieldSpecification.fieldId, weightMeasured) {
            readOnly = false
            decimals = 0
            requirementStatus = fieldSpecification.requirementStatus
            maxValue = SpeciesConstants.MAX_WEIGHT
            label = stringProvider.getString(RR.string.group_hunting_harvest_field_weight_measured)
        }
    }

    private fun fitnessClass(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): StringListField<GroupHarvestField> {
        val fitnessClass: BackendEnum<GameFitnessClass> =
            harvest.specimens.firstOrNull()?.fitnessClass ?: BackendEnum.create(null)
        return fitnessClassFieldFactory.create(
                fieldId = fieldSpecification.fieldId,
                currentEnumValue = fitnessClass,
                allowEmptyValue = fieldSpecification.requirementStatus.isRequired().not()
        ) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            label = stringProvider.getString(RR.string.group_hunting_harvest_field_fitness_class)
        }
    }

    private fun antlersType(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): StringListField<GroupHarvestField> {
        val antlersType: BackendEnum<GameAntlersType> =
            harvest.specimens.firstOrNull()?.antlersType ?: BackendEnum.create(null)
        return antlersTypeFieldFactory.create(
                fieldId = fieldSpecification.fieldId,
                currentEnumValue = antlersType,
                allowEmptyValue = fieldSpecification.requirementStatus.isRequired().not()
        ) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            label = stringProvider.getString(RR.string.group_hunting_harvest_field_antlers_type)
        }
    }

    private fun antlersWidth(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): IntField<GroupHarvestField> {
        val antlersWidth = harvest.specimens.firstOrNull()?.antlersWidth
        return IntField(fieldSpecification.fieldId, antlersWidth) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            maxValue = SpeciesConstants.MAX_ANTLERS_WIDTH
            label = stringProvider.getString(RR.string.group_hunting_harvest_field_antlers_width)
        }
    }

    private fun antlerPointsLeft(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): IntField<GroupHarvestField> {
        val antlerPointsLeft = harvest.specimens.firstOrNull()?.antlerPointsLeft
        return IntField(fieldSpecification.fieldId, antlerPointsLeft) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            maxValue = SpeciesConstants.MAX_ANTLER_POINTS
            label =
                stringProvider.getString(RR.string.group_hunting_harvest_field_antler_points_left)
        }
    }

    private fun antlerPointsRight(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): IntField<GroupHarvestField> {
        val antlerPointsRight = harvest.specimens.firstOrNull()?.antlerPointsRight
        return IntField(fieldSpecification.fieldId, antlerPointsRight) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            maxValue = SpeciesConstants.MAX_ANTLER_POINTS
            label =
                stringProvider.getString(RR.string.group_hunting_harvest_field_antler_points_right)
        }
    }

    private fun antlersLost(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): BooleanField<GroupHarvestField> {
        val antlersLost = harvest.specimens.firstOrNull()?.antlersLost
        return BooleanField(fieldSpecification.fieldId, antlersLost) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            label = stringProvider.getString(RR.string.group_hunting_harvest_field_antlers_lost)
            // Todo: preferred view style (checkbox, radio toggle)
        }
    }

    private fun antlersGirth(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): IntField<GroupHarvestField> {
        val antlersGirth = harvest.specimens.firstOrNull()?.antlersGirth
        return IntField(fieldSpecification.fieldId, antlersGirth) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            maxValue = SpeciesConstants.MAX_ANTLERS_GIRTH
            label = stringProvider.getString(RR.string.group_hunting_harvest_field_antlers_girth)
        }
    }

    private fun antlerShaftWidth(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): IntField<GroupHarvestField> {
        val antlerShaftWidth = harvest.specimens.firstOrNull()?.antlerShaftWidth
        return IntField(fieldSpecification.fieldId, antlerShaftWidth) {
            readOnly = false
            maxValue = SpeciesConstants.MAX_ANTLER_SHAFT_WIDTH
            label =
                stringProvider.getString(RR.string.group_hunting_harvest_field_antler_shaft_width)
        }
    }

    private fun antlersLength(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): IntField<GroupHarvestField> {
        val antlersLength = harvest.specimens.firstOrNull()?.antlersLength
        return IntField(fieldSpecification.fieldId, antlersLength) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            maxValue = SpeciesConstants.MAX_ANTLERS_LENGTH
            label = stringProvider.getString(RR.string.group_hunting_harvest_field_antlers_length)
        }
    }

    private fun antlersInnerWidth(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): IntField<GroupHarvestField> {
        val antlersInnerWidth = harvest.specimens.firstOrNull()?.antlersInnerWidth
        return IntField(fieldSpecification.fieldId, antlersInnerWidth) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            maxValue = SpeciesConstants.MAX_ANTLERS_INNER_WIDTH
            label =
                stringProvider.getString(RR.string.group_hunting_harvest_field_antlers_inner_width)
        }
    }

    private fun alone(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): BooleanField<GroupHarvestField> {
        val alone = harvest.specimens.firstOrNull()?.alone
        return BooleanField(fieldSpecification.fieldId, alone) {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            label = stringProvider.getString(RR.string.group_hunting_harvest_field_alone)
            // Todo: preferred view style (checkbox, radio toggle)
        }
    }

    private fun additionalInformation(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData
    ): StringField<GroupHarvestField> {
        val additionalInfo = harvest.specimens.firstOrNull()?.additionalInfo
        return StringField(fieldSpecification.fieldId, additionalInfo ?: "") {
            readOnly = false
            requirementStatus = fieldSpecification.requirementStatus
            label =
                stringProvider.getString(RR.string.group_hunting_harvest_field_additional_information)
        }
    }

    private fun additionalInformationInstructions(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData,
    ): LabelField<GroupHarvestField> {
        val instructions = when (harvest.gameSpeciesCode) {
            SpeciesCodes.MOOSE_ID,
            SpeciesCodes.FALLOW_DEER_ID,
            SpeciesCodes.WILD_FOREST_DEER_ID,
            SpeciesCodes.ROE_DEER_ID ->
                stringProvider.getString(RR.string.group_hunting_harvest_field_additional_information_instructions)
            SpeciesCodes.WHITE_TAILED_DEER_ID ->
                stringProvider.getString(RR.string.group_hunting_harvest_field_additional_information_instructions_white_tailed_deer)
            else -> error("Invalid species for additional information instructions")
        }
        return LabelField(
            id = fieldSpecification.fieldId,
            text = instructions,
            type = LabelField.Type.INFO,
        ) {
            paddingTop = Padding.NONE
        }
    }

    private fun antlerInstructions(
        fieldSpecification: FieldSpecification<GroupHarvestField>,
        harvest: GroupHuntingHarvestData,
    ): InstructionsField<GroupHarvestField> {
        val type = when (harvest.gameSpeciesCode) {
            SpeciesCodes.MOOSE_ID -> InstructionsField.Type.MOOSE_ANTLER_INSTRUCTIONS
            SpeciesCodes.WHITE_TAILED_DEER_ID -> InstructionsField.Type.WHITE_TAILED_DEER_ANTLER_INSTRUCTIONS
            SpeciesCodes.ROE_DEER_ID -> InstructionsField.Type.ROE_DEER_ANTLER_INSTRUCTIONS
            else -> error("Invalid species for antler instructions")
        }
        return InstructionsField(fieldSpecification.fieldId, type)
    }
}

internal fun PersonWithHunterNumber.toStringWithId(
    prefix: String = "",
    includeHunterNumber: Boolean
): StringWithId {
    val hunterNumberPostfix = hunterNumber
        ?.takeIf { includeHunterNumber }
        ?.let { " ($it)" }
        ?: ""

    return StringWithId(
        id = id,
        string = "$prefix$byName $lastName$hunterNumberPostfix"
    )
}

internal fun List<HuntingGroupMember>.toStringWithIdList(
    includeHunterNumber: Boolean
): List<StringWithId> {
    return sortedBy { it.lastName + it.firstName }
        .map { member ->
            val hunterNumberPostfix = member.hunterNumber
                ?.takeIf { includeHunterNumber }
                ?.let { " ($it)" }
                ?: ""

            StringWithId(
                id = member.personId,
                string = "${member.firstName} ${member.lastName}$hunterNumberPostfix"
            )
        }
}
