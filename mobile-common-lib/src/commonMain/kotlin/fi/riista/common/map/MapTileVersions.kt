package fi.riista.common.map

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.logging.getLogger
import fi.riista.common.util.deserializeFromJson

class MapTileVersions: MapTileVersionProvider {
    // key: tileType, value: tile version
    private val tileVersions: AtomicReference<Map<String, String>> = AtomicReference(mapOf())

    override fun getTileVersion(tileType: String?): String {
        if (tileType == null) {
            return ""
        }

        return tileVersions.value[tileType] ?: ""
    }

    fun parseMapTileVersions(versionsJson: String?) {
        if (versionsJson == null) {
            logger.d { "No versions json, cannot parse" }
            return
        }

        val versions: Map<String, String>? = versionsJson.deserializeFromJson()
        if (versions == null) {
            logger.w { "Failed to parse versions" }
            return
        }

        logger.v { "Applying tile versions: $versionsJson" }

        tileVersions.set(versions)
    }

    companion object {
        private val logger by getLogger(MapTileVersions::class)
    }
}
