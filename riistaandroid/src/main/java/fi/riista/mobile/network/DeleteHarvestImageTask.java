package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.HarvestDatabase;
import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

/**
 * Delete image related to harvest from server.
 */
public class DeleteHarvestImageTask extends TextTask {

    private final HarvestDatabase mHarvestDatabase;
    private final GameLogImage mImage;

    public DeleteHarvestImageTask(final WorkContext workContext,
                                  final HarvestDatabase harvestDatabase,
                                  final GameLogImage image) {
        super(workContext);

        mHarvestDatabase = harvestDatabase;
        mImage = image;

        setBaseUrl(AppConfig.getBaseUrl() + "/gamediary/image/" + mImage.uuid);
        setHttpMethod(HttpMethod.DELETE);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
    }

    @Override
    protected void onFinishText(final String text) {
        mHarvestDatabase.removeImage(mImage);
    }

    @Override
    protected void onError() {
        // If the image being removed doesn't exist anymore on server, remove it locally too.
        mHarvestDatabase.removeImage(mImage);
    }
}
