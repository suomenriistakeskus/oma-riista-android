package fi.riista.mobile.feature.harvest

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.LocationListener
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.ui.modify.CreateHarvestController
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.util.toETRMSGeoLocation
import fi.riista.mobile.LocationClientProvider
import fi.riista.mobile.riistaSdkHelpers.AppLanguageProvider
import fi.riista.mobile.riistaSdkHelpers.AppSpeciesResolver
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory

/**
 * A fragment for creating [CommonHarvest].
 */
class CreateHarvestFragment
    : ModifyHarvestFragment<
        CreateHarvestController,
        CreateHarvestFragment.Manager>()
    , LocationListener {

    interface Manager : BaseManager, LocationClientProvider {
        fun onCreateHarvestCompleted(harvest: CommonHarvest)
    }

    override val controller: CreateHarvestController by lazy {
        CreateHarvestController(
            harvestSeasons = RiistaSDK.harvestSeasons,
            harvestContext = RiistaSDK.harvestContext,
            harvestPermitProvider = harvestPermitProvider,
            selectableHuntingClubs = RiistaSDK.huntingClubsSelectableForEntriesFactory.create(),
            languageProvider = AppLanguageProvider(requireContext()),
            preferences = RiistaSDK.preferences,
            speciesResolver = AppSpeciesResolver(),
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

        if (controller.canMoveHarvestToCurrentUserLocation()) {
            manager.locationClient.addListener(this)
        }
    }

    override fun onPause() {
        super.onPause()

        manager.locationClient.removeListener(this)
    }

    override fun notifyManagerAboutSuccessfulSave(harvest: CommonHarvest) {
        manager.onCreateHarvestCompleted(harvest)
    }

    override fun getManagerFromContext(context: Context): Manager {
        return context as Manager
    }

    override fun onLocationChanged(location: Location) {
        val locationChanged = controller.tryMoveHarvestToCurrentUserLocation(
            location = location.toETRMSGeoLocation(GeoLocationSource.GPS_DEVICE)
        )

        if (!locationChanged) {
            manager.locationClient.removeListener(this)
        }
    }

    companion object {

        private const val ARGS_PREFIX = "CreateHarvestFragment"
        private const val KEY_SPECIES_CODE = "${ARGS_PREFIX}_species_code"


        fun create(speciesCode: SpeciesCode?): CreateHarvestFragment {
            return CreateHarvestFragment().apply {
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
