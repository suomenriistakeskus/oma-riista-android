package fi.riista.common.domain.userInfo.repository

import fi.riista.common.database.DatabaseDriverFactory
import fi.riista.common.database.DatabaseWriteContext
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.dao.UserInformationDAO
import fi.riista.common.domain.dao.toUserInformationDAO
import fi.riista.common.domain.dao.toUserInformation
import fi.riista.common.domain.model.UserInformation
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import kotlinx.coroutines.withContext


interface UserInformationRepository {
    suspend fun saveUserInformation(userInformation: UserInformation)
    fun hasUserInformation(username: String): Boolean
    fun getUserInformation(username: String): UserInformation?
    suspend fun deleteUserInformation(username: String)
}

internal class UserInformationDatabaseRepository(
    databaseDriverFactory: DatabaseDriverFactory
): UserInformationRepository {
    private val database = RiistaDatabase(
        driver = databaseDriverFactory.createDriver(),
    )

    private val userInformationQueries = database.dbUserInformationQueries

    override suspend fun saveUserInformation(userInformation: UserInformation) {
        saveUserInformation(userInformationDAO = userInformation.toUserInformationDAO())
    }

    override fun hasUserInformation(username: String): Boolean {
        return userInformationQueries.hasUserInformation(username).executeAsOne()
    }

    override fun getUserInformation(username: String): UserInformation? {
        return getUserInformationDAO(username)?.toUserInformation()
    }

    override suspend fun deleteUserInformation(username: String) = withContext(DatabaseWriteContext) {
        userInformationQueries.deleteUserInformation(username = username)
    }

    private suspend fun saveUserInformation(
        userInformationDAO: UserInformationDAO,
    ) = withContext(DatabaseWriteContext) {
        userInformationQueries.transaction {
            val existingUserInformation = userInformationQueries.selectUserInformation(
                username = userInformationDAO.username,
                json_format_version = UserInformationDAO.DAO_VERSION
            ).executeAsOneOrNull()

            val userInfoJson = userInformationDAO.serializeToJson()
            if (existingUserInformation == null) {
                userInformationQueries.insertUserInformation(
                    username = userInformationDAO.username,
                    json_format_version = UserInformationDAO.DAO_VERSION,
                    user_information_json = userInfoJson
                )
            } else {
                userInformationQueries.updateUserInformation(
                    username = userInformationDAO.username,
                    json_format_version = UserInformationDAO.DAO_VERSION,
                    user_information_json = userInfoJson
                )
            }
        }
    }

    private fun getUserInformationDAO(username: String): UserInformationDAO? {
        val userInformationJson = userInformationQueries.selectUserInformation(
            username = username,
            json_format_version = UserInformationDAO.DAO_VERSION
        ).executeAsOneOrNull()?.user_information_json

        return userInformationJson?.deserializeFromJson()
    }
}
