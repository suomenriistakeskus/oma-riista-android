package fi.riista.common.authentication

import fi.riista.common.authentication.passwordReset.RequestPasswordReset
import fi.riista.common.authentication.passwordReset.RequestPasswordResetDTO
import fi.riista.common.authentication.registration.RequestStartRegistration
import fi.riista.common.authentication.registration.StartRegistrationDTO
import fi.riista.common.model.Language
import fi.riista.common.network.NetworkClient
import fi.riista.common.network.calls.NetworkResponse

class EmailService internal constructor(
    internal val networkClient: NetworkClient
) {
    suspend fun sendPasswordResetEmail(email: String, language: Language): NetworkResponse<Unit> {
        return networkClient.performRequest(RequestPasswordReset(
            passwordResetDTO = RequestPasswordResetDTO(email, language.languageCode)
        ))
    }

    suspend fun sendStartRegistrationEmail(email: String, language: Language): NetworkResponse<Unit> {
        return networkClient.performRequest(RequestStartRegistration(
            startRegistrationDTO = StartRegistrationDTO(email, language.languageCode)
        ))

    }
}
