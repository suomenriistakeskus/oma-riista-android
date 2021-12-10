package fi.riista.common.poi

import co.touchlab.stately.collections.IsoMutableMap
import fi.riista.common.network.BackendApiProvider

/**
 * Context for holding [PoiLocationGroupContext]s
 */
class PoiContext internal constructor(
    private val backendApiProvider: BackendApiProvider,
) {
    private val poiLocationGroupContexts = IsoMutableMap<String, PoiLocationGroupContext>()

    fun getPoiLocationGroupContext(externalId: String): PoiLocationGroupContext {
        return poiLocationGroupContexts.getOrPut(externalId) {
            PoiLocationGroupContext(backendApiProvider, externalId)
        }
    }
}
