package fi.riista.common.domain.huntingclub.ui

import fi.riista.common.RiistaSDK
import fi.riista.common.helpers.TestStringProvider
import fi.riista.common.helpers.initializeMocked
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.Language
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class HuntingClubControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = getController()
        assertSame(ViewModelLoadStatus.NotLoaded, controller.viewModelLoadStatus.value)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = getController()
        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
    }

    @Test
    fun testRefreshLoadsDataFromNetwork() = runBlockingTest {
        val backendAPI = BackendAPIMock()
        val controller = getController(backendAPIMock = backendAPI)
        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        assertEquals(1, backendAPI.callCount(BackendAPI::fetchHuntingClubMemberInvitations.name))
        controller.loadViewModel(refresh = true)
        assertEquals(2, backendAPI.callCount(BackendAPI::fetchHuntingClubMemberInvitations.name))
        controller.loadViewModel(refresh = false)
        assertEquals(2, backendAPI.callCount(BackendAPI::fetchHuntingClubMemberInvitations.name))
    }

    @Test
    fun testHuntingClubViewModelHasCorrectData() = runBlockingTest {
        val controller = getController()
        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        val viewModel = controller.viewModelLoadStatus.value.loadedViewModel
        assertEquals(4, viewModel!!.items.size)

        assertTrue(viewModel.items[0] is HuntingClubViewModel.Header)
        assertEquals("header_invitations".hashCode().toLong(), viewModel.items[0].id)

        assertTrue(viewModel.items[1] is HuntingClubViewModel.Invitation)
        val invitation = viewModel.items[1] as HuntingClubViewModel.Invitation
        assertEquals("invitation_12".hashCode().toLong(), invitation.id)
        assertEquals(12, invitation.invitationId)
        assertEquals("1007921", invitation.officialCode)
        assertEquals("Porrassalmen eräveikot ry", invitation.name)

        assertTrue(viewModel.items[2] is HuntingClubViewModel.Header)
        assertEquals("header_membership".hashCode().toLong(), viewModel.items[2].id)

        assertTrue(viewModel.items[3] is HuntingClubViewModel.HuntingClub)
        val membership = viewModel.items[3] as HuntingClubViewModel.HuntingClub
        assertEquals("membership_46".hashCode().toLong(), membership.id)
        assertEquals("123456", membership.officialCode)
        assertEquals("Nokian metsästysseura", membership.name)
    }

    private fun getController(
        backendAPIMock: BackendAPIMock = BackendAPIMock(),
    ): HuntingClubController {
        RiistaSDK.initializeMocked(
            mockBackendAPI = backendAPIMock,
            performLoginWithPentti = true
        )

        val userContext = RiistaSDK.currentUserContext

        return HuntingClubController(
            huntingClubsContext = userContext.huntingClubsContext,
            usernameProvider = userContext,
            huntingClubOccupationsProvider = RiistaSDK.huntingClubOccupations,
            languageProvider = getLanguageProvider(),
            stringProvider = getStringProvider(),
        )
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
