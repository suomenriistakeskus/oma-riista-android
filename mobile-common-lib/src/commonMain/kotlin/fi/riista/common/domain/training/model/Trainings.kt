package fi.riista.common.domain.training.model

data class Trainings(
    val jhtTrainings: List<JhtTraining>,
    val occupationTrainings: List<OccupationTraining>,
)
