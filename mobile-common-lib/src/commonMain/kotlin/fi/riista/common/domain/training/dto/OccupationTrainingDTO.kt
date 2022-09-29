package fi.riista.common.domain.training.dto

import fi.riista.common.domain.training.model.OccupationTraining
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class OccupationTrainingDTO(
    val id: Long,
    val trainingType: String,
    val occupationType: String,
    val date: LocalDateDTO,
)

fun OccupationTrainingDTO.toOccupationTraining(): OccupationTraining? {
    val localDate = date.toLocalDate() ?: return null
    return OccupationTraining(
        id = id,
        trainingType = trainingType.toBackendEnum(),
        occupationType = occupationType.toBackendEnum(),
        date = localDate,
    )
}
