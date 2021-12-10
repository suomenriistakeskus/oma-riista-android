package fi.riista.mobile.gamelog

import fi.riista.mobile.AppConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Temporary class during deer pilot for resolving HarvestSpecVersion.
 *
 * TODO Remove this class when deer pilot 2020 is over.
 */
@Singleton
class HarvestSpecVersionResolver @Inject constructor(
        private val deerHuntingFeatureAvailability: DeerHuntingFeatureAvailability) {

    fun resolveHarvestSpecVersion(): Int = when (deerHuntingFeatureAvailability.enabled) {
        true -> AppConfig.HARVEST_SPEC_VERSION_IN_DEER_PILOT
        false -> AppConfig.HARVEST_SPEC_VERSION
    }
}
