package fi.riista.common.resources

import fi.riista.common.model.LocalDate

interface StringProvider {
    fun getString(stringId: RR.string): String

    fun getFormattedString(stringFormatId: RR.stringFormat, arg: String): String
    fun getFormattedString(stringFormatId: RR.stringFormat, arg1: String, arg2: String): String

    fun getFormattedDouble(stringFormatId: RR.stringFormat, arg: Double): String

    fun getQuantityString(pluralsId: RR.plurals, quantity: Int, arg: Int): String

    fun getFormattedDate(dateFormatId: RR.stringFormat, arg: LocalDate): String
}

fun RR.string.localized(stringProvider: StringProvider) = stringProvider.getString(this)
