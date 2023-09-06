package fi.riista.common.domain.observation

import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.sync.ObservationSynchronizationContext
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.domain.userInfo.LoginStatus
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.sync.delegated
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider

class ObservationContext internal constructor(
    backendApiProvider: BackendApiProvider,
    preferences: Preferences,
    localDateTimeProvider: LocalDateTimeProvider,
    commonFileProvider: CommonFileProvider,
    database: RiistaDatabase,
    private val currentUserContextProvider: CurrentUserContextProvider,
) : BackendApiProvider by backendApiProvider {

    internal constructor(
        backendApiProvider: BackendApiProvider,
        preferences: Preferences,
        localDateTimeProvider: LocalDateTimeProvider,
        commonFileProvider: CommonFileProvider,
        currentUserContextProvider: CurrentUserContextProvider,
    ): this(
        backendApiProvider = backendApiProvider,
        preferences = preferences,
        localDateTimeProvider = localDateTimeProvider,
        commonFileProvider = commonFileProvider,
        database = RiistaSDK.INSTANCE.database,
        currentUserContextProvider = currentUserContextProvider,
    )

    // internal so that it can be accessed from tests
    internal val repository = ObservationRepository(database)

    private val synchronizationContext = ObservationSynchronizationContext(
        backendApiProvider = backendApiProvider,
        database = database,
        preferences = preferences,
        localDateTimeProvider = localDateTimeProvider,
        commonFileProvider = commonFileProvider,
        currentUserContextProvider = currentUserContextProvider
    ).delegated(onSyncFinished = ::syncFinished)

    private val _observationProvider = ObservationFromDatabaseProvider(
        database = database,
        currentUserContextProvider = currentUserContextProvider,
    )
    val observationProvider: ObservationProvider = _observationProvider

    private val observationUpdater: ObservationUpdater = ObservationDatabaseUpdater(
        database = database,
        currentUserContextProvider = currentUserContextProvider,
    )

    fun initialize() {
        RiistaSDK.registerSynchronizationContext(synchronizationContext)
        clearWhenUserLoggedOut()
    }

    suspend fun saveObservation(observation: CommonObservation): ObservationOperationResponse {
        val response = observationUpdater.saveObservation(observation)
        when (response) {
            is ObservationOperationResponse.Error,
            is ObservationOperationResponse.SaveFailure,
            is ObservationOperationResponse.NetworkFailure -> logger.w { "Observation save failed: $response" }
            is ObservationOperationResponse.Success -> _observationProvider.forceRefreshOnNextFetch()
        }
        return response
    }

    suspend fun deleteObservation(observationLocalId: Long?): CommonObservation? {
        val deletedObservation = repository.markDeleted(observationLocalId = observationLocalId)
        if (deletedObservation != null) {
            _observationProvider.forceRefreshOnNextFetch()
        }

        return deletedObservation
    }

    internal suspend fun sendObservationToBackend(observation: CommonObservation): ObservationOperationResponse {
        return synchronizationContext.childContext.sendObservationToBackend(observation)
    }

    internal suspend fun deleteObservationInBackend(observation: CommonObservation) {
        synchronizationContext.childContext.deleteObservationInBackend(observation)
    }

    /**
     * Get all hunting years that have observations. List is not sorted.
     */
    fun getObservationHuntingYears(): List<Int> {
        return when (val user = currentUserContextProvider.userContext.username) {
            null -> emptyList()
            else -> repository.getObservationHuntingYears(user)
        }
    }

    suspend fun getLocalObservationImageIds(): List<String> {
        currentUserContextProvider.userContext.username?.let { username ->
            return repository.getObservationsWithLocalImages(username = username).map { event ->
                event.images.localImages
                    .filter { image ->
                        image.status == EntityImage.Status.LOCAL || image.status == EntityImage.Status.UPLOADED
                    }
                    .mapNotNull { image -> image.serverId }
            }.flatten()
        } ?: return emptyList()
    }

    fun getLatestObservationSpecies(size: Int): List<Species> {
        currentUserContextProvider.userContext.username?.let { username ->
            return repository.getLatestObservationSpecies(username = username, size = size)
        } ?: return emptyList()
    }

    fun getByLocalId(localId: Long): CommonObservation? {
        return currentUserContextProvider.userContext.username?.let {
            repository.getByLocalId(localId)
        }
    }

    private fun clearWhenUserLoggedOut() {
        currentUserContextProvider.userContext.loginStatus.bindAndNotify {  loginStatus ->
            if (loginStatus is LoginStatus.NotLoggedIn) {
                clear()
            }
        }
    }

    private fun clear() {
        _observationProvider.clear()
    }

    private fun syncFinished() {
        _observationProvider.forceRefreshOnNextFetch()
    }

    companion object {
        private val logger by getLogger(ObservationContext::class)
    }
}
