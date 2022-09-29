package fi.riista.common.domain.huntingControl.model

import fi.riista.common.model.LocalDate

typealias HuntingControlGameWardenId = Long

data class HuntingControlGameWarden(
    val remoteId: HuntingControlGameWardenId,
    val firstName: String,
    val lastName: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)
