package fi.riista.common.domain.groupHunting.model

data class HuntingGroupStatus(
    val canCreateHuntingDay: Boolean,
    val canCreateHarvest: Boolean,
    val canCreateObservation: Boolean,
    val canEditDiaryEntry: Boolean,
    val canEditHuntingDay: Boolean,
    val canEditHarvest: Boolean,
    val canEditObservation: Boolean,
    val huntingFinished: Boolean,
)