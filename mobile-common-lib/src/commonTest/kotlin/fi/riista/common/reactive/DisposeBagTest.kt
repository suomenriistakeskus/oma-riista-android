package fi.riista.common.reactive

import kotlin.test.Test
import kotlin.test.assertEquals

class DisposeBagTest {
    @Test
    fun testDisposeAll() {
        val disposeBag = DisposeBag()

        val observable = Observable(3)
        var callCount = 0
        observable.bind {
            callCount += 1
        }.disposeBy(disposeBag)

        assertEquals(0, callCount)
        observable.set(2)
        assertEquals(1, callCount)

        disposeBag.disposeAll()

        observable.set(4)
        assertEquals(1, callCount)
    }
}
