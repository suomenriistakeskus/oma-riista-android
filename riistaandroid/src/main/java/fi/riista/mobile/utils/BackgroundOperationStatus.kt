package fi.riista.mobile.utils

import fi.riista.common.reactive.AppObservable

class BackgroundOperationStatus {
    enum class Operation {
        INITIAL_RELOGIN, // initial relogin attempt after starting the app
        SYNCHRONIZATION,
        DATABASE_MIGRATION
    }
    private val operationsInProgress = mutableSetOf<Operation>()

    val backgroundOperationInProgress = AppObservable(initialValue = false)

    fun startOperation(operation: Operation) {
        if (operationsInProgress.add(operation)) {
            backgroundOperationInProgress.set(true)
        }
    }

    fun finishOperation(operation: Operation) {
        operationsInProgress.remove(operation)

        if (operationsInProgress.isEmpty()) {
            backgroundOperationInProgress.set(false)
        }
    }
}
