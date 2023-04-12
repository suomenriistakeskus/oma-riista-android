package fi.riista.mobile.feature.srva

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationListener
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.ui.modify.CreateSrvaEventController
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.util.toETRMSGeoLocation
import fi.riista.mobile.LocationClientProvider
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory

/**
 * A fragment for creating [CommonSrvaEvent].
 */
class CreateSrvaEventFragment
    : ModifySrvaEventFragment<CreateSrvaEventController, CreateSrvaEventFragment.Manager>()
    , LocationListener
{
    interface Manager : BaseManager, LocationClientProvider {
        fun onNewSrvaEventCreateCompleted(srvaEvent: CommonSrvaEvent)
    }

    override val controller: CreateSrvaEventController by lazy {
        CreateSrvaEventController(
            metadataProvider = RiistaSDK.metadataProvider,
            stringProvider = ContextStringProviderFactory.createForContext(requireContext()),
            srvaContext = RiistaSDK.srvaContext,
        )
    }

    override fun onResume() {
        super.onResume()

        if (controller.canMoveSrvaEventToCurrentUserLocation()) {
            manager.locationClient.addListener(this)
        }
    }

    override fun onPause() {
        super.onPause()

        manager.locationClient.removeListener(this)
    }

    override fun notifyManagerAboutSuccessfulSave(srvaEvent: CommonSrvaEvent) {
        manager.onNewSrvaEventCreateCompleted(srvaEvent)
    }

    override fun getManagerFromContext(context: Context): Manager {
        return context as Manager
    }

    override fun onLocationChanged(location: Location) {
        val locationChanged = controller.tryMoveSrvaEventToCurrentUserLocation(
            location = location.toETRMSGeoLocation(GeoLocationSource.GPS_DEVICE)
        )

        if (!locationChanged) {
            manager.locationClient.removeListener(this)
        }
    }
}
