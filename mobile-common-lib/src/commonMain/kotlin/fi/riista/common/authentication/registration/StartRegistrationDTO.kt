package fi.riista.common.authentication.registration

import kotlinx.serialization.Serializable

@Serializable
data class StartRegistrationDTO(
    val email: String,
    val lang: String,
)
