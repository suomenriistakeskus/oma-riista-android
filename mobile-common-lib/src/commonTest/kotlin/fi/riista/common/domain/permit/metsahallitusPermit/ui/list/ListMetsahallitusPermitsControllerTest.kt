package fi.riista.common.domain.permit.metsahallitusPermit.ui.list

import fi.riista.common.RiistaSDK
import fi.riista.common.helpers.initializeMocked
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ListMetsahallitusPermitsControllerTest {
    @Test
    fun `list permits`() = runBlockingTest {
        RiistaSDK.initializeMocked(performLoginWithPentti = true)

        val controller = ListMetsahallitusPermitsController(
            usernameProvider = RiistaSDK.currentUserContext,
            permitProvider = RiistaSDK.metsahallitusPermits
        )

        assertEquals(ViewModelLoadStatus.NotLoaded, controller.viewModelLoadStatus.value)

        controller.loadViewModel()

        val viewModel = assertNotNull(controller.getLoadedViewModelOrNull())
        assertEquals(1, viewModel.permits.size)
        assertEquals("4949110101", viewModel.permits.first().permitIdentifier)
    }
}