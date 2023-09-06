package fi.riista.mobile.utils

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.util.deserializeFromJson
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

object UserInfoMigrationToRiistaSDK {

    fun copyUserInformation(userInfoStore: UserInfoStore, copyFinished: () -> Unit) {
        val userInfoDTO: UserInfoDTO? = userInfoStore.getUserInfoJson()?.deserializeFromJson()

        if (userInfoDTO != null) {
            MainScope().launch {
                // todo: consider removing cached user information if migration succeeds
                // - this requires refactoring all places that use UserInfoStore.getUserInfo() and thus don't do it yet.
                RiistaSDK.currentUserContext.migrateUserInformationFromApplication(userInfo = userInfoDTO)

                copyFinished()
            }
        } else {
            copyFinished()
        }
    }

}
