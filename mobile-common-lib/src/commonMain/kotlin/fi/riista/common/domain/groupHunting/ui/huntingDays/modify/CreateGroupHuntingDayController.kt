package fi.riista.common.domain.groupHunting.ui.huntingDays.modify

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.GroupHuntingDayUpdateResponse
import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LocalDate
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.intent.IntentHandler
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.SystemDateTimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CreateGroupHuntingDayController internal constructor(
    private val groupHuntingContext: GroupHuntingContext,
    private val huntingGroupTarget: IdentifiesHuntingGroup,
    stringProvider: StringProvider,
    currentTimeProvider: LocalDateTimeProvider,
) : ModifyGroupHuntingDayController(currentTimeProvider, stringProvider, startDateIsReadonly = false),
    IntentHandler<ModifyGroupHuntingDayIntent> {

    var preferredDate: LocalDate? = null

    constructor(groupHuntingContext: GroupHuntingContext,
                huntingGroupTarget: IdentifiesHuntingGroup,
                stringProvider: StringProvider):
            this(groupHuntingContext, huntingGroupTarget, stringProvider, SystemDateTimeProvider())


    override suspend fun saveHuntingDay(): GroupHuntingDayUpdateResponse {
        val viewModel = viewModelLoadStatus.value.loadedViewModel
                ?: kotlin.run {
                    logger.w { "Failed to obtain loaded viewmodel for saving hunting day" }
                    return GroupHuntingDayUpdateResponse.Error
                }

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
                identifiesClubAndGroup = huntingGroupTarget,
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

        return groupContext.createHuntingDay(huntingDayToBeSaved, currentTimeProvider)
    }

    override fun getHuntingGroup(): HuntingGroup? {
        return groupHuntingContext.findHuntingGroupContext(
                identifiesClubAndGroup = huntingGroupTarget
        )?.huntingGroup
    }

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ModifyGroupHuntingDayViewModel>> = flow {

        emit(ViewModelLoadStatus.Loading)

        val huntingGroupContext = groupHuntingContext.fetchHuntingGroupContext(
                identifiesClubAndGroup = huntingGroupTarget,
                allowCached = true
        ) ?: run {
            logger.w { "Failed to obtain HuntingGroup!" }
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val huntingDay = restoredHuntingDay ?: createNewHuntingDay(
                permit = huntingGroupContext.huntingGroup.permit,
                speciesCode = huntingGroupContext.huntingGroup.speciesCode,
                preferredDate = preferredDate
        )

        emit(ViewModelLoadStatus.Loaded(
                viewModel = createViewModel(huntingDay, huntingGroupContext.huntingGroup)
        ))
    }

    private fun createNewHuntingDay(
        permit: HuntingGroupPermit,
        speciesCode: SpeciesCode,
        preferredDate: LocalDate?,
    ): GroupHuntingDay {
        val now = currentTimeProvider.now()
        val selectedDate = (preferredDate ?: now.date).coerceInPermitValidityPeriods(permit)

        return selectedDate.toLocalHuntingDay(
            huntingGroupId = huntingGroupTarget.huntingGroupId,
            speciesCode = speciesCode,
            now = now,
        )
    }

    companion object {
        private val logger by getLogger(CreateGroupHuntingDayController::class)
    }
}
