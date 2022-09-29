package fi.riista.common.domain.training.dto

import fi.riista.common.domain.training.model.JhtTraining
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class JhtTrainingDTO(
    val id: Long,
    val trainingType: String,
    val occupationType: String,
    val date: LocalDateDTO,
    val location: String,
)

fun JhtTrainingDTO.toJhtTraining(): JhtTraining? {
    val localDate = date.toLocalDate() ?: return null
    return JhtTraining(
        id = id,
        trainingType = trainingType.toBackendEnum(),
        occupationType = occupationType.toBackendEnum(),
        date = localDate,
        location = location,
    )
}
