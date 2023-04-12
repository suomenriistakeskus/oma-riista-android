package fi.riista.mobile.pages

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.model.getHuntingYear
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.mobile.R
import fi.riista.mobile.adapter.GalleryAdapter
import fi.riista.mobile.adapter.GalleryAdapter.GalleryItem
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.ui.GameLogFilterView
import fi.riista.mobile.ui.toGameLogImage
import fi.riista.mobile.utils.UiUtils
import fi.riista.mobile.utils.UserInfoStore
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

    private val disposeBag = DisposeBag()
    private val harvestProvider = RiistaSDK.harvestContext.harvestProvider
    private val srvaEventProvider = RiistaSDK.srvaContext.srvaEventProvider
    private val observationProvider = RiistaSDK.observationContext.observationProvider

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
        viewModel.getSeasonSelected().observe(viewLifecycleOwner) {
            refreshList(viewModel.getTypeSelected().value)
        }
        viewModel.getSpeciesSelected().observe(viewLifecycleOwner) { species ->
            filterView.setupSpecies(species, viewModel.getCategorySelected().value)
            refreshList(viewModel.getTypeSelected().value)
        }
        viewModel.getSeasons().observe(viewLifecycleOwner) { seasons ->
            filterView.setupSeasons(seasons, viewModel.getSeasonSelected().value)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        harvestProvider.loadStatus.bind { status ->
            val type = viewModel.getTypeSelected().value
            if (status.loaded && type == GameLog.TYPE_HARVEST) {
                val harvests = harvestProvider.getHarvestsHavingImages()
                adapter.setDataSet(harvestItemsWithImageMatchingSpeciesFilter(harvests))
            }
        }.disposeBy(disposeBag)
        srvaEventProvider.loadStatus.bind { status ->
            val type = viewModel.getTypeSelected().value
            if (status.loaded && type == GameLog.TYPE_SRVA) {
                val srvaEvents = srvaEventProvider.getEventsHavingImages()
                adapter.setDataSet(srvaItemsMatchingFilter(srvaEvents))
            }
        }.disposeBy(disposeBag)
        observationProvider.loadStatus.bind { status ->
            val type = viewModel.getTypeSelected().value
            if (status.loaded && type == GameLog.TYPE_OBSERVATION) {
                val observations = observationProvider.getObservationsHavingImages()
                adapter.setDataSet(observationItemsMatchingFilter(observations))
            }
        }.disposeBy(disposeBag)

        refreshList(viewModel.getTypeSelected().value)
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
            harvestProvider.fetch(refresh = true)
        }
    }

    private fun harvestItemsWithImageMatchingSpeciesFilter(items: List<CommonHarvest>): ArrayList<GalleryItem> {
        val data = arrayListOf<GalleryItem>()

        val seasonStartYear = viewModel.getSeasonSelected().value
        val species = viewModel.getSpeciesSelected().value

        if (species != null) {
            val speciesNotSelected = species.isEmpty()

            for (harvest in items) {
                if (seasonStartYear == harvest.pointOfTime.date.getHuntingYear()
                    && (speciesNotSelected || species.contains(harvest.species.knownSpeciesCodeOrNull()))) {

                    val localId = harvest.localId
                    val image = harvest.images.primaryImage?.toGameLogImage()
                    if (localId != null && image != null) {
                        val item = GalleryItem(GameLog.TYPE_HARVEST, localId, image)
                        data.add(item)
                    }

                }
            }
        }

        return data
    }

    private fun observationsSelected() {
        MainScope().launch {
            observationProvider.fetch(refresh = true)
        }
    }

    private fun observationItemsMatchingFilter(items: List<CommonObservation>): ArrayList<GalleryItem> {
        val data = arrayListOf<GalleryItem>()

        val seasonStartYear = viewModel.getSeasonSelected().value
        val species = viewModel.getSpeciesSelected().value

        if (species != null) {
            val speciesNotSelected = species.isEmpty()

            for (obs in items) {
                if (seasonStartYear == obs.pointOfTime.date.getHuntingYear()
                        && (speciesNotSelected || species.contains(obs.species.knownSpeciesCodeOrNull()))) {

                    val localId = obs.localId
                    val image = obs.images.primaryImage?.toGameLogImage()
                    if (localId != null && image != null) {
                        val item = GalleryItem(GameLog.TYPE_OBSERVATION, localId, image)
                        data.add(item)
                    }
                }
            }
        }

        return data
    }

    private fun srvaEventsSelected() {
        MainScope().launch {
            srvaEventProvider.fetch(refresh = true)
        }
    }

    private fun srvaItemsMatchingFilter(items: List<CommonSrvaEvent>): ArrayList<GalleryItem> {
        val data = arrayListOf<GalleryItem>()

        val seasonStartYear = viewModel.getSeasonSelected().value
        val species = viewModel.getSpeciesSelected().value

        if (species != null) {
            val speciesNotSelected = species.isEmpty()

            for (srvaEvent in items) {
                if (seasonStartYear == srvaEvent.pointOfTime.year
                        && (speciesNotSelected || species.contains(srvaEvent.species.knownSpeciesCodeOrNull()))) {

                    val localId = srvaEvent.localId
                    val image = srvaEvent.images.primaryImage?.toGameLogImage()
                    if (localId != null && image != null) {
                        val item = GalleryItem(GameLog.TYPE_SRVA, localId, image)
                        data.add(item)
                    }
                }
            }
        }

        return data
    }

    override fun onLogTypeSelected(type: String) {
        viewModel.selectLogType(type)
    }

    override fun onLogSeasonSelected(season: Int) {
        viewModel.selectLogSeason(season)
    }

    override fun onLogSpeciesSelected(speciesIds: List<Int>) {
        viewModel.selectSpeciesIds(speciesIds)
    }

    override fun onLogSpeciesCategorySelected(categoryId: Int) {
        viewModel.selectSpeciesCategory(categoryId)
    }

    companion object {
        @JvmStatic
        fun newInstance() = GalleryFragment().apply {}
    }
}
