package fi.riista.common.groupHunting.ui.diary

import fi.riista.common.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.groupHunting.model.GroupHuntingObservation

data class DiaryViewModel(
    val harvests: List<GroupHuntingHarvest>,
    val observations: List<GroupHuntingObservation>
)
