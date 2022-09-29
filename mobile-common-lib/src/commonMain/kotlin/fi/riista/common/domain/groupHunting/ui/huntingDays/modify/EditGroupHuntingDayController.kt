package fi.riista.common.domain.groupHunting.ui.huntingDays.modify

import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.GroupHuntingDayUpdateResponse
import fi.riista.common.domain.groupHunting.model.HuntingGroup
import fi.riista.common.domain.groupHunting.model.IdentifiesGroupHuntingDay
import fi.riista.common.logging.getLogger
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.intent.IntentHandler
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.SystemDateTimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EditGroupHuntingDayController internal constructor(
    private val groupHuntingContext: GroupHuntingContext,
    private val huntingDayTarget: IdentifiesGroupHuntingDay,
    stringProvider: StringProvider,
    currentTimeProvider: LocalDateTimeProvider,
) : ModifyGroupHuntingDayController(currentTimeProvider, stringProvider, startDateIsReadonly = true),
    IntentHandler<ModifyGroupHuntingDayIntent> {

    constructor(groupHuntingContext: GroupHuntingContext,
                huntingDayTarget: IdentifiesGroupHuntingDay,
                stringProvider: StringProvider):
            this(groupHuntingContext, huntingDayTarget, stringProvider, SystemDateTimeProvider())

    override suspend fun saveHuntingDay(): GroupHuntingDayUpdateResponse {
        val viewModel = viewModelLoadStatus.value.loadedViewModel
                ?: kotlin.run {
                    logger.w { "Failed to obtain loaded viewmodel for saving hunting day" }
                    return GroupHuntingDayUpdateResponse.Error
                }

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
                identifiesClubAndGroup = huntingDayTarget,
                allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to obtain group context in order to save hunting day" }
            return GroupHuntingDayUpdateResponse.Error
        }

        // the viewmodel hunting day may contain data that should not be sent
        // - it contains all the values added by the user
        //
        // -> strip the values that should not be sent
        val huntingDayToBeSaved = createHuntingDayToBeSaved(viewModel.huntingDay)

        return groupContext.updateHuntingDay(huntingDayToBeSaved, currentTimeProvider)
    }

    override fun getHuntingGroup(): HuntingGroup? {
        return groupHuntingContext.findHuntingGroupContext(
                identifiesClubAndGroup = huntingDayTarget
        )?.huntingGroup
    }

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ModifyGroupHuntingDayViewModel>> = flow {

        emit(ViewModelLoadStatus.Loading)

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
                identifiesClubAndGroup = huntingDayTarget,
                allowCached = true
        ) ?: kotlin.run {
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val fetchedHuntingDay = groupContext.fetchHuntingDay(huntingDayTarget, allowCached = !refresh)
        if (fetchedHuntingDay == null) {
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        // use restored hunting day values if hunting day id it matches the id of the found hunting day
        val huntingDay = restoredHuntingDay?.takeIf { it.id == fetchedHuntingDay.id }
                ?: fetchedHuntingDay

        emit(ViewModelLoadStatus.Loaded(
                viewModel = createViewModel(huntingDay, groupContext.huntingGroup)
        ))
    }

    companion object {
        private val logger by getLogger(EditGroupHuntingDayController::class)
    }
}
