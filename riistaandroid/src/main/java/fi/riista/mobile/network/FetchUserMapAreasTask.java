package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.ClubAreaMap;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonListTask;

public abstract class FetchUserMapAreasTask extends JsonListTask<ClubAreaMap> {
    public FetchUserMapAreasTask(WorkContext context) {
        super(context, ClubAreaMap.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/area/club");
    }
}
