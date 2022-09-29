package fi.riista.common.domain.huntingControl.model

import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class HuntingControlEventType(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
) : RepresentsBackendEnum, LocalizableEnum {
    MOOSELIKE_HUNTING_CONTROL("MOOSELIKE_HUNTING_CONTROL", RR.string.hunting_control_event_type_mooselike),
    LARGE_CARNIVORE_HUNTING_CONTROL("LARGE_CARNIVORE_HUNTING_CONTROL", RR.string.hunting_control_event_type_large_carnivore),
    GROUSE_HUNTING_CONTROL("GROUSE_HUNTING_CONTROL", RR.string.hunting_control_event_type_grouse),
    WATERFOWL_HUNTING_CONTROL("WATERFOWL_HUNTING_CONTROL", RR.string.hunting_control_event_type_waterfowl),
    DOG_DISCIPLINE_CONTROL("DOG_DISCIPLINE_CONTROL", RR.string.hunting_control_event_type_dog_discipline),
    OTHER("OTHER", RR.string.hunting_control_event_type_other),
}
