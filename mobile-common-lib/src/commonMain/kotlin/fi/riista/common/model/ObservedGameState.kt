package fi.riista.common.model

enum class ObservedGameState(override val rawBackendEnumValue: String): RepresentsBackendEnum {
    HEALTHY("HEALTHY"),
    ILL("ILL"),
    WOUNDED("WOUNDED"),
    CARCASS("CARCASS"),
    DEAD("DEAD"),
    ;
}
