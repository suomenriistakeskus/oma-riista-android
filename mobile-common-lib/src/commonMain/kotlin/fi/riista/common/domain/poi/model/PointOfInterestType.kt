package fi.riista.common.domain.poi.model

import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class PointOfInterestType(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
): RepresentsBackendEnum, LocalizableEnum {
    // Passi
    SIGHTING_PLACE(
        rawBackendEnumValue = "SIGHTING_PLACE",
        resourcesStringId = RR.string.poi_location_group_type_sighting_place,
    ),
    // Nuolukivi
    MINERAL_LICK(
        rawBackendEnumValue = "MINERAL_LICK",
        resourcesStringId = RR.string.poi_location_group_type_mineral_lick,
    ),
    // Ruokintapaikka
    FEEDING_PLACE(
        rawBackendEnumValue = "FEEDING_PLACE",
        resourcesStringId = RR.string.poi_location_group_type_feeding_place,
    ),
    // Muu
    OTHER(
        rawBackendEnumValue = "OTHER",
        resourcesStringId = RR.string.poi_location_group_type_other,
    ),
}
