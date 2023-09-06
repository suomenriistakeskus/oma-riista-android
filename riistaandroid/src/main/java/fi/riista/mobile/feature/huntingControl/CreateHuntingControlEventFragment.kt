package fi.riista.mobile.feature.huntingControl

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationListener
import com.google.android.material.button.MaterialButton
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.domain.huntingControl.HuntingControlEventOperationResponse
import fi.riista.common.domain.huntingControl.model.HuntingControlAttachment
import fi.riista.common.domain.huntingControl.model.HuntingControlEventId
import fi.riista.common.domain.huntingControl.model.HuntingControlRhyTarget
import fi.riista.common.domain.huntingControl.ui.HuntingControlEventField
import fi.riista.common.domain.huntingControl.ui.modify.CreateHuntingControlEventController
import fi.riista.common.domain.huntingControl.ui.modify.ModifyHuntingControlEventViewModel
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.common.util.toETRMSGeoLocation
import fi.riista.mobile.LocationClientProvider
import fi.riista.mobile.R
import fi.riista.mobile.sync.SyncConfig
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.ui.NoChangeAnimationsItemAnimator
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import javax.inject.Inject

class CreateHuntingControlEventFragment
    : ModifyHuntingControlEventFragment<CreateHuntingControlEventController>()
    , LocationListener
{
    interface InteractionManager: LocationClientProvider {
        val createHuntingControlEventController: CreateHuntingControlEventController
        val huntingControlRhyTarget: HuntingControlRhyTarget

        fun onCreatingNewHuntingControlEvent()
        fun onNewHuntingControlEventCreateCompleted(
            success: Boolean, eventId: HuntingControlEventId?,
            indicatorsDismissed: () -> Unit = {}
        )
        fun cancelCreateNewHuntingControlEvent()
        fun showAttachment(attachment: HuntingControlAttachment)
    }

    private lateinit var adapter: DataFieldRecyclerViewAdapter<HuntingControlEventField>
    private lateinit var saveButton: MaterialButton

    private lateinit var interactionManager: InteractionManager
    private lateinit var controller: CreateHuntingControlEventController
    private val disposeBag = DisposeBag()
    private var saveScope: CoroutineScope? = null

    @Inject
    lateinit var syncConfig: SyncConfig

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        interactionManager = context as InteractionManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_modify_hunting_control_event, container, false)
        setViewTitle(R.string.hunting_control_view_page_title)

        controller = interactionManager.createHuntingControlEventController

        savedInstanceState?.let {
            controller.restoreFromBundle(it, CONTROLLER_STATE_PREFIX)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_data_fields)!!
        recyclerView.adapter = createAdapter(viewHolderTypeResolver = this)
            .also { adapter ->
                this.adapter = adapter
                registerViewHolderFactories(adapter)
            }
        recyclerView.itemAnimator = NoChangeAnimationsItemAnimator()

        view.findViewById<MaterialButton>(R.id.btn_cancel)?.let { btn ->
            btn.setOnClickListener {
                interactionManager.cancelCreateNewHuntingControlEvent()
            }
        }

        saveButton = view.findViewById<MaterialButton>(R.id.btn_save)!!
            .also { btn ->
                btn.setOnClickListener {
                    createHuntingControlEvent()
                }
            }

        registerFragmentResultListeners()
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller.saveToBundle(outState, CONTROLLER_STATE_PREFIX)
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { viewModelLoadStatus ->
            updateBasedOnViewModel(viewModelLoadStatus)
        }.disposeBy(disposeBag)

        if (controller.canMoveEventToCurrentUserLocation()) {
            interactionManager.locationClient.addListener(this)
        }

        loadHuntingControlEventIfNotLoaded()
    }

    override fun onPause() {
        super.onPause()

        saveScope?.cancel()
        disposeBag.disposeAll()
        interactionManager.locationClient.removeListener(this)
    }

    override fun onLocationChanged(location: Location) {
        val eventLocationChanged = controller.tryMoveEventToCurrentUserLocation(
            location = location.toETRMSGeoLocation(GeoLocationSource.GPS_DEVICE)
        )

        if (!eventLocationChanged) {
            interactionManager.locationClient.removeListener(this)
        }
    }

    private fun updateBasedOnViewModel(
        viewModelLoadStatus: ViewModelLoadStatus<ModifyHuntingControlEventViewModel>
    ) {
        when (viewModelLoadStatus) {
            ViewModelLoadStatus.NotLoaded,
            ViewModelLoadStatus.Loading,
            ViewModelLoadStatus.LoadFailed -> {
                adapter.setDataFields(listOf())
                saveButton.isEnabled = false
            }
            is ViewModelLoadStatus.Loaded -> {
                val viewModel = viewModelLoadStatus.viewModel
                adapter.setDataFields(viewModel.fields)
                saveButton.isEnabled = viewModel.eventIsValid
            }
        }
    }

    private fun loadHuntingControlEventIfNotLoaded() {
        if (controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }

        MainScope().launch {
            controller.loadViewModel()
        }
    }

    private fun createHuntingControlEvent() {
        interactionManager.onCreatingNewHuntingControlEvent()

        val scope = MainScope()

        scope.launch {
            val result = controller.saveHuntingControlEvent(updateToBackend = syncConfig.isAutomatic())

            // allow cancellation to take effect i.e don't continue updating UI
            // if saveScope has been cancelled
            yield()

            if (!isResumed) {
                return@launch
            }

            if (result is HuntingControlEventOperationResponse.Success) {
                interactionManager.onNewHuntingControlEventCreateCompleted(true, result.event.localId)
            } else {
                interactionManager.onNewHuntingControlEventCreateCompleted(false, null) {
                    AlertDialogFragment.Builder(
                        requireContext(),
                        AlertDialogId.CREATE_HUNTING_CONTROL_EVENT_FRAGMENT_SAVE_FAILED
                    )
                        .setMessage(R.string.hunting_control_event_save_failed_generic)
                        .setPositiveButton(R.string.ok)
                        .build()
                        .show(requireActivity().supportFragmentManager)
                }
            }
        }
        saveScope = scope
    }

    override fun getController(): CreateHuntingControlEventController {
        return controller
    }

    override fun showAttachment(attachment: HuntingControlAttachment) {
        interactionManager.showAttachment(attachment)
    }

    companion object {
        private const val CONTROLLER_STATE_PREFIX = "CHCE_controller"
    }
}
