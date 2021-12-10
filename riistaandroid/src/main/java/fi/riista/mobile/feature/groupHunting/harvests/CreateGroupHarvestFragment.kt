package fi.riista.mobile.feature.groupHunting.harvests

import android.app.AlertDialog
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.LocationListener
import fi.riista.common.groupHunting.GroupHuntingHarvestOperationResponse
import fi.riista.common.groupHunting.model.*
import fi.riista.common.groupHunting.ui.GroupHarvestField
import fi.riista.common.groupHunting.ui.groupHarvest.modify.CreateGroupHarvestController
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.HunterNumber
import fi.riista.common.ui.dataField.*
import fi.riista.common.util.toETRMSGeoLocation
import fi.riista.mobile.LocationClientProvider
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.viewHolder.*
import fi.riista.mobile.ui.DateTimePickerFragment
import kotlinx.coroutines.*

/**
 * A fragment for creating a new [GroupHuntingHarvest]
 */
class CreateGroupHarvestFragment
    : ModifyGroupHarvestFragment<CreateGroupHarvestController, CreateGroupHarvestFragment.Manager>()
    , DataFieldViewHolderTypeResolver<GroupHarvestField>
    , MapOpener
    , SelectHuntingDayLauncher<GroupHarvestField>
    , DateTimePickerFragmentLauncher<GroupHarvestField>
    , DateTimePickerFragment.Listener
    , LocationListener {

    interface Manager : BaseManager, LocationClientProvider {
        val createGroupHarvestController: CreateGroupHarvestController

        fun onCreatingNewHarvest()
        fun onNewHarvestCreateCompleted(success: Boolean, harvestId: GroupHuntingHarvestId?,
                                        createObservation: Boolean,
                                        indicatorsDismissed: () -> Unit = {})
    }

    private var saveScope: CoroutineScope? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        setViewTitle(R.string.loggame)
        saveButton.setText(R.string.save)

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

    override fun onLocationChanged(location: Location?) {
        if (location == null) {
            return
        }

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
                    AlertDialog.Builder(requireContext())
                        .setMessage(R.string.group_hunting_create_observation_from_harvest)
                        .setPositiveButton(R.string.yes) { _, _ ->
                            manager.onNewHarvestCreateCompleted(
                                success = true,
                                harvestId = result.harvest.id,
                                createObservation = false
                            )
                        }
                        .setNegativeButton(R.string.no) { _, _ ->
                            manager.onNewHarvestCreateCompleted(
                                success = true,
                                harvestId = result.harvest.id,
                                createObservation = true
                            )
                        }
                        .create()
                        .show()
                } else {
                    manager.onNewHarvestCreateCompleted(
                        success = true,
                        harvestId = result.harvest.id,
                        createObservation = false
                    )
                }
            } else {
                manager.onNewHarvestCreateCompleted(false, null, false) {
                    AlertDialog.Builder(requireContext())
                        .setMessage(R.string.group_hunting_harvest_save_failed_generic)
                        .setPositiveButton(R.string.ok, null)
                        .create()
                        .show()
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
