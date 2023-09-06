package fi.riista.common.domain.poi

import fi.riista.common.network.BackendApiProvider

/**
 * Context for holding [PoiLocationGroupContext]s
 */
class PoiContext internal constructor(
    private val backendApiProvider: BackendApiProvider,
) {
    private val poiLocationGroupContexts = mutableMapOf<String, PoiLocationGroupContext>()

    fun getPoiLocationGroupContext(externalId: String): PoiLocationGroupContext {
        return poiLocationGroupContexts.getOrPut(externalId) {
            PoiLocationGroupContext(backendApiProvider, externalId)
        }
    }
}
