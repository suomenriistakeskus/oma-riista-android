package fi.riista.common.domain.userInfo

import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.domain.dto.toUserInformation
import fi.riista.common.domain.groupHunting.GroupHuntingAvailabilityResolver
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.huntingclub.HuntingClubsContext
import fi.riista.common.domain.model.OccupationType
import fi.riista.common.domain.model.UserInformation
import fi.riista.common.domain.training.TrainingContext
import fi.riista.common.domain.userInfo.repository.UserInformationRepository
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.Preferences
import fi.riista.common.reactive.Observable
import fi.riista.common.util.contains

sealed class LoginStatus {
    open val userInformation: UserInformation? = null

    object NotLoggedIn: LoginStatus()
    data class LoggedIn(override val userInformation: UserInformation): LoginStatus()
}

enum class UserInformationMigrationResult {
    ALREADY_MIGRATED,
    MIGRATED,
    REFUSED,
}

class UserContext internal constructor(
    backendApiProvider: BackendApiProvider,
    private val groupHuntingAvailabilityResolver: GroupHuntingAvailabilityResolver,
    private val userInformationRepository: UserInformationRepository,
    private val preferences: Preferences,
): BackendApiProvider by backendApiProvider,
    CarnivoreAuthorityInformationProvider,
    UsernameProvider {
    val loginStatus = Observable<LoginStatus>(LoginStatus.NotLoggedIn)

    /**
     * Convenience check for whether user has logged in or not
     */
    val isLoggedIn: Boolean
        get() {
            return loginStatus.value is LoginStatus.LoggedIn
        }

    /**
     * Username of the logged in user, or if login failed then fallback/cached username if available.
     */
    override val username: String?
        get() {
            return userInformation?.username ?: cachedUsername
        }

    private val cachedUsername: String?
        get() {
            return preferences.getString(FALLBACK_USERNAME_KEY)
        }

    /**
     * Gets information about the current user.
     *
     * The provided information may come from either
     * - recent login response
     * - database
     */
    val userInformation: UserInformation?
        get() {
            return loginStatus.value.userInformation
                ?: cachedUsername?.let { username ->
                    userInformationRepository.getUserInformation(username = username)
                }
        }

    /**
     * Is the user carnivore authority? Falls back to stored information if not logged in.
     */
    override val userIsCarnivoreAuthority: Boolean
        get() {
            // Don't migrate just yet away from fallbacking to getting value from preferences since
            // userInformationRepository does not necessarily have user information (it was added later)
            return userInformation?.carnivoreAuthority
                ?: preferences.getBoolean(FALLBACK_USER_IS_CARNIVORE_AUTHORITY, defaultValue = null)
                ?: false
        }

    val groupHuntingContext by lazy {
        GroupHuntingContext(
            backendApiProvider = this,
            isGroupHuntingAvailable = { isGroupHuntingAvailable() },
        )
    }

    val huntingClubsContext by lazy {
        HuntingClubsContext(
            backendApiProvider = this
        )
    }

    val trainingContext by lazy {
        TrainingContext(
            backendApiProvider = backendApiProvider,
        )
    }

    private fun isGroupHuntingAvailable(): Boolean {
        return groupHuntingAvailabilityResolver
            .isGroupHuntingFunctionalityEnabledFor(userInformation?.hunterNumber)
    }

    /**
     * Migrates user information from the application to the common lib.
     */
    suspend fun migrateUserInformationFromApplication(userInfo: UserInfoDTO): UserInformationMigrationResult {
        // nothing to do if there already is user information on the repository
        if (userInformationRepository.hasUserInformation(userInfo.username)) {
            return UserInformationMigrationResult.ALREADY_MIGRATED
        }

        // migration requires that the previously logged in user is the same
        if (userInfo.username != cachedUsername) {
            return UserInformationMigrationResult.REFUSED
        }

        userInformationRepository.saveUserInformation(userInfo.toUserInformation())
        return UserInformationMigrationResult.MIGRATED
    }

    suspend fun userLoggedIn(userInfo: UserInfoDTO) {
        val userInformation = userInfo.toUserInformation()

        userInformationRepository.saveUserInformation(userInformation = userInformation)
        preferences.putString(FALLBACK_USERNAME_KEY, userInformation.username)
        preferences.putBoolean(FALLBACK_USER_IS_CARNIVORE_AUTHORITY, userInformation.carnivoreAuthority)

        loginStatus.set(LoginStatus.LoggedIn(userInformation))
    }

    suspend fun userLoggedOut() {
        groupHuntingContext.clear()
        huntingClubsContext.clear()
        trainingContext.clear()

        username?.let { userInformationRepository.deleteUserInformation(it) }
        preferences.remove(FALLBACK_USERNAME_KEY)
        preferences.remove(FALLBACK_USER_IS_CARNIVORE_AUTHORITY)

        loginStatus.set(LoginStatus.NotLoggedIn)
    }

    companion object {
        internal const val FALLBACK_USERNAME_KEY = "uc_fallback_username"
        private const val FALLBACK_USER_IS_CARNIVORE_AUTHORITY = "uc_carnivore_authority"
    }
}

internal val UserInformation.carnivoreAuthority: Boolean
    get() {
        return occupations.contains { occupation ->
            occupation.occupationType.value == OccupationType.CARNIVORE_AUTHORITY
        }
    }
