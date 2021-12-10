package fi.riista.common.groupHunting.dto

import kotlinx.serialization.Serializable

@Serializable
data class RejectDiaryEntryDTO(
    val type: DiaryEntryTypeDTO,
    val id: Long, // id of the hunting group
    val entryId: Long,
)
