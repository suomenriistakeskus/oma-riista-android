package fi.riista.common.domain.huntingControl.ui.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.huntingControl.HuntingControlContext
import fi.riista.common.domain.huntingControl.HuntingControlRhyContext
import fi.riista.common.domain.huntingControl.model.HuntingControlEventData
import fi.riista.common.domain.huntingControl.model.HuntingControlEventStatus
import fi.riista.common.domain.huntingControl.model.HuntingControlGameWarden
import fi.riista.common.domain.huntingControl.model.HuntingControlRhyTarget
import fi.riista.common.domain.huntingControl.model.toHuntingControlEventInspector
import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.userInfo.UserContext
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.isInsideFinland
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.SystemDateTimeProvider
import fi.riista.common.util.generateMobileClientRefId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CreateHuntingControlEventController(
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val huntingControlRhyTarget: HuntingControlRhyTarget,
    stringProvider: StringProvider,
    huntingControlContext: HuntingControlContext,
    commonFileProvider: CommonFileProvider,
    userContext: UserContext,
) : ModifyHuntingControlEventController(stringProvider, huntingControlContext, userContext, commonFileProvider, huntingControlRhyTarget) {

    constructor(
        huntingControlContext: HuntingControlContext,
        huntingControlRhyTarget: HuntingControlRhyTarget,
        stringProvider: StringProvider,
        commonFileProvider: CommonFileProvider,
        userContext: UserContext,
    ) : this(SystemDateTimeProvider(), huntingControlRhyTarget, stringProvider, huntingControlContext, commonFileProvider, userContext)

    override fun createLoadViewModelFlow(
        refresh: Boolean,
    ): Flow<ViewModelLoadStatus<ModifyHuntingControlEventViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        val rhyContext = huntingControlContext.findRhyContext(
            identifiesRhy = huntingControlRhyTarget,
        ) ?: kotlin.run {
            logger.w { "Failed to fetch the RHY (id: ${huntingControlRhyTarget.rhyId})" }
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val gameWardens = rhyContext.fetchGameWardens()

        val event = restoredEvent ?: createEmptyHuntingControlEvent(rhyContext, gameWardens)

        val viewModel = createViewModel(
            event = event,
            allGameWardens = gameWardens,
            selfInspectorWarning = false,
        ).applyPendingIntents()

        emit(ViewModelLoadStatus.Loaded(viewModel))
    }

    fun canMoveEventToCurrentUserLocation(): Boolean {
        return eventLocationCanBeMovedAutomatically
    }

    fun tryMoveEventToCurrentUserLocation(location: ETRMSGeoLocation): Boolean {
        if (!location.isInsideFinland()) {
            // It is possible that the exact GPS location is not yet known and the attempted
            // location is outside of Finland. Don't prevent future updates because of this.
            return true
        }

        if (!canMoveEventToCurrentUserLocation()) {
            return false
        }

        // don't allow automatic location updates unless the hunting control event really is loaded
        // - otherwise we might get pending location updates which could become disallowed
        //   when the harvest is being loaded
        if (getLoadedViewModelOrNull() == null) {
            return true
        }

        handleIntent(
            ModifyHuntingControlEventIntent.ChangeLocation(
                newLocation = location,
                locationChangedAfterUserInteraction = false
            )
        )
        return true
    }

    private fun createEmptyHuntingControlEvent(
        rhyContext: HuntingControlRhyContext,
        gameWardens: List<HuntingControlGameWarden>,
    ): HuntingControlEventData {
        val userId = userContext.userInformation?.id
        val userAsGameWarden = userId?.let { gameWardens.firstOrNull { it.remoteId == userId } }
        return HuntingControlEventData(
            localId = null,
            remoteId = null,
            specVersion = Constants.HUNTING_CONTROL_EVENT_SPEC_VERSION,
            mobileClientRefId = generateMobileClientRefId(),
            rhyId = rhyContext.rhyId,
            eventType = BackendEnum.create(null),
            status = BackendEnum.create(HuntingControlEventStatus.PROPOSED),
            inspectors = listOfNotNull(userAsGameWarden?.toHuntingControlEventInspector()),
            cooperationTypes = listOf(),
            otherParticipants = null,
            location = CommonLocation.Unknown,
            date = localDateTimeProvider.now().date,
            startTime = null,
            endTime = null,
            wolfTerritory = null,
            description = null,
            locationDescription = null,
            proofOrderCount = null,
            customerCount = null,
            canEdit = true,
            modified = true,
            attachments = listOf(),
        )
    }

    companion object {
        private val logger by getLogger(CreateHuntingControlEventController::class)
    }
}
