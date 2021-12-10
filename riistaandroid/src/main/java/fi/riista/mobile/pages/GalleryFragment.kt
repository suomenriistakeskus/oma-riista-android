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
import fi.riista.mobile.R
import fi.riista.mobile.adapter.GalleryAdapter
import fi.riista.mobile.adapter.GalleryAdapter.GalleryItem
import fi.riista.mobile.database.HarvestDatabase
import fi.riista.mobile.models.GameHarvest
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.models.GameLogImage
import fi.riista.mobile.models.observation.GameObservation
import fi.riista.mobile.models.srva.SrvaEvent
import fi.riista.mobile.observation.ObservationDatabase
import fi.riista.mobile.srva.SrvaDatabase
import fi.riista.mobile.ui.GameLogFilterView
import fi.riista.mobile.utils.DateTimeUtils
import fi.riista.mobile.utils.UiUtils
import fi.riista.mobile.utils.UserInfoStore
import fi.riista.mobile.viewmodel.GameLogViewModel
import javax.inject.Inject

class GalleryFragment : GameLogFilterView.GameLogFilterListener, PageFragment() {

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var userInfoStore: UserInfoStore

    @Inject
    internal lateinit var harvestDatabase: HarvestDatabase

    @Inject
    internal lateinit var observationDatabase: ObservationDatabase

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GalleryAdapter
    private lateinit var viewModel: GameLogViewModel

    private lateinit var filterView: GameLogFilterView

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

        adapter = GalleryAdapter(context, harvestDatabase, observationDatabase)

        val layoutManager = GridLayoutManager(context, 2)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter

        filterView = view.findViewById(R.id.gallery_filter_view)
        filterView.listener = this

        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(GameLogViewModel::class.java)

        filterView.setupTypes(
            showSrva = UiUtils.isSrvaVisible(userInfoStore.getUserInfo()),
            showPoi = false,
            selected = viewModel.getTypeSelected().value,
        )
        filterView.setupSeasons(viewModel.getSeasons().value, viewModel.getSeasonSelected().value)
        filterView.setupSpecies(viewModel.getSpeciesSelected().value!!, viewModel.getCategorySelected().value)

        viewModel.getTypeSelected().observe(viewLifecycleOwner, androidx.lifecycle.Observer { type ->
            refreshList(type, viewModel.getSeasonSelected().value, viewModel.getSpeciesSelected().value)
        })
        viewModel.getSeasonSelected().observe(viewLifecycleOwner, androidx.lifecycle.Observer { season ->
            refreshList(viewModel.getTypeSelected().value, season, viewModel.getSpeciesSelected().value)
        })
        viewModel.getSpeciesSelected().observe(viewLifecycleOwner, androidx.lifecycle.Observer { species ->
            filterView.setupSpecies(species, viewModel.getCategorySelected().value)
            refreshList(viewModel.getTypeSelected().value, viewModel.getSeasonSelected().value, species)
        })
        viewModel.getSeasons().observe(viewLifecycleOwner, androidx.lifecycle.Observer { seasons ->
            filterView.setupSeasons(seasons, viewModel.getSeasonSelected().value)
        })
        return view
    }

    private fun refreshList(type: String?, season: Int?, species: List<Int>?) {
        when (type) {
            GameLog.TYPE_HARVEST -> harvestsSelected()
            GameLog.TYPE_OBSERVATION -> observationsSelected()
            GameLog.TYPE_SRVA -> srvaEventsSelected()
        }
    }

    private fun harvestsSelected() {
        val seasonStartYear = viewModel.getSeasonSelected().value
        val harvests = harvestDatabase.getHarvestsByHuntingYear(seasonStartYear!!)

        adapter.setDataSet(harvestItemsWithImageMatchingSpeciesFilter(harvests))
    }

    private fun harvestItemsWithImageMatchingSpeciesFilter(items: List<GameHarvest>): ArrayList<GalleryItem> {
        val data = arrayListOf<GalleryItem>()

        val species = viewModel.getSpeciesSelected().value

        if (species != null) {
            val speciesNotSelected = species.isEmpty()

            // Reverse ordering to descending
            for (harvest in items.reversed()) {
                if (harvest.mImages.isNotEmpty() && (speciesNotSelected || species.contains(harvest.mSpeciesID))) {
                    val item = GalleryItem(GameLog.TYPE_HARVEST, harvest.mLocalId.toLong(), GameLogImage(harvest.mImages[0].uuid))
                    data.add(item)
                }
            }
        }

        return data
    }

    private fun observationsSelected() {
        observationDatabase.loadObservationsWithAnyImages { observations ->
            adapter.setDataSet(observationItemsMatchingFilter(observations))
        }
    }

    private fun observationItemsMatchingFilter(items: List<GameObservation>): ArrayList<GalleryItem> {
        val data = arrayListOf<GalleryItem>()

        val seasonStartYear = viewModel.getSeasonSelected().value
        val species = viewModel.getSpeciesSelected().value

        if (species != null) {
            val speciesNotSelected = species.isEmpty()

            for (obs in items) {
                if (seasonStartYear == DateTimeUtils.getHuntingYearForDate(obs.toDateTime().toLocalDate())
                        && (speciesNotSelected || species.contains(obs.gameSpeciesCode))) {

                    val item = GalleryItem(GameLog.TYPE_OBSERVATION, obs.localId, GameLogImage(obs.images[0].uuid))
                    data.add(item)
                }
            }
        }

        return data
    }

    private fun srvaEventsSelected() {
        SrvaDatabase.getInstance().loadEventsWithAnyImages { srvaEvents ->
            adapter.setDataSet(srvaItemsMatchingFilter(srvaEvents))
        }
    }

    private fun srvaItemsMatchingFilter(items: List<SrvaEvent>): ArrayList<GalleryItem> {
        val data = arrayListOf<GalleryItem>()

        val seasonStartYear = viewModel.getSeasonSelected().value
        val species = viewModel.getSpeciesSelected().value

        if (species != null) {
            val speciesNotSelected = species.isEmpty()

            for (srvaEvent in items) {
                if (seasonStartYear == srvaEvent.toDateTime().year
                        && (speciesNotSelected || species.contains(srvaEvent.gameSpeciesCode))) {

                    val item = GalleryItem(GameLog.TYPE_SRVA, srvaEvent.localId, GameLogImage(srvaEvent.images[0].uuid))
                    data.add(item)
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
