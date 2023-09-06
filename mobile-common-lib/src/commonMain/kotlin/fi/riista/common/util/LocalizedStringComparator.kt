package fi.riista.common.util

import fi.riista.common.model.LocalizedString

object LocalizedStringComparator: Comparator<LocalizedString> {
    private val comparator by lazy {
        LocalizedStringTranslationComparator { fi }
            .then(LocalizedStringTranslationComparator { sv })
            .then(LocalizedStringTranslationComparator { en })
    }

    override fun compare(a: LocalizedString, b: LocalizedString): Int {
        return comparator.compare(a, b)
    }
}

internal class LocalizedStringTranslationComparator(
    private val accessTranslation: LocalizedString.() -> String?
) : Comparator<LocalizedString> {
    override fun compare(a: LocalizedString, b: LocalizedString): Int {
        val translationA = a.accessTranslation()
        val translationB = b.accessTranslation()

        return when {
            translationA == null && translationB == null -> 0
            translationB == null -> -1
            translationA == null -> 1
            else -> translationA.compareTo(translationB)
        }
    }
}
