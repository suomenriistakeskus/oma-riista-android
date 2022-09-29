package fi.riista.mobile.feature.groupHunting.observations

import android.app.AlertDialog
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationListener
import com.google.android.material.button.MaterialButton
import fi.riista.common.domain.groupHunting.GroupHuntingObservationOperationResponse
import fi.riista.common.domain.groupHunting.model.GroupHuntingObservationId
import fi.riista.common.domain.groupHunting.model.HuntingGroupTarget
import fi.riista.common.domain.groupHunting.ui.GroupObservationField
import fi.riista.common.domain.groupHunting.ui.groupObservation.modify.CreateGroupObservationController
import fi.riista.common.domain.groupHunting.ui.groupObservation.modify.ModifyGroupObservationViewModel
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.common.util.toETRMSGeoLocation
import fi.riista.mobile.LocationClientProvider
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.NoChangeAnimationsItemAnimator
import kotlinx.coroutines.*

class CreateGroupObservationFragment
    : ModifyGroupObservationFragment<CreateGroupObservationController>()
    , LocationListener
{

    interface InteractionManager: LocationClientProvider {
        val createGroupObservationController: CreateGroupObservationController
        val huntingGroupTarget: HuntingGroupTarget

        fun onCreatingNewObservation()
        fun onNewObservationCreateCompleted(
            success: Boolean, observationId: GroupHuntingObservationId?,
            indicatorsDismissed: () -> Unit = {}
        )
        fun cancelCreateNewObservation()
    }

    private lateinit var adapter: DataFieldRecyclerViewAdapter<GroupObservationField>
    private lateinit var approveButton: MaterialButton

    private lateinit var interactionManager: InteractionManager
    private lateinit var controller: CreateGroupObservationController
    private val disposeBag = DisposeBag()
    private var saveScope: CoroutineScope? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interactionManager = context as InteractionManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_group_observation, container, false)
        setViewTitle(R.string.group_hunting_new_observation)

        controller = interactionManager.createGroupObservationController

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
                interactionManager.cancelCreateNewObservation()
            }
        }

        approveButton = view.findViewById<MaterialButton>(R.id.btn_save)!!
            .also { btn ->
                btn.setOnClickListener {
                    createObservation()
                }
            }

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

        controller.observationLocationCanBeMovedAutomatically.bindAndNotify {
            when (it) {
                true -> interactionManager.locationClient.addListener(this)
                false -> interactionManager.locationClient.removeListener(this)
            }
        }.disposeBy(disposeBag)

        loadObservationIfNotLoaded()
    }

    override fun onPause() {
        super.onPause()

        saveScope?.cancel()
        disposeBag.disposeAll()

        interactionManager.locationClient.removeListener(this)
    }

    override fun onLocationChanged(location: Location) {
        val observationLocationChanged = controller.tryMoveObservationToCurrentUserLocation(
                location = location.toETRMSGeoLocation(GeoLocationSource.GPS_DEVICE)
        )

        if (!observationLocationChanged) {
            interactionManager.locationClient.removeListener(this)
        }
    }

    private fun updateBasedOnViewModel(
        viewModelLoadStatus: ViewModelLoadStatus<ModifyGroupObservationViewModel>
    ) {
        when (viewModelLoadStatus) {
            ViewModelLoadStatus.NotLoaded,
            ViewModelLoadStatus.Loading,
            ViewModelLoadStatus.LoadFailed -> {
                adapter.setDataFields(listOf())
                approveButton.isEnabled = false
            }
            is ViewModelLoadStatus.Loaded -> {
                val viewModel = viewModelLoadStatus.viewModel
                adapter.setDataFields(viewModel.fields)
                approveButton.isEnabled = viewModel.observationIsValid
            }
        }
    }

    private fun loadObservationIfNotLoaded() {
        if (controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }

        MainScope().launch {
            controller.loadViewModel()
        }
    }

    private fun createObservation() {
        interactionManager.onCreatingNewObservation()

        val scope = MainScope()

        scope.launch {
            val result = controller.createObservation()

            // allow cancellation to take effect i.e don't continue updating UI
            // if saveScope has been cancelled
            yield()

            if (!isResumed) {
                return@launch
            }

            if (result is GroupHuntingObservationOperationResponse.Success) {
                interactionManager.onNewObservationCreateCompleted(true, result.observation.id)
            } else {
                interactionManager.onNewObservationCreateCompleted(false, null) {
                    AlertDialog.Builder(requireContext())
                        .setMessage(R.string.group_hunting_observation_save_failed_generic)
                        .setPositiveButton(R.string.ok, null)
                        .create()
                        .show()
                }
            }
        }
        saveScope = scope
    }

    override fun getHuntingGroupTarget(): HuntingGroupTarget {
        return interactionManager.huntingGroupTarget
    }

    override fun getController(): CreateGroupObservationController {
        return controller
    }

    companion object {
        private const val CONTROLLER_STATE_PREFIX = "CGOF_controller"
    }
}
