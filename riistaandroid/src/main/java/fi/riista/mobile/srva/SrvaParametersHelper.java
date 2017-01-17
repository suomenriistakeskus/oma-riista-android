package fi.riista.mobile.srva;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.srva.SrvaParameters;
import fi.riista.mobile.network.FetchSrvaParametersTask;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.NetworkTask;

public class SrvaParametersHelper {

    private static final String CACHE_FILE = "srvaparameters_" + AppConfig.SRVA_SPEC_VERSION + ".json";

    private static SrvaParametersHelper sInstance;

    public static void init(WorkContext context) {
        sInstance = new SrvaParametersHelper(context);
    }

    public static SrvaParametersHelper getInstance() {
        return sInstance;
    }

    private WorkContext mAppWorkContext;
    private SrvaParameters mParameters;

    private SrvaParametersHelper(WorkContext workContext) {
        mAppWorkContext = workContext;
    }

    public void fetchParameters() {
        FetchSrvaParametersTask task = new FetchSrvaParametersTask(mAppWorkContext, NetworkTask.SCHEME_INTERNAL + CACHE_FILE) {
            @Override
            protected void onFinishObject(SrvaParameters result) {
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
        String url = AppConfig.BASE_URL + "/srva/parameters?srvaEventSpecVersion=" + AppConfig.SRVA_SPEC_VERSION;

        FetchSrvaParametersTask task = new FetchSrvaParametersTask(mAppWorkContext, url) {
            @Override
            protected void onFinishObject(SrvaParameters result) {
                Utils.LogMessage("Got server SRVA parameters");

                mParameters = result;

                JsonUtils.writeToFileAsync(mParameters, CACHE_FILE, null);
            }

            @Override
            protected void onError() {
                Utils.LogMessage("SRVA server parameters error: " + getError().getMessage());
            }
        };
        task.start();
    }

    public boolean hasParameters() {
        return mParameters != null;
    }

    public SrvaParameters getParameters() {
        return mParameters;
    }
}
