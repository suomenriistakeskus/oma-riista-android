package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonListTask;

public abstract class FetchSrvasTask extends JsonListTask<SrvaEvent> {
    protected FetchSrvasTask(WorkContext context) {
        super(context, SrvaEvent.class);

        setCookieStore(GameDatabase.getInstance().getCookieStore());
        setBaseUrl(AppConfig.BASE_URL + "/srva/srvaevents");
        addParameter("srvaEventSpecVersion", "" + AppConfig.SRVA_SPEC_VERSION);
    }
}
