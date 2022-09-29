package fi.riista.common.domain.training

import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.MockResponse
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class TrainingProviderTest {
    @Test
    fun testTrainingsCanBeFetched() {
        val trainingProvider = getTrainingProvider()
        assertTrue(trainingProvider.loadStatus.value.notLoaded)

        runBlocking {
            trainingProvider.fetch(refresh = false)
        }

        assertTrue(trainingProvider.loadStatus.value.loaded)
        assertNotNull(trainingProvider.trainings)
    }

    @Test
    fun testNetworkErrorDoesNotClearPreviousTrainings() {
        val backendAPI = BackendAPIMock()
        val trainingProvider = getTrainingProvider(backendAPI)
        runBlocking {
            trainingProvider.fetch(refresh = false)
        }
        assertTrue(trainingProvider.loadStatus.value.loaded)
        assertNotNull(trainingProvider.trainings)

        backendAPI.fetchTrainingsResponse = MockResponse.error(null)
        runBlocking {
            trainingProvider.fetch(refresh = true)
        }
        assertTrue(trainingProvider.loadStatus.value.error)
        assertNotNull(trainingProvider.trainings)
    }

    @Test
    fun test401ErrorClearsTrainings() {
        val backendAPI = BackendAPIMock()
        val trainingProvider = getTrainingProvider(backendAPI)
        runBlocking {
            trainingProvider.fetch(refresh = false)
        }
        assertTrue(trainingProvider.loadStatus.value.loaded)
        assertNotNull(trainingProvider.trainings)

        backendAPI.fetchTrainingsResponse = MockResponse.error(401)
        runBlocking {
            trainingProvider.fetch(refresh = true)
        }
        assertTrue(trainingProvider.loadStatus.value.error)
        assertNull(trainingProvider.trainings)
    }

    private fun getTrainingProvider(backendAPI: BackendAPI = BackendAPIMock()): TrainingProvider {
        return TrainingsFromNetworkProvider(backendApiProvider = object : BackendApiProvider {
            override val backendAPI: BackendAPI = backendAPI
        })
    }
}
