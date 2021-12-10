package fi.riista.mobile.models.shootingTest;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import fi.riista.mobile.R;
import fi.riista.mobile.utils.Utils;

import static fi.riista.mobile.models.shootingTest.ShootingTestType.BOW;

public class ShootingTestAttemptDetailed implements Serializable {

    @JsonProperty("id")
    public long id = -1;

    @JsonProperty("rev")
    public int rev;

    // TODO Verify that enum type works flawlessly when Proguard minification is enabled.
    @JsonProperty("type")
    public ShootingTestType type;

    // TODO Verify that enum type works flawlessly when Proguard minification is enabled.
    @JsonProperty("result")
    public ShootingTestResult result;

    @JsonProperty("hits")
    public int hits;

    @JsonProperty("note")
    public String note;

    @JsonProperty("author")
    public String author;

    public static String localizedTypeText(@NonNull final Context context, @Nullable final ShootingTestType testType) {
        @StringRes Integer stringResId = null;

        if (testType != null) {
            switch (testType) {
                case MOOSE:
                    stringResId = R.string.shooting_test_type_moose;
                    break;
                case BEAR:
                    stringResId = R.string.shooting_test_type_bear;
                    break;
                case ROE_DEER:
                    stringResId = R.string.shooting_test_type_roe_deer;
                    break;
                case BOW:
                    stringResId = R.string.shooting_test_type_bow;
                    break;
            }
        }

        return stringResId != null ? context.getString(stringResId) : null;
    }

    public static String localisedResultText(@NonNull final Context context,
                                             @Nullable final ShootingTestResult result) {

        @StringRes Integer stringResId = null;

        if (result != null) {
            switch (result) {
                case QUALIFIED:
                    stringResId = R.string.shooting_test_result_qualified;
                    break;
                case UNQUALIFIED:
                    stringResId = R.string.shooting_test_result_unqualified;
                    break;
                case TIMED_OUT:
                    stringResId = R.string.shooting_test_result_timed_out;
                    break;
                case REBATED:
                    stringResId = R.string.shooting_test_result_rebated;
                    break;
            }
        }

        return stringResId != null ? context.getString(stringResId) : null;
    }

    public boolean validateData() {
        if (type == null) {
            Utils.LogMessage("Attempt type not set");
            return false;
        }
        if (hits < 0 || hits > 4) {
            Utils.LogMessage("Attempt hits not valid: " + hits);
            return false;
        }
        if (BOW.equals(type) && hits == 4) {
            Utils.LogMessage("Attempt hits too high for bow");
            return false;
        }
        if (result == null) {
            Utils.LogMessage("Attempt result not set");
            return false;
        }

        // TODO: Validate note

        return true;
    }
}
