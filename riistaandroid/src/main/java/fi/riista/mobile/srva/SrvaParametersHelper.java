package fi.riista.mobile.srva;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.srva.SrvaParameters;
import fi.riista.mobile.network.FetchSrvaParametersTask;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.NetworkTask;

import static java.util.Objects.requireNonNull;

public class SrvaParametersHelper {

    private static final String CACHE_FILE = "srvaparameters_" + AppConfig.SRVA_SPEC_VERSION + ".json";

    private static SrvaParametersHelper sInstance;

    private final WorkContext mAppWorkContext;
    private SrvaParameters mParameters;

    public static void init(@NonNull final WorkContext context) {
        sInstance = new SrvaParametersHelper(context);
    }

    public static SrvaParametersHelper getInstance() {
        return sInstance;
    }

    private SrvaParametersHelper(@NonNull final WorkContext workContext) {
        mAppWorkContext = requireNonNull(workContext);
    }

    public void fetchParameters() {
        final String url = NetworkTask.SCHEME_INTERNAL + CACHE_FILE;

        final FetchSrvaParametersTask task = new FetchSrvaParametersTask(mAppWorkContext, url) {
            @Override
            protected void onFinishObject(final SrvaParameters result) {
                Utils.LogMessage("Got cached SRVA parameters");

                mParameters = result;
            }

            @Override
            protected void onError() {
                Utils.LogMessage("SRVA cached parameters error: " + getError().getMessage());
            }

            @Override
            protected void onEnd() {
                fetchServerParameters();
            }
        };
        task.start();
    }

    private void fetchServerParameters() {
        final String url = AppConfig.getBaseUrl() + "/srva/parameters?srvaEventSpecVersion=" + AppConfig.SRVA_SPEC_VERSION;

        final FetchSrvaParametersTask task = new FetchSrvaParametersTask(mAppWorkContext, url) {
            @Override
            protected void onFinishObject(final SrvaParameters result) {
                Utils.LogMessage("Got server SRVA parameters");

                mParameters = result;

                JsonUtils.writeToFileAsync(mAppWorkContext, mParameters, CACHE_FILE);
            }

            @Override
            protected void onError() {
                Utils.LogMessage("SRVA server parameters error: " + getError().getMessage());
            }
        };
        task.start();
    }

    public void loadFallbackMetadata() {
        Utils.LogMessage(this.getClass().getSimpleName(), "Use fallback");

        final String filename = String.format(Locale.getDefault(), "srva_meta_%d.json", AppConfig.SRVA_SPEC_VERSION);

        try (final InputStream inputStream = mAppWorkContext.getContext().getAssets().open(filename)) {

            mParameters = JsonUtils.jsonToObject(inputStream, SrvaParameters.class);

        } catch (final IOException e) {
            Utils.LogMessage(SrvaParameters.class.getSimpleName(), e.getMessage());
        }
    }

    public boolean hasParameters() {
        return mParameters != null;
    }

    public SrvaParameters getParameters() {
        return mParameters;
    }
}
