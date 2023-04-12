package fi.riista.mobile.models.shootingTest

import android.content.Context
import androidx.annotation.StringRes
import com.fasterxml.jackson.annotation.JsonProperty
import fi.riista.common.domain.model.ShootingTestType
import fi.riista.common.domain.shootingTest.model.ShootingTestResult
import fi.riista.mobile.R
import fi.riista.mobile.utils.Utils
import java.io.Serializable

class ShootingTestAttemptDetailed : Serializable {
    @JvmField
    @JsonProperty("id")
    var id: Long = -1

    @JsonProperty("rev")
    var rev = 0

    // TODO Verify that enum type works flawlessly when Proguard minification is enabled.
    @JvmField
    @JsonProperty("type")
    var type: ShootingTestType? = null

    // TODO Verify that enum type works flawlessly when Proguard minification is enabled.
    @JvmField
    @JsonProperty("result")
    var result: ShootingTestResult? = null

    @JvmField
    @JsonProperty("hits")
    var hits = 0

    @JsonProperty("note")
    var note: String? = null

    @JsonProperty("author")
    var author: String? = null
    fun validateData(): Boolean {
        if (type == null) {
            Utils.LogMessage("Attempt type not set")
            return false
        }
        if (hits < 0 || hits > 4) {
            Utils.LogMessage("Attempt hits not valid: $hits")
            return false
        }
        if (ShootingTestType.BOW == type && hits == 4) {
            Utils.LogMessage("Attempt hits too high for bow")
            return false
        }
        if (result == null) {
            Utils.LogMessage("Attempt result not set")
            return false
        }

        // TODO: Validate note
        return true
    }

    companion object {
        @JvmStatic
        fun localizedTypeText(context: Context, testType: ShootingTestType?): String? {
            @StringRes var stringResId: Int? = null
            if (testType != null) {
                stringResId = when (testType) {
                    ShootingTestType.MOOSE -> R.string.shooting_test_type_moose
                    ShootingTestType.BEAR -> R.string.shooting_test_type_bear
                    ShootingTestType.ROE_DEER -> R.string.shooting_test_type_roe_deer
                    ShootingTestType.BOW -> R.string.shooting_test_type_bow
                }
            }
            return if (stringResId != null) context.getString(stringResId) else null
        }

        @JvmStatic
        fun localisedResultText(
            context: Context,
            result: ShootingTestResult?
        ): String? {
            @StringRes var stringResId: Int? = null
            if (result != null) {
                stringResId = when (result) {
                    ShootingTestResult.QUALIFIED -> R.string.shooting_test_result_qualified
                    ShootingTestResult.UNQUALIFIED -> R.string.shooting_test_result_unqualified
                    ShootingTestResult.TIMED_OUT -> R.string.shooting_test_result_timed_out
                    ShootingTestResult.REBATED -> R.string.shooting_test_result_rebated
                }
            }
            return if (stringResId != null) context.getString(stringResId) else null
        }
    }
}
