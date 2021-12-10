package fi.riista.common.model

enum class GameAge(override val rawBackendEnumValue: String): RepresentsBackendEnum {
    ADULT("ADULT"),
    YOUNG("YOUNG"),
    UNKNOWN("UNKNOWN"),
    ;
}
