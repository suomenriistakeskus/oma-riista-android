package fi.riista.mobile.network;

import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.srva.SrvaParameters;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;

public abstract class FetchSrvaParametersTask extends JsonObjectTask<SrvaParameters> {
    protected FetchSrvaParametersTask(WorkContext workContext, String url) {
        super(workContext, SrvaParameters.class);

        setCookieStore(GameDatabase.getInstance().getCookieStore());
        setBaseUrl(url);
    }
}
