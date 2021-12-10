package fi.riista.common.groupHunting.model

data class GroupHuntingDiary(
        val harvests: List<GroupHuntingHarvest>,
        val observations: List<GroupHuntingObservation>,
        val rejectedHarvests: List<GroupHuntingHarvest>,
        val rejectedObservations: List<GroupHuntingObservation>,
)
