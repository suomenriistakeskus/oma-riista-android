package fi.riista.common.remoteSettings

import fi.riista.common.dto.HunterNumberDTO
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.getJson
import fi.riista.common.util.putJson
import fi.riista.common.util.serializeToJson
import kotlin.test.*


private const val HUNTER_NUMBER = "11111111"

class RemoteSettingsTest {

    @Test
    fun testDefaultSettingsAreUsedIfNoSavedSettings() {
        val remoteSettings = remoteSettings()
        assertEquals(RemoteSettings.defaultRemoteSettings, remoteSettings.remoteSettings)
    }

    @Test
    fun testSettingsCanBeUpdatedWithJson() {
        val remoteSettings = remoteSettings()

        val updatedSettings = settingsDto()

        assertNotEquals(remoteSettings.remoteSettings, updatedSettings)
        remoteSettings.parseRemoteSettingsJson(updatedSettings.serializeToJson())
            ?.also { settingsDTO ->
                remoteSettings.updateWithRemoteSettings(settingsDTO)
            }
        assertEquals(updatedSettings, remoteSettings.remoteSettings)
    }

    @Test
    fun testSettingsAreSaved() {
        val preferences = MockPreferences()
        assertFalse(preferences.contains(RemoteSettings.keyGroupHuntingEnabledForAll))
        assertFalse(preferences.contains(RemoteSettings.keyGroupHuntingEnabledForHunters))

        val remoteSettings = remoteSettings(preferences)
        val dto = settingsDto()
        remoteSettings.parseRemoteSettingsJson(dto.serializeToJson())
            ?.also { settingsDTO ->
                remoteSettings.updateWithRemoteSettings(settingsDTO)
            }

        assertEquals(dto.groupHunting.enabledForAll,
                     preferences.getBoolean(RemoteSettings.keyGroupHuntingEnabledForAll))
        val hunterNumbers: List<String> =
            preferences.getJson(RemoteSettings.keyGroupHuntingEnabledForHunters)!!
        assertEquals(dto.groupHunting.enabledForHunters, hunterNumbers)
    }

    @Test
    fun testSettingsAreLoadedUponCreation() {
        val preferences = MockPreferences()
        assertFalse(preferences.contains(RemoteSettings.keyGroupHuntingEnabledForAll))
        assertFalse(preferences.contains(RemoteSettings.keyGroupHuntingEnabledForHunters))

        val settingsDTO = settingsDto()

        preferences.putBoolean(RemoteSettings.keyGroupHuntingEnabledForAll,
                               settingsDTO.groupHunting.enabledForAll)
        preferences.putJson(RemoteSettings.keyGroupHuntingEnabledForHunters,
                            settingsDTO.groupHunting.enabledForHunters)

        val remoteSettings = remoteSettings(preferences)
        assertEquals(settingsDTO, remoteSettings.remoteSettings)
    }

    @Test
    fun testGroupHuntingFunctionalityCanBeEnabledForAll() {
        val remoteSettings = remoteSettings()
        assertFalse(remoteSettings.isGroupHuntingFunctionalityEnabledFor(null))
        assertFalse(remoteSettings.isGroupHuntingFunctionalityEnabledFor(HUNTER_NUMBER))
        remoteSettings.updateWithRemoteSettings(settingsDto(
                groupHuntingEnabledForAll = true,
                groupHuntingEnabledForHunters = listOf()
        ))
        assertTrue(remoteSettings.isGroupHuntingFunctionalityEnabledFor(null))
        assertTrue(remoteSettings.isGroupHuntingFunctionalityEnabledFor(HUNTER_NUMBER))
    }

    @Test
    fun testGroupHuntingFunctionalityCanBeEnabledForHunter() {
        val remoteSettings = remoteSettings()
        assertFalse(remoteSettings.isGroupHuntingFunctionalityEnabledFor(null))
        assertFalse(remoteSettings.isGroupHuntingFunctionalityEnabledFor(HUNTER_NUMBER))
        remoteSettings.updateWithRemoteSettings(settingsDto(
                groupHuntingEnabledForAll = false,
                groupHuntingEnabledForHunters = listOf(HUNTER_NUMBER)
        ))
        assertFalse(remoteSettings.isGroupHuntingFunctionalityEnabledFor(null))
        assertTrue(remoteSettings.isGroupHuntingFunctionalityEnabledFor(HUNTER_NUMBER))
    }

    private fun remoteSettings(preferences: Preferences = MockPreferences()) =
        RemoteSettings(preferences)

    private fun settingsDto(groupHuntingEnabledForAll: Boolean = true,
                            groupHuntingEnabledForHunters: List<HunterNumberDTO> = listOf(HUNTER_NUMBER)
    ) = RemoteSettingsDTO(
            groupHunting = GroupHuntingSettingsDTO(
                    enabledForAll = groupHuntingEnabledForAll,
                    enabledForHunters = groupHuntingEnabledForHunters
            )
    )
}
