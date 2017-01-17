package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.announcement.Announcement;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonListTask;

public abstract class ListAnnouncementsTask extends JsonListTask<Announcement> {
    protected ListAnnouncementsTask(WorkContext context) {
        super(context, Announcement.class);

        setCookieStore(GameDatabase.getInstance().getCookieStore());
        setBaseUrl(AppConfig.BASE_URL + "/announcement/list");
        // Limit results with since parameter addParameter("since", "" + "DATETIME");
    }
}
