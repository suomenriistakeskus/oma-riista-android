package fi.riista.common.domain.huntingControl.ui.hunterInfo

import fi.riista.common.RiistaSDK
import fi.riista.common.database.DatabaseDriverFactory
import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.domain.huntingControl.HuntingControlContext
import fi.riista.common.domain.huntingControl.ui.HuntingControlHunterInfoResponse
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.helpers.initializeMocked
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.LocalDate
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HuntingControlContextTest {

    @Test
    fun testFetchHunterInfo() = runBlockingTest {
        val dbDriverFactory = createDatabaseDriverFactory()
        val context = getHuntingControlContext(dbDriverFactory)
        val response = context.fetchHunterInfoByHunterNumber(MockHunterInfoData.HunterNumber)
        assertTrue(response is HuntingControlHunterInfoResponse.Success)
        val hunterInfo = response.hunter
        assertNotNull(hunterInfo)
        assertEquals("Pasi Puurtinen", hunterInfo.name)
        assertEquals(LocalDate(1911, 11, 11), hunterInfo.dateOfBirth)
        assertEquals("22222222", hunterInfo.hunterNumber)
        assertEquals("Nokia", hunterInfo.homeMunicipality?.fi)
        assertTrue(hunterInfo.huntingLicenseActive)
        assertEquals(LocalDate(2022, 6, 28), hunterInfo.huntingLicenseDateOfPayment)
    }

    private fun getHuntingControlContext(
        databaseDriverFactory: DatabaseDriverFactory,
        backendApi: BackendAPI = BackendAPIMock(),
    ): HuntingControlContext {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked()
        runBlocking {
            userContextProvider.userLoggedIn(MockUserInfo.parse(MockUserInfo.Pentti))
        }

        RiistaSDK.initializeMocked(
            databaseDriverFactory = databaseDriverFactory,
            mockBackendAPI = backendApi,
            mockCurrentUserContextProvider = userContextProvider,
        )

        return RiistaSDK.huntingControlContext
    }
}
