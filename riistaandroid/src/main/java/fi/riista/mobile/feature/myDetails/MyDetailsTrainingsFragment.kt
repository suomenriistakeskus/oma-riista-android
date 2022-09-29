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
import fi.riista.common.RiistaSDK
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.domain.training.ui.ListTrainingsController
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.mobile.R
import fi.riista.mobile.feature.myDetails.dataFields.viewHolder.TrainingsRecyclerViewAdapter
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MyDetailsTrainingsFragment : DialogFragment() {

    private lateinit var controller: ListTrainingsController
    private lateinit var adapter: TrainingsRecyclerViewAdapter
    private lateinit var noContent: TextView
    private lateinit var contentLoaded: RecyclerView
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
        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.item_refresh -> {
                    loadViewModel(refresh = true)
                    true
                }
                else -> false
            }
        }
        noContent = view.findViewById(R.id.tv_content_not_loaded)
        contentLoaded = view.findViewById(R.id.rv_data_fields)
        contentLoaded.addItemDecoration(createDividerItemDecorator())

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
                    noContent.visibility = View.VISIBLE
                    noContent.text = getString(R.string.loading_content)
                    contentLoaded.visibility = View.GONE
                }
                ViewModelLoadStatus.LoadFailed -> {
                    noContent.visibility = View.VISIBLE
                    noContent.text = getString(R.string.content_loading_failed)
                    contentLoaded.visibility = View.GONE
                }
                is ViewModelLoadStatus.Loaded -> {
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

        loadViewModel()
    }

    private fun loadViewModel(refresh: Boolean = false) {
        MainScope().launch {
            controller.loadViewModel(refresh)
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
