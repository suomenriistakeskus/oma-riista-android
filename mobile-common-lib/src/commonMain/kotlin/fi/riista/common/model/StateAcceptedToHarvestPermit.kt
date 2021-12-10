package fi.riista.common.model

enum class StateAcceptedToHarvestPermit(override val rawBackendEnumValue: String) : RepresentsBackendEnum {
    PROPOSED("PROPOSED"),
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED"),
    ;
}
