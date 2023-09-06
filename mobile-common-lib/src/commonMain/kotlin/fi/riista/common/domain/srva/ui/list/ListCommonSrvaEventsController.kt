package fi.riista.common.domain.srva.ui.list

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.srva.SrvaContext
import fi.riista.common.domain.srva.SrvaEventFilter
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.logging.getLogger
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class ListCommonSrvaEventsController(
    private val metadataProvider: MetadataProvider,
    private val srvaContext: SrvaContext,
    private val listOnlySrvaEventsWithImages: Boolean, // true for gallery
) : ControllerWithLoadableModel<ListCommonSrvaEventsViewModel>(),
    HasUnreproducibleState<ListCommonSrvaEventsController.FilterState> {

    private val repository = srvaContext.repository

    private val allSrvaSpecies: List<Species> by lazy {
        metadataProvider.srvaMetadata.species + Species.Other
    }

    /**
     * Pending filter to be applied once view model is loaded. Only taken into account if viewmodel
     * has not been loaded when [loadViewModel] is called.
     *
     * Same thing could probably be achieved using [restoredFilterState] but that is conceptually
     * not meant for this purpose.
     */
    private var pendingFilter: FilterState? = null

    private var restoredFilterState: FilterState? = null

    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<ListCommonSrvaEventsViewModel>> = flow {
        // store the previously loaded viewmodel in case were just refreshing srvas. We want to keep
        // the current filtering after all.
        val previouslyLoadedViewModel = getLoadedViewModelOrNull()

        emit(ViewModelLoadStatus.Loading)

        val srvaEventYears = srvaContext.getSrvaYears().sortedDescending()

        val viewModel = if (previouslyLoadedViewModel != null) {
            val srvaEvents = fetchWithFilter(
                year = previouslyLoadedViewModel.filterYear,
                withSpecies = previouslyLoadedViewModel.filterSpecies,
            ) ?: kotlin.run {
                logger.w { "Unable to load srva events" }
                emit(ViewModelLoadStatus.LoadFailed)
                return@flow
            }
            previouslyLoadedViewModel.copy(
                srvaEventYears = srvaEventYears,
                srvaSpecies = allSrvaSpecies,
                filteredSrvaEvents = srvaEvents,
            )
        } else {
            val srvaEvents = fetchWithFilter(
                year = restoredFilterState?.year ?: pendingFilter?.year,
                withSpecies = restoredFilterState?.species ?: pendingFilter?.species,
            ) ?: kotlin.run {
                logger.w { "Unable to load srva events" }
                emit(ViewModelLoadStatus.LoadFailed)
                return@flow
            }
            ListCommonSrvaEventsViewModel(
                srvaEventYears = srvaEventYears,
                srvaSpecies = allSrvaSpecies,
                filterYear = restoredFilterState?.year ?: pendingFilter?.year,
                filterSpecies = restoredFilterState?.species ?: pendingFilter?.species,
                filteredSrvaEvents = srvaEvents,
            )
        }

        pendingFilter = null
        restoredFilterState = null

        emit(ViewModelLoadStatus.Loaded(viewModel))
    }

    fun setFilters(year: Int?, species: List<Species>?) {
        val currentViewModel = getLoadedViewModelOrNull() ?: kotlin.run {
            logger.v { "Cannot filter now, no viewmodel. Adding as a pending filter." }
            pendingFilter = FilterState(
                year = year,
                species = species
            )
            return
        }
        val srvaEvents = fetchWithFilter(
            year = year,
            withSpecies = species,
        ) ?: kotlin.run {
            logger.w { "Unable to load srva events" }
            return
        }
        updateViewModel(
            viewModel = currentViewModel.copy(
                filterYear = year,
                filterSpecies = species,
                filteredSrvaEvents = srvaEvents,
            )
        )
    }

    fun setYearFilter(year: Int) {
        val speciesFilter = getLoadedViewModelOrNull()?.filterSpecies
        setFilters(year, speciesFilter)
    }

    fun setSpeciesFilter(species: List<Species>) {
        val yearFilter = getLoadedViewModelOrNull()?.filterYear
        setFilters(yearFilter, species)
    }

    fun clearAllFilters() {
        setFilters(null, null)
    }

    private fun fetchWithFilter(
        year: Int?,
        withSpecies: List<Species>?,
    ): List<CommonSrvaEvent>? {
        val username = RiistaSDK.currentUserContext.username  ?: kotlin.run {
            return null
        }
        return repository.filter(
            username = username,
            filter = SrvaEventFilter(
                year = year,
                species = withSpecies,
                requireImages = listOnlySrvaEventsWithImages,
            )
        )
    }

    override fun getUnreproducibleState(): FilterState? {
        return getLoadedViewModelOrNull()?.let { viewModel ->
            FilterState(
                year = viewModel.filterYear,
                species = viewModel.filterSpecies,
            )
        }
    }

    override fun restoreUnreproducibleState(state: FilterState) {
        restoredFilterState = state
    }

    @Serializable
    data class FilterState(
        internal val year: Int?,
        internal val species: List<Species>?,
    )

    companion object {
        private val logger by getLogger(ListCommonSrvaEventsController::class)
    }
}
