package fi.riista.common.domain.training.ui

import fi.riista.common.model.LocalDate

sealed class TrainingViewModel {
    abstract val id: Long
    abstract val date: LocalDate
    abstract val trainingType: String
    abstract val occupationType: String

    data class JhtTraining(
        override val id: Long,
        override val trainingType: String,
        override val occupationType: String,
        override val date: LocalDate,
        val location: String?,
    ) : TrainingViewModel()

    data class OccupationTraining(
        override val id: Long,
        override val trainingType: String,
        override val occupationType: String,
        override val date: LocalDate,
    ) : TrainingViewModel()
}
