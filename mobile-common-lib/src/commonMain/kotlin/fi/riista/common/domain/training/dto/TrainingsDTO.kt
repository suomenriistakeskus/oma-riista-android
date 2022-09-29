package fi.riista.common.domain.training.dto

import fi.riista.common.domain.training.model.Trainings
import kotlinx.serialization.Serializable

@Serializable
data class TrainingsDTO(
    val jhtTrainings: List<JhtTrainingDTO>,
    val occupationTrainings: List<OccupationTrainingDTO>,
)

fun TrainingsDTO.toTrainings(): Trainings {
    return Trainings(
        jhtTrainings = jhtTrainings.mapNotNull { it.toJhtTraining() },
        occupationTrainings = occupationTrainings.mapNotNull { it.toOccupationTraining() }
    )
}
