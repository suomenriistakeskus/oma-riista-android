package fi.riista.common.domain.observation.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class ObservationSpecimenMarking(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string
): RepresentsBackendEnum, LocalizableEnum {
    NOT_MARKED("NOT_MARKED", RR.string.specimen_marking_not_marked),
    COLLAR_OR_RADIO_TRANSMITTER("COLLAR_OR_RADIO_TRANSMITTER", RR.string.specimen_marking_collar_or_radio_transmitter),
    LEG_RING_OR_WING_TAG("LEG_RING_OR_WING_TAG", RR.string.specimen_marking_leg_ring_or_wing_tag),
    EARMARK("EARMARK", RR.string.specimen_marking_earmark),
    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<ObservationSpecimenMarking> = value.toBackendEnum()
    }
}
