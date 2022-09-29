package fi.riista.common.domain.groupHunting.ui.huntingDays.select

import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.model.LocalDateTime

data class SelectableHuntingDayViewModel(
    val huntingDayId: GroupHuntingDayId,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val selected: Boolean,
)