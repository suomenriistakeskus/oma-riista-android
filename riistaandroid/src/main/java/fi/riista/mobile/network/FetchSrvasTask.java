package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonListTask;

public abstract class FetchSrvasTask extends JsonListTask<SrvaEvent> {
    protected FetchSrvasTask(WorkContext context) {
        super(context, SrvaEvent.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/srva/srvaevents");
        addParameter("srvaEventSpecVersion", "" + AppConfig.SRVA_SPEC_VERSION);
    }
}
