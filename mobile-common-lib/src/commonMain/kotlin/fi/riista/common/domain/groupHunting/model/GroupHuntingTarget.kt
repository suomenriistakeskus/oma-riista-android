package fi.riista.common.domain.groupHunting.model

import fi.riista.common.domain.model.OrganizationId

/**
 * A base class for classes that can target a group hunting related object such as Club,
 * [HuntingGroup] or even [GroupHuntingHarvest] or [GroupHuntingObservation]
 */
sealed class GroupHuntingTarget

data class GroupHuntingClubTarget(
    override val clubId: OrganizationId
) : GroupHuntingTarget(), IdentifiesGroupHuntingClub

data class HuntingGroupTarget(
    override val clubId: OrganizationId,
    override val huntingGroupId: HuntingGroupId,
) : GroupHuntingTarget(), IdentifiesHuntingGroup

data class GroupHuntingHarvestTarget(
    override val clubId: OrganizationId,
    override val huntingGroupId: HuntingGroupId,
    override val harvestId: GroupHuntingHarvestId
) : GroupHuntingTarget(), IdentifiesGroupHuntingHarvest

data class GroupHuntingObservationTarget(
    override val clubId: OrganizationId,
    override val huntingGroupId: HuntingGroupId,
    override val observationId: GroupHuntingObservationId
) : GroupHuntingTarget(), IdentifiesGroupHuntingObservation

data class GroupHuntingDayTarget(
    override val clubId: OrganizationId,
    override val huntingGroupId: HuntingGroupId,
    override val huntingDayId: GroupHuntingDayId
) : GroupHuntingTarget(), IdentifiesGroupHuntingDay {
    fun targetsRemoteDay() = huntingDayId.isRemote()
}


fun IdentifiesGroupHuntingClub.createTargetForHuntingGroup(huntingGroupId: HuntingGroupId) =
    HuntingGroupTarget(clubId, huntingGroupId)

fun IdentifiesHuntingGroup.createTargetForHarvest(harvestId: GroupHuntingHarvestId) =
    GroupHuntingHarvestTarget(clubId, huntingGroupId, harvestId)

fun IdentifiesHuntingGroup.createTargetForObservation(observationId: GroupHuntingObservationId) =
    GroupHuntingObservationTarget(clubId, huntingGroupId, observationId)

fun IdentifiesHuntingGroup.createTargetForHuntingDay(huntingDayId: GroupHuntingDayId) =
    GroupHuntingDayTarget(clubId, huntingGroupId, huntingDayId)

fun IdentifiesHuntingGroup.asGroupTarget() =
    HuntingGroupTarget(clubId, huntingGroupId)
