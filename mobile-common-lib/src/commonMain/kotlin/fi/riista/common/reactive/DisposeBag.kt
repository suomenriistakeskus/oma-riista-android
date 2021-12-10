package fi.riista.common.reactive

import co.touchlab.stately.collections.IsoMutableList

class DisposeBag {
    private val subscriptions = IsoMutableList<Subscription>()

    fun addSubscription(subscription: Subscription) {
        subscriptions.add(subscription)
    }

    fun disposeAll() {
        subscriptions.forEach {
            it.unbind()
        }
        subscriptions.clear()
    }
}

fun Subscription.disposeBy(disposeBag: DisposeBag): Subscription {
    disposeBag.addSubscription(this)

    return this
}