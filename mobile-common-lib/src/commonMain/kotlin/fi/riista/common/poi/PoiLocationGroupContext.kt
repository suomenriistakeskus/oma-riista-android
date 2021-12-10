package fi.riista.common.poi

import fi.riista.common.RiistaSDK
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.poi.model.PoiLocationGroup

class PoiLocationGroupContext internal constructor(
    backendApiProvider: BackendApiProvider,
    externalId: String,
): BackendApiProvider by backendApiProvider {

    private val _poiLocationGroupProvider = PoiLocationGroupFromNetworkOrDatabaseProvider(
        backendApiProvider = backendApiProvider,
        databaseDriverFactory = RiistaSDK.INSTANCE.databaseDriverFactory,
        externalId = externalId,
    )
    val poiLocationGroupProvider: PoiLocationGroupProvider = _poiLocationGroupProvider

    val poiLocationGroups: List<PoiLocationGroup>?
        get() = poiLocationGroupProvider.poiLocationGroups

    suspend fun fetch(refresh: Boolean = false) {
        poiLocationGroupProvider.fetch(refresh)
    }
}
