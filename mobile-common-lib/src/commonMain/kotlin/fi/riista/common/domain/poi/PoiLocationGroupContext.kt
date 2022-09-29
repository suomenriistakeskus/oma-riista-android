package fi.riista.common.domain.poi

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.poi.model.PoiLocationGroup
import fi.riista.common.domain.poi.model.PoiLocationGroupId
import fi.riista.common.network.BackendApiProvider

class PoiLocationGroupContext internal constructor(
    backendApiProvider: BackendApiProvider,
    externalId: String,
): BackendApiProvider by backendApiProvider {

    private val _poiLocationGroupProvider = PoiLocationGroupFromNetworkOrDatabaseProvider(
        backendApiProvider = backendApiProvider,
        database = RiistaSDK.INSTANCE.database,
        externalId = externalId,
    )
    val poiLocationGroupProvider: PoiLocationGroupProvider = _poiLocationGroupProvider

    val poiLocationGroups: List<PoiLocationGroup>?
        get() = poiLocationGroupProvider.poiLocationGroups

    suspend fun fetch(refresh: Boolean = false) {
        poiLocationGroupProvider.fetch(refresh)
    }

    fun findPoiLocationGroup(poiLocationGroupId: PoiLocationGroupId): PoiLocationGroup? {
        return poiLocationGroups?.find { group -> group.id == poiLocationGroupId }
    }
}
