package fi.riista.common.helpers

import fi.riista.common.RiistaSDK
import fi.riista.common.RiistaSdkConfiguration
import fi.riista.common.RiistaSdkImpl
import fi.riista.common.database.DatabaseDriverFactory
import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.io.CommonFileProviderMock
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.MockDateTimeProvider
import fi.riista.common.util.coroutines.MainScopeProvider
import kotlinx.coroutines.runBlocking

internal fun RiistaSDK.initializeMocked(
    sdkConfiguration: RiistaSdkConfiguration = RiistaSdkConfiguration(
        "1",
        "2",
        "https://oma.riista.fi",
        TestCrashlyticsLogger
    ),
    databaseDriverFactory: DatabaseDriverFactory = createDatabaseDriverFactory(),
    mockBackendAPI: BackendAPI = BackendAPIMock(),
    mockPreferences: Preferences = MockPreferences(),
    mockCurrentUserContextProvider: CurrentUserContextProvider = CurrentUserContextProviderFactory.createMocked(
        backendAPI = mockBackendAPI,
        preferences = mockPreferences,
    ),
    mockLocalDateTimeProvider: LocalDateTimeProvider = MockDateTimeProvider(),
    mockMainScopeProvider: MainScopeProvider = MockMainScopeProvider(),
    mockFileProvider: CommonFileProvider = CommonFileProviderMock(),
    // should the user be logged in or not?
    performLoginWithPentti: Boolean = false
) {
    val instance = RiistaSdkImpl(
        sdkConfiguration = sdkConfiguration,
        databaseDriverFactory = databaseDriverFactory,
        mockBackendAPI = mockBackendAPI,
        mockCurrentUserContextProvider = mockCurrentUserContextProvider,
        mockLocalDateTimeProvider = mockLocalDateTimeProvider,
        mockMainScopeProvider = mockMainScopeProvider,
        mockFileProvider = mockFileProvider,
        mockPreferences = mockPreferences,
    )

    if (performLoginWithPentti) {
        runBlocking {
            mockCurrentUserContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }
    }

    initializeMockInstance(instance)
}
