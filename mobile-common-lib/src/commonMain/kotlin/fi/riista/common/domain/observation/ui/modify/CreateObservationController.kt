package fi.riista.common.domain.observation.ui.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.model.CommonObservationData
import fi.riista.common.domain.userInfo.UserContext
import fi.riista.common.metadata.MetadataProvider
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


/**
 * A controller for creating [CommonObservation] data.
 */
class CreateObservationController(
    userContext: UserContext,
    metadataProvider: MetadataProvider,
    stringProvider: StringProvider,
    private val dateTimeProvider: LocalDateTimeProvider
) : ModifyObservationController(userContext, metadataProvider, stringProvider) {

    var initialSpeciesCode: SpeciesCode? = null

    constructor(
        userContext: UserContext,
        metadataProvider: MetadataProvider,
        stringProvider: StringProvider,
    ) : this(
        userContext = userContext,
        metadataProvider = metadataProvider,
        stringProvider = stringProvider,
        dateTimeProvider = SystemDateTimeProvider()
    )

    /**
     * Can the observation be moved to current user location?
     *
     * Observation is not allowed to be moved automatically if its location has been updated
     * manually by the user.
     */
    fun canMoveObservationToCurrentUserLocation(): Boolean {
        return observationLocationCanBeUpdatedAutomatically
    }

    /**
     * Tries to move the Observation to the current user location.
     *
     * Controller will only update the location of the Observation if user has not explicitly
     * set the location previously and if the given [location] is inside Finland.
     *
     * The return value indicates whether it might be possible to update Observation location to
     * current user location in the future.
     *
     * @return  True if Observation location can be changed in the future, false otherwise.
     */
    fun tryMoveObservationToCurrentUserLocation(location: ETRMSGeoLocation): Boolean {
        if (!location.isInsideFinland()) {
            // It is possible that the exact GPS location is not yet known and the attempted
            // location is outside of Finland. Don't prevent future updates because of this.
            return true
        }

        if (!canMoveObservationToCurrentUserLocation()) {
            return false
        }

        // don't allow automatic location updates unless the observation really is loaded
        // - otherwise we might get pending location updates which could become disallowed
        //   when the observation is being loaded
        if (getLoadedViewModelOrNull() == null) {
            return true
        }

        handleIntent(
            ModifyObservationIntent.ChangeLocation(
                location = location,
                locationChangedAfterUserInteraction = false
            )
        )
        return true
    }

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ModifyObservationViewModel>> = flow {
        val existingViewModel = getLoadedViewModelOrNull()
        if (existingViewModel != null) {
            return@flow
        }

        val viewModel = createViewModel(
            observation = restoredObservationData ?: createNewObservation(),
        ).applyPendingIntents()

        emit(ViewModelLoadStatus.Loaded(viewModel))
    }

    private fun createNewObservation(): CommonObservationData {
        return CommonObservationData(
            localId = null,
            localUrl = null,
            remoteId = null,
            revision = null,
            mobileClientRefId = generateMobileClientRefId(),
            observationSpecVersion = Constants.OBSERVATION_SPEC_VERSION,
            species = Species.Unknown,
            observationCategory = BackendEnum.create(null),
            observationType = BackendEnum.create(null),
            deerHuntingType = BackendEnum.create(null),
            deerHuntingOtherTypeDescription = null,
            location = CommonLocation.Unknown,
            pointOfTime = dateTimeProvider.now(),
            description = null,
            images = EntityImages.noImages(),
            totalSpecimenAmount = 1,
            specimens = listOf(CommonSpecimenData()),
            canEdit = true,
            mooselikeMaleAmount = null,
            mooselikeFemaleAmount = null,
            mooselikeFemale1CalfAmount = null,
            mooselikeFemale2CalfsAmount = null,
            mooselikeFemale3CalfsAmount = null,
            mooselikeFemale4CalfsAmount = null,
            mooselikeCalfAmount = null,
            mooselikeUnknownSpecimenAmount = null,
            observerName = null,
            observerPhoneNumber = null,
            officialAdditionalInfo = null,
            verifiedByCarnivoreAuthority = null,
            inYardDistanceToResidence = null,
            litter = null,
            pack = null,
        ).let { observationData ->
            initialSpeciesCode?.let { speciesCode ->
                observationData.changeSpecies(newSpecies = Species.Known(speciesCode))
            } ?: observationData
        }
    }
}

