package fi.riista.mobile.network.shootingTest;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.shootingTest.ShootingTestParticipant;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;

public abstract class GetShootingTestParticipantSummaryTask extends JsonObjectTask<ShootingTestParticipant> {
    protected GetShootingTestParticipantSummaryTask(WorkContext context, long participantId) {
        super(context, ShootingTestParticipant.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(AppConfig.getBaseUrl() + "/shootingtest/participant/" + participantId);
    }
}
