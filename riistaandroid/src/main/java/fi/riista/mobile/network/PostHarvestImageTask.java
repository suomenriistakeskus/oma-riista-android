package fi.riista.mobile.network;

import androidx.annotation.NonNull;

import java.io.File;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.client.methods.HttpRequestBase;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.HarvestDatabase;
import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.riista.mobile.utils.ImageUtils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

import static java.util.Objects.requireNonNull;

/**
 * Post harvest image to server.
 */
public class PostHarvestImageTask extends TextTask {

    private static final int NETWORK_TIMEOUT = 20 * 1000;

    private final HarvestDatabase mHarvestDatabase;

    private final String mHarvestId;
    private final GameLogImage mImage;

    public PostHarvestImageTask(@NonNull final WorkContext workContext,
                                @NonNull final HarvestDatabase harvestDatabase,
                                final int harvestId,
                                @NonNull final GameLogImage image) {
        super(workContext);

        mHarvestDatabase = harvestDatabase;

        mHarvestId = String.valueOf(harvestId);
        mImage = requireNonNull(image);

        setHttpMethod(HttpMethod.POST);
        setBaseUrl(AppConfig.getBaseUrl() + "/gamediary/image/uploadforharvest");

        setHttpEntity(createMultipartEntity());
        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());

        setTimeout(NETWORK_TIMEOUT);
    }

    @Override
    protected void onAsyncRequest(final HttpRequestBase request) {
        final File imgFile = getImageFile();

        if (!imgFile.exists()) {
            throw new IllegalStateException("Harvest image file does not exist: " + imgFile.getAbsolutePath());
        }
    }

    @Override
    protected void onError() {
        if (getHttpStatusCode() == 204) {
            mHarvestDatabase.markImageAsSent(mImage);
        }
    }

    @Override
    protected void onFinishText(final String text) {
        mHarvestDatabase.markImageAsSent(mImage);
    }

    private HttpEntity createMultipartEntity() {
        final File imageFile = getImageFile();

        return MultipartEntityBuilder.create()
                .addTextBody("harvestId", mHarvestId)
                .addTextBody("uuid", mImage.uuid)
                .addBinaryBody("file", imageFile, ContentType.create("image/jpeg"), "")
                .build();
    }

    private File getImageFile() {
        return ImageUtils.getImageFile(getWorkContext().getContext(), mImage.uuid);
    }
}
