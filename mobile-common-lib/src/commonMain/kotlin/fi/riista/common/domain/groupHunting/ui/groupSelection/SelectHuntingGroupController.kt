package fi.riista.common.domain.groupHunting.ui.groupSelection

import co.touchlab.stately.concurrency.AtomicLong
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.isMoose
import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.groupHunting.GroupHuntingClubContext
import fi.riista.common.domain.groupHunting.GroupHuntingClubGroupContext
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.HuntingGroupFilter
import fi.riista.common.domain.groupHunting.model.AcceptStatus
import fi.riista.common.domain.groupHunting.model.GroupHuntingClubTarget
import fi.riista.common.domain.groupHunting.model.HuntingGroupId
import fi.riista.common.domain.groupHunting.model.HuntingGroupTarget
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.domain.model.toSeasonString
import fi.riista.common.logging.getLogger
import fi.riista.common.model.StringWithId
import fi.riista.common.model.localizedWithFallbacks
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.DataFieldProducer
import fi.riista.common.ui.dataField.DataFieldProducerProxy
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.Padding
import fi.riista.common.ui.dataField.StringListField
import fi.riista.common.ui.intent.IntentHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class SelectHuntingGroupController(
    private val groupHuntingContext: GroupHuntingContext,
    private val stringProvider: StringProvider,
    private val languageProvider: LanguageProvider,
    private val speciesResolver: SpeciesResolver,
) : ControllerWithLoadableModel<SelectHuntingGroupViewModel>(),
    IntentHandler<SelectHuntingGroupIntent>,
    HasUnreproducibleState<SelectHuntingGroupController.SelectedFilterValues> {

    val eventDispatcher: SelectHuntingGroupEventDispatcher =
        SelectHuntingGroupEventToIntentMapper(intentHandler = this)

    /**
     * Encapsulates the selection state i.e. selected club + filter state for selecting
     * the hunting group.
     */
    private class SelectionState(
        val clubContext: GroupHuntingClubContext,
    ) {
        val huntingGroupFilter = HuntingGroupFilter(clubContext.huntingGroups)

        val huntingGroupTarget: HuntingGroupTarget?
            get() {
                return huntingGroupFilter.selectedHuntingGroup?.id?.let { huntingGroupId ->
                    HuntingGroupTarget(
                            clubId = clubContext.club.id,
                            huntingGroupId = huntingGroupId
                    )
                }
            }

        val huntingGroupContext: GroupHuntingClubGroupContext?
            get() {
                val huntingGroup = huntingGroupFilter.selectedHuntingGroup
                return if (huntingGroup != null) {
                    clubContext.getHuntingGroupContext(huntingGroup)
                } else {
                    null
                }
            }
    }

    /**
     * The current selection state. Only exists if club has been selected.
     */
    private var selectionState: SelectionState? = null

    private var stateToRestore: SelectedFilterValues? = null

    /**
     * The id of the hunting group for which data fetch was performed last time?
     *
     * Allows limiting fetching data again.
     */
    private val lastDataFetchHuntingGroupId = AtomicLong(-1L)

    private lateinit var dataFieldProducers:
            List<DataFieldProducer<SelectionState?, SelectHuntingGroupField>>

    init {
        initializeDataFieldProducers()
    }

    /**
     * Fetches the data for the selected hunting group if necessary.
     *
     * This function should be called everytime ViewModel changes.
     */
    suspend fun fetchHuntingGroupDataIfNeeded(refresh: Boolean = false) {
        val huntingGroupContext = selectionState?.huntingGroupContext
                ?: kotlin.run {
                    logger.v { "Refusing to fetch data, no hunting group context available!" }
                    return
                }

        val huntingGroupId = huntingGroupContext.huntingGroup.id
        if (huntingGroupId == lastDataFetchHuntingGroupId.get() && !refresh) {
            logger.v { "Data has been already fetched for hunting group $huntingGroupId." }
            return
        }

        lastDataFetchHuntingGroupId.set(huntingGroupId)

        logger.v { "Fetching the data for hunting group $huntingGroupId." }

        huntingGroupContext.fetchAllData(refresh)

        // it is possible that hunting group was changed while fetching the data. Only update
        // viewmodel if we're still fetching data for the original hunting group
        if (lastDataFetchHuntingGroupId.get() == huntingGroupId) {
            logger.v { "Data fetch completed for hunting group $huntingGroupId. Refreshing viewmodel." }
            refreshViewModel()
        } else {
            logger.v { "Data fetch completed for hunting group $huntingGroupId, " +
                    "but hunting group changed while fetching. Not refreshing viewmodel." }
        }
    }

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<SelectHuntingGroupViewModel>> = flow {

        // update the state which we'll try to restore. This needs to be done before entering
        // loading state as we may want to restore the current state which is only available
        // if already in loaded state
        updateStateToRestore()

        if (!groupHuntingContext.groupHuntingAvailable || refresh) {
            emit(ViewModelLoadStatus.Loading)
            groupHuntingContext.checkAvailabilityAndFetchClubs(refresh = refresh)
        }

        if (!groupHuntingContext.groupHuntingAvailable) {
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        // no need for specific loading state i.e. go directly from NotLoaded to Loaded
        val clubContexts = getClubContexts()

        val stateWasRestored = stateToRestore?.let { state ->
            selectClub(state.clubId)
            state.huntingYear?.let { selectHuntingYear(it) }
            state.speciesCode?.let { selectSpecies(it) }
            state.huntingGroupId?.let { selectHuntingGroup(it) }
            true
        } ?: false

        if (!stateWasRestored) {
            selectionState = if (clubContexts.size == 1) {
                SelectionState(clubContexts[0])
            } else {
                null
            }
        }

        // state restoration has been done. Clear the stateToRestore in order to
        // NOT use same values again when restoring
        clearStateToRestore()

        emit(ViewModelLoadStatus.Loaded(
                viewModel = createViewModel()
        ))
    }

    override fun handleIntent(intent: SelectHuntingGroupIntent) {
        when (intent) {
            is SelectHuntingGroupIntent.SelectClub -> {
                if (!selectClub(intent.clubId)) {
                    return
                }
            }
            is SelectHuntingGroupIntent.SelectSeason -> {
                selectHuntingYear(intent.huntingYear)
            }
            is SelectHuntingGroupIntent.SelectSpecies -> {
                selectSpecies(intent.speciesCode)
            }
            is SelectHuntingGroupIntent.SelectHuntingGroup -> {
                selectHuntingGroup(intent.huntingGroupId)
            }
        }

        refreshViewModel()
    }

    private fun createViewModel(): SelectHuntingGroupViewModel {
        val currentState = selectionState

        return SelectHuntingGroupViewModel(
                fields = dataFieldProducers.mapNotNull {
                    it.produceDataField(currentState)
                },
                canCreateHarvest = canCreateHarvest(currentState),
                canCreateObservation = canCreateObservation(currentState),
                proposedEventsCount = countProposedEvents(currentState),
                selectedSpecies = currentState?.huntingGroupFilter?.selectedSpecies,
                selectedHuntingGroupTarget = currentState?.huntingGroupTarget
        )
    }

    private fun refreshViewModel() {
        updateViewModel(ViewModelLoadStatus.Loaded(
                viewModel = createViewModel()
        ))
    }

    private fun canCreateObservation(currentState: SelectionState?): Boolean {
        return (currentState?.huntingGroupContext?.huntingStatusProvider?.status?.canCreateHarvest ?: false) &&
                (currentState?.huntingGroupFilter?.selectedSpecies?.isMoose() ?: false)
    }

    private fun canCreateHarvest(currentState: SelectionState?): Boolean {
        return currentState?.huntingGroupContext?.huntingStatusProvider?.status?.canCreateHarvest ?: false
    }

    private fun countProposedEvents(currentState: SelectionState?): Int {
        return currentState?.huntingGroupContext?.diaryProvider?.diary?.let { diary ->
            diary.harvests.count { it.acceptStatus == AcceptStatus.PROPOSED } +
                    diary.observations.count { it.acceptStatus == AcceptStatus.PROPOSED }
        } ?: 0
    }

    private fun selectClub(clubId: OrganizationId): Boolean {
        if (clubId == selectionState?.clubContext?.club?.id) {
            // selected same club, nothing to do
            return false
        }

        return groupHuntingContext.findClubContext(GroupHuntingClubTarget(clubId))
            ?.let {
                selectionState = SelectionState(it)
                true
            } ?: false
    }

    private fun selectHuntingYear(huntingYear: HuntingYear) {
        selectionState?.huntingGroupFilter?.selectableHuntingYears
            ?.firstOrNull { it == huntingYear }
            ?.let {
                selectionState?.huntingGroupFilter?.selectedHuntingYear = it
            }
    }

    private fun selectSpecies(speciesCode: SpeciesCode) {
        selectionState?.huntingGroupFilter?.selectableSpecies
            ?.firstOrNull { it == speciesCode }
            ?.let {
                selectionState?.huntingGroupFilter?.selectedSpecies = it
            }
    }

    private fun selectHuntingGroup(huntingGroupId: HuntingGroupId) {
        selectionState?.huntingGroupFilter?.selectableHuntingGroups
            ?.firstOrNull { it.id == huntingGroupId }
            ?.let {
                selectionState?.huntingGroupFilter?.selectedHuntingGroup = it
            }
    }

    private fun getClubContexts(): List<GroupHuntingClubContext> {
        return groupHuntingContext.clubContexts
    }

    private fun initializeDataFieldProducers() {
        dataFieldProducers = listOf(
                { state: SelectionState? ->
                    val possibleClubs = getClubContexts().mapNotNull { clubContext ->
                        clubContext.club.toStringWithId()
                    }
                    StringListField(
                            id = SelectHuntingGroupField.HUNTING_CLUB,
                            values = possibleClubs,
                            selected = listOfNotNull(state?.clubContext?.club?.id)
                    ) {
                        label = stringProvider.getString(RR.string.group_hunting_label_club)
                        readOnly = false
                        paddingTop = Padding.SMALL
                        paddingBottom = Padding.NONE
                    }
                },
                { state: SelectionState? ->
                    val possibleSeasons = state?.huntingGroupFilter?.selectableHuntingYears
                        ?.map {
                            StringWithId(it.toSeasonString(), it.toLong())
                        }

                    StringListField(
                            id = SelectHuntingGroupField.SEASON,
                            values = possibleSeasons ?: listOf(),
                            selected = listOfNotNull(state?.huntingGroupFilter?.selectedHuntingYear?.toLong())
                    ) {
                        label = stringProvider.getString(RR.string.group_hunting_label_season)
                        readOnly = false
                        paddingTop = Padding.NONE
                        paddingBottom = Padding.NONE
                    }
                },
                { state: SelectionState? ->
                    val possibleSpecies = state?.huntingGroupFilter?.selectableSpecies
                        ?.mapNotNull { speciesCode ->
                            speciesResolver.getSpeciesName(speciesCode)
                                ?.let { speciesName ->
                                    StringWithId(speciesName, speciesCode.toLong())
                                }
                        }

                    StringListField(
                            id = SelectHuntingGroupField.SPECIES,
                            values = possibleSpecies ?: listOf(),
                            selected = listOfNotNull(state?.huntingGroupFilter?.selectedSpecies?.toLong())
                    ) {
                        label = stringProvider.getString(RR.string.group_hunting_label_species)
                        readOnly = false
                        paddingTop = Padding.NONE
                        paddingBottom = Padding.NONE
                    }
                },
                { state: SelectionState? ->
                    val possibleGroups = state?.huntingGroupFilter?.selectableHuntingGroups
                        ?.mapNotNull { group ->
                            group.name.localizedWithFallbacks(languageProvider)
                                ?.let {
                                    StringWithId(it, group.id)
                                }
                        }

                    StringListField(
                            id = SelectHuntingGroupField.HUNTING_GROUP,
                            values = possibleGroups ?: listOf(),
                            selected = listOfNotNull(state?.huntingGroupFilter?.selectedHuntingGroup?.id)
                    ) {
                        label = stringProvider.getString(RR.string.group_hunting_label_hunting_group)
                        readOnly = false
                        paddingTop = Padding.NONE
                        paddingBottom = Padding.NONE
                    }
                },
                { state: SelectionState? ->
                    state?.huntingGroupFilter?.selectedHuntingGroup
                        ?.let {
                            val permitNumber = stringProvider.getFormattedString(
                                    RR.stringFormat.group_hunting_label_permit_formatted,
                                    it.permit.permitNumber
                            )

                            LabelField(
                                    id = SelectHuntingGroupField.PERMIT_INFORMATION,
                                    text = permitNumber,
                                    type = LabelField.Type.CAPTION
                            ) {
                                paddingTop = Padding.SMALL
                                paddingBottom = Padding.SMALL
                            }
                        }


                },
                { state: SelectionState? ->
                    state?.huntingGroupContext?.huntingStatusProvider?.status
                        ?.huntingFinished
                        ?.takeIf { it }
                        ?.let {
                            val huntingHasEndedText = stringProvider.getString(
                                stringId = RR.string.group_hunting_error_hunting_has_finished
                            )

                            LabelField(
                                id = SelectHuntingGroupField.HUNTING_HAS_ENDED,
                                text = huntingHasEndedText,
                                type = LabelField.Type.ERROR
                            ) {
                                paddingTop = Padding.SMALL
                                paddingBottom = Padding.SMALL
                            }
                        }
                },
        ).map { DataFieldProducerProxy(it) }
    }

    /**
     * Call this function before updating viewmodel to loading state!
     */
    private fun updateStateToRestore() {
        // since thin function is called before entering the loading state we can try to
        // obtain the current state. This way we can restore user selections if we're just
        // group hunting data (i.e. viewmodel is in loaded state).
        val currentState = if (viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            getUnreproducibleState()
        } else {
            null
        }

        if (currentState != null) {
            stateToRestore = currentState
        }
    }

    private fun clearStateToRestore() {
        stateToRestore = null
    }

    override fun getUnreproducibleState(): SelectedFilterValues? {
        return selectionState?.let { state ->
            SelectedFilterValues(
                    clubId = state.clubContext.club.id,
                    huntingYear = state.huntingGroupFilter.selectedHuntingYear,
                    speciesCode = state.huntingGroupFilter.selectedSpecies,
                    huntingGroupId = state.huntingGroupFilter.selectedHuntingGroup?.id
            )
        }
    }

    override fun restoreUnreproducibleState(state: SelectedFilterValues) {
        stateToRestore = state
    }

    @Serializable
    data class SelectedFilterValues(
        val clubId: OrganizationId,
        val huntingYear: HuntingYear? = null,
        val speciesCode: SpeciesCode? = null,
        val huntingGroupId: HuntingGroupId? = null
    )

    /**
     * Converts clubs (i.e [Organization]s) to [StringWithId]
     */
    private fun Organization.toStringWithId(): StringWithId? {
        return name.localizedWithFallbacks(languageProvider)
            ?.let { name ->
                StringWithId(name, id)
            }
    }

    companion object {
        private val logger by getLogger(SelectHuntingGroupController::class)
    }
}
