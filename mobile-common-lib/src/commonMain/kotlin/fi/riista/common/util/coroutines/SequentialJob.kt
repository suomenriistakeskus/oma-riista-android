package fi.riista.common.util.coroutines


import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import fi.riista.common.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class SequentialJob(
    private val jobName: String,
    private val logger: Logger,
) {
    private var existingJob = AtomicReference<Job?>(null)

    suspend fun join(block: suspend () -> Unit): Unit = coroutineScope {
        // don't allow simultaneous jobs
        var activeJob = existingJob.value
        if (activeJob != null) {
            logger.v { "An active $jobName found." }
        } else {
            logger.v { "No active $jobName, creating one.." }

            // create an async task for the job. Don't utilize suspend function here in order to allow
            // other callers to wait for the same result
            activeJob = launch(start = CoroutineStart.LAZY) {
                block()
            }.also {
                existingJob.value = it
            }
        }

        logger.v { "Waiting for the $jobName to be performed.." }
        try {
            activeJob.join()
            existingJob.value = null
        } catch (e: CancellationException) {
            logger.i { "$jobName was cancelled." }

            existingJob.value = null

            // rethrow exception as that will cause the cancellation of other jobs
            throw e
        }

        logger.v { "$jobName operation completed." }
    }
}
