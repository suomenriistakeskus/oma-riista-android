package fi.riista.common.network

import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.logging.Logger
import fi.riista.common.logging.getLogger
import fi.riista.common.network.calls.NetworkResponse
import fi.riista.common.network.calls.NetworkResponseData
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.*

private const val RESPONSE_VALUE = 99

class FetchedFromNetworkTest {

    @Test
    fun testFetchSuccess() {
        val fetcher = getFetcher(200)
        assertTrue(fetcher.loadStatus.value.notLoaded)

        runBlocking {
            fetcher.fetch(refresh = false)
        }

        assertTrue(fetcher.loadStatus.value.loaded)
        assertEquals(fetcher.responseValue, RESPONSE_VALUE)
    }

    @Test
    fun testFetchSuccessCanBeHandled() {
        var successHandlerCalled = false
        val fetcher = getFetcher(200, onSuccess = {
            successHandlerCalled = true
        })
        assertTrue(fetcher.loadStatus.value.notLoaded)

        runBlocking {
            fetcher.fetch(refresh = false)
        }

        assertTrue(fetcher.loadStatus.value.loaded)
        assertTrue(successHandlerCalled)
    }

    @Test
    fun testNotRefetched() {
        val fetcher = getFetcher(200)
        runBlocking {
            fetcher.fetch(refresh = false)
        }

        fetcher.loadStatus.bind {
            fail("Should not refetch")
        }

        runBlocking {
            fetcher.fetch(refresh = false)
        }
    }

    @Test
    fun testRefetchedIfShouldRefresh() {
        val fetcher = getFetcher(200)
        runBlocking {
            fetcher.fetch(refresh = false)
        }

        var loadingObserved = false
        fetcher.loadStatus.bind {
            if (it.loading) {
                loadingObserved = true
            }
        }

        fetcher.shouldRefreshUponNextFetch()

        runBlocking {
            fetcher.fetch(refresh = false)
        }

        assertTrue(loadingObserved)
    }

    @Test
    fun testCanBeRefetched() {
        val fetcher = getFetcher(200)
        runBlocking {
            fetcher.fetch(refresh = false)
        }

        var loadingObserved = false
        fetcher.loadStatus.bind {
            if (it.loading) {
                loadingObserved = true
            }
        }

        runBlocking {
            fetcher.fetch(refresh = true)
        }

        assertTrue(loadingObserved)
    }

    @Test
    fun testSecondFetchWaitsFirstOne() = runBlockingTest {
        val fetcher = getFetcher(200, responseDelayMs = 1000)
        var firstStarted = false
        var firstCompleted = false
        var secondStarted = false
        var secondCompleted = false
        val fetches = listOf(
                launch {
                    firstStarted = true
                    println("start first")
                    fetcher.fetch(refresh = false)
                    println("finish first")
                    firstCompleted = true
                },
                launch {
                    assertTrue(firstStarted)
                    secondStarted = true
                    println("start second")
                    fetcher.fetch(refresh = false)
                    println("finish second")
                    assertTrue(firstCompleted)
                    secondCompleted = true
                }
        )

        assertFalse(firstStarted)
        assertFalse(secondStarted)

        fetches.joinAll()

        assertTrue(firstCompleted)
        assertTrue(secondCompleted)

        assertTrue(fetcher.loadStatus.value.loaded)
    }

    @Test
    fun testLoadErrorsAreReported() {
        val fetcher = getFetcher(404)

        runBlocking {
            fetcher.fetch(refresh = false)
        }

        assertTrue(fetcher.loadStatus.value.error)
        assertNull(fetcher.responseValue)
    }

    @Test
    fun testLoadErrorsCanBeHandled() {
        var errorHandlerCalled = false
        val fetcher = getFetcher(404, onError = {
            errorHandlerCalled = true
        })

        runBlocking {
            fetcher.fetch(refresh = false)
        }

        assertTrue(fetcher.loadStatus.value.error)
        assertTrue(errorHandlerCalled)
    }

    private fun getFetcher(
        statusCode: Int,
        onSuccess: ((NetworkResponseData<out Int>) -> Unit)? = null,
        onError: ((Int?) -> Unit)? = null,
        responseDelayMs: Long = 0,
    ): MockFetcher {
        return MockFetcher(
                statusCode,
                onSuccess,
                onError,
                responseDelayMs
        )
    }
}

private class MockFetcher(
    var statusCode: Int,
    val onSuccess: ((NetworkResponseData<out Int>) -> Unit)? = null,
    val onError: ((Int?) -> Unit)? = null,
    val responseDelayMs: Long = 0,
) : NetworkDataFetcher<Int>() {
    var responseValue: Int? = null
    var error: Boolean = false

    override suspend fun fetchFromNetwork(): NetworkResponse<Int> {
        delay(responseDelayMs)
        return respond()
    }

    private fun respond(): NetworkResponse<Int> {
        return when (statusCode) {
            in 200..299 -> {
                NetworkResponse.Success(
                        statusCode = statusCode,
                        data = NetworkResponseData(
                                raw = RESPONSE_VALUE.toString(),
                                typed = RESPONSE_VALUE
                        )
                )
            }
            0 -> {
                NetworkResponse.NetworkError(null)
            }
            else -> {
                NetworkResponse.ResponseError(statusCode)
            }
        }
    }

    override fun handleSuccess(
        statusCode: Int,
        responseData: NetworkResponseData<out Int>
    ) {
        responseValue = responseData.typed
        onSuccess?.invoke(responseData)
    }

    override fun handleError401() {
        error = true
        onError?.invoke(401)
    }

    override fun handleError(statusCode: Int?, exception: Throwable?) {
        error = true
        onError?.invoke(statusCode)
    }

    override fun logger(): Logger = logger

    companion object {
        private val logger by getLogger("MockFetcher")
    }
}
