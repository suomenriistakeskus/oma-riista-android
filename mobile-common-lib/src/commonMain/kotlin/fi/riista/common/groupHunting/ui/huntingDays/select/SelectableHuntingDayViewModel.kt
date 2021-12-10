package fi.riista.common.groupHunting.ui.huntingDays.select

import fi.riista.common.groupHunting.model.GroupHuntingDayId
import fi.riista.common.model.LocalDateTime

data class SelectableHuntingDayViewModel(
    val huntingDayId: GroupHuntingDayId,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val selected: Boolean,
)