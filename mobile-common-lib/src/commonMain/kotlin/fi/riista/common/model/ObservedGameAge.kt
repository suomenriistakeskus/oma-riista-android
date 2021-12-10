package fi.riista.common.model

enum class ObservedGameAge(override val rawBackendEnumValue: String): RepresentsBackendEnum {
    ADULT("ADULT"),
    LT1Y("LT1Y"), // less than one year old
    _1TO2Y("_1TO2Y"), // one-to-two-year old
    UNKNOWN("UNKNOWN"),
    ;
}
