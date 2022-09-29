package fi.riista.common.domain.groupHunting.dto

import kotlinx.serialization.Serializable

@Serializable
data class RejectDiaryEntryDTO(
    val type: DiaryEntryTypeDTO,
    val id: Long, // id of the hunting group
    val entryId: Long,
)
