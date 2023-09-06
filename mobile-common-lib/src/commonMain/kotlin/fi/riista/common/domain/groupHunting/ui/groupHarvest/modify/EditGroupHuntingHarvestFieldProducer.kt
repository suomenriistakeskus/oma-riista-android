package fi.riista.common.domain.groupHunting.ui.groupHarvest.modify

import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.groupHunting.model.HuntingGroupMember
import fi.riista.common.domain.groupHunting.model.isMember
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.model.HarvestReportingType
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.modify.ModifyHarvestFieldProducer
import fi.riista.common.domain.model.PersonWithHunterNumber
import fi.riista.common.model.StringWithId
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.IntField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.Padding
import fi.riista.common.ui.dataField.StringListField
import fi.riista.common.util.LocalDateTimeProvider

internal class EditGroupHuntingHarvestFieldProducer(
    private val stringProvider: StringProvider,
    currentDateTimeProvider: LocalDateTimeProvider,
) {
    private val commonHarvestFieldProducer = ModifyHarvestFieldProducer(
        canChangeSpecies = false,
        harvestPermitProvider = null,
        huntingClubsSelectableForHarvests = null,
        stringProvider = stringProvider,
        languageProvider = null,
        currentDateTimeProvider = currentDateTimeProvider
    )

    fun createField(
        fieldSpecification: FieldSpecification<CommonHarvestField>,
        harvest: CommonHarvestData,
        groupMembers: List<HuntingGroupMember>,
    ) : DataField<CommonHarvestField>? {
        return when (fieldSpecification.fieldId) {
            CommonHarvestField.ACTOR -> actor(fieldSpecification, harvest, groupMembers)
            CommonHarvestField.ACTOR_HUNTER_NUMBER -> actorHunterNumber(fieldSpecification, harvest)
            CommonHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR ->
                actorHunterNumberInfoOrError(fieldSpecification, harvest)
            else ->
                commonHarvestFieldProducer.createField(
                    fieldSpecification = fieldSpecification,
                    harvest = harvest,
                    // always season reporting when handling group hunting harvests
                    harvestReportingType = HarvestReportingType.SEASON,
                    shooters = emptyList(), // not used in group hunting
                    ownHarvest = false, // not used in group hunting
                )
        }
    }

    private fun actor(
        fieldSpecification: FieldSpecification<CommonHarvestField>,
        harvest: CommonHarvestData,
        groupMembers: List<HuntingGroupMember>
    ): StringListField<CommonHarvestField> {
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
        fieldSpecification: FieldSpecification<CommonHarvestField>,
        harvest: CommonHarvestData,
    ): IntField<CommonHarvestField> {
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
        fieldSpecification: FieldSpecification<CommonHarvestField>,
        harvest: CommonHarvestData,
    ): LabelField<CommonHarvestField> {
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
