package fi.riista.common.reactive

import co.touchlab.stately.concurrency.AtomicReference
import fi.riista.common.util.WeakRef
import fi.riista.common.util.removeFirst

// todo: consider using e.g. Reaktive library

typealias ObserverId = Int
typealias Observer<T> = (newValue: T) -> Unit

@Suppress("MemberVisibilityCanBePrivate")
class Subscription internal constructor(
    val observerId: ObserverId,
    observable: Observable<*>,
) {
    private val weakObservable = WeakRef(observable)

    fun unbind() {
        weakObservable.get()?.unbind(observerId)
    }
}


open class Observable<T>(initialValue: T) {
    private val observers = mutableListOf<ObserverRecord<T>>()

    private var _valueHolder = AtomicReference(initialValue)
    private var _value: T
        get() {
            return _valueHolder.get()
        }
        set(value) {
            _valueHolder.set(value)
            notifyObservers(value)
        }

    val value: T
        get() = _value

    /**
     * Only allow updating the value from the RiistaSDK library.
     */
    internal open fun set(value: T) {
        this._value = value
    }

    fun bind(observer: Observer<T>): Subscription {
        val lastObserverId = observers.lastOrNull()?.id ?: 0
        val observerId = lastObserverId + 1
        observers.add(ObserverRecord(observerId, observer))

        return Subscription(observerId, observable = this)
    }

    fun bindAndNotify(observer: Observer<T>): Subscription {
        val subscription = bind(observer)
        notifyObservers(listOf(observer), value)

        return subscription
    }

    fun unbind(observerId: ObserverId) {
        observers.removeFirst { it.id == observerId }
    }

    fun unbind(subscription: Subscription) {
        unbind(subscription.observerId)
    }

    private fun notifyObservers(newValue: T) {
        notifyObservers(observers.map { it.observer }, newValue)
    }

    private fun notifyObservers(observers: List<Observer<T>>, value: T) {
        observers.forEach { it(value) }
    }
}

private class ObserverRecord<T>(
    val id: ObserverId,
    val observer: Observer<T>
)

/**
 * An [Observable] version that can be used on the application side.
 *
 * Don't use in RiistaCommon implementation!
 */
class AppObservable<T>(initialValue: T): Observable<T>(initialValue) {
    public override fun set(value: T) {
        super.set(value)
    }
}