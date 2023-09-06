package fi.riista.common.domain.huntingControl.ui.hunterInfo

import fi.riista.common.RiistaSDK
import fi.riista.common.database.DatabaseDriverFactory
import fi.riista.common.domain.dto.MockUserInfo
import fi.riista.common.domain.huntingControl.HuntingControlContext
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.helpers.TestStringProvider
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.helpers.getButtonField
import fi.riista.common.helpers.getCustomField
import fi.riista.common.helpers.getIntField
import fi.riista.common.helpers.getLabelField
import fi.riista.common.helpers.getStringField
import fi.riista.common.helpers.initializeMocked
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.Language
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.Padding
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HunterInfoControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = HunterInfoController(
            huntingControlContext = getHuntingControlContext(),
            languageProvider = getLanguageProvider(),
            stringProvider = getStringProvider(),
        )

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = HunterInfoController(
            huntingControlContext = getHuntingControlContext(),
            languageProvider = getLanguageProvider(),
            stringProvider = getStringProvider(),
        )
        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        assertEquals(HunterSearch.Status.ENTERING_HUNTER_NUMBER, viewModel.hunterSearch.status)
        val searchTerm = viewModel.hunterSearch.searchTerm
        assertTrue(searchTerm is SearchTerm.SearchableHunterNumber)
        assertEquals("", searchTerm.hunterNumber)
        assertNull(viewModel.hunterInfo)
    }

    @Test
    fun testProducedFieldsMatchData() = runBlockingTest {
        val controller = HunterInfoController(
            huntingControlContext = getHuntingControlContext(),
            languageProvider = getLanguageProvider(),
            stringProvider = getStringProvider(),
        )
        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        controller.eventDispatcher.dispatchHunterNumber("88888888")
        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)
        assertEquals(HunterSearch.Status.VALID_SEARCH_TERM_ENTERED, viewModel.hunterSearch.status)

        val updatedViewModel = RiistaSDK.mainScopeProvider.scope.async {
            controller.getLoadedViewModelOrNull()
        }.await()
        assertNotNull(updatedViewModel)
        assertEquals(HunterSearch.Status.HUNTER_FOUND, updatedViewModel.hunterSearch.status)

        val fields = updatedViewModel.fields
        assertEquals(18, fields.size)
        var expectedIndex = 0
        fields.getCustomField(expectedIndex++, HunterInfoField.Type.SCAN_QR_CODE.toField()).let {
            assertEquals(Padding.SMALL, it.settings.paddingTop)
            assertEquals(Padding.SMALL, it.settings.paddingBottom)
        }
        fields.getIntField(expectedIndex++, HunterInfoField.Type.ENTER_HUNTER_NUMBER.toField()).let {
            assertEquals(88888888, it.value)
            assertEquals("hunter_id", it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getLabelField(expectedIndex++, HunterInfoField.Type.PERSONAL_DATA_HEADER.toField()).let {
            assertEquals("hunter_details", it.text)
            assertFalse(it.settings.allCaps)
            assertNull(it.settings.captionIcon)
        }
        fields.getStringField(expectedIndex++, HunterInfoField.Type.NAME.toField()).let {
            assertEquals("Pasi Puurtinen", it.value)
            assertEquals("hunter_name", it.settings.label)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
        }
        fields.getStringField(expectedIndex++, HunterInfoField.Type.DATE_OF_BIRTH.toField()).let {
            assertEquals("11.11.1911", it.value)
            assertEquals("date_of_birth", it.settings.label)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
        }
        fields.getStringField(expectedIndex++, HunterInfoField.Type.HOME_MUNICIPALITY.toField()).let {
            assertEquals("Nokia", it.value)
            assertEquals("home_municipality", it.settings.label)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
        }
        fields.getStringField(expectedIndex++, HunterInfoField.Type.HUNTER_NUMBER.toField()).let {
            assertEquals("22222222", it.value)
            assertEquals("hunter_id", it.settings.label)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
        }
        fields.getLabelField(expectedIndex++, HunterInfoField.Type.HUNTING_LICENSE_HEADER.toField()).let {
            assertEquals("hunting_license", it.text)
            assertFalse(it.settings.allCaps)
            assertEquals(LabelField.Icon.VERIFIED, it.settings.captionIcon)
        }
        fields.getStringField(expectedIndex++, HunterInfoField.Type.HUNTING_LICENSE_STATUS.toField()).let {
            assertEquals("hunting_license_status_active", it.value)
            assertEquals("hunting_license_status", it.settings.label)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
        }
        fields.getStringField(expectedIndex++, HunterInfoField.Type.HUNTING_LICENSE_DAY_OF_PAYMENT.toField()).let {
            assertEquals("28.6.2022", it.value)
            assertEquals("hunting_license_date_of_payment", it.settings.label)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.singleLine)
        }
        fields.getLabelField(expectedIndex++, HunterInfoField.Type.SHOOTING_TEST_HEADER.toField()).let {
            assertEquals("shooting_tests", it.text)
            assertFalse(it.settings.allCaps)
            assertNull(it.settings.captionIcon)
        }
        fields.getLabelField(expectedIndex++, HunterInfoField.Type.SPECIES_CAPTION.toField(index = 0)).let {
            assertEquals("bear", it.text)
            assertFalse(it.settings.allCaps)
            assertNull(it.settings.captionIcon)
        }
        fields.getLabelField(expectedIndex++, HunterInfoField.Type.SHOOTING_TEST_INFO.toField(index = 0)).let {
            assertEquals("Tampereen riistanhoitoyhdistys\n30.9.2019 - 30.9.2022", it.text)
            assertFalse(it.settings.allCaps)
            assertNull(it.settings.captionIcon)
        }
        fields.getLabelField(expectedIndex++, HunterInfoField.Type.SPECIES_CAPTION.toField(index = 1)).let {
            assertEquals("moose", it.text)
            assertFalse(it.settings.allCaps)
            assertNull(it.settings.captionIcon)
        }
        fields.getLabelField(expectedIndex++, HunterInfoField.Type.SHOOTING_TEST_INFO.toField(index = 1)).let {
            assertEquals("Tampereen riistanhoitoyhdistys\n13.5.2019 - 13.5.2022", it.text)
            assertFalse(it.settings.allCaps)
            assertNull(it.settings.captionIcon)
        }
        fields.getLabelField(expectedIndex++, HunterInfoField.Type.SPECIES_CAPTION.toField(index = 2)).let {
            assertEquals("deer", it.text)
            assertFalse(it.settings.allCaps)
            assertNull(it.settings.captionIcon)
        }
        fields.getLabelField(expectedIndex++, HunterInfoField.Type.SHOOTING_TEST_INFO.toField(index = 2)).let {
            assertEquals("Tampereen riistanhoitoyhdistys\n10.1.2018 - 10.1.2021", it.text)
            assertFalse(it.settings.allCaps)
            assertNull(it.settings.captionIcon)
        }
        fields.getButtonField(expectedIndex, HunterInfoField.Type.RESET_BUTTON.toField()).let {
            assertEquals("reset", it.text)
            assertEquals(Padding.MEDIUM, it.settings.paddingTop)
            assertEquals(Padding.SMALL, it.settings.paddingBottom)
        }
    }

    private fun getHuntingControlContext(
        databaseDriverFactory: DatabaseDriverFactory = createDatabaseDriverFactory(),
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

    private fun getLanguageProvider(): LanguageProvider {
        return object : LanguageProvider {
            override fun getCurrentLanguage(): Language {
                return Language.FI
            }
        }
    }

    private fun getStringProvider(): StringProvider = TestStringProvider.INSTANCE
}
