package fi.riista.common.model

import fi.riista.common.groupHunting.model.*
import kotlin.test.Test
import kotlin.test.assertEquals

class GroupHuntingTargetTest {

    @Test
    fun testCreatingHuntingGroupTarget() {
        val clubTarget = GroupHuntingClubTarget(clubId = 1)
        val huntingGroupTarget = clubTarget.createTargetForHuntingGroup(huntingGroupId = 2)
        assertEquals(1, huntingGroupTarget.clubId)
        assertEquals(2, huntingGroupTarget.huntingGroupId)
    }

    @Test
    fun testCreatingGroupHuntingHarvestTarget() {
        val huntingGroupTarget = HuntingGroupTarget(clubId = 1, huntingGroupId = 2)
        val harvestTarget = huntingGroupTarget.createTargetForHarvest(harvestId = 3)
        assertEquals(1, harvestTarget.clubId)
        assertEquals(2, harvestTarget.huntingGroupId)
        assertEquals(3, harvestTarget.harvestId)
    }

    @Test
    fun testCreatingGroupHuntingDayTarget() {
        val huntingGroupTarget = HuntingGroupTarget(clubId = 1, huntingGroupId = 2)
        val huntingDayTarget = huntingGroupTarget.createTargetForHuntingDay(huntingDayId = GroupHuntingDayId.remote(3))
        assertEquals(1, huntingDayTarget.clubId)
        assertEquals(2, huntingDayTarget.huntingGroupId)
        assertEquals(3, huntingDayTarget.huntingDayId.remoteId)
    }
}