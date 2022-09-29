package fi.riista.common.domain.srva.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class SrvaEventType(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
): RepresentsBackendEnum, LocalizableEnum {
    // Accident
    TRAFFIC_ACCIDENT("TRAFFIC_ACCIDENT", RR.string.srva_event_type_traffic_accident),
    RAILWAY_ACCIDENT("RAILWAY_ACCIDENT", RR.string.srva_event_type_railway_accident),

    // Deportation
    ANIMAL_NEAR_HOUSES_AREA("ANIMAL_NEAR_HOUSES_AREA", RR.string.srva_event_type_animal_near_houses_area),
    ANIMAL_AT_FOOD_DESTINATION("ANIMAL_AT_FOOD_DESTINATION", RR.string.srva_event_type_animal_at_food_destination),

    // Injured animal
    INJURED_ANIMAL("INJURED_ANIMAL", RR.string.srva_event_type_injured_animal),
    ANIMAL_ON_ICE("ANIMAL_ON_ICE", RR.string.srva_event_type_animal_on_ice),

    // Common
    OTHER("OTHER", RR.string.srva_event_type_other),

    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<SrvaEventType> = value.toBackendEnum()
    }
}
