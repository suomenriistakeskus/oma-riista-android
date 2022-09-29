package fi.riista.common.domain.userInfo

import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.domain.dto.toOccupation
import fi.riista.common.domain.groupHunting.GroupHuntingAvailabilityResolver
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.huntingControl.HuntingControlContext
import fi.riista.common.domain.huntingclub.HuntingClubsContext
import fi.riista.common.domain.model.OccupationType
import fi.riista.common.domain.training.TrainingContext
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.Preferences
import fi.riista.common.reactive.Observable
import fi.riista.common.util.LocalDateTimeProvider

sealed class LoginStatus {
    class NotLoggedIn: LoginStatus()
    // class LoggingIn: LoginStatus() // todo: consider adding
    class LoggedIn(val userInfoDTO: UserInfoDTO): LoginStatus()
    // class LoginError: LoginStatus() // todo: consider adding

    val userInfo: UserInfoDTO?
        get() {
            return when (this) {
                is LoggedIn -> userInfoDTO
                else -> null
            }
        }
}

class UserContext internal constructor(
    backendApiProvider: BackendApiProvider,
    private val groupHuntingAvailabilityResolver: GroupHuntingAvailabilityResolver,
    private val preferences: Preferences,
    localDateTimeProvider: LocalDateTimeProvider,
    commonFileProvider: CommonFileProvider,
): BackendApiProvider by backendApiProvider, CarnivoreAuthorityInformationProvider {
    val loginStatus = Observable<LoginStatus>(LoginStatus.NotLoggedIn())

    /**
     * Username of the logged in user, or if login failed then fallback username if available.
     */
    val username: String?
        get() {
            return userInfo?.username ?: preferences.getString(FALLBACK_USERNAME_KEY)
        }

    /**
     * Is the user carnivore authority? Falls back to stored information if not logged in.
     */
    override val userIsCarnivoreAuthority: Boolean
        get() {
            return userInfo?.carnivoreAuthority
                ?: preferences.getBoolean(FALLBACK_USER_IS_CARNIVORE_AUTHORITY, defaultValue = null)
                ?: false
        }

    val userInfo: UserInfoDTO?
        get() {
            return loginStatus.value.userInfo
        }

    val groupHuntingContext by lazy {
        GroupHuntingContext(
            backendApiProvider = this,
            isGroupHuntingAvailable = { isGroupHuntingAvailable() },
        )
    }

    val huntingClubsContext by lazy {
        HuntingClubsContext(backendApiProvider = this)
    }

    val huntingControlContext by lazy {
        HuntingControlContext(
            backendApiProvider = this,
            preferences = preferences,
            localDateTimeProvider = localDateTimeProvider,
            commonFileProvider = commonFileProvider,
        )
    }

    val trainingContext by lazy {
        TrainingContext(
            backendApiProvider = backendApiProvider,
        )
    }

    private fun isGroupHuntingAvailable(): Boolean {
        return groupHuntingAvailabilityResolver
            .isGroupHuntingFunctionalityEnabledFor(userInfo?.hunterNumber)
    }

    fun userLoggedIn(userInfo: UserInfoDTO) {
        preferences.putString(FALLBACK_USERNAME_KEY, userInfo.username)
        preferences.putBoolean(FALLBACK_USER_IS_CARNIVORE_AUTHORITY, userInfo.carnivoreAuthority)
        loginStatus.set(LoginStatus.LoggedIn(userInfo))
    }

    fun userLoggedOut() {
        groupHuntingContext.clear()
        huntingClubsContext.clear()
        huntingControlContext.clear()
        trainingContext.clear()
        preferences.remove(FALLBACK_USERNAME_KEY)
        preferences.remove(FALLBACK_USER_IS_CARNIVORE_AUTHORITY)
        loginStatus.set(LoginStatus.NotLoggedIn())
    }

    companion object {
        private const val FALLBACK_USERNAME_KEY = "uc_fallback_username"
        private const val FALLBACK_USER_IS_CARNIVORE_AUTHORITY = "uc_carnivore_authority"
    }
}

internal val UserInfoDTO.carnivoreAuthority: Boolean
    get() {
        val carnivoreAuthorityOccupation = occupations.firstOrNull { occupationDTO ->
            val occupation = occupationDTO.toOccupation()
            occupation.occupationType.value == OccupationType.CARNIVORE_AUTHORITY
        }

        return carnivoreAuthorityOccupation != null
    }