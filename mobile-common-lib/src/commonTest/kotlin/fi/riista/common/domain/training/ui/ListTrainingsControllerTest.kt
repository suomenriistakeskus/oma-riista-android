package fi.riista.common.domain.training.ui

import fi.riista.common.helpers.TestStringProvider
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.LocalDate
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.domain.training.TrainingContext
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ListTrainingsControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = getController()
        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = getController()
        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        assertEquals(2, viewModel.trainings.size)
    }

    @Test
    fun testListTrainingsViewModelHasCorrectData() = runBlockingTest {
        val controller = getController()
        controller.loadViewModel()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)
        val occupationTraining = viewModel.trainings[0]
        assertTrue(occupationTraining is TrainingViewModel.OccupationTraining)
        assertEquals("training_type_sahkoinen", occupationTraining.trainingType)
        assertEquals("occupation_type_petoyhdyshenkilo", occupationTraining.occupationType)
        assertEquals(LocalDate(2022, 1, 12), viewModel.trainings[0].date)

        val jhtTraining = viewModel.trainings[1]
        assertTrue(jhtTraining is TrainingViewModel.JhtTraining)
        assertEquals("training_type_lahi", jhtTraining.trainingType)
        assertEquals("occupation_type_ampumakokeen_vastaanottaja", jhtTraining.occupationType)
        assertEquals(LocalDate(2021, 10, 21), jhtTraining.date)
        assertEquals("Kokkola", jhtTraining.location)
    }

    private fun getTrainingContext(backendAPI: BackendAPI = BackendAPIMock()): TrainingContext {
        val userContextProvider = CurrentUserContextProviderFactory.createMocked(
            backendAPI = backendAPI
        )
        return userContextProvider.userContext.trainingContext
    }

    private fun getController(trainingContext: TrainingContext = getTrainingContext()) =
        ListTrainingsController(
            trainingContext = trainingContext,
            stringProvider = TestStringProvider.INSTANCE,
        )
}
