package fi.riista.mobile.network.shootingTest;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.shootingTest.ShootingTestParticipantDetailed;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;

public abstract class GetShootingTestParticipantDetailedTask extends JsonObjectTask<ShootingTestParticipantDetailed> {
    protected GetShootingTestParticipantDetailedTask(WorkContext context, long participantId) {
        super(context, ShootingTestParticipantDetailed.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/participant/" + participantId + "/attempts");
    }
}
