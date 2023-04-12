package fi.riista.mobile.feature.groupHunting.harvests

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.LocationListener
import fi.riista.common.domain.groupHunting.GroupHuntingHarvestOperationResponse
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvestId
import fi.riista.common.domain.groupHunting.ui.groupHarvest.modify.CreateGroupHarvestController
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.util.toETRMSGeoLocation
import fi.riista.mobile.LocationClientProvider
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.viewHolder.SelectHuntingDayLauncher
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.DelegatingAlertDialogListener
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderTypeResolver
import fi.riista.mobile.ui.dataFields.viewHolder.DateTimePickerFragmentLauncher
import fi.riista.mobile.ui.dataFields.viewHolder.MapOpener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * A fragment for creating a new [GroupHuntingHarvest]
 */
class CreateGroupHarvestFragment
    : ModifyGroupHarvestFragment<CreateGroupHarvestController, CreateGroupHarvestFragment.Manager>()
    , DataFieldViewHolderTypeResolver<CommonHarvestField>
    , MapOpener
    , SelectHuntingDayLauncher<CommonHarvestField>
    , DateTimePickerFragmentLauncher<CommonHarvestField>
    , LocationListener
{

    interface Manager : BaseManager, LocationClientProvider {
        val createGroupHarvestController: CreateGroupHarvestController

        fun onCreatingNewHarvest()
        fun onNewHarvestCreateCompleted(success: Boolean, harvestId: GroupHuntingHarvestId?,
                                        createObservation: Boolean,
                                        indicatorsDismissed: () -> Unit = {})
    }

    private lateinit var dialogListener: AlertDialogFragment.Listener

    private var saveScope: CoroutineScope? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        setViewTitle(R.string.loggame)
        saveButton.setText(R.string.save)

        dialogListener = DelegatingAlertDialogListener(requireActivity()).apply {
            registerPositiveCallback(AlertDialogId.CREATE_GROUP_HARVEST_FRAGMENT_CREATE_OBSERVATION_QUESTION) { value ->
                value?.toLong().let { harvestId ->
                    manager.onNewHarvestCreateCompleted(success = true, harvestId = harvestId, createObservation = false)
                }
            }
            registerNegativeCallback(AlertDialogId.CREATE_GROUP_HARVEST_FRAGMENT_CREATE_OBSERVATION_QUESTION) { value ->
                value?.toLong().let { harvestId ->
                    manager.onNewHarvestCreateCompleted(success = true, harvestId = harvestId, createObservation = true)
                }
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()

        if (controller.canMoveHarvestToCurrentUserLocation()) {
            manager.locationClient.addListener(this)
        }
    }

    override fun onPause() {
        super.onPause()
        manager.locationClient.removeListener(this)
        saveScope?.cancel()
    }

    override fun onLocationChanged(location: Location) {
        val harvestLocationChanged = controller.tryMoveHarvestToCurrentUserLocation(
                location = location.toETRMSGeoLocation(GeoLocationSource.GPS_DEVICE)
        )

        if (!harvestLocationChanged) {
            manager.locationClient.removeListener(this)
        }
    }

    override fun onSaveButtonClicked() {
        manager.onCreatingNewHarvest()

        val scope = MainScope()

        scope.launch {
            val shouldCreateObservation = controller.shouldCreateObservation()
            val result = controller.createHarvest()

            // allow cancellation to take effect i.e don't continue updating UI
            // if saveScope has been cancelled
            yield()

            if (!isResumed) {
                return@launch
            }

            if (result is GroupHuntingHarvestOperationResponse.Success) {
                if (shouldCreateObservation) {
                    AlertDialogFragment.Builder(
                        requireContext(),
                        AlertDialogId.CREATE_GROUP_HARVEST_FRAGMENT_CREATE_OBSERVATION_QUESTION
                    )
                        .setMessage(R.string.group_hunting_create_observation_from_harvest)
                        .setPositiveButton(R.string.yes, result.harvest.id.toString())
                        .setNegativeButton(R.string.no, result.harvest.id.toString())
                        .build()
                        .show(requireActivity().supportFragmentManager)
                } else {
                    manager.onNewHarvestCreateCompleted(
                        success = true,
                        harvestId = result.harvest.id,
                        createObservation = false
                    )
                }
            } else {
                manager.onNewHarvestCreateCompleted(false, null, false) {
                    AlertDialogFragment.Builder(
                        requireContext(),
                        AlertDialogId.CREATE_GROUP_HARVEST_FRAGMENT_HARVEST_SAVE_FAILED
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

    override fun getControllerFromManager(): CreateGroupHarvestController {
        return manager.createGroupHarvestController
    }
}
