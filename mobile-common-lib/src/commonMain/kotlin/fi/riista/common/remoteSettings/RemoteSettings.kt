package fi.riista.common.remoteSettings

import co.touchlab.stately.concurrency.AtomicReference
import fi.riista.common.groupHunting.GroupHuntingAvailabilityResolver
import fi.riista.common.logging.getLogger
import fi.riista.common.model.HunterNumber
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.getJson
import fi.riista.common.util.putJson


class RemoteSettings(internal val preferences: Preferences): GroupHuntingAvailabilityResolver {

    private val remoteSettingsHolder = AtomicReference(defaultRemoteSettings)
    internal val remoteSettings: RemoteSettingsDTO
        get() {
            return remoteSettingsHolder.get()
        }

    init {
        loadRemoteSettingsFromPreferences()
    }

    override fun isGroupHuntingFunctionalityEnabledFor(hunterNumber: HunterNumber?): Boolean {
        val settings = remoteSettings

        if (settings.groupHunting.enabledForAll) {
            return true
        }

        return when(hunterNumber) {
            null -> false
            else -> settings.groupHunting.enabledForHunters.contains(hunterNumber)
        }
    }

    fun parseRemoteSettingsJson(remoteSettingsJson: String?): RemoteSettingsDTO? {
        val remoteSettings = remoteSettingsJson?.deserializeFromJson<RemoteSettingsDTO>()
        if (remoteSettings == null) {
            logger.d { "Failed to parse remote settings with json (\"" +
                    "${remoteSettingsJson?.take(32)}\")" }
        }
        return remoteSettings
    }

    fun updateWithRemoteSettings(remoteSettings: RemoteSettingsDTO) {
        logger.v { "Applying remote settings $remoteSettings" }
        updateWithRemoteSettings(remoteSettings, saveToSettings = true)
    }

    internal fun updateWithRemoteSettings(remoteSettings: RemoteSettingsDTO, saveToSettings: Boolean = true) {
        remoteSettingsHolder.set(remoteSettings)
        if (saveToSettings) {
            saveRemoteSettingsToPreferences(remoteSettings)
        }
    }

    private fun loadRemoteSettingsFromPreferences() {
        // load using separate settings key-values in order to reduce the need for migrations
        // in the future. The json parsing could e.g. fail if we introduced a non-null
        // field to settings in the future RiistaSDK versions.
        val enabledForAll = preferences.getBoolean(keyGroupHuntingEnabledForAll)
                ?: defaultRemoteSettings.groupHunting.enabledForAll
        val enabledForHunters = preferences.getJson(keyGroupHuntingEnabledForHunters)
                ?: defaultRemoteSettings.groupHunting.enabledForHunters

        val settings = RemoteSettingsDTO(
                groupHunting = GroupHuntingSettingsDTO(
                        enabledForAll = enabledForAll,
                        enabledForHunters = enabledForHunters
                )
        )
        updateWithRemoteSettings(settings, saveToSettings = false)
    }

    private fun saveRemoteSettingsToPreferences(remoteSettings: RemoteSettingsDTO) {
        preferences.putBoolean(keyGroupHuntingEnabledForAll, remoteSettings.groupHunting.enabledForAll)
        preferences.putJson(keyGroupHuntingEnabledForHunters, remoteSettings.groupHunting.enabledForHunters)
    }

    companion object {
        // internal for testing purposes. The settings are stored using separate variables
        // in order to make things more future proof. It is e.g. possible that new fields
        // are added in the future and that could either break the json-deserialization or
        // would require dropping non-null constraints from settings
        internal const val keyGroupHuntingEnabledForAll = "RS_keyGroupHuntingEnabledForAll"
        internal const val keyGroupHuntingEnabledForHunters = "RS_keyGroupHuntingEnabledForHunters"

        internal val defaultRemoteSettings = RemoteSettingsDTO(
                groupHunting = GroupHuntingSettingsDTO(
                        enabledForAll = false,
                        enabledForHunters = listOf()
                )
        )

        private val logger by getLogger(RemoteSettings::class)
    }
}