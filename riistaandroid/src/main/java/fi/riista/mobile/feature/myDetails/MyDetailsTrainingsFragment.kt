package fi.riista.mobile.feature.myDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.training.ui.ListTrainingsController
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.mobile.R
import fi.riista.mobile.feature.myDetails.dataFields.viewHolder.TrainingsRecyclerViewAdapter
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.riistaSdkHelpers.loadViewModelIfNotLoaded
import fi.riista.mobile.utils.Constants
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyDetailsTrainingsFragment : DialogFragment() {

    private lateinit var controller: ListTrainingsController
    private lateinit var adapter: TrainingsRecyclerViewAdapter
    private lateinit var noContent: TextView
    private lateinit var contentLoaded: RecyclerView
    private var refreshMenuItem: MenuItem? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private val disposeBag = DisposeBag()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_my_details_trainings, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.inflateMenu(R.menu.menu_refresh)
        refreshMenuItem = toolbar.menu.findItem(R.id.item_refresh)
        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.item_refresh -> {
                    reloadContent(launchedUsingButton = true)
                    true
                }
                else -> false
            }
        }
        noContent = view.findViewById(R.id.tv_content_not_loaded)
        contentLoaded = view.findViewById(R.id.rv_data_fields)
        contentLoaded.addItemDecoration(createDividerItemDecorator())

        swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.srl_refresh_layout)
            ?.also { layout ->
                layout.setOnRefreshListener {
                    reloadContent(launchedUsingButton = false)
                }
                layout.setColorSchemeResources(R.color.colorPrimary)
            }

        controller = ListTrainingsController(
            trainingContext = RiistaSDK.currentUserContext.trainingContext,
            stringProvider = ContextStringProviderFactory.createForContext(requireContext()),
        )

        adapter = TrainingsRecyclerViewAdapter(
            layoutInflater = layoutInflater,
        )
        view.findViewById<RecyclerView>(R.id.rv_data_fields).also {
            it.adapter = adapter
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { viewModelLoadStatus ->
            when (viewModelLoadStatus) {
                ViewModelLoadStatus.NotLoaded -> {
                    noContent.visibility = View.GONE
                    contentLoaded.visibility = View.GONE
                }
                ViewModelLoadStatus.Loading -> {
                    // indicate loading state with a text if there are currently no trainings.
                    // Otherwise it should be enough to just disable update elements
                    if (adapter.itemCount == 0) {
                        noContent.visibility = View.VISIBLE
                        noContent.text = getString(R.string.loading_content)
                        contentLoaded.visibility = View.GONE
                    }
                }
                ViewModelLoadStatus.LoadFailed -> {
                    noContent.visibility = View.VISIBLE
                    noContent.text = getString(R.string.content_loading_failed)
                    contentLoaded.visibility = View.GONE
                    onViewModelLoadCompleted()
                }
                is ViewModelLoadStatus.Loaded -> {
                    onViewModelLoadCompleted()
                    if (viewModelLoadStatus.viewModel.trainings.isEmpty()) {
                        noContent.visibility = View.VISIBLE
                        noContent.text = getString(R.string.my_details_no_trainings)
                        contentLoaded.visibility = View.GONE
                    } else {
                        noContent.visibility = View.GONE
                        contentLoaded.visibility = View.VISIBLE
                        adapter.setItems(viewModelLoadStatus.viewModel.trainings)
                    }
                }
            }
        }.disposeBy(disposeBag)

        controller.loadViewModelIfNotLoaded {
            swipeRefreshLayout?.isEnabled = false
            updateRefreshMenuItem(loadingContent = true)
        }
    }

    private fun reloadContent(launchedUsingButton: Boolean) {
        updateRefreshMenuItem(loadingContent = true)
        if (launchedUsingButton) {
            swipeRefreshLayout?.post { swipeRefreshLayout?.isRefreshing = true }
            swipeRefreshLayout?.isEnabled = false
        }

        MainScope().launch {
            // delay the actual loading by half a second. Typically loading occurs within few hundred milliseconds
            // and thus the UI just flashes the loading indicator. By delaying the loading the user gets a chance
            // to actually see the indicator and experience that "app is actually now doing something"
            delay(500)
            controller.loadViewModel(refresh = true)
        }
    }

    private fun onViewModelLoadCompleted() {
        swipeRefreshLayout?.isRefreshing = false
        swipeRefreshLayout?.isEnabled = true
        updateRefreshMenuItem(loadingContent = false)
    }

    private fun updateRefreshMenuItem(loadingContent: Boolean) {
        refreshMenuItem?.let { item ->
            item.isEnabled = !loadingContent
            item.icon?.alpha = when (loadingContent) {
                true -> Constants.DISABLED_ALPHA
                false -> 255
            }
        }
    }

    override fun onPause() {
        super.onPause()
        disposeBag.disposeAll()
    }

    private fun createDividerItemDecorator(): DividerItemDecoration {
        val divider = DividerItemDecoration(contentLoaded.context, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(contentLoaded.context, R.drawable.line_divider_horizontal)
            ?.let { divider.setDrawable(it) }
        return divider
    }

    companion object {
        const val TAG = "MyDetailsTrainingsFragment"
    }
}
