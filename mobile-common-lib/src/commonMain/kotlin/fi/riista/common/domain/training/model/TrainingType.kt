package fi.riista.common.domain.training.model

import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class TrainingType(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
): RepresentsBackendEnum, LocalizableEnum {
    LAHI(
        rawBackendEnumValue = "LAHI",
        resourcesStringId = RR.string.training_type_lahi,
    ),
    SAHKOINEN(
        rawBackendEnumValue = "SAHKOINEN",
        resourcesStringId = RR.string.training_type_sahkoinen,
    ),
    ;
}
