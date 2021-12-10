package fi.riista.mobile.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME
import fi.riista.mobile.models.shootingTest.ShootingTestCalendarEvent
import fi.riista.mobile.models.shootingTest.ShootingTestOfficial
import fi.riista.mobile.models.shootingTest.ShootingTestParticipant
import fi.riista.mobile.models.user.Occupation
import fi.riista.mobile.network.shootingTest.*
import fi.riista.mobile.utils.Authenticator
import fi.riista.mobile.utils.UserInfoStore
import fi.riista.mobile.utils.Utils
import fi.vincit.androidutilslib.context.WorkContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class ShootingTestMainViewModel @Inject constructor(@Named(APPLICATION_WORK_CONTEXT_NAME) private val appWorkContext: WorkContext,
                                                    private val userInfoStore: UserInfoStore,
                                                    private val authenticator: Authenticator) : ViewModel() {

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

    fun refreshCalendarEvent() {
        val calendarEventId = calendarEventId
        val user = userInfoStore.getUserInfo()
        if (calendarEventId == null || user == null) {
            return
        }

        val task: GetShootingTestCalendarEventTask = object : GetShootingTestCalendarEventTask(appWorkContext, calendarEventId) {
            override fun onFinishObject(result: ShootingTestCalendarEvent) {
                testEventId = result.shootingTestEventId
                rhyId = result.rhyId
                val rhyId = rhyId
                if (rhyId != null) {
                    isOngoing.value = result.isOngoing
                    noAttemptsCount.value = result.numberOfParticipantsWithNoAttempts
                    noPaymentCount.value = result.numberOfAllParticipants - result.numberOfCompletedParticipants
                    val shootingTestOfficialOccupation = user.findOccupationOfTypeForRhy(Occupation.OCCUPATION_SHOOTING_TEST_OFFICIAL, rhyId.toInt())
                    val coordinatorOccupation = user.findOccupationOfTypeForRhy(Occupation.OCCUPATION_COORDINATOR, rhyId.toInt())
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

            override fun onError() {
                super.onError()
                if (httpStatusCode == 401) {
                    CoroutineScope(Dispatchers.Main).launch {
                        authenticator.reauthenticate()
                    }
                }
            }

            override fun onEnd() {
                super.onEnd()
                refreshing.decrement()
            }
        }
        refreshing.increment()
        task.start()
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
        val task: ListSelectedShootingTestOfficialsForEventTask = object : ListSelectedShootingTestOfficialsForEventTask(appWorkContext, testEventId) {
            override fun onFinishObjects(results: List<ShootingTestOfficial>) {
                selectedOfficials.value = results
            }

            override fun onEnd() {
                super.onEnd()
                refreshing.decrement()
            }
        }
        refreshing.increment()
        task.start()
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
        val task: ListAvailableShootingTestOfficialsForEventTask = object : ListAvailableShootingTestOfficialsForEventTask(appWorkContext, testEventId) {
            override fun onFinishObjects(results: List<ShootingTestOfficial>) {
                availableOfficials.value = results
            }

            override fun onEnd() {
                super.onEnd()
                refreshing.decrement()
            }
        }
        refreshing.increment()
        task.start()
    }

    private fun loadAvailableOfficialsForRhy() {
        val rhyId = rhyId
        if (rhyId == null) {
            Utils.LogMessage("Skip loading available officials: No RHY ID")
            return
        }
        val task: ListAvailableShootingTestOfficialsForRhyTask = object : ListAvailableShootingTestOfficialsForRhyTask(appWorkContext, rhyId) {
            override fun onFinishObjects(results: List<ShootingTestOfficial>) {
                availableOfficials.value = results
            }

            override fun onEnd() {
                super.onEnd()
                refreshing.decrement()
            }
        }
        refreshing.increment()
        task.start()
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
        val task: ListShootingTestParticipantsTask = object : ListShootingTestParticipantsTask(appWorkContext, testEventId) {
            override fun onFinishObjects(results: List<ShootingTestParticipant>) {
                participants.value = results
            }

            override fun onEnd() {
                super.onEnd()
                refreshing.decrement()
            }
        }
        refreshing.increment()
        task.start()
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
