package fi.riista.common.domain.observation.ui.list

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.ObservationContext
import fi.riista.common.domain.observation.ObservationFilter
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.logging.getLogger
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class ListCommonObservationsController(
    private val metadataProvider: MetadataProvider,
    private val observationContext: ObservationContext,
    private val listOnlyObservationsWithImages: Boolean, // true for gallery
) : ControllerWithLoadableModel<ListCommonObservationsViewModel>(),
    HasUnreproducibleState<ListCommonObservationsController.FilterState> {

    private val repository = observationContext.repository

    /**
     * Pending filter to be applied once view model is loaded. Only taken into account if viewmodel
     * has not been loaded when [loadViewModel] is called.
     *
     * Same thing could probably be achieved using [restoredFilterState] but that is conceptually
     * not meant for this purpose.
     */
    private var pendingFilter: FilterState? = null

    private var restoredFilterState: FilterState? = null

    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<ListCommonObservationsViewModel>> = flow {
        // store the previously loaded viewmodel in case were just refreshing observations. We want to keep
        // the current filtering after all.
        val previouslyLoadedViewModel = getLoadedViewModelOrNull()

        emit(ViewModelLoadStatus.Loading)

        val observationHuntingYears = observationContext.getObservationHuntingYears().sortedDescending()

        val viewModel = if (previouslyLoadedViewModel != null) {
            val observations = fetchWithFilter(
                huntingYear = previouslyLoadedViewModel.filterHuntingYear,
                withSpecies = previouslyLoadedViewModel.filterSpecies,
            ) ?: kotlin.run {
                logger.w { "Unable to load observations" }
                emit(ViewModelLoadStatus.LoadFailed)
                return@flow
            }
            previouslyLoadedViewModel.copy(
                observationHuntingYears = observationHuntingYears,
                filteredObservations = observations,
            )
        } else {
            val observations = fetchWithFilter(
                huntingYear = restoredFilterState?.huntingYear ?: pendingFilter?.huntingYear,
                withSpecies = restoredFilterState?.species ?: pendingFilter?.species,
            ) ?: kotlin.run {
                logger.w { "Unable to load observations" }
                emit(ViewModelLoadStatus.LoadFailed)
                return@flow
            }
            ListCommonObservationsViewModel(
                observationHuntingYears = observationHuntingYears,
                filterHuntingYear = restoredFilterState?.huntingYear ?: pendingFilter?.huntingYear,
                filterSpecies = restoredFilterState?.species ?: pendingFilter?.species,
                filteredObservations = observations,
            )
        }

        pendingFilter = null
        restoredFilterState = null

        emit(ViewModelLoadStatus.Loaded(viewModel))
    }

    fun setFilters(huntingYear: Int?, species: List<Species>?) {
        val currentViewModel = getLoadedViewModelOrNull() ?: kotlin.run {
            logger.v { "Cannot filter now, no viewmodel. Adding as a pending filter." }
            pendingFilter = FilterState(
                huntingYear = huntingYear,
                species = species
            )
            return
        }
        val observations = fetchWithFilter(
            huntingYear = huntingYear,
            withSpecies = species,
        ) ?: kotlin.run {
            logger.w { "Unable to load observations" }
            return
        }

        updateViewModel(
            viewModel = currentViewModel.copy(
                filterHuntingYear = huntingYear,
                filterSpecies = species,
                filteredObservations = observations,
            )
        )
    }

    fun setHuntingYearFilter(huntingYear: Int) {
        val speciesFilter = getLoadedViewModelOrNull()?.filterSpecies
        setFilters(huntingYear, speciesFilter)
    }

    fun setSpeciesFilter(species: List<Species>) {
        val huntingYearFilter = getLoadedViewModelOrNull()?.filterHuntingYear
        setFilters(huntingYearFilter, species)
    }

    fun clearAllFilters() {
        setFilters(null, null)
    }

    private fun fetchWithFilter(
        huntingYear: Int?,
        withSpecies: List<Species>?,
    ): List<CommonObservation>? {
        val username = RiistaSDK.currentUserContext.username  ?: kotlin.run {
            return null
        }
        return repository.filter(
            username = username,
            filter = ObservationFilter(
                huntingYear = huntingYear,
                species = withSpecies,
                requireImages = listOnlyObservationsWithImages,
            )
        )
    }

    override fun getUnreproducibleState(): FilterState? {
        return getLoadedViewModelOrNull()?.let { viewModel ->
            FilterState(
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
        internal val huntingYear: Int?,
        internal val species: List<Species>?,
    )

    companion object {
        private val logger by getLogger(ListCommonObservationsController::class)
    }
}
