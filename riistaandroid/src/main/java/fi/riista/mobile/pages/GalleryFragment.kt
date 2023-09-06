package fi.riista.mobile.pages

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.harvest.ui.list.ListCommonHarvestsController
import fi.riista.common.domain.harvest.ui.settings.showActorSelection
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.ui.list.ListCommonObservationsController
import fi.riista.common.domain.srva.ui.list.ListCommonSrvaEventsController
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.mobile.R
import fi.riista.mobile.adapter.GalleryAdapter
import fi.riista.mobile.adapter.GalleryAdapter.GalleryItem
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.ui.GameLogFilterView
import fi.riista.mobile.ui.OwnHarvestsMenuProvider
import fi.riista.mobile.ui.toGameLogImage
import fi.riista.mobile.ui.updateBasedOnViewModel
import fi.riista.mobile.utils.UiUtils
import fi.riista.mobile.utils.UserInfoStore
import fi.riista.mobile.utils.toVisibility
import fi.riista.mobile.viewmodel.GameLogViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class GalleryFragment : GameLogFilterView.GameLogFilterListener, PageFragment() {

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var userInfoStore: UserInfoStore

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GalleryAdapter
    private lateinit var viewModel: GameLogViewModel

    private lateinit var filterView: GameLogFilterView
    private lateinit var forOthersTextView: TextView

    private val disposeBag = DisposeBag()

    private val ownHarvestsMenuProvider by lazy {
        OwnHarvestsMenuProvider {
            toggleOwnHarvests()
            true
        }
    }

    private val listSrvaEventsController = ListCommonSrvaEventsController(
        metadataProvider = RiistaSDK.metadataProvider,
        srvaContext = RiistaSDK.srvaContext,
        listOnlySrvaEventsWithImages = true,
    )
    private val listObservationsController = ListCommonObservationsController(
        metadataProvider = RiistaSDK.metadataProvider,
        observationContext = RiistaSDK.observationContext,
        listOnlyObservationsWithImages = true,
    )
    private val listHarvestsController = ListCommonHarvestsController(
        harvestContext = RiistaSDK.harvestContext,
        listOnlyHarvestsWithImages = true,
    )

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_gallery, container, false)
        setupActionBar(R.layout.actionbar_gallery, false)

        recyclerView = view.findViewById(R.id.recycler_view)

        adapter = GalleryAdapter(context)

        val layoutManager = GridLayoutManager(context, 2)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter

        filterView = view.findViewById(R.id.gallery_filter_view)
        filterView.listener = this

        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[GameLogViewModel::class.java]

        filterView.setupTypes(
            showSrva = UiUtils.isSrvaVisible(userInfoStore.getUserInfo()),
            showPoi = false,
            selected = viewModel.getTypeSelected().value,
        )
        filterView.setupSeasons(viewModel.getSeasons().value, viewModel.getSeasonSelected().value)
        filterView.setupSpecies(viewModel.getSpeciesSelected().value!!, viewModel.getCategorySelected().value)

        viewModel.getTypeSelected().observe(viewLifecycleOwner) { type ->
            refreshList(type)
        }
        viewModel.isOwnHarvests().observe(viewLifecycleOwner) { ownHarvests ->
            ownHarvestsMenuProvider.setOwnHarvests(ownHarvests)
        }
        filterView.updateBasedOnViewModel(viewModel, viewLifecycleOwner)
        viewModel.getSeasonSelected().observe(viewLifecycleOwner) {
            refreshList(viewModel.getTypeSelected().value)
        }
        viewModel.getSpeciesSelected().observe(viewLifecycleOwner) {
            refreshList(viewModel.getTypeSelected().value)
        }
        forOthersTextView = view.findViewById(R.id.tv_showing_harvest_for_others)

        requireActivity().addMenuProvider(ownHarvestsMenuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
        ownHarvestsMenuProvider.setOwnHarvests(viewModel.isOwnHarvests().value ?: true)
        updateOwnHarvestVisibility()

        return view
    }

    override fun onResume() {
        super.onResume()
        listSrvaEventsController.viewModelLoadStatus.bind { loadStatus ->
            val type = viewModel.getTypeSelected().value
            if (loadStatus is ViewModelLoadStatus.Loaded && type == GameLog.TYPE_SRVA) {
                listSrvaEventsController.getLoadedViewModelOrNull()?.filteredSrvaEvents?.let { srvaEvents ->
                    val items = srvaEvents.mapNotNull { srvaEvent ->
                        val localId = srvaEvent.localId
                        val image = srvaEvent.images.primaryImage?.toGameLogImage()
                        if (localId != null && image != null) {
                            GalleryItem(GameLog.TYPE_SRVA, localId, image)
                        } else {
                            null
                        }
                    }
                    adapter.setDataSet(items)
                }
            }
        }.disposeBy(disposeBag)
        listHarvestsController.viewModelLoadStatus.bind { loadStatus ->
            val type = viewModel.getTypeSelected().value
            if (loadStatus is ViewModelLoadStatus.Loaded && type == GameLog.TYPE_HARVEST) {
                listHarvestsController.getLoadedViewModelOrNull()?.filteredHarvests?.let { harvests ->
                    val items = harvests.mapNotNull { harvest ->
                        val localId = harvest.localId
                        val image = harvest.images.primaryImage?.toGameLogImage()
                        if (localId != null && image != null) {
                            GalleryItem(GameLog.TYPE_HARVEST, localId, image)
                        } else {
                            null
                        }
                    }
                    adapter.setDataSet(items)
                }
            }
        }.disposeBy(disposeBag)
        listObservationsController.viewModelLoadStatus.bind { loadStatus ->
            val type = viewModel.getTypeSelected().value
            if (loadStatus is ViewModelLoadStatus.Loaded && type == GameLog.TYPE_OBSERVATION) {
                listObservationsController.getLoadedViewModelOrNull()?.filteredObservations?.let { observations ->
                    val items = observations.mapNotNull { observation ->
                        val localId = observation.localId
                        val image = observation.images.primaryImage?.toGameLogImage()
                        if (localId != null && image != null) {
                            GalleryItem(GameLog.TYPE_OBSERVATION, localId, image)
                        } else {
                            null
                        }
                    }
                    adapter.setDataSet(items)
                }
            }
        }.disposeBy(disposeBag)
        refreshList(viewModel.getTypeSelected().value)
        ensureCorrectOwnHarvestsStatus()
        updateOwnHarvestVisibility()
        updateForOthersTextVisibility()
    }

    override fun onPause() {
        super.onPause()
        disposeBag.disposeAll()
    }

    private fun refreshList(type: String?) {
        if (!isResumed) return

        when (type) {
            GameLog.TYPE_HARVEST -> harvestsSelected()
            GameLog.TYPE_OBSERVATION -> observationsSelected()
            GameLog.TYPE_SRVA -> srvaEventsSelected()
        }
    }

    private fun harvestsSelected() {
        MainScope().launch {
            listHarvestsController.loadViewModel(refresh = true)
        }
    }

    private fun observationsSelected() {
        MainScope().launch {
            listObservationsController.loadViewModel(refresh = true)
        }
    }

    private fun srvaEventsSelected() {
        MainScope().launch {
            listSrvaEventsController.loadViewModel(refresh = true)
        }
    }

    override fun onLogTypeSelected(type: String) {
        viewModel.selectLogType(type)
        updateOwnHarvestVisibility()
        updateFilters()
    }

    override fun onLogSeasonSelected(season: Int) {
        viewModel.selectLogSeason(season)
        updateFilters()
    }

    override fun onLogSpeciesSelected(speciesIds: List<Int>) {
        viewModel.selectSpeciesIds(speciesIds)
        updateFilters()
    }

    override fun onLogSpeciesCategorySelected(categoryId: Int) {
        viewModel.selectSpeciesCategory(categoryId)
        updateFilters()
    }

    private fun updateFilters() {
        val ownHarvest = viewModel.isOwnHarvests().value ?: true
        val huntingYear = viewModel.getSeasonSelected().value ?: return
        val species = viewModel.getSpeciesSelected().value?.map { speciesCode ->
            if (speciesCode == null) {
                Species.Other
            } else {
                Species.Known(speciesCode)
            }
        } ?: emptyList()

        when (viewModel.getTypeSelected().value) {
            GameLog.TYPE_HARVEST -> {
                MainScope().launch {
                    listHarvestsController.setFilters(ownHarvest, huntingYear, species)
                }
            }
            GameLog.TYPE_OBSERVATION -> listObservationsController.setFilters(huntingYear, species)
            GameLog.TYPE_SRVA -> listSrvaEventsController.setFilters(huntingYear, species)
        }
    }

    private fun toggleOwnHarvests() {
        MainScope().launch {
            val previousOwnHarvests = viewModel.isOwnHarvests().value ?: true
            val newOwnHarvests = !previousOwnHarvests
            viewModel.setOwnHarvests(newOwnHarvests)
            listHarvestsController.setOwnHarvestsFilter(newOwnHarvests)
            ownHarvestsMenuProvider.setOwnHarvests(newOwnHarvests)
            updateForOthersTextVisibility()
        }
    }

    private fun updateOwnHarvestVisibility() {
        val showOwnHarvestsToggle = RiistaSDK.preferences.showActorSelection() &&
                viewModel.getTypeSelected().value == GameLog.TYPE_HARVEST
        ownHarvestsMenuProvider.setVisibility(showOwnHarvestsToggle)
    }

    private fun ensureCorrectOwnHarvestsStatus() {
        // If actor selection is disabled then make sure that own harvests are selected in model
        if (!RiistaSDK.preferences.showActorSelection() && viewModel.isOwnHarvests().value == false) {
            MainScope().launch {
                viewModel.setOwnHarvests(true)
                listHarvestsController.setOwnHarvestsFilter(true)
                ownHarvestsMenuProvider.setOwnHarvests(true)
                updateForOthersTextVisibility()
            }
        }
    }

    private fun updateForOthersTextVisibility() {
        val ownHarvests = viewModel.isOwnHarvests().value ?: true
        forOthersTextView.visibility = (!ownHarvests).toVisibility()
    }
}
