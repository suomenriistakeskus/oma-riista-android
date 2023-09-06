package fi.riista.common.network

internal class MockBackendAPIProvider(
    var backendAPIMock: BackendAPIMock
): BackendApiProvider {
    override val backendAPI: BackendAPI = backendAPIMock
}
