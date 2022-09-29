package fi.riista.common.domain.groupHunting.ui.diary

import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.GroupHuntingObservation

data class DiaryViewModel(
    val harvests: List<GroupHuntingHarvest>,
    val observations: List<GroupHuntingObservation>
)
