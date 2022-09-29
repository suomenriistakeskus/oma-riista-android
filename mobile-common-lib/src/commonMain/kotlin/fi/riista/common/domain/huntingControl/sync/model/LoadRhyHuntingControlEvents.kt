package fi.riista.common.domain.huntingControl.sync.model

import fi.riista.common.domain.model.Organization

data class LoadRhyHuntingControlEvents(
    val specVersion: Int,
    val rhy: Organization,
    val gameWardens: List<GameWarden>,
    val events: List<LoadHuntingControlEvent>,
)
