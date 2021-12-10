package fi.riista.common.model

enum class Gender(override val rawBackendEnumValue: String): RepresentsBackendEnum {
    FEMALE("FEMALE"),
    MALE("MALE"),
    UNKNOWN("UNKNOWN"),
    ;
}
