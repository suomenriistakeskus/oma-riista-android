package fi.riista.common.extensions

import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.model.StringWithId
import fi.riista.common.util.MockBundle
import org.junit.Test
import kotlin.test.assertEquals

class GroupHuntingTargetExtensionsTest {
    @Test
    fun testSavingClubTargetToBundle() {
        val bundle = MockBundle()
        val clubTarget = GroupHuntingClubTarget(clubId = 1)
        clubTarget.saveToBundle(bundle, prefix = "prefix")

        assertEquals(1, bundle.getLongOrNull("prefix_key_club_id"))
    }

    @Test
    fun testLoadingClubTargetFromBundle() {
        val bundle = MockBundle().also {
            it.putLong("prefix_key_club_id", 1)
        }
        val clubTarget = bundle.loadGroupHuntingClubTarget("prefix")
        assertEquals(1, clubTarget?.clubId)
    }

    @Test
    fun testSavingHuntingGroupTargetToBundle() {
        val bundle = MockBundle()
        val huntingGroupTarget = HuntingGroupTarget(clubId = 1, huntingGroupId = 2)
        huntingGroupTarget.saveToBundle(bundle, prefix = "prefix")

        assertEquals(1, bundle.getLongOrNull("prefix_key_club_id"))
        assertEquals(2, bundle.getLongOrNull("prefix_key_hunting_group_id"))
    }

    @Test
    fun testLoadingHuntingGroupTargetFromBundle() {
        val bundle = MockBundle().also {
            it.putLong("prefix_key_club_id", 1)
            it.putLong("prefix_key_hunting_group_id", 2)
        }
        val huntingGroupTarget = bundle.loadHuntingGroupTarget("prefix")
        assertEquals(1, huntingGroupTarget?.clubId)
        assertEquals(2, huntingGroupTarget?.huntingGroupId)
    }

    @Test
    fun testSavingGroupHuntingHarvestTargetToBundle() {
        val bundle = MockBundle()
        val harvestTarget = GroupHuntingHarvestTarget(
                clubId = 1, huntingGroupId = 2, harvestId = 3
        )
        harvestTarget.saveToBundle(bundle, prefix = "prefix")

        assertEquals(1, bundle.getLongOrNull("prefix_key_club_id"))
        assertEquals(2, bundle.getLongOrNull("prefix_key_hunting_group_id"))
        assertEquals(3, bundle.getLongOrNull("prefix_key_group_hunting_harvest_id"))
    }

    @Test
    fun testLoadingGroupHuntingObservationTargetFromBundle() {
        val bundle = MockBundle().also {
            it.putLong("prefix_key_club_id", 1)
            it.putLong("prefix_key_hunting_group_id", 2)
            it.putLong("prefix_key_group_hunting_observation_id", 3)
        }
        val observationTarget = bundle.loadGroupHuntingObservationTarget("prefix")
        assertEquals(1, observationTarget?.clubId)
        assertEquals(2, observationTarget?.huntingGroupId)
        assertEquals(3, observationTarget?.observationId)
    }

    @Test
    fun testSavingGroupHuntingObservationTargetToBundle() {
        val bundle = MockBundle()
        val observationTarget = GroupHuntingObservationTarget(
            clubId = 1, huntingGroupId = 2, observationId = 3
        )
        observationTarget.saveToBundle(bundle, prefix = "prefix")

        assertEquals(1, bundle.getLongOrNull("prefix_key_club_id"))
        assertEquals(2, bundle.getLongOrNull("prefix_key_hunting_group_id"))
        assertEquals(3, bundle.getLongOrNull("prefix_key_group_hunting_observation_id"))
    }

    @Test
    fun testLoadingGroupHuntingHarvestTargetFromBundle() {
        val bundle = MockBundle().also {
            it.putLong("prefix_key_club_id", 1)
            it.putLong("prefix_key_hunting_group_id", 2)
            it.putLong("prefix_key_group_hunting_harvest_id", 3)
        }
        val harvestTarget = bundle.loadGroupHuntingHarvestTarget("prefix")
        assertEquals(1, harvestTarget?.clubId)
        assertEquals(2, harvestTarget?.huntingGroupId)
        assertEquals(3, harvestTarget?.harvestId)
    }

    @Test
    fun testSavingGroupHuntingDayTargetToBundle() {
        val bundle = MockBundle()
        val harvestTarget = GroupHuntingDayTarget(
                clubId = 1, huntingGroupId = 2, huntingDayId = GroupHuntingDayId.remote(3)
        )
        harvestTarget.saveToBundle(bundle, prefix = "prefix")

        assertEquals(1, bundle.getLongOrNull("prefix_key_club_id"))
        assertEquals(2, bundle.getLongOrNull("prefix_key_hunting_group_id"))
        assertEquals(3, bundle.getLongOrNull("prefix_key_group_hunting_day_id"))
    }

    @Test
    fun testLoadingGroupHuntingDayTargetFromBundle() {
        val bundle = MockBundle().also {
            it.putLong("prefix_key_club_id", 1)
            it.putLong("prefix_key_hunting_group_id", 2)
            it.putLong("prefix_key_group_hunting_day_id", 3)
        }
        val huntingDayTarget = bundle.loadGroupHuntingDayTarget("prefix")
        assertEquals(1, huntingDayTarget?.clubId)
        assertEquals(2, huntingDayTarget?.huntingGroupId)
        assertEquals(GroupHuntingDayId.remote(3), huntingDayTarget?.huntingDayId)
    }

    @Test
    fun testSavingStringWithIdToBundle() {
        val bundle = MockBundle()
        val stringWithId = StringWithId(id = 1232, string = "Hello")
        stringWithId.saveToBundle(bundle, prefix = "prefix")

        assertEquals(1232, bundle.getLongOrNull("prefix_key_string_with_id_id"))
        assertEquals("Hello", bundle.getString("prefix_key_string_with_id_string"))
    }

    @Test
    fun testLoadingStringWithIdFromBundle() {
        val bundle = MockBundle().also {
            it.putLong("prefix_key_string_with_id_id", 321)
            it.putString("prefix_key_string_with_id_string", "Hello")
        }

        val stringWithId = bundle.loadStringWithId("prefix")
        assertEquals(321, stringWithId?.id)
        assertEquals("Hello", stringWithId?.string)
    }
}
