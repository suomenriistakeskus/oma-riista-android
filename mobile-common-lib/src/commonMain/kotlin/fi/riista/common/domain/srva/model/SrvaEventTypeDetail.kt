package fi.riista.common.domain.srva.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class SrvaEventTypeDetail(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
): RepresentsBackendEnum, LocalizableEnum {
    // SrvaEventType.ANIMAL_NEAR_HOUSES_AREA
    CARED_HOUSE_AREA("CARED_HOUSE_AREA", RR.string.srva_event_type_detail_cared_house_area),
    FARM_ANIMAL_BUILDING("FARM_ANIMAL_BUILDING", RR.string.srva_event_type_detail_farm_animal_building),
    URBAN_AREA("URBAN_AREA", RR.string.srva_event_type_detail_urban_area),

    // SrvaEventType.ANIMAL_AT_FOOD_DESTINATION
    CARCASS_AT_FOREST("CARCASS_AT_FOREST", RR.string.srva_event_type_detail_carcass_at_forest),
    CARCASS_NEAR_HOUSES_AREA("CARCASS_NEAR_HOUSES_AREA", RR.string.srva_event_type_detail_carcass_near_houses_area),
    GARBAGE_CAN("GARBAGE_CAN", RR.string.srva_event_type_detail_garbage_can),
    BEEHIVE("BEEHIVE", RR.string.srva_event_type_detail_beehive),

    // SrvaEventType.ANIMAL_NEAR_HOUSES_AREA, SrvaEventType.ANIMAL_AT_FOOD_DESTINATION
    OTHER("OTHER", RR.string.srva_event_type_detail_other),

    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<SrvaEventTypeDetail> = value.toBackendEnum()
    }
}
