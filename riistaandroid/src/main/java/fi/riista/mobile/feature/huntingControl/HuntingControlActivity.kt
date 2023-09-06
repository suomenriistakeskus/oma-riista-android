package fi.riista.mobile.feature.huntingControl

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.huntingControl.model.HuntingControlEventId
import fi.riista.common.domain.huntingControl.model.HuntingControlEventTarget
import fi.riista.common.domain.huntingControl.model.HuntingControlRhyTarget
import fi.riista.common.domain.huntingControl.ui.eventSelection.SelectHuntingControlEventController
import fi.riista.common.domain.huntingControl.ui.eventSelection.SelectHuntingControlEventViewModel
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.riistaSdkHelpers.AppLanguageProvider
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.ui.NotificationButton
import fi.riista.mobile.ui.StringWithIdChoiceView
import fi.riista.mobile.ui.StringWithIdWrapper
import fi.riista.mobile.utils.toVisibility
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class HuntingControlActivity : BaseActivity(), HuntingControlEventViewHolder.SelectHuntingControlEventListener {

    private lateinit var layoutHuntingControlAvailable: ViewGroup
    private lateinit var layoutHuntingControlNotAvailable: ViewGroup
    private lateinit var notAvailableText: TextView
    private lateinit var eventsView: RecyclerView
    private lateinit var noEventsView: TextView
    private lateinit var adapter: HuntingControlEventRecyclerViewAdapter
    private lateinit var rhyChoice: StringWithIdChoiceView
    private lateinit var checkHunterButton: NotificationButton
    private lateinit var newEventButton: NotificationButton
    private lateinit var progressBar: ProgressBar

    private lateinit var controller: SelectHuntingControlEventController
    private val disposeBag = DisposeBag()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hunting_control)
        setCustomTitle(getString(R.string.hunting_control_main_page_title))

        controller = SelectHuntingControlEventController(
            huntingControlContext = RiistaSDK.huntingControlContext,
            languageProvider = AppLanguageProvider(this),
            stringProvider = ContextStringProviderFactory.createForContext(this),
        )

        if (savedInstanceState != null) {
            controller.restoreFromBundle(savedInstanceState, PREFIX_CONTROLLER_STATE)
        }

        layoutHuntingControlAvailable = findViewById(R.id.layout_hunting_control_available)
        layoutHuntingControlNotAvailable = findViewById(R.id.layout_hunting_control_not_available)
        notAvailableText = findViewById(R.id.tv_group_hunting_not_available_message)

        eventsView = findViewById(R.id.rv_hunting_control_events)
        eventsView.adapter = HuntingControlEventRecyclerViewAdapter(
            layoutInflater = layoutInflater,
            listener = this,
        ).also {
            adapter = it
        }
        eventsView.addItemDecoration(createDividerItemDecorator())

        noEventsView = findViewById(R.id.tv_no_events)

        rhyChoice = findViewById(R.id.cv_rhy)
        rhyChoice.setTitle(getString(R.string.huntint_control_rhy))

        checkHunterButton = findViewById<NotificationButton>(R.id.btn_check_hunter).also { button ->
            button.findViewById<View>(R.id.button_content).setOnClickListener {
                onHunterCheckClicked()
            }
        }
        newEventButton = findViewById<NotificationButton>(R.id.btn_add_event).also { button ->
            button.findViewById<View>(R.id.button_content).setOnClickListener {
                onNewEventClicked()
            }
        }
        progressBar = findViewById(R.id.progress_horizontal)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_refresh, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_refresh -> {
                MainScope().launch {
                    loadViewModel(refresh = true)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        controller.saveToBundle(outState, PREFIX_CONTROLLER_STATE)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { loadStatus ->
            when (loadStatus) {
                is ViewModelLoadStatus.NotLoaded -> {
                    // No op
                }
                is ViewModelLoadStatus.Loading -> onViewModelLoading()
                is ViewModelLoadStatus.LoadFailed -> onViewModelLoadFailed()
                is ViewModelLoadStatus.Loaded -> onViewModelLoaded(loadStatus.viewModel)
            }
        }.disposeBy(disposeBag)

        loadViewModel(refresh = false)
    }

    override fun onPause() {
        super.onPause()
        disposeBag.disposeAll()
    }

    override fun huntingControlEventSelected(eventId: HuntingControlEventId) {
        controller.getLoadedViewModelOrNull()?.selectedRhy?.id?.let { rhyId ->
            val intent = HuntingControlEventActivity.getLaunchIntentForViewing(
                context = this,
                huntingControlEventTarget = HuntingControlEventTarget(
                    rhyId = rhyId,
                    eventId = eventId,
                ),
            )
            startActivity(intent)
        }
    }

    private fun onViewModelLoading() {
        progressBar.visibility = View.VISIBLE
        layoutHuntingControlAvailable.visibility = View.GONE
        layoutHuntingControlNotAvailable.visibility = View.VISIBLE
        notAvailableText.text = getText(R.string.loading_content)
    }

    private fun onViewModelLoadFailed() {
        progressBar.visibility = View.GONE
        layoutHuntingControlAvailable.visibility = View.GONE
        layoutHuntingControlNotAvailable.visibility = View.VISIBLE
        notAvailableText.text = getText(R.string.content_loading_failed)
    }

    private fun onViewModelLoaded(viewModel: SelectHuntingControlEventViewModel) {
        progressBar.visibility = View.GONE
        layoutHuntingControlAvailable.visibility = View.VISIBLE
        layoutHuntingControlNotAvailable.visibility = View.GONE

        rhyChoice.visibility = viewModel.showRhy.toVisibility()
        rhyChoice.setChoices(
            viewModel.rhys.map { StringWithIdWrapper(it) },
            viewModel.selectedRhy?.let { StringWithIdWrapper(it) },
            false
        ) { _, rhy ->
            controller.eventDispatcher.dispatchRhySelected(rhy.stringWithId.id)
        }

        val events = viewModel.events
        if (events.isNullOrEmpty()) {
            adapter.setItems(listOf())
            if (viewModel.selectedRhy != null) {
                noEventsView.text = getText(R.string.hunting_control_no_events)
                newEventButton.isEnabled = true
            } else {
                noEventsView.text = getText(R.string.hunting_control_select_rhy_to_display_events)
                newEventButton.isEnabled = false
            }
            noEventsView.visibility = View.VISIBLE
            eventsView.visibility = View.GONE
        } else {
            adapter.setItems(events)
            noEventsView.visibility = View.GONE
            eventsView.visibility = View.VISIBLE
            newEventButton.isEnabled = true
        }
    }

    private fun loadViewModel(refresh: Boolean) {
        MainScope().launch {
            controller.loadViewModel(refresh = refresh)
        }
    }

    private fun onHunterCheckClicked() {
        startActivity(Intent(this, HuntingControlHunterInfoActivity::class.java))
    }

    private fun onNewEventClicked() {
        controller.getLoadedViewModelOrNull()?.selectedRhy?.id?.let { rhyId ->
            val intent = HuntingControlEventActivity.getLaunchIntentForCreating(
                packageContext = this,
                huntingControlRhyTarget = HuntingControlRhyTarget(rhyId),
                huntingControlEventTarget = null,
            )
            startActivity(intent)
        }
    }

    private fun createDividerItemDecorator(): DividerItemDecoration {
        val padding = resources.getDimension(R.dimen.page_padding_vertical).toInt()
        return MarginItemDecoration(
            eventsView.context,
            padding,
        )
    }

    companion object {
        private const val PREFIX_CONTROLLER_STATE = "HGA_controller"
    }
}

private class MarginItemDecoration(context: Context, private val spaceSize: Int) : DividerItemDecoration(context, LinearLayout.VERTICAL) {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        with(outRect) {
            if (parent.getChildAdapterPosition(view) != 0) {
                top = spaceSize
            }
            left = 0
            right = 0
            bottom = spaceSize
        }
    }
}
