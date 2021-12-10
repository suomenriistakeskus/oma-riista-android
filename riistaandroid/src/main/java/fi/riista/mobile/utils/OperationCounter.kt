package fi.riista.mobile.utils

import java.util.concurrent.atomic.AtomicInteger

class OperationCounter(val totalImageOperations: Int) {

    private val imageOperationsDone = AtomicInteger(0)

    @Volatile
    var errors = false

    init {
        if (totalImageOperations < 0) {
            throw IllegalArgumentException("totalImageOperations must not be negative")
        }
    }

    fun incrementOperationsDone(): Boolean = imageOperationsDone.incrementAndGet() >= totalImageOperations
}
