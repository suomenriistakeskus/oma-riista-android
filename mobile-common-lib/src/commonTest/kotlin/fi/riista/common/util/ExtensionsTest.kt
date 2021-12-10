package fi.riista.common.util

import kotlin.test.*

class ExtensionsTest {

    @Test
    fun testMutableListRemoveFirstWithPrimitives() {
        val list = mutableListOf(1, 2, 3)
        assertEquals(3, list.size)
        assertTrue(list.contains(1))
        assertTrue(list.contains(2))
        assertTrue(list.contains(3))

        list.removeFirst { it == 2 }

        assertEquals(2, list.size)
        assertTrue(list.contains(1))
        assertTrue(list.contains(3))
    }

    @Test
    fun testMutableListRemoveFirstWithDataClasses() {
        val list = mutableListOf(
                ComplexObject(0, "0"),
                ComplexObject(1, "1"),
                ComplexObject(2, "2")
        )
        assertEquals(3, list.size)
        for (i in 0..2) {
            assertEquals(i, list[i].num)
            assertEquals(i.toString(), list[i].text)
        }

        list.removeFirst { it.num == 1 }

        assertEquals(2, list.size)
        assertEquals(0, list[0].num)
        assertEquals("0", list[0].text)
        assertEquals(2, list[1].num)
        assertEquals("2", list[1].text)
    }

    @Test
    fun testFirstAndOnlyWithNoElements() {
        val list = listOf<Int>()
        assertNull(list.firstAndOnly())
    }

    @Test
    fun testFirstAndOnlyWithMultipleElements() {
        val list = listOf(1, 2)
        assertNull(list.firstAndOnly())
    }

    @Test
    fun testFirstAndOnlyWithOneElement() {
        val list = listOf(1)
        assertEquals(1, list.firstAndOnly())
    }

    @Test
    fun testLetWithNull() {
        val first = 1
        val second = null
        first.letWith(second) { _, _ ->
            fail("Should not have been called")
        }
    }

    @Test
    fun testLetWithNonNull() {
        val first = 1
        val second = 2

        var called = false
        first.letWith(second) { one, two ->
            assertEquals(1, one)
            assertEquals(2, two)
            called = true
        }

        assertTrue(called)
    }

    @Test
    fun testLetIf() {
        var calledValue: Int? = null
        1.letIf(true) {
            calledValue = it
        }
        assertEquals(1, calledValue)

        2.letIf(false) {
            fail("Should not be called for false condition")
        }
    }

    @Test
    fun testIsNullOr() {
        assertTrue(null.isNullOr(1))
        assertTrue(1.isNullOr(1))
        assertFalse(1.isNullOr(2))
    }
}

private data class ComplexObject(
    val num: Int,
    val text: String
)
