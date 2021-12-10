package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.announcement.Announcement;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonListTask;

public abstract class ListAnnouncementsTask extends JsonListTask<Announcement> {
    protected ListAnnouncementsTask(WorkContext context) {
        super(context, Announcement.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/announcement/list");
        // Limit results with since parameter addParameter("since", "" + "DATETIME");
    }
}
