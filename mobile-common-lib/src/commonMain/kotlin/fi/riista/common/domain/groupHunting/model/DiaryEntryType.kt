package fi.riista.common.domain.groupHunting.model

import fi.riista.common.model.RepresentsBackendEnum

enum class DiaryEntryType(
    override val rawBackendEnumValue: String
) : RepresentsBackendEnum {
    HARVEST("HARVEST"),
    OBSERVATION("OBSERVATION"),
    SRVA("SRVA"),
}
