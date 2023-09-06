package fi.riista.common.domain.harvest

import fi.riista.common.RiistaSDK
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.dto.PersonWithHunterNumberDTO
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.sync.HarvestSynchronizationContext
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.HunterNumber
import fi.riista.common.domain.model.PersonWithHunterNumber
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.domain.userInfo.LoginStatus
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.logging.getLogger
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.sync.delegated
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider

class HarvestContext internal constructor(
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

    internal val repository = HarvestRepository(database)

    private val synchronizationContext = HarvestSynchronizationContext(
        backendApiProvider = backendApiProvider,
        database = database,
        preferences = preferences,
        localDateTimeProvider = localDateTimeProvider,
        commonFileProvider = commonFileProvider,
        currentUserContextProvider = currentUserContextProvider,
    ).delegated(onSyncFinished = ::syncFinished)

    private val _harvestProvider = HarvestFromDatabaseProvider(
        database = database,
        currentUserContextProvider = currentUserContextProvider,
    )
    val harvestProvider: HarvestProvider = _harvestProvider

    private val harvestUpdater: HarvestUpdater = HarvestDatabaseUpdater(
        database = database,
        currentUserContextProvider = currentUserContextProvider,
    )

    fun initialize() {
        RiistaSDK.registerSynchronizationContext(synchronizationContext)
        clearWhenUserLoggedOut()
    }

    suspend fun saveHarvest(harvest: CommonHarvest): HarvestOperationResponse {
        val response = harvestUpdater.saveHarvest(harvest)
        when (response) {
            is HarvestOperationResponse.Error,
            is HarvestOperationResponse.SaveFailure,
            is HarvestOperationResponse.NetworkFailure -> logger.w { "Harvest save failed: $response" }
            is HarvestOperationResponse.Success -> _harvestProvider.forceRefreshOnNextFetch()
        }
        return response
    }

    suspend fun deleteHarvest(harvestLocalId: Long?): CommonHarvest? {
        val deletedHarvest = repository.markDeleted(harvestLocalId = harvestLocalId)
        if (deletedHarvest != null) {
            _harvestProvider.forceRefreshOnNextFetch()
        }

        return deletedHarvest
    }

    internal suspend fun sendHarvestToBackend(harvest: CommonHarvest): HarvestOperationResponse {
        return synchronizationContext.childContext.sendHarvestToBackend(harvest)
    }

    internal suspend fun deleteHarvestInBackend(harvest: CommonHarvest) {
        synchronizationContext.childContext.deleteHarvestInBackend(harvest)
    }

    internal fun getShooters(): List<PersonWithHunterNumber> {
        return when (val user = currentUserContextProvider.userContext.username) {
            null -> emptyList()
            else -> repository.getShooters(user)
        }
    }

    internal suspend fun searchPersonByHunterNumber(hunterNumber: HunterNumber): PersonWithHunterNumberDTO? {
        val response = backendAPI.searchPersonByHunterNumber(hunterNumber)
        return response.transformSuccessData { _, responseDTO ->
            responseDTO.typed
        }
    }

    /**
     * Get all hunting years that have harvests. List is not sorted.
     */
    fun getHarvestHuntingYears(): List<Int> {
        return when (val user = currentUserContextProvider.userContext.username) {
            null -> emptyList()
            else -> repository.getHarvestHuntingYears(user)
        }
    }

    fun getLatestHarvestSpecies(size: Int): List<Species> {
        currentUserContextProvider.userContext.username?.let { username ->
            return repository.getLatestHarvestSpecies(username = username, size = size)
        } ?: return emptyList()
    }

    private fun clearWhenUserLoggedOut() {
        currentUserContextProvider.userContext.loginStatus.bindAndNotify {  loginStatus ->
            if (loginStatus is LoginStatus.NotLoggedIn) {
                clear()
            }
        }
    }

    suspend fun getLocalHarvestImageIds(): List<String> {
        currentUserContextProvider.userContext.username?.let { username ->
            return repository.getHarvestsWithLocalImages(username = username).map { event ->
                event.images.localImages
                    .filter { image ->
                        image.status == EntityImage.Status.LOCAL || image.status == EntityImage.Status.UPLOADED
                    }
                    .mapNotNull { image -> image.serverId }
            }.flatten()
        } ?: return emptyList()
    }

    suspend fun getByLocalId(localId: Long): CommonHarvest? {
        return currentUserContextProvider.userContext.username?.let {
            repository.getByLocalId(localId)
        }
    }

    private fun clear() {
        _harvestProvider.clear()
    }

    private fun syncFinished() {
        _harvestProvider.forceRefreshOnNextFetch()
    }

    companion object {
        private val logger by getLogger(HarvestContext::class)
    }
}
