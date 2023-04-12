package fi.riista.mobile.feature.groupHunting.harvests

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fi.riista.common.domain.groupHunting.GroupHuntingHarvestOperationResponse
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvestId
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvestTarget
import fi.riista.common.domain.groupHunting.ui.groupHarvest.modify.EditGroupHarvestController
import fi.riista.mobile.R
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.DelegatingAlertDialogListener
import fi.riista.mobile.ui.AlertDialogId
import kotlinx.coroutines.*

/**
 * A fragment for either approving a proposed [GroupHuntingHarvest] or
 * editing an already accepted [GroupHuntingHarvest].
 */
class EditGroupHarvestFragment
    : ModifyGroupHarvestFragment<EditGroupHarvestController, EditGroupHarvestFragment.Manager>()
{

    interface Manager : BaseManager {
        val editGroupHarvestController: EditGroupHarvestController
        val groupHuntingHarvestTarget: GroupHuntingHarvestTarget

        fun onSavingHarvest()
        fun onHarvestSaveCompleted(success: Boolean, harvestId: GroupHuntingHarvestId?, createObservation: Boolean, indicatorsDismissed: () -> Unit = {})
    }

    enum class Mode {
        APPROVE,
        EDIT
    }

    private lateinit var dialogListener: AlertDialogFragment.Listener

    private lateinit var mode: Mode
    private var saveScope: CoroutineScope? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        mode = getModeFromArgs(arguments)
        setViewTitle(
                when (mode) {
                    Mode.APPROVE -> R.string.group_hunting_approve_proposed_harvest
                    Mode.EDIT -> R.string.edit
                }
        )
        saveButton.setText(
                when (mode) {
                    Mode.APPROVE -> R.string.group_hunting_approve
                    Mode.EDIT -> R.string.save
                }
        )

        dialogListener = DelegatingAlertDialogListener(requireActivity()).apply {
            registerPositiveCallback(AlertDialogId.EDIT_GROUP_HARVEST_FRAGMENT_CREATE_OBSERVATION_QUESTION) { value ->
                value?.toLong().let { harvestId ->
                    manager.onHarvestSaveCompleted(success = true, harvestId = harvestId, createObservation = false)
                }
            }
            registerNegativeCallback(AlertDialogId.EDIT_GROUP_HARVEST_FRAGMENT_CREATE_OBSERVATION_QUESTION) { value ->
                value?.toLong().let { harvestId ->
                    manager.onHarvestSaveCompleted(success = true, harvestId = harvestId, createObservation = true)
                }
            }
        }
        return view
    }

    override fun onPause() {
        super.onPause()
        saveScope?.cancel()
    }

    override fun onSaveButtonClicked() {
        manager.onSavingHarvest()

        val scope = MainScope()

        scope.launch {
            val result = when (mode) {
                Mode.APPROVE -> controller.acceptHarvest()
                Mode.EDIT -> controller.updateHarvest()
            }

            // allow cancellation to take effect i.e don't continue updating UI
            // if saveScope has been cancelled
            yield()

            if (result is GroupHuntingHarvestOperationResponse.Success) {
                if (!isResumed) {
                    return@launch
                }
                val shouldCreateObservation = (mode == Mode.APPROVE) && controller.shouldCreateObservation()

                if (shouldCreateObservation) {
                    AlertDialogFragment.Builder(
                        requireContext(),
                        AlertDialogId.EDIT_GROUP_HARVEST_FRAGMENT_CREATE_OBSERVATION_QUESTION
                    )
                        .setMessage(R.string.group_hunting_create_observation_from_harvest)
                        .setPositiveButton(R.string.yes, result.harvest.id.toString())
                        .setNegativeButton(R.string.no, result.harvest.id.toString())
                        .build()
                        .show(requireActivity().supportFragmentManager)
                } else {
                    manager.onHarvestSaveCompleted(success = true, harvestId = result.harvest.id, createObservation = false)
                }
            } else {
                manager.onHarvestSaveCompleted(success = false, harvestId = null, createObservation = false) {
                    if (!isResumed) {
                        return@onHarvestSaveCompleted
                    }
                    AlertDialogFragment.Builder(
                        requireContext(),
                        AlertDialogId.EDIT_GROUP_HARVEST_FRAGMENT_HARVEST_SAVE_FAILED
                    )
                        .setMessage(R.string.group_hunting_harvest_save_failed_generic)
                        .setPositiveButton(R.string.ok)
                        .build()
                        .show(requireActivity().supportFragmentManager)
                }
            }
        }
        saveScope = scope
    }

    override fun getManagerFromContext(context: Context): Manager {
        return context as Manager
    }

    override fun getControllerFromManager(): EditGroupHarvestController {
        return manager.editGroupHarvestController
    }

    companion object {
        private const val ARGS_MODE = "EGHF_args_mode"

        fun create(mode: Mode): EditGroupHarvestFragment {
            return EditGroupHarvestFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGS_MODE, mode.toString())
                }
            }
        }

        private fun getModeFromArgs(arguments: Bundle?): Mode {
            return Mode.valueOf(requireNotNull(arguments?.getString(ARGS_MODE)))
        }
    }
}
