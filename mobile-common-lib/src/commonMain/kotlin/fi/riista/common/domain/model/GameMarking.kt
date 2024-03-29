package fi.riista.common.domain.model

import fi.riista.common.model.RepresentsBackendEnum

enum class GameMarking(override val rawBackendEnumValue: String): RepresentsBackendEnum {
    NOT_MARKED("NOT_MARKED"),
    COLLAR_OR_RADIO_TRANSMITTER("COLLAR_OR_RADIO_TRANSMITTER"),
    LEG_RING_OR_WING_TAG("LEG_RING_OR_WING_TAG"),
    EARMARK("EARMARK"),
    ;
}
