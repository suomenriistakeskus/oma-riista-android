package fi.riista.mobile.feature.poi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.poi.ui.PoiFilter
import fi.riista.common.domain.poi.ui.PoiListController
import fi.riista.common.domain.poi.ui.PoiListItem
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.mobile.R
import fi.riista.mobile.utils.toVisibility
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class PoiListFragment : DialogFragment(), PoiGroupItemViewHolder.Listener, PoiItemViewHolder.Listener {

    private var externalId: String? = null
    private lateinit var filter: PoiFilter
    private lateinit var poiList: RecyclerView
    private lateinit var noPoisTextView: TextView
    private lateinit var adapter: PoiListRecyclerViewAdapter
    private lateinit var controller: PoiListController
    private val disposeBag = DisposeBag()
    var listener: PoiLocationActivity.CenterMapListener? = null

    private val resultLauncher = PoiLocationActivity.registerForActivityResult(this) { location ->
        listener?.centerMapTo(location)
        dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_poi_list_list, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.inflateMenu(R.menu.menu_refresh)
        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.item_refresh -> {
                    loadViewModel(refresh = true)
                    true
                }
                else -> false
            }
        }

        externalId = getExternalId(requireArguments())
        filter = getPoiFilter(requireArguments())
        controller = PoiListController(RiistaSDK.poiContext, externalId, filter)
        savedInstanceState?.let {
            controller.restoreFromBundle(it, CONTROLLER_STATE_PREFIX)
        }


        noPoisTextView = view.findViewById(R.id.tv_no_pois)
        poiList = view.findViewById(R.id.poi_list)
        poiList.layoutManager = LinearLayoutManager(context)
        poiList.adapter = PoiListRecyclerViewAdapter(
            poiGroupItemListener = this,
            poiItemListener = this,
            layoutInflater = inflater,
            context = requireContext()
        ).also { adapter ->
            this.adapter = adapter
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        controller.viewModelLoadStatus.bindAndNotify { viewModelLoadStatus ->
            when (viewModelLoadStatus) {
                ViewModelLoadStatus.NotLoaded,
                ViewModelLoadStatus.Loading -> {
                }
                ViewModelLoadStatus.LoadFailed -> {
                    poiList.visibility = View.GONE
                    noPoisTextView.visibility = View.VISIBLE
                }
                is ViewModelLoadStatus.Loaded -> {
                    adapter.setItems(viewModelLoadStatus.viewModel.visibleItems)

                    poiList.visibility = viewModelLoadStatus.viewModel.visibleItems.isNotEmpty().toVisibility()
                    noPoisTextView.visibility = viewModelLoadStatus.viewModel.visibleItems.isEmpty().toVisibility()
                }
            }
        }.disposeBy(disposeBag)

        loadViewModelIfNotLoaded()
    }

    override fun onPause() {
        super.onPause()
        disposeBag.disposeAll()
    }

    private fun loadViewModelIfNotLoaded() {
        val viewModelStatus = controller.viewModelLoadStatus.value
        if (viewModelStatus is ViewModelLoadStatus.Loaded) {
            return
        }
        loadViewModel(false)
    }

    private fun loadViewModel(refresh: Boolean) {
        MainScope().launch {
            controller.loadViewModel(refresh = refresh)
        }
    }

    override fun poiGroupItemSelected(poiGroupItem: PoiListItem.PoiGroupItem) {
        controller.eventDispatcher.dispatchPoiGroupSelected(poiGroupItem.id)
    }

    override fun poiItemSelected(poiItem: PoiListItem.PoiItem) {
        val groupAndLocation = controller.findPoiLocationAndItsGroup(poiItem.id)
        val extId = externalId
        if (groupAndLocation != null && extId != null) {
            val intent = PoiLocationActivity.getIntent(
                context = requireContext(),
                externalId = extId,
                poiLocationGroup = groupAndLocation.first,
                poiLocation = groupAndLocation.second,
            )
            resultLauncher.launch(intent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller.saveToBundle(outState, CONTROLLER_STATE_PREFIX)
    }

    companion object {
        private const val ARGS_PREFIX = "PLF_args"
        private const val ARGS_EXTERNAL_ID = "${ARGS_PREFIX}_external_id"
        private const val ARGS_FILTER = "${ ARGS_PREFIX}_filter"
        private const val CONTROLLER_STATE_PREFIX = "PLF_controller"

        fun create(
            externalId: String?,
            filter: PoiFilter,
        ): PoiListFragment {
            return PoiListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGS_EXTERNAL_ID, externalId)
                    putString(ARGS_FILTER, filter.poiFilterType.name)
                }
            }
        }

        private fun getExternalId(args: Bundle): String? {
            return args.getString(ARGS_EXTERNAL_ID)
        }

        private fun getPoiFilter(args: Bundle): PoiFilter {
            val filterType = PoiFilter.PoiFilterType.valueOf(
                args.getString(ARGS_FILTER) ?: PoiFilter.PoiFilterType.ALL.name
            )
            return PoiFilter(filterType)
        }
    }
}
