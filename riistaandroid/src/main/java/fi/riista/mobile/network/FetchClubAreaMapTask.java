package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.ClubAreaMap;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;


public abstract class FetchClubAreaMapTask extends JsonObjectTask<ClubAreaMap> {
    public FetchClubAreaMapTask(WorkContext context, String externalId) {
        super(context, ClubAreaMap.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/area/code/" + externalId);
    }
}
