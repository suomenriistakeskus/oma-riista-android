package fi.riista.common.domain.harvest.ui.settings

sealed class HarvestSettingsIntent {
    class ChangeOtherHuntersEnabled(val enabled: Boolean) : HarvestSettingsIntent()
    class ChangeClubSelectionEnabled(val enabled: Boolean) : HarvestSettingsIntent()
}
