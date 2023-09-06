package fi.riista.common.domain.harvest.ui.list

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.harvest.HarvestContext
import fi.riista.common.domain.harvest.HarvestFilter
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.model.Species
import fi.riista.common.logging.getLogger
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class ListCommonHarvestsController(
    private val harvestContext: HarvestContext,
    private val listOnlyHarvestsWithImages: Boolean, // true for gallery
) : ControllerWithLoadableModel<ListCommonHarvestsViewModel>(),
    HasUnreproducibleState<ListCommonHarvestsController.FilterState> {

    private val repository = harvestContext.repository

    /**
     * Pending filter to be applied once view model is loaded. Only taken into account if viewmodel
     * has not been loaded when [loadViewModel] is called.
     *
     * Same thing could probably be achieved using [restoredFilterState] but that is conceptually
     * not meant for this purpose.
     */
    private var pendingFilter: FilterState? = null

    private var restoredFilterState: FilterState? = null

    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<ListCommonHarvestsViewModel>> = flow {
        // store the previously loaded viewmodel in case were just refreshing harvests. We want to keep
        // the current filtering after all.
        val previouslyLoadedViewModel = getLoadedViewModelOrNull()

        emit(ViewModelLoadStatus.Loading)

        val harvestHuntingYears = harvestContext.getHarvestHuntingYears().sortedDescending()

        val viewModel = if (previouslyLoadedViewModel != null) {
            val harvests = fetchWithFilter(
                ownHarvests = previouslyLoadedViewModel.filterOwnHarvests,
                huntingYear = previouslyLoadedViewModel.filterHuntingYear,
                withSpecies = previouslyLoadedViewModel.filterSpecies,
            ) ?: kotlin.run {
                logger.w { "Unable to load harvests" }
                emit(ViewModelLoadStatus.LoadFailed)
                return@flow
            }
            previouslyLoadedViewModel.copy(
                harvestHuntingYears = harvestHuntingYears,
                filteredHarvests = harvests,
            )
        } else {
            val harvests = fetchWithFilter(
                ownHarvests = restoredFilterState?.ownHarvests ?: pendingFilter?.ownHarvests ?: true,
                huntingYear = restoredFilterState?.huntingYear ?: pendingFilter?.huntingYear,
                withSpecies = restoredFilterState?.species ?: pendingFilter?.species,
            ) ?: kotlin.run {
                logger.w { "Unable to load harvests" }
                emit(ViewModelLoadStatus.LoadFailed)
                return@flow
            }
            ListCommonHarvestsViewModel(
                harvestHuntingYears = harvestHuntingYears,
                filterOwnHarvests = restoredFilterState?.ownHarvests ?: pendingFilter?.ownHarvests ?: true,
                filterHuntingYear = restoredFilterState?.huntingYear ?: pendingFilter?.huntingYear,
                filterSpecies = restoredFilterState?.species ?: pendingFilter?.species,
                filteredHarvests = harvests,
            )
        }

        pendingFilter = null
        restoredFilterState = null

        emit(ViewModelLoadStatus.Loaded(viewModel))
    }

    suspend fun setFilters(ownHarvests: Boolean, huntingYear: Int?, species: List<Species>?) {
        val currentViewModel = getLoadedViewModelOrNull() ?: kotlin.run {
            logger.v { "Cannot filter now, no viewmodel. Adding as a pending filter." }
            pendingFilter = FilterState(
                ownHarvests = ownHarvests,
                huntingYear = huntingYear,
                species = species
            )
            return
        }

        val harvests = fetchWithFilter(
            ownHarvests = ownHarvests,
            huntingYear = huntingYear,
            withSpecies = species,
        ) ?: kotlin.run {
            logger.w { "Unable to load harvests" }
            return
        }
        updateViewModel(
            viewModel = ListCommonHarvestsViewModel(
                harvestHuntingYears = currentViewModel.harvestHuntingYears,
                filterOwnHarvests = ownHarvests,
                filterHuntingYear = huntingYear,
                filterSpecies = species,
                filteredHarvests = harvests,
            )
        )
    }

    suspend fun setHuntingYearFilter(huntingYear: Int) {
        val ownHarvests = getLoadedViewModelOrNull()?.filterOwnHarvests ?: true
        val speciesFilter = getLoadedViewModelOrNull()?.filterSpecies
        setFilters(ownHarvests, huntingYear, speciesFilter)
    }

    suspend fun setSpeciesFilter(species: List<Species>) {
        val ownHarvests = getLoadedViewModelOrNull()?.filterOwnHarvests ?: true
        val huntingYearFilter = getLoadedViewModelOrNull()?.filterHuntingYear
        setFilters(ownHarvests, huntingYearFilter, species)
    }

    suspend fun setOwnHarvestsFilter(ownHarvests: Boolean) {
        val huntingYearFilter = getLoadedViewModelOrNull()?.filterHuntingYear
        val speciesFilter = getLoadedViewModelOrNull()?.filterSpecies
        setFilters(ownHarvests, huntingYearFilter, speciesFilter)
    }

    suspend fun clearAllFilters() {
        setFilters(true, null, null)
    }

    private suspend fun fetchWithFilter(
        ownHarvests: Boolean,
        huntingYear: Int?,
        withSpecies: List<Species>?,
    ): List<CommonHarvest>? {

        val username = RiistaSDK.currentUserContext.username  ?: kotlin.run {
            return null
        }
        return repository.filter(
            username = username,
            filter = HarvestFilter(
                ownHarvests = ownHarvests,
                huntingYear = huntingYear,
                species = withSpecies,
                requireImages = listOnlyHarvestsWithImages,
            )
        )
    }

    override fun getUnreproducibleState(): FilterState? {
        return getLoadedViewModelOrNull()?.let { viewModel ->
            FilterState(
                ownHarvests = viewModel.filterOwnHarvests,
                huntingYear = viewModel.filterHuntingYear,
                species = viewModel.filterSpecies,
            )
        }
    }

    override fun restoreUnreproducibleState(state: FilterState) {
        restoredFilterState = state
    }

    @Serializable
    data class FilterState(
        internal val ownHarvests: Boolean,
        internal val huntingYear: Int?,
        internal val species: List<Species>?,
    )

    companion object {
        private val logger by getLogger(ListCommonHarvestsController::class)
    }
}
