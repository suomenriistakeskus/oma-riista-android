package fi.riista.common.util

import co.touchlab.stately.concurrency.AtomicBoolean
import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LoadStatus
import fi.riista.common.reactive.Observable
import kotlinx.coroutines.*

/**
 * A [DataFetcher] that only allows one simultaneous data fetch at any time i.e.
 * data fetches are performed sequentially.
 *
 * Well, mostly sequentially. If a second [fetch] call is made while the first one is still being
 * active, the second call will join (see [Job.join]) the first call and thus it will "fetch"
 * the same data as the first call and it won't return before the first call has been finished.
 *
 * The purpose of this behaviour is to let suspend functions really suspend until
 * the data loading has completed.
 */
abstract class SequentialDataFetcher: DataFetcher {
    override val loadStatus: Observable<LoadStatus> = Observable(LoadStatus.NotLoaded())

    private var existingFetch = AtomicReference<Job?>(null)

    protected val logger by getLogger(this::class)

    /**
     * Should the data be refreshed when loading it the next time.
     *
     * This flag is useful when we're about to start updating the data e.g. on the backend.
     * We can then mark the data to be refreshed _before_ updating the data. This way if
     * the data is updated on the backend but we fail to receive the response, we're still
     * going to update the data next time we're fetching it.
     */
    private var shouldRefresh = AtomicBoolean(false)

    fun shouldRefreshUponNextFetch() {
        shouldRefresh.value = true
    }

    override suspend fun fetch(refresh: Boolean): Unit = coroutineScope {
        if (!refresh && !shouldRefresh.value && loadStatus.value.loaded) {
            logger.v { "Data has been already loaded, not refreshing" }
            return@coroutineScope
        }


        // don't allow simultaneous fetches even if refreshing
        var activeFetch = existingFetch.value
        if (activeFetch != null) {
            logger.v { "An active fetch operation found." }
        } else {
            logger.v { "No active fetch operation, creating one.." }

            shouldRefresh.value = false

            loadStatus.set(LoadStatus.Loading())

            // create an async task for fetching the data. Don't utilize suspend
            // function here in order to allow other callees to wait for the same result
            activeFetch = launch(start = CoroutineStart.LAZY) {
                doFetch()
            }.also {
                existingFetch.value = it
            }
        }

        logger.v { "Waiting for the fetch operation to be performed.."}
        try {
            activeFetch.join()
            existingFetch.value = null
        } catch (e: CancellationException) {
            logger.i { "Fetch operation was cancelled." }

            existingFetch.value = null
            // todo: should a separate status be used for cancelled?
            loadStatus.set(LoadStatus.LoadError())

            // rethrow exception as that will cause the cancellation of other jobs
            throw e
        }

        logger.v { "Fetch operation completed."}
    }

    /**
     * Implement this one in the subclass i.e. perform the fetch. The subclass also
     * needs to update [loadStatus] accordingly when data fetch has completed (i.e. either
     * [LoadStatus.Loaded] or [LoadStatus.LoadError]).
     *
     * The subclass should preferably also perform logging using [logger].
     */
    protected abstract suspend fun doFetch()
}
