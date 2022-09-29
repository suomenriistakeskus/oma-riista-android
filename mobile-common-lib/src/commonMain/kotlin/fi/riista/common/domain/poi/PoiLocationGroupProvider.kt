package fi.riista.common.domain.poi

import co.touchlab.stately.collections.IsoMutableList
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.poi.dto.PoiLocationGroupsDTO
import fi.riista.common.domain.poi.dto.toPoiLocationGroup
import fi.riista.common.domain.poi.model.PoiLocationGroup
import fi.riista.common.logging.Logger
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LoadStatus
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.NetworkDataFetcher
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import fi.riista.common.util.DataFetcher
import fi.riista.common.util.SequentialDataFetcher

interface PoiLocationGroupProvider: DataFetcher {
    val poiLocationGroups: List<PoiLocationGroup>?
}

internal class PoiLocationGroupFromNetworkOrDatabaseProvider(
    backendApiProvider: BackendApiProvider,
    database: RiistaDatabase,
    private val externalId: String,
) : PoiLocationGroupProvider,
    SequentialDataFetcher() {

    private val repository = PoiRepository(database)
    private val networkProvider = PoiLocationGroupFromNetworkProvider(
        backendApiProvider = backendApiProvider,
        externalId = externalId,
    )

    private var _poiLocationGroups = IsoMutableList<PoiLocationGroup>()
    override val poiLocationGroups: List<PoiLocationGroup>?
        get() {
            return if (_poiLocationGroups.isEmpty() && !loadStatus.value.loaded) {
                return null
            } else {
                _poiLocationGroups
            }
        }

    override suspend fun doFetch() {
        networkProvider.fetch(refresh = true)
        var newPoiLocationGroups = networkProvider.poiLocationGroups
        if (networkProvider.loadStatus.value.loaded && newPoiLocationGroups != null) {
            logger.d { "Successfully loaded from network -> write location groups to the database" }
            repository.replacePoiLocationGroups(externalId, newPoiLocationGroups)
            _poiLocationGroups.clear()
            _poiLocationGroups.addAll(newPoiLocationGroups)
            loadStatus.set(LoadStatus.Loaded())
        } else {
            logger.d { "Loading from network failed, try to load from DB" }
            newPoiLocationGroups = repository.getPoiLocationGroups(externalId)
            if (newPoiLocationGroups.isNotEmpty()) {
                logger.d { "Got data from DB" }
                _poiLocationGroups.clear()
                _poiLocationGroups.addAll(newPoiLocationGroups)
                loadStatus.set(LoadStatus.Loaded())
            } else {
                logger.d { "No data in DB, loading failed" }
                loadStatus.set(networkProvider.loadStatus.value)
            }
        }
    }

    override fun logger(): Logger = logger

    companion object {
        private val logger by getLogger(PoiLocationGroupFromNetworkOrDatabaseProvider::class)
    }
}

internal class PoiLocationGroupFromNetworkProvider(
    backendApiProvider: BackendApiProvider,
    private val externalId: String,
) : PoiLocationGroupProvider,
    NetworkDataFetcher<PoiLocationGroupsDTO>(),
    BackendApiProvider by backendApiProvider {

    private var _poiLocationGroups = IsoMutableList<PoiLocationGroup>()
    override val poiLocationGroups: List<PoiLocationGroup>?
        get() {
            return if (_poiLocationGroups.isEmpty() && !loadStatus.value.loaded) {
                return null
            } else {
                _poiLocationGroups
            }
        }

    override suspend fun fetchFromNetwork(): NetworkResponse<PoiLocationGroupsDTO> {
        return backendAPI.fetchPoiLocationGroups(externalId)
    }

    override fun handleSuccess(statusCode: Int, responseData: NetworkResponseData<out PoiLocationGroupsDTO>) {
        // Sort POI groups and locations by visible id
        val newPois = responseData.typed.map { poiLocationGroupDTO ->
            poiLocationGroupDTO.toPoiLocationGroup()
        }.map { poiLocationGroup ->
            poiLocationGroup.copy(locations = poiLocationGroup.locations.sortedBy { location -> location.visibleId })
        }
        _poiLocationGroups.clear()
        _poiLocationGroups.addAll(newPois.sortedBy { it.visibleId })
    }

    override fun handleError401() {
        _poiLocationGroups.clear()
    }

    override fun logger(): Logger = logger

    companion object {
        private val logger by getLogger(PoiLocationGroupFromNetworkProvider::class)
    }
}
