package fi.riista.common.reactive

class DisposeBag {
    private val subscriptions = mutableListOf<Subscription>()

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