package fi.riista.mobile.models.shootingTest

import com.fasterxml.jackson.annotation.JsonProperty
import fi.riista.common.domain.model.ShootingTestType

class ShootingTestAttemptSummary {
    // TODO Verify that enum type works flawlessly when Proguard minification is enabled.
    @JsonProperty("type")
    var type: ShootingTestType? = null

    @JsonProperty("attemptCount")
    var attemptCount = 0

    @JsonProperty("qualified")
    var qualified = false
}
