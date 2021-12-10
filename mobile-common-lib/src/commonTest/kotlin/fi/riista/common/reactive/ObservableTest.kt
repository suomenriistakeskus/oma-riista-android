package fi.riista.common.reactive

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ObservableTest {

    @Test
    fun testInitialValueExists() {
        val observable = Observable(3)
        assertEquals(3, observable.value)
    }

    @Test
    fun testInitialValueCanBeUpdated() {
        val observable = Observable(3)
        assertEquals(3, observable.value)
        observable.set(4)
        assertEquals(4, observable.value)
        observable.set(5)
        assertEquals(5, observable.value)
    }

    @Test
    fun testObserverNotNotified() {
        val observable = Observable(3)
        observable.bind {
            fail("observer was called")
        }
    }

    @Test
    fun testObserverIsNotified() {
        val observable = Observable(3)
        var observerCallCount = 0
        observable.bindAndNotify {
            observerCallCount += 1
        }
        assertEquals(1, observerCallCount)
    }

    @Test
    fun testObserverIsNotifiedWhenValueIsUpdated() {
        val observable = Observable(3)
        var observerCallCount = 0
        observable.bind {
            observerCallCount += 1
        }
        assertEquals(0, observerCallCount)
        observable.set(2)
        assertEquals(1, observerCallCount)
    }

    @Test
    fun testObserverCanBeRemoved() {
        val observable = Observable(3)
        var firstCallCount = 0
        val firstSubscription = observable.bind {
            firstCallCount += 1
        }
        var secondCallCount = 0
        val secondSubscription = observable.bind {
            secondCallCount += 1
        }
        assertEquals(0, firstCallCount)
        assertEquals(0, secondCallCount)
        observable.set(2)
        assertEquals(1, firstCallCount)
        assertEquals(1, secondCallCount)
        observable.unbind(firstSubscription)
        observable.set(4)
        assertEquals(1, firstCallCount)
        assertEquals(2, secondCallCount)
    }

    @Test
    fun testObserverCanBeRemovedByUnbinding() {
        val observable = Observable(3)
        var firstCallCount = 0
        val firstSubscription = observable.bind {
            firstCallCount += 1
        }
        var secondCallCount = 0
        val secondSubscription = observable.bind {
            secondCallCount += 1
        }
        assertEquals(0, firstCallCount)
        assertEquals(0, secondCallCount)
        observable.set(2)
        assertEquals(1, firstCallCount)
        assertEquals(1, secondCallCount)
        firstSubscription.unbind()
        observable.set(4)
        assertEquals(1, firstCallCount)
        assertEquals(2, secondCallCount)
    }

}
