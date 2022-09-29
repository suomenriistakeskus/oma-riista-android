package fi.riista.mobile.feature.observation

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.LocationListener
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.ui.modify.CreateObservationController
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.util.toETRMSGeoLocation
import fi.riista.mobile.LocationClientProvider
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory

/**
 * A fragment for creating [CommonObservation].
 */
class CreateObservationFragment
    : ModifyObservationFragment<
        CreateObservationController,
        CreateObservationFragment.Manager>()
    , LocationListener {

    interface Manager : BaseManager, LocationClientProvider {
        fun onCreateObservation(observation: CommonObservation)
    }

    override val controller: CreateObservationController by lazy {
        CreateObservationController(
            userContext = RiistaSDK.currentUserContext,
            metadataProvider = RiistaSDK.metadataProvider,
            stringProvider = ContextStringProviderFactory.createForContext(requireContext())
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  super.onCreateView(inflater, container, savedInstanceState)

        arguments?.let { args ->
            controller.initialSpeciesCode = getSpeciesCodeFromArgs(args)
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        if (controller.canMoveObservationToCurrentUserLocation()) {
            manager.locationClient.addListener(this)
        }
    }

    override fun onPause() {
        super.onPause()

        manager.locationClient.removeListener(this)
    }



    override fun onSaveButtonClicked() {
        val observation = controller.getValidatedObservation()
            ?: kotlin.run {
                return
            }

        manager.onCreateObservation(observation = observation)
    }

    override fun getManagerFromContext(context: Context): Manager {
        return context as Manager
    }

    override fun onLocationChanged(location: Location) {
        val locationChanged = controller.tryMoveObservationToCurrentUserLocation(
            location = location.toETRMSGeoLocation(GeoLocationSource.GPS_DEVICE)
        )

        if (!locationChanged) {
            manager.locationClient.removeListener(this)
        }
    }

    companion object {

        private const val ARGS_PREFIX = "CreateObservationFragment"
        private const val KEY_SPECIES_CODE = "${ARGS_PREFIX}_species_code"


        fun create(speciesCode: SpeciesCode?): CreateObservationFragment {
            return CreateObservationFragment().apply {
                arguments = Bundle().also { bundle ->
                    if (speciesCode != null) {
                        bundle.putInt(KEY_SPECIES_CODE, speciesCode)
                    }
                }
            }
        }

        private fun getSpeciesCodeFromArgs(arguments: Bundle): SpeciesCode? {
            return arguments.getInt(KEY_SPECIES_CODE, -100)
                .takeIf { it != -100 }
        }
    }
}
