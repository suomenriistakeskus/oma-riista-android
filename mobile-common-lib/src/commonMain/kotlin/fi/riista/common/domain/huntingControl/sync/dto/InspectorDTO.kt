package fi.riista.common.domain.huntingControl.sync.dto

import fi.riista.common.domain.huntingControl.model.HuntingControlEventInspector
import kotlinx.serialization.Serializable

@Serializable
data class InspectorDTO(
    val id: Long,
    val firstName: String,
    val lastName: String,
)

fun InspectorDTO.toHuntingControlEventInspector(): HuntingControlEventInspector {
    return HuntingControlEventInspector(
        id = id,
        firstName = firstName,
        lastName = lastName,
    )
}

fun HuntingControlEventInspector.toInspectorDTO(): InspectorDTO {
    return InspectorDTO(
        id = id,
        firstName = firstName,
        lastName = lastName,
    )
}
