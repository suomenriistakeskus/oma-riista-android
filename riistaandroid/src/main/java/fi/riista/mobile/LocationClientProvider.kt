package fi.riista.mobile

/**
 * An interface for classes that are able to provide a [LocationClient]
 */
interface LocationClientProvider {
    val locationClient: LocationClient
}