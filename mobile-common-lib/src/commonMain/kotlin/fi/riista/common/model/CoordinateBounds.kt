package fi.riista.common.model

data class CoordinateBounds(
    val minCoordinate: Coordinate,
    val maxCoordinate: Coordinate,
) {
    val centerCoordinate: Coordinate by lazy {
        // naive implementation which does not work when longitudes are near -180/180
        Coordinate(
                latitude = (minCoordinate.latitude + maxCoordinate.latitude) / 2,
                longitude = (minCoordinate.longitude + maxCoordinate.longitude) / 2,
        )
    }
}
