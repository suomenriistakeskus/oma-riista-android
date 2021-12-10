package fi.riista.mobile.network;

import fi.riista.mobile.models.srva.SrvaParameters;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;

public abstract class FetchSrvaParametersTask extends JsonObjectTask<SrvaParameters> {
    protected FetchSrvaParametersTask(WorkContext workContext, String url) {
        super(workContext, SrvaParameters.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(url);
    }
}
