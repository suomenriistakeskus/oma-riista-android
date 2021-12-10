package fi.riista.mobile.network.shootingTest;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.shootingTest.ShootingTestParticipant;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonListTask;

public abstract class ListShootingTestParticipantsTask extends JsonListTask<ShootingTestParticipant> {
    protected ListShootingTestParticipantsTask(WorkContext context, long eventId) {
        super(context, ShootingTestParticipant.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/event/" + eventId + "/participants");

        addParameter("unfinishedOnly", String.valueOf(false));
    }
}
