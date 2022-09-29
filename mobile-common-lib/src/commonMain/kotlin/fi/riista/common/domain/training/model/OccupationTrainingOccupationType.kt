package fi.riista.common.domain.training.model

import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class OccupationTrainingOccupationType(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
): RepresentsBackendEnum, LocalizableEnum {
    PETOYHDYSHENKILO(
        rawBackendEnumValue = "PETOYHDYSHENKILO",
        resourcesStringId = RR.string.occupation_training_occupation_type_petoyhdyshenkilo,
    )
    ;
}
