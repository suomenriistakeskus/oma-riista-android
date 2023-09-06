package fi.riista.common.domain.permit.metsahallitusPermit.ui.view

import fi.riista.common.RiistaSDK
import fi.riista.common.helpers.initializeMocked
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ViewMetsahallitusPermitControllerTest {
    @Test
    fun `view permit`() = runBlockingTest {
        RiistaSDK.initializeMocked(performLoginWithPentti = true)

        val controller = ViewMetsahallitusPermitController(
            permitIdentifier = "4949110101",
            usernameProvider = RiistaSDK.currentUserContext,
            permitProvider = RiistaSDK.metsahallitusPermits
        )

        assertEquals(ViewModelLoadStatus.NotLoaded, controller.viewModelLoadStatus.value)

        controller.loadViewModel()

        val viewModel = assertNotNull(controller.getLoadedViewModelOrNull())
        assertEquals("4949110101", viewModel.permit.permitIdentifier)
    }
}