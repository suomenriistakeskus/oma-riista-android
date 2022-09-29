package fi.riista.common.domain.groupHunting.ui.huntingDays.view

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.domain.constants.isMoose
import fi.riista.common.domain.groupHunting.GroupHuntingClubGroupContext
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.model.AcceptStatus
import fi.riista.common.domain.groupHunting.model.GroupHuntingDay
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayTarget
import fi.riista.common.domain.groupHunting.model.toLocalHuntingDay
import fi.riista.common.domain.groupHunting.ui.huntingDays.HuntingDayViewModel
import fi.riista.common.domain.groupHunting.ui.huntingDays.toHuntingDayHarvestViewModel
import fi.riista.common.domain.groupHunting.ui.huntingDays.toHuntingDayObservationViewModel
import fi.riista.common.model.LocalDateTime
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.SystemDateTimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ViewGroupHuntingDayController internal constructor(
    private val groupHuntingContext: GroupHuntingContext,
    private val huntingDayTarget: GroupHuntingDayTarget,
    private val currentTimeProvider: LocalDateTimeProvider,
) : ControllerWithLoadableModel<HuntingDayViewModel>() {

    constructor(
        groupHuntingContext: GroupHuntingContext,
        huntingDayTarget: GroupHuntingDayTarget,
    ): this(
        groupHuntingContext = groupHuntingContext,
        huntingDayTarget = huntingDayTarget,
        currentTimeProvider = SystemDateTimeProvider(),
    )

    init {
        ensureNeverFrozen()
    }

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<HuntingDayViewModel>> = flow {

        emit(ViewModelLoadStatus.Loading)

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
                identifiesClubAndGroup = huntingDayTarget,
                allowCached = true
        ) ?: kotlin.run {
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val huntingDay = fetchHuntingDay(groupContext, refresh) ?: kotlin.run {
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val diaryProvider = groupContext.diaryProvider
        diaryProvider.fetch(refresh = refresh)

        val statusProvider = groupContext.huntingStatusProvider
        statusProvider.fetch(refresh = refresh)

        val harvests = diaryProvider.diary.harvests.filter { harvest ->
            harvest.huntingDayId == huntingDay.id
                    || harvest.pointOfTime.isWithinHuntingDay(huntingDay)
        }
        val observations = diaryProvider.diary.observations.filter { observation ->
            observation.huntingDayId == huntingDay.id
                    || observation.pointOfTime.isWithinHuntingDay(huntingDay)
        }

        val hasProposedEntries =
            (harvests.find { it.acceptStatus == AcceptStatus.PROPOSED } != null) ||
                    (observations.find { it.acceptStatus == AcceptStatus.PROPOSED } != null)

        val isMoose = groupContext.huntingGroup.speciesCode.isMoose()
        val canEditHuntingDay = (statusProvider.status?.canEditHuntingDay ?: false) &&
                huntingDay.type.isRemote() // only remote hunting days can be edited
        val canCreateHuntingDay = (statusProvider.status?.canCreateHuntingDay ?: false) &&
                huntingDay.type.isLocal() && isMoose // only local hunting days can be created

        val viewModel = HuntingDayViewModel(
                huntingDay = huntingDay,
                harvests = harvests.map { it.toHuntingDayHarvestViewModel() },
                observations = observations.map { it.toHuntingDayObservationViewModel() },
                hasProposedEntries = hasProposedEntries,
                canEditHuntingDay = canEditHuntingDay,
                canCreateHuntingDay = canCreateHuntingDay,
                showHuntingDayDetails = isMoose && huntingDay.type.isRemote()
        )

        emit(ViewModelLoadStatus.Loaded(viewModel))
    }

    private suspend fun fetchHuntingDay(
        groupContext: GroupHuntingClubGroupContext,
        refresh: Boolean,
    ): GroupHuntingDay? {
        return when {
            huntingDayTarget.targetsRemoteDay() ->
                groupContext.fetchHuntingDay(huntingDayTarget, allowCached = !refresh)
            else -> {
                // we're dealing with a huntingDayTarget that points to a local hunting day.
                // It is, however, possible that a hunting day has been created based
                // the local hunting day data
                // -> attempt to look for a hunting day that has the same date
                val createdDay = huntingDayTarget.huntingDayId.date?.let { date ->
                    groupContext.fetchHuntingDay(startDate = date, allowCached = !refresh)
                }

                // fallback to local day if no such day is found..
                createdDay ?: huntingDayTarget.huntingDayId.toLocalHuntingDay(
                        huntingGroupId = huntingDayTarget.huntingGroupId,
                        speciesCode = groupContext.huntingGroup.speciesCode,
                        now = currentTimeProvider.now()
                )
            }
        }
    }
}

fun LocalDateTime.isWithinHuntingDay(huntingDay: GroupHuntingDay): Boolean {
    // only take dates into account since it is possible that harvests/observations
    // have timestamps outside of hunting day start / end time. This can be particularly
    // the case with suggested hunting days.
    return date >= huntingDay.startDateTime.date && date <= huntingDay.endDateTime.date
}
