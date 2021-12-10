package fi.riista.mobile

import fi.riista.common.remoteSettings.RemoteSettingsDTO

/**
Can be used to change some values received from remote config
 */
object RemoteSettingsUpdater {
    @JvmStatic
    fun updateRiistaSdkSettings(remoteSettings: RemoteSettingsDTO): RemoteSettingsDTO {
        return remoteSettings.copy(groupHunting = remoteSettings.groupHunting.copy(enabledForAll = true))
    }
}
