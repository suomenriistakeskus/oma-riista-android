package fi.riista.common.groupHunting.huntingDays.select

import fi.riista.common.groupHunting.GroupHuntingContext
import fi.riista.common.groupHunting.MockGroupHuntingData
import fi.riista.common.groupHunting.model.GroupHuntingDayId
import fi.riista.common.groupHunting.model.HuntingGroupTarget
import fi.riista.common.groupHunting.ui.huntingDays.select.SelectHuntingDayController
import fi.riista.common.helpers.getLoadedViewModel
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.LocalDateTime
import fi.riista.common.resources.RR
import fi.riista.common.resources.RStringId
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.userInfo.CurrentUserContextProviderFactory
import kotlin.test.*

class SelectHuntingDayControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = SelectHuntingDayController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider(),
        )

        assertSame(ViewModelLoadStatus.NotLoaded, controller.viewModelLoadStatus.value)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = SelectHuntingDayController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider(),
        )

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        with(controller.getLoadedViewModel()) {
            assertEquals(2, huntingDays.size)
            assertEquals(5, huntingDays[1].huntingDayId.remoteId)
            assertEquals(LocalDateTime(2015, 9, 1, 6, 0, 0), huntingDays[1].startDateTime)
            assertEquals(LocalDateTime(2015, 9, 1, 21, 0, 0), huntingDays[1].endDateTime)
            assertFalse(huntingDays[1].selected)
            assertEquals(6, huntingDays[0].huntingDayId.remoteId)
            assertEquals(LocalDateTime(2015, 9, 3, 6, 0, 0), huntingDays[0].startDateTime)
            assertEquals(LocalDateTime(2015, 9, 3, 21, 0, 0), huntingDays[0].endDateTime)
            assertFalse(huntingDays[0].selected)
            assertNull(selectedHuntingDayId)
            assertFalse(isHuntingDaySelected)
        }
    }

    @Test
    fun testHuntingDaysAreSortedCorrectly() = runBlockingTest {
        val controller = SelectHuntingDayController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider(),
        )

        controller.loadViewModel()

        with(controller.getLoadedViewModel()) {
            assertTrue(huntingDays[0].startDateTime >= huntingDays[1].startDateTime)
        }
    }

    @Test
    fun testHuntingDayCanBeInitiallySelected() = runBlockingTest {
        val controller = SelectHuntingDayController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider(),
        )
        controller.initiallySelectedHuntingDayId = GroupHuntingDayId.remote(6)

        controller.loadViewModel()

        with(controller.getLoadedViewModel()) {
            assertEquals(2, huntingDays.size)
            assertEquals(5, huntingDays[1].huntingDayId.remoteId)
            assertFalse(huntingDays[1].selected)
            assertEquals(6, huntingDays[0].huntingDayId.remoteId)
            assertTrue(huntingDays[0].selected)
            assertEquals(6, selectedHuntingDayId?.remoteId)
            assertTrue(isHuntingDaySelected)
        }
    }

    @Test
    fun testHuntingDayCanBeSelected() = runBlockingTest {
        val controller = SelectHuntingDayController(
                groupHuntingContext = getGroupHuntingContext(),
                groupTarget = getHuntingGroupTarget(),
                stringProvider = getStringProvider(),
        )

        controller.loadViewModel()

        with(controller.getLoadedViewModel()) {
            assertEquals(5, huntingDays[1].huntingDayId.remoteId)
            assertFalse(huntingDays[1].selected)
            assertEquals(6, huntingDays[0].huntingDayId.remoteId)
            assertFalse(huntingDays[0].selected)
            assertNull(selectedHuntingDayId)
            assertFalse(isHuntingDaySelected)
        }

        controller.eventDispatcher.dispatchHuntingDaySelected(GroupHuntingDayId.remote(5))

        with(controller.getLoadedViewModel()) {
            assertEquals(5, huntingDays[1].huntingDayId.remoteId)
            assertTrue(huntingDays[1].selected)
            assertEquals(6, huntingDays[0].huntingDayId.remoteId)
            assertFalse(huntingDays[0].selected)
            assertEquals(5, selectedHuntingDayId?.remoteId)
            assertTrue(isHuntingDaySelected)
        }

        controller.eventDispatcher.dispatchHuntingDaySelected(GroupHuntingDayId.remote(6))

        with(controller.getLoadedViewModel()) {
            assertEquals(5, huntingDays[1].huntingDayId.remoteId)
            assertFalse(huntingDays[1].selected)
            assertEquals(6, huntingDays[0].huntingDayId.remoteId)
            assertTrue(huntingDays[0].selected)
            assertEquals(6, selectedHuntingDayId?.remoteId)
            assertTrue(isHuntingDaySelected)
        }
    }

    private fun getGroupHuntingContext(): GroupHuntingContext {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
                groupHuntingEnabledForAll = true,
        )
        return userContextProvider.userContext.groupHuntingContext
    }

    private fun getHuntingGroupTarget(): HuntingGroupTarget {
        return HuntingGroupTarget(
                clubId = MockGroupHuntingData.FirstClubId,
                huntingGroupId = MockGroupHuntingData.FirstHuntingGroupId
        )
    }

    private fun getStringProvider(): StringProvider {
        return object : StringProvider {
            @Suppress("SpellCheckingInspection")
            override fun getString(stringId: RStringId): String {
                return when (stringId) {
                    RR.string.group_hunting_message_no_hunting_days_but_can_create -> "no_hunting_days_but_can_create"
                    RR.string.group_hunting_message_no_hunting_days -> "no_hunting_days"
                    RR.string.group_hunting_message_no_hunting_days_deer -> "no_hunting_days_deer"
                    else -> {
                        throw RuntimeException("Unexpected stringId ($stringId) requested")
                    }
                }
            }

            override fun getFormattedString(stringId: RStringId, arg: String): String {
                throw RuntimeException("Unexpected stringId ($stringId) requested")
            }
        }
    }
}
