package fi.riista.common.domain.training.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDate

typealias JhtTrainingId = Long

data class JhtTraining(
    val id: JhtTrainingId,
    val trainingType: BackendEnum<TrainingType>,
    val occupationType: BackendEnum<JhtTrainingOccupationType>,
    val date: LocalDate,
    val location: String?,
)
