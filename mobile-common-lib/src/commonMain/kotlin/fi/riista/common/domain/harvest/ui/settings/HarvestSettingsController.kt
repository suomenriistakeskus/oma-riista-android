package fi.riista.common.domain.harvest.ui.settings

import fi.riista.common.domain.harvest.ui.settings.HarvestSettingsController.Companion.KEY_CLUB_SELECTION_ENABLED
import fi.riista.common.domain.harvest.ui.settings.HarvestSettingsController.Companion.KEY_OTHER_HUNTERS_ENABLED
import fi.riista.common.preferences.Preferences
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.BooleanField
import fi.riista.common.ui.dataField.FieldRequirement
import fi.riista.common.ui.intent.IntentHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HarvestSettingsController(
    private val stringProvider: StringProvider,
    private val preferences: Preferences,
) : ControllerWithLoadableModel<HarvestSettingsViewModel>()
  , IntentHandler<HarvestSettingsIntent> {

    val eventDispatchers: HarvestSettingsEventDispatcher by lazy {
        HarvestSettingsEventToIntentMapper(intentHandler = this)
    }

    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<HarvestSettingsViewModel>> = flow {
        val existingViewModel = getLoadedViewModelOrNull()
        if (existingViewModel != null) {
            return@flow
        }

        val viewModel = createViewModel(
            settings = HarvestSettingsData(
                harvestForOtherHunterEnabled = preferences.showActorSelection(),
                clubSelectionEnabled = preferences.isClubSelectionEnabledForHarvests()
            )
        )
        emit(ViewModelLoadStatus.Loaded(viewModel))
    }

    override fun handleIntent(intent: HarvestSettingsIntent) {
        val settings = getLoadedViewModelOrNull()?.settings
            ?: HarvestSettingsData(
                harvestForOtherHunterEnabled = false,
                clubSelectionEnabled = false,
            )

        val updatedSettings = when (intent) {
            is HarvestSettingsIntent.ChangeOtherHuntersEnabled -> {
                preferences.putBoolean(KEY_OTHER_HUNTERS_ENABLED, intent.enabled)
                settings.copy(harvestForOtherHunterEnabled = intent.enabled)
            }
            is HarvestSettingsIntent.ChangeClubSelectionEnabled -> {
                preferences.putBoolean(KEY_CLUB_SELECTION_ENABLED, intent.enabled)
                settings.copy(clubSelectionEnabled = intent.enabled)
            }
        }

        updateViewModel(
            viewModel = createViewModel(settings = updatedSettings)
        )
    }

    private fun createViewModel(settings: HarvestSettingsData): HarvestSettingsViewModel {
        return HarvestSettingsViewModel(
            settings = settings,
            fields = listOf(
                BooleanField(HarvestSettingsField.HARVEST_FOR_OTHER_HUNTER, settings.harvestForOtherHunterEnabled) {
                    readOnly = false
                    requirementStatus = FieldRequirement.noRequirement()
                    label = stringProvider.getString(RR.string.harvest_settings_add_harvest_for_other_hunter)
                    text = stringProvider.getString(RR.string.harvest_settings_add_harvest_for_other_hunter_explanation)
                    appearance = BooleanField.Appearance.SWITCH
                },
                BooleanField(HarvestSettingsField.ENABLE_CLUB_SELECTION, settings.clubSelectionEnabled) {
                    readOnly = false
                    requirementStatus = FieldRequirement.noRequirement()
                    label = stringProvider.getString(RR.string.harvest_settings_enable_club_selection)
                    text = stringProvider.getString(RR.string.harvest_settings_enable_club_selection_explanation)
                    appearance = BooleanField.Appearance.SWITCH
                }
            )
        )
    }

    companion object {
        const val KEY_OTHER_HUNTERS_ENABLED = "HarvestForOtherHuntersEnabled"
        const val KEY_CLUB_SELECTION_ENABLED = "HarvestSettingsClubSelectionEnabled"
    }
}

fun Preferences.showActorSelection() = this.getBoolean(KEY_OTHER_HUNTERS_ENABLED) ?: false
fun Preferences.isClubSelectionEnabledForHarvests() = this.getBoolean(KEY_CLUB_SELECTION_ENABLED) ?: false
