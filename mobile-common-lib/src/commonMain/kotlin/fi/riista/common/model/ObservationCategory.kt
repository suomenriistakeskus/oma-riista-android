package fi.riista.common.model

enum class ObservationCategory(override val rawBackendEnumValue: String): RepresentsBackendEnum {
    NORMAL("NORMAL"),
    MOOSE_HUNTING("MOOSE_HUNTING"),
    DEER_HUNTING("DEER_HUNTING"),
    ;
}
