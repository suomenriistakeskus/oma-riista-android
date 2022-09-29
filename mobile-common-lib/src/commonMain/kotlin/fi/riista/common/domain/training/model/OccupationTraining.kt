package fi.riista.common.domain.training.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDate

data class OccupationTraining(
    val id: JhtTrainingId,
    val trainingType: BackendEnum<TrainingType>,
    val occupationType: BackendEnum<OccupationTrainingOccupationType>,
    val date: LocalDate,
)
