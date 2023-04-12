package fi.riista.mobile.models.shootingTest

import com.fasterxml.jackson.annotation.JsonProperty
import fi.riista.common.domain.model.ShootingTestType
import java.io.Serializable

class ShootingTestParticipant : Serializable {
    @JsonProperty("id")
    var id: Long = 0

    @JsonProperty("rev")
    var rev = 0

    @JsonProperty("firstName")
    var firstName: String? = null

    @JsonProperty("lastName")
    var lastName: String? = null

    @JsonProperty("hunterNumber")
    var hunterNumber: String? = null

    @JsonProperty("mooseTestIntended")
    var mooseTestIntended = false

    @JsonProperty("bearTestIntended")
    var bearTestIntended = false

    @JsonProperty("deerTestIntended")
    var deerTestIntended = false

    @JsonProperty("bowTestIntended")
    var bowTestIntended = false

    @JsonProperty("attempts")
    var attempts: List<ShootingTestAttemptSummary> = ArrayList()

    @JsonProperty("totalDueAmount")
    var totalDueAmount = 0

    @JsonProperty("paidAmount")
    var paidAmount = 0

    @JsonProperty("remainingAmount")
    var remainingAmount = 0

    @JsonProperty("registrationTime")
    var registrationTime: String? = null

    @JsonProperty("completed")
    var completed = false

    fun getAttemptSummaryFor(type: ShootingTestType?): ShootingTestAttemptSummary? {
        if (type != null) {
            return attempts.firstOrNull { it.type === type }
        }
        return null
    }
}
