package fi.riista.common.domain.observation.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class ObservationSpecimenState(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
): RepresentsBackendEnum, LocalizableEnum {
    HEALTHY("HEALTHY", RR.string.specimen_state_of_health_healthy),
    ILL("ILL", RR.string.specimen_state_of_health_ill),
    WOUNDED("WOUNDED", RR.string.specimen_state_of_health_wounded),
    CARCASS("CARCASS", RR.string.specimen_state_of_health_carcass),
    DEAD("DEAD", RR.string.specimen_state_of_health_dead),
    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<ObservationSpecimenState> = value.toBackendEnum()
    }
}
