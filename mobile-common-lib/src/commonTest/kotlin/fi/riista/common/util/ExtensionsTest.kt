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
    fun testHasSameElements() {
        assertFalse(listOf(1, 2).hasSameElements(listOf()), "empty other")
        assertFalse(listOf(1, 2).hasSameElements(listOf(1)), "missing other")
        assertFalse(listOf<Int>().hasSameElements(listOf(1, 2)), "empty this")
        assertFalse(listOf(1).hasSameElements(listOf(1, 2)), "missing this")
        assertTrue(listOf<Int>().hasSameElements(listOf()), "both empty")
        assertTrue(listOf(1, 2).hasSameElements(listOf(1, 2)), "same values")
    }

    @Test
    fun testContainsAny() {
        assertFalse(listOf<Int>().containsAny(emptyList()), "empty : empty")
        assertFalse(listOf<Int>().containsAny(listOf(1)), "empty : 1")
        assertFalse(listOf(1, 2).containsAny(emptyList()), "1,2 : empty")
        assertFalse(listOf(1, 2).containsAny(listOf(3)), "1,2 : 3")

        assertTrue(listOf(1).containsAny(listOf(1)), "1 : 1")
        assertFalse(listOf(1).containsAny(listOf(2)), "1 : 2")

        assertTrue(listOf(1, 2).containsAny(listOf(1)), "1,2 : 1")
        assertTrue(listOf(1, 2).containsAny(listOf(2)), "1,2 : 2")
        assertFalse(listOf(1, 2).containsAny(listOf(3)), "1,2 : 3")
    }

    @Test
    fun testReplaceValidIndex() {
        assertEquals(listOf(0, 2), listOf(1, 2).replace(0, 0))
        assertEquals(listOf(1, 3), listOf(1, 2).replace(1, 3))
    }

    @Test
    fun testReplaceInvalidIndex() {
        assertEquals(listOf(1, 2), listOf(1, 2).replace(-1, 0))
        assertEquals(listOf(1, 2), listOf(1, 2).replace(2, 3))
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

    @Test
    fun testNumberOfElementsMatchesCount() {
        var createCalled = false
        val initialList = listOf(0, 1, 2)

        var i = 3
        val result = initialList.withNumberOfElements(3) {
            createCalled = true
            i++
        }

        assertFalse(createCalled, "create called")
        assertEquals(initialList, result)
    }

    @Test
    fun testDroppingElements() {
        var createCalled = false
        val initialList = listOf(0, 1, 2)

        var i = 3
        var result = initialList.withNumberOfElements(2) {
            createCalled = true
            i++
        }

        assertFalse(createCalled, "create called 2")
        assertEquals(listOf(0, 1), result)

        result = initialList.withNumberOfElements(1) {
            createCalled = true
            i++
        }

        assertFalse(createCalled, "create called 1")
        assertEquals(listOf(0), result)

        result = initialList.withNumberOfElements(0) {
            createCalled = true
            i++
        }

        assertFalse(createCalled, "create called 0")
        assertEquals(listOf(), result)

        result = initialList.withNumberOfElements(-1) {
            createCalled = true
            i++
        }

        assertFalse(createCalled, "create called -1")
        assertEquals(listOf(), result)
    }

    @Test
    fun testCreatingElements() {
        val initialList = listOf(1, 2, 3)

        assertEquals(
            listOf(1, 2, 3, 4),
            initialList.withNumberOfElements(4) { 4 }
        )

        assertEquals(
            listOf(1, 2, 3, 5, 5),
            initialList.withNumberOfElements(5) { 5 }
        )
    }

    @Test
    fun `prefixing strings`() {
        assertEquals("foobar", "bar".prefixed("foo"))
        assertEquals("bar", "bar".prefixed(""))
    }
}

private data class ComplexObject(
    val num: Int,
    val text: String
)
