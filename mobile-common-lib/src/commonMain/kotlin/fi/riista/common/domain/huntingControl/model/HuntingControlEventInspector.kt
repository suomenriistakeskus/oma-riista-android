package fi.riista.common.domain.huntingControl.model

import kotlinx.serialization.Serializable

typealias HuntingControlEventInspectorId = Long

@Serializable
data class HuntingControlEventInspector(
    val id: HuntingControlEventInspectorId,
    val firstName: String,
    val lastName: String,
)

