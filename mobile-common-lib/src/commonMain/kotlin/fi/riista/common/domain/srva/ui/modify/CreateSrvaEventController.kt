package fi.riista.common.domain.srva.ui.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.model.CommonSrvaEventData
import fi.riista.common.domain.srva.model.SrvaEventState
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.isInsideFinland
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.SystemDateTimeProvider
import fi.riista.common.util.generateMobileClientRefId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A controller for creating [CommonSrvaEvent] data.
 */
class CreateSrvaEventController(
    metadataProvider: MetadataProvider,
    stringProvider: StringProvider,
    private val dateTimeProvider: LocalDateTimeProvider
) : ModifySrvaEventController(metadataProvider, stringProvider) {

    constructor(
        metadataProvider: MetadataProvider,
        stringProvider: StringProvider,
    ) : this(
        metadataProvider = metadataProvider,
        stringProvider = stringProvider,
        dateTimeProvider = SystemDateTimeProvider()
    )

    /**
     * Can the SRVA event be moved to current user location?
     *
     * SRVA event is not allowed to be moved automatically if its location has been updated
     * manually by the user.
     */
    fun canMoveSrvaEventToCurrentUserLocation(): Boolean {
        return srvaEventLocationCanBeUpdatedAutomatically
    }

    /**
     * Tries to move the SRVA event to the current user location.
     *
     * Controller will only update the location of the SRVA event if user has not explicitly
     * set the location previously and if the given [location] is inside Finland.
     *
     * The return value indicates whether it might be possible to update SRVA event location to
     * current user location in the future.
     *
     * @return  True if SRVA event location can be changed in the future, false otherwise.
     */
    fun tryMoveSrvaEventToCurrentUserLocation(location: ETRMSGeoLocation): Boolean {
        if (!location.isInsideFinland()) {
            // It is possible that the exact GPS location is not yet known and the attempted
            // location is outside of Finland. Don't prevent future updates because of this.
            return true
        }

        if (!canMoveSrvaEventToCurrentUserLocation()) {
            return false
        }

        // don't allow automatic location updates unless the SRVA event really is loaded
        // - otherwise we might get pending location updates which could become disallowed
        //   when the SRVA event is being loaded
        if (getLoadedViewModelOrNull() == null) {
            return true
        }

        handleIntent(
            ModifySrvaEventIntent.ChangeLocation(
                location = location,
                locationChangedAfterUserInteraction = false
            )
        )
        return true
    }

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ModifySrvaEventViewModel>> = flow {
        val existingViewModel = getLoadedViewModelOrNull()
        if (existingViewModel != null) {
            return@flow
        }

        val viewModel = createViewModel(
            srvaEvent = restoredSrvaEventData ?: createNewSrvaEvent(),
        ).applyPendingIntents()

        emit(ViewModelLoadStatus.Loaded(viewModel))
    }

    private fun createNewSrvaEvent(): CommonSrvaEventData {
        return CommonSrvaEventData(
            localId = null,
            localUrl = null,
            remoteId = null,
            revision = null,
            mobileClientRefId = generateMobileClientRefId(),
            srvaSpecVersion = Constants.SRVA_SPEC_VERSION,
            state = SrvaEventState.UNFINISHED.toBackendEnum(),
            rhyId = null,
            canEdit = true,
            location = CommonLocation.Unknown,
            pointOfTime = dateTimeProvider.now(),
            author = null,
            approver = null,
            species = Species.Unknown,
            otherSpeciesDescription = null,
            specimenAmount = 1,
            specimens = listOf(CommonSpecimenData()),
            eventCategory = BackendEnum.create(null),
            deportationOrderNumber = null,
            eventType = BackendEnum.create(null),
            otherEventTypeDescription = null,
            eventTypeDetail = BackendEnum.create(null),
            otherEventTypeDetailDescription = null,
            eventResult = BackendEnum.create(null),
            eventResultDetail = BackendEnum.create(null),
            methods = listOf(),
            otherMethodDescription = null,
            personCount = null,
            hoursSpent = null,
            description = null,
            images = EntityImages.noImages(),
        )
    }
}

