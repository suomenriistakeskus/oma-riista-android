package fi.riista.common.groupHunting.model

data class HuntingGroupStatus(
    val canCreateHuntingDay: Boolean,
    val canCreateHarvest: Boolean,
    val canCreateObservation: Boolean,
    val canEditDiaryEntry: Boolean,
    val canEditHuntingDay: Boolean,
)