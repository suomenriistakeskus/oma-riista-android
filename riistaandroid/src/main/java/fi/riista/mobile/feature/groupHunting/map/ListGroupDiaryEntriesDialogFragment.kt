package fi.riista.mobile.feature.groupHunting.map

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.domain.groupHunting.ui.diary.GroupDiaryEntryViewModel
import fi.riista.common.domain.groupHunting.ui.diary.ListGroupDiaryEntriesController
import fi.riista.common.logging.getLogger
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.mobile.R
import fi.riista.mobile.database.SpeciesResolver
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * A fragment for viewing a list of [GroupHuntingHarvest]s and [GroupHuntingObservation]s
 */
class ListGroupDiaryEntriesDialogFragment
    : BottomSheetDialogFragment(), GroupDiaryEntryViewHolder.Listener {

    interface Manager {
        val listGroupDiaryEntriesController: ListGroupDiaryEntriesController

        fun onViewHarvest(harvestId: GroupHuntingHarvestId,
                          harvestAcceptStatus: AcceptStatus)
        fun onViewObservation(observationId: GroupHuntingObservationId,
                              observationAcceptStatus: AcceptStatus)
    }

    @Inject
    lateinit var speciesResolver: SpeciesResolver

    private lateinit var manager: Manager
    private lateinit var adapter: ListGroupDiaryEntriesAdapter

    private lateinit var layoutContentLoaded: View
    private lateinit var textViewContentNotLoaded: TextView

    private lateinit var controller: ListGroupDiaryEntriesController

    private val disposeBag = DisposeBag()

    private var shouldRefreshEntries: Boolean = false

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)

        super.onAttach(context)
        this.manager = context as Manager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_group_diary_entries, container, false)

        layoutContentLoaded = view.findViewById(R.id.layout_content_loaded)
        textViewContentNotLoaded = view.findViewById(R.id.tv_content_not_loaded)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_diary_entries)!!
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recyclerView.adapter = ListGroupDiaryEntriesAdapter(
                layoutInflater = inflater,
                speciesResolver = speciesResolver,
                itemListener = this
        )
            .also { adapter ->
                this.adapter = adapter
            }

        controller = manager.listGroupDiaryEntriesController
        controller.harvestIds = getHarvestIds(requireArguments())
        controller.observationIds = getObservationIds(requireArguments())

        return view
    }


    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { viewModelLoadStatus ->
            when (viewModelLoadStatus) {
                ViewModelLoadStatus.NotLoaded -> {
                    layoutContentLoaded.visibility = View.GONE
                    textViewContentNotLoaded.visibility = View.GONE
                }
                ViewModelLoadStatus.Loading -> {
                    layoutContentLoaded.visibility = View.GONE
                    textViewContentNotLoaded.visibility = View.VISIBLE
                    textViewContentNotLoaded.setText(R.string.map_loading_location_entries)
                }
                ViewModelLoadStatus.LoadFailed -> {
                    layoutContentLoaded.visibility = View.GONE
                    textViewContentNotLoaded.visibility = View.VISIBLE
                    textViewContentNotLoaded.setText(R.string.map_loading_location_entries_failed)
                    adapter.diaryEntries = listOf()
                }
                is ViewModelLoadStatus.Loaded -> {
                    layoutContentLoaded.visibility = View.VISIBLE
                    textViewContentNotLoaded.visibility = View.GONE
                    adapter.diaryEntries = viewModelLoadStatus.viewModel.entries
                }
            }
        }.disposeBy(disposeBag)

        loadEntries(refresh = shouldRefreshEntries)
    }

    override fun onPause() {
        super.onPause()

        disposeBag.disposeAll()
    }

    private fun loadEntries(refresh: Boolean) {
        MainScope().launch {
            controller.loadViewModel(refresh = refresh)
            shouldRefreshEntries = false
        }
    }

    override fun onViewGroupDiaryEntry(entry: GroupDiaryEntryViewModel) {
        when (entry.type) {
            DiaryEntryType.HARVEST -> {
                shouldRefreshEntries = true
                manager.onViewHarvest(
                        harvestId = entry.remoteId,
                        harvestAcceptStatus = entry.acceptStatus
                )
            }
            DiaryEntryType.OBSERVATION -> {
                shouldRefreshEntries = true
                manager.onViewObservation(
                        observationId = entry.remoteId,
                        observationAcceptStatus = entry.acceptStatus
                )
            }
            DiaryEntryType.SRVA -> {
                logger.d { "Viewing SRVAs not implemented!" }
            }
        }
    }

    companion object {
        private val logger by getLogger(ListGroupDiaryEntriesDialogFragment::class)

        private const val ARGS_PREFIX = "LGDEDF_args"
        private const val ARGS_HARVEST_IDS = "${ARGS_PREFIX}_harvest_ids"
        private const val ARGS_OBSERVATION_IDS = "${ARGS_PREFIX}_observation_ids"

        fun create(
            harvestIds: List<GroupHuntingHarvestId>,
            observationIds: List<GroupHuntingObservationId>,
        ): ListGroupDiaryEntriesDialogFragment {
            return ListGroupDiaryEntriesDialogFragment().apply {
                arguments = Bundle().apply {
                    putLongArray(ARGS_HARVEST_IDS, harvestIds.toLongArray())
                    putLongArray(ARGS_OBSERVATION_IDS, observationIds.toLongArray())
                }
            }
        }

        private fun getHarvestIds(args: Bundle): List<GroupHuntingHarvestId> {
            return args.getLongArray(ARGS_HARVEST_IDS)?.toList() ?: listOf()
        }

        private fun getObservationIds(args: Bundle): List<GroupHuntingHarvestId> {
            return args.getLongArray(ARGS_OBSERVATION_IDS)?.toList() ?: listOf()
        }
    }

}
