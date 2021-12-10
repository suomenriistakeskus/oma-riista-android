package fi.riista.common.model

import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR
import fi.riista.common.resources.RStringId

enum class GameFitnessClass(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RStringId,
) : RepresentsBackendEnum, LocalizableEnum {

    EXCELLENT("ERINOMAINEN", RR.string.harvest_fitness_class_erinomainen),
    NORMAL("NORMAALI", RR.string.harvest_fitness_class_normaali),
    SKINNY("LAIHA", RR.string.harvest_fitness_class_laiha),
    EXHAUSTED("NAANTYNYT", RR.string.harvest_fitness_class_naantynyt),
    ;
}
