package fi.riista.common.domain.model

import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class GameFitnessClass(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
) : RepresentsBackendEnum, LocalizableEnum {

    EXCELLENT("ERINOMAINEN", RR.string.harvest_fitness_class_erinomainen),
    NORMAL("NORMAALI", RR.string.harvest_fitness_class_normaali),
    SKINNY("LAIHA", RR.string.harvest_fitness_class_laiha),
    EXHAUSTED("NAANTYNYT", RR.string.harvest_fitness_class_naantynyt),
    ;
}
