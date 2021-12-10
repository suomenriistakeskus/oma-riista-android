package fi.riista.common.resources

interface StringProvider {
    fun getString(stringId: RStringId): String

    fun getFormattedString(stringId: RStringId, arg: String): String
}