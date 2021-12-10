package fi.riista.mobile.feature.groupHunting.observations

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import fi.riista.common.groupHunting.GroupHuntingObservationOperationResponse
import fi.riista.common.groupHunting.model.GroupHuntingObservationTarget
import fi.riista.common.groupHunting.model.HuntingGroupTarget
import fi.riista.common.groupHunting.model.asGroupTarget
import fi.riista.common.groupHunting.ui.GroupObservationField
import fi.riista.common.groupHunting.ui.groupObservation.modify.EditGroupObservationController
import fi.riista.common.groupHunting.ui.groupObservation.modify.ModifyGroupObservationViewModel
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.common.ui.dataField.*
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.feature.groupHunting.dataFields.viewHolder.*
import fi.riista.mobile.ui.NoChangeAnimationsItemAnimator
import kotlinx.coroutines.*

class EditGroupObservationFragment
    : ModifyGroupObservationFragment<EditGroupObservationController>()
{

    interface InteractionManager {
        val editGroupObservationController: EditGroupObservationController
        val groupHuntingObservationTarget: GroupHuntingObservationTarget

        fun onSavingProposedObservation()
        fun onProposedObservationSaveCompleted(success: Boolean, indicatorsDismissed: () -> Unit = {})
        fun cancelProposedObservationApproval()
    }

    private lateinit var adapter: DataFieldRecyclerViewAdapter<GroupObservationField>
    private lateinit var approveButton: MaterialButton

    private lateinit var interactionManager: InteractionManager
    private lateinit var controller: EditGroupObservationController
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
        val view = inflater.inflate(R.layout.fragment_edit_group_observation, container, false)

        val observationAlreadyAccepted = getObservationAlreadyAcceptedFromArgs(requireArguments())
        val viewTitle = when(observationAlreadyAccepted) {
            true -> R.string.edit
            false -> R.string.group_hunting_approve_proposed_observation
        }
        setViewTitle(viewTitle)

        controller = interactionManager.editGroupObservationController

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
                interactionManager.cancelProposedObservationApproval()
            }
        }

        approveButton = view.findViewById<MaterialButton>(R.id.btn_approve_proposed_observation)!!
            .also { btn ->
                val approveButtonText = when(observationAlreadyAccepted) {
                    true -> R.string.save
                    false -> R.string.group_hunting_approve
                }
                btn.setText(approveButtonText)

                btn.setOnClickListener {
                    approveProposedObservation()
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
            when (viewModelLoadStatus) {
                ViewModelLoadStatus.NotLoaded -> {}
                ViewModelLoadStatus.Loading -> {}
                ViewModelLoadStatus.LoadFailed -> {}
                is ViewModelLoadStatus.Loaded -> {
                    updateBasedOnViewModel(viewModelLoadStatus.viewModel)
                }
            }
        }.disposeBy(disposeBag)

        loadObservationIfNotLoaded()
    }

    override fun onPause() {
        super.onPause()

        saveScope?.cancel()
        disposeBag.disposeAll()
    }

    private fun updateBasedOnViewModel(viewModel: ModifyGroupObservationViewModel) {
        adapter.setDataFields(viewModel.fields)
        approveButton.isEnabled = viewModel.observationIsValid
    }

    private fun loadObservationIfNotLoaded() {
        val loadStatus = controller.viewModelLoadStatus.value
        if (loadStatus is ViewModelLoadStatus.Loaded) {
            return
        }

        MainScope().launch {
            controller.loadViewModel()
        }
    }

    private fun approveProposedObservation() {
        interactionManager.onSavingProposedObservation()

        val scope = MainScope()

        scope.launch {
            val result = controller.acceptObservation()

            // allow cancellation to take effect i.e don't continue updating UI
            // if saveScope has been cancelled
            yield()
            if (!isResumed) {
                return@launch
            }

            if (result is GroupHuntingObservationOperationResponse.Success) {
                interactionManager.onProposedObservationSaveCompleted(true)
            } else {
                interactionManager.onProposedObservationSaveCompleted(false) {
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
        return interactionManager.groupHuntingObservationTarget.asGroupTarget()
    }

    override fun getController(): EditGroupObservationController {
        return controller;
    }

    companion object {
        private const val CONTROLLER_STATE_PREFIX = "PPGOF_controller"
        private const val KEY_OBSERVATION_ALREADY_ACCEPTED = "PPGOF_observationAlreadyAccepted"

        fun create(observationAlreadyAccepted: Boolean): EditGroupObservationFragment {
            return EditGroupObservationFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(KEY_OBSERVATION_ALREADY_ACCEPTED, observationAlreadyAccepted)
                }
            }
        }

        private fun getObservationAlreadyAcceptedFromArgs(args: Bundle): Boolean {
            return args.getBoolean(KEY_OBSERVATION_ALREADY_ACCEPTED, true)
        }
    }
}
