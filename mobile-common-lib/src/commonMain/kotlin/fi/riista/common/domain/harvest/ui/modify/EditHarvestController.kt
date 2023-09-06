package fi.riista.common.domain.harvest.ui.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.harvest.HarvestContext
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.huntingclub.selectableForEntries.HuntingClubsSelectableForEntries
import fi.riista.common.domain.permit.harvestPermit.HarvestPermitProvider
import fi.riista.common.domain.permit.harvestPermit.getPermit
import fi.riista.common.domain.season.HarvestSeasons
import fi.riista.common.preferences.Preferences
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.SystemDateTimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


/**
 * A controller for editing [CommonHarvest] data.
 */
class EditHarvestController internal constructor(
    harvestSeasons: HarvestSeasons,
    harvestContext: HarvestContext,
    currentTimeProvider: LocalDateTimeProvider,
    harvestPermitProvider: HarvestPermitProvider,
    selectableHuntingClubs: HuntingClubsSelectableForEntries,
    languageProvider: LanguageProvider,
    preferences: Preferences,
    speciesResolver: SpeciesResolver,
    stringProvider: StringProvider,
) : ModifyHarvestController(
    harvestSeasons = harvestSeasons,
    harvestContext = harvestContext,
    currentTimeProvider = currentTimeProvider,
    harvestPermitProvider = harvestPermitProvider,
    selectableHuntingClubs = selectableHuntingClubs,
    languageProvider = languageProvider,
    preferences = preferences,
    speciesResolver = speciesResolver,
    stringProvider = stringProvider,
) {

    constructor(
        harvestSeasons: HarvestSeasons,
        harvestContext: HarvestContext,
        harvestPermitProvider: HarvestPermitProvider,
        selectableHuntingClubs: HuntingClubsSelectableForEntries,
        languageProvider: LanguageProvider,
        preferences: Preferences,
        speciesResolver: SpeciesResolver,
        stringProvider: StringProvider,
    ) : this(
        harvestSeasons = harvestSeasons,
        harvestContext = harvestContext,
        currentTimeProvider = SystemDateTimeProvider(),
        harvestPermitProvider = harvestPermitProvider,
        selectableHuntingClubs = selectableHuntingClubs,
        languageProvider = languageProvider,
        preferences = preferences,
        speciesResolver = speciesResolver,
        stringProvider = stringProvider,
    )

    var editableHarvest: EditableHarvest? = null

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ModifyHarvestViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        // prefer harvest data in following order:
        // - currently loaded harvest data
        // - restored harvest data
        // - initial harvest data (= editableHarvest)
        val harvestData = getLoadedViewModelOrNull()?.harvest
            ?: restoredHarvestData
            ?: editableHarvest?.harvest?.copy(
                // transform to latest spec version when editing
                harvestSpecVersion = Constants.HARVEST_SPEC_VERSION
            )

        val shooters = harvestContext.getShooters()

        if (harvestData != null) {
            val viewModel = createViewModel(
                harvest = harvestData,
                permit = harvestPermitProvider.getPermit(harvestData),
                ownHarvest = harvestData.actorInfo !is GroupHuntingPerson.Guest,
                shooters = shooters,
            ).applyPendingIntents()

            emit(ViewModelLoadStatus.Loaded(viewModel))
        } else {
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }
}
