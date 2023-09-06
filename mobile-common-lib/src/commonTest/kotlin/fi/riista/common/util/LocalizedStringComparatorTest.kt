package fi.riista.common.util

import fi.riista.common.model.LocalizedString
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalizedStringComparatorTest {
    @Test
    fun equality() {
        assertEquals(
            expected = 0,
            actual = LocalizedStringComparator.compare(s(), s()),
            message = "nulls"
        )
        assertEquals(
            expected = 0,
            actual = LocalizedStringComparator.compare(s(fi = "fi"), s(fi = "fi")),
            message = "fi"
        )
        assertEquals(
            expected = 0,
            actual = LocalizedStringComparator.compare(s(fi = "sv"), s(fi = "sv")),
            message = "sv"
        )
        assertEquals(
            expected = 0,
            actual = LocalizedStringComparator.compare(s(fi = "en"), s(fi = "en")),
            message = "en"
        )
    }

    @Test
    fun `text before null`() {
        assertEquals(
            expected = -1,
            actual = LocalizedStringComparator.compare(s(fi = "fi"), s()),
            message = "fi"
        )
        assertEquals(
            expected = -1,
            actual = LocalizedStringComparator.compare(s(sv = "sv"), s()),
            message = "sv"
        )
        assertEquals(
            expected = -1,
            actual = LocalizedStringComparator.compare(s(en = "en"), s()),
            message = "en"
        )
    }

    @Test
    fun `text after null`() {
        assertEquals(
            expected = 1,
            actual = LocalizedStringComparator.compare(s(), s(fi = "fi")),
            message = "fi"
        )
        assertEquals(
            expected = 1,
            actual = LocalizedStringComparator.compare(s(), s(sv = "sv")),
            message = "sv"
        )
        assertEquals(
            expected = 1,
            actual = LocalizedStringComparator.compare(s(), s(en = "en")),
            message = "en"
        )
    }

    @Test
    fun `texts compared if not null`() {
        assertEquals(
            expected = -1,
            actual = LocalizedStringComparator.compare(s(fi = "a"), s(fi = "b")),
            message = "fi-1"
        )
        assertEquals(
            expected = -1,
            actual = LocalizedStringComparator.compare(s(sv = "a"), s(sv = "b")),
            message = "sv-1"
        )
        assertEquals(
            expected = -1,
            actual = LocalizedStringComparator.compare(s(en = "a"), s(en = "b")),
            message = "en-1"
        )

        assertEquals(
            expected = 1,
            actual = LocalizedStringComparator.compare(s(fi = "b"), s(fi = "a")),
            message = "fi-2"
        )
        assertEquals(
            expected = 1,
            actual = LocalizedStringComparator.compare(s(sv = "b"), s(sv = "a")),
            message = "sv-2"
        )
        assertEquals(
            expected = 1,
            actual = LocalizedStringComparator.compare(s(en = "b"), s(en = "a")),
            message = "en-2"
        )
    }

    @Test
    fun `fi before sv before en`() {
        assertEquals(
            expected = -1,
            actual = LocalizedStringComparator.compare(
                a = s(fi = "a", sv = "a"),
                b = s(fi = "a")
            ),
            message = "sv-1"
        )
        assertEquals(
            expected = -1,
            actual = LocalizedStringComparator.compare(
                a = s(fi = "a", sv = "a", en = "a"),
                b = s(fi = "a", sv = "a")
            ),
            message = "en-1"
        )

        assertEquals(
            expected = 1,
            actual = LocalizedStringComparator.compare(
                a = s(fi = "a"),
                b = s(fi = "a", sv = "a")
            ),
            message = "sv-2"
        )
        assertEquals(
            expected = 1,
            actual = LocalizedStringComparator.compare(
                a = s(fi = "a", sv = "a"),
                b = s(fi = "a", sv = "a", en = "a")
            ),
            message = "en-2"
        )
    }

    private fun s(fi: String? = null, sv: String? = null, en: String? = null) =
        LocalizedString(fi, sv, en)
}
