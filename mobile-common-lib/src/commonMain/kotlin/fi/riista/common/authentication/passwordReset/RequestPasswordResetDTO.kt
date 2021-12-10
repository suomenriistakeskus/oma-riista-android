package fi.riista.common.authentication.passwordReset

import kotlinx.serialization.Serializable

@Serializable
data class RequestPasswordResetDTO(
    val email: String,
    val lang: String,
)
