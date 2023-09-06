package fi.riista.common.map

interface MapTileVersionProvider {
    /**
     * Gets the tile type version for the given [tileType]. Should return an empty string
     * if default version is to be used.
     */
    fun getTileVersion(tileType: String?): String
}
