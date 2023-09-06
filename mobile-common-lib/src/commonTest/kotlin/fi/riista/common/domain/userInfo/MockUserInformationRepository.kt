package fi.riista.common.domain.userInfo

import fi.riista.common.domain.model.UserInformation
import fi.riista.common.domain.userInfo.repository.UserInformationRepository

class MockUserInformationRepository: UserInformationRepository {
    internal val userInformations = mutableMapOf<String, UserInformation>()

    override suspend fun saveUserInformation(userInformation: UserInformation) {
        userInformations[userInformation.username] = userInformation
    }

    override fun hasUserInformation(username: String): Boolean {
        return userInformations.contains(username)
    }

    override fun getUserInformation(username: String): UserInformation? {
        return userInformations[username]
    }

    override suspend fun deleteUserInformation(username: String) {
        userInformations.remove(username)
    }
}

