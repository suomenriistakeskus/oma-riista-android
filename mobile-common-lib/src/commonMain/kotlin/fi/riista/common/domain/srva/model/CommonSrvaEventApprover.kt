package fi.riista.common.domain.srva.model

import kotlinx.serialization.Serializable

@Serializable
data class CommonSrvaEventApprover(
    val firstName: String?,
    val lastName: String?,
)
