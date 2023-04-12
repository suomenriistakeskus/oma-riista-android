package fi.riista.mobile.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.OperationResultWithData
import fi.riista.mobile.models.shootingTest.ShootingTestCalendarEvent
import fi.riista.mobile.models.shootingTest.ShootingTestOfficial
import fi.riista.mobile.models.shootingTest.ShootingTestParticipant
import fi.riista.mobile.models.user.Occupation
import fi.riista.mobile.riistaSdkHelpers.toShootingTestCalendarEvent
import fi.riista.mobile.riistaSdkHelpers.toShootingTestOfficial
import fi.riista.mobile.riistaSdkHelpers.toShootingTestParticipant
import fi.riista.mobile.utils.UserInfoStore
import fi.riista.mobile.utils.Utils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShootingTestMainViewModel @Inject constructor(private val userInfoStore: UserInfoStore) : ViewModel() {

    val participants: MutableLiveData<List<ShootingTestParticipant>> = MutableLiveData()
    val selectedOfficials: MutableLiveData<List<ShootingTestOfficial>> = MutableLiveData()
    val availableOfficials: MutableLiveData<List<ShootingTestOfficial>> = MutableLiveData()
    val calendarEvent = MutableLiveData<ShootingTestCalendarEvent>()
    var calendarEventId: Long? = null
    var testEventId: Long? = null
        private set
    var rhyId: Long? = null
        private set
    val isOngoing = MutableLiveData<Boolean>()
    val isUserSelectedOfficial = MutableLiveData<Boolean>()
    val isUserCoordinator = MutableLiveData<Boolean>()
    val noAttemptsCount = MutableLiveData<Int>()
    val noPaymentCount = MutableLiveData<Int>()
    val refreshing: MutableLiveData<Int> = MutableLiveData(0)

    private val shootingTestContext = RiistaSDK.shootingTestContext

    fun refreshCalendarEvent() {
        val calendarEventId = calendarEventId
        val user = userInfoStore.getUserInfo()
        if (calendarEventId == null || user == null) {
            return
        }

        MainScope().launch {
            refreshing.increment()
            when (val response = shootingTestContext.fetchShootingTestCalendarEvent(calendarEventId)) {
                is OperationResultWithData.Success -> {
                    val result = response.data.toShootingTestCalendarEvent()
                    testEventId = result.shootingTestEventId
                    rhyId = result.rhyId
                    val rhyId = rhyId
                    if (rhyId != null) {
                        isOngoing.value = result.isOngoing
                        noAttemptsCount.value = result.numberOfParticipantsWithNoAttempts
                        noPaymentCount.value = result.numberOfAllParticipants - result.numberOfCompletedParticipants
                        val shootingTestOfficialOccupation = user.findOccupationOfTypeForRhy(
                            Occupation.OCCUPATION_SHOOTING_TEST_OFFICIAL, rhyId.toInt()
                        )
                        val coordinatorOccupation = user.findOccupationOfTypeForRhy(
                            Occupation.OCCUPATION_COORDINATOR, rhyId.toInt()
                        )
                        var userSelectedAsOfficial = false
                        if (shootingTestOfficialOccupation != null) {
                            for (official in result.officials) {
                                if (official.occupationId.toInt() == shootingTestOfficialOccupation.id) {
                                    userSelectedAsOfficial = true
                                }
                            }
                        }
                        isUserSelectedOfficial.value = userSelectedAsOfficial
                        isUserCoordinator.value = coordinatorOccupation != null
                        calendarEvent.value = result
                    }
                }
                else -> {
                    // No op
                }
            }
            refreshing.decrement()
        }
    }

    fun refreshSelectedOfficials() {
        loadSelectedOfficials()
    }

    private fun loadSelectedOfficials() {
        val testEventId = testEventId
        if (testEventId == null) {
            Utils.LogMessage("Skip loading selected officials: No test ID")
            return
        }

        MainScope().launch {
            refreshing.increment()
            when (val response = shootingTestContext.fetchSelectedShootingTestOfficialsForEvent(testEventId)) {
                is OperationResultWithData.Success -> {
                    selectedOfficials.value = response.data.map { it.toShootingTestOfficial() }
                }
                else -> {
                    // No op
                }
            }
            refreshing.decrement()
        }
    }

    fun refreshAvailableOfficials() {
        val testEventId = testEventId
        val rhyId = rhyId
        if (testEventId != null && testEventId > 0) {
            loadAvailableOfficialsForEvent()
        } else if (rhyId != null && rhyId > 0) {
            loadAvailableOfficialsForRhy()
        }
    }

    private fun loadAvailableOfficialsForEvent() {
        val testEventId = testEventId
        if (testEventId == null) {
            Utils.LogMessage("Skip loading available officials: No event ID")
            return
        }

        MainScope().launch {
            refreshing.increment()
            when (val response = shootingTestContext.fetchAvailableShootingTestOfficialsForEvent(testEventId)) {
                is OperationResultWithData.Success -> {
                    availableOfficials.value = response.data.map { it.toShootingTestOfficial() }
                }
                else -> {
                    // No op
                }
            }
            refreshing.decrement()
        }
    }

    private fun loadAvailableOfficialsForRhy() {
        val rhyId = rhyId
        if (rhyId == null) {
            Utils.LogMessage("Skip loading available officials: No RHY ID")
            return
        }

        MainScope().launch {
            refreshing.increment()
            when (val response = shootingTestContext.fetchAvailableShootingTestOfficialsForRhy(rhyId)) {
                is OperationResultWithData.Success -> {
                    availableOfficials.value = response.data.map { it.toShootingTestOfficial() }
                }
                else -> {
                    // No op
                }
            }
            refreshing.decrement()
        }
    }

    fun refreshParticipants() {
        loadParticipants()
    }

    private fun loadParticipants() {
        val testEventId = testEventId
        if (testEventId == null) {
            Utils.LogMessage("Skip loading participants: No test ID")
            return
        }

        MainScope().launch {
            refreshing.increment()
            when (val response = shootingTestContext.fetchShootingTestParticipants(testEventId)) {
                is OperationResultWithData.Success -> {
                    participants.value = response.data.map { it.toShootingTestParticipant() }
                }
                else -> {
                    // No op
                }
            }
            refreshing.decrement()
        }
    }

    private fun MutableLiveData<Int>.increment() {
        value?.let {
            value = it + 1
        }
    }

    private fun MutableLiveData<Int>.decrement() {
        value?.let {
            value = it - 1
        }
    }
}
