package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.network.json.MetsahallitusPermitResponse;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonListTask;

public abstract class FetchMetsahallitusPermitsTask extends JsonListTask<MetsahallitusPermitResponse> {

    protected FetchMetsahallitusPermitsTask(final WorkContext context) {
        super(context, MetsahallitusPermitResponse.class);

        setBaseUrl(AppConfig.getBaseUrl() + "/permit/mh");

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
    }
}
