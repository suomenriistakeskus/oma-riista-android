package fi.riista.mobile.network;

import androidx.annotation.NonNull;

import java.io.File;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.client.methods.HttpRequestBase;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.models.observation.GameObservation;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.riista.mobile.utils.ImageUtils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

import static java.util.Objects.requireNonNull;

public abstract class PostObservationImageTask extends TextTask {

    private static final int NETWORK_TIMEOUT = 20 * 1000;

    private final String mObservationId;
    private final String mImageUuid;

    protected PostObservationImageTask(@NonNull final WorkContext workContext,
                                       @NonNull final GameObservation observation,
                                       @NonNull final LocalImage image) {
        super(workContext);

        requireNonNull(observation);
        mObservationId = observation.remoteId.toString();

        requireNonNull(image);
        mImageUuid = requireNonNull(image.serverId);

        setHttpMethod(HttpMethod.POST);
        setBaseUrl(AppConfig.getBaseUrl() + "/gamediary/image/uploadforobservation");

        setHttpEntity(createMultipartEntity());
        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());

        setTimeout(NETWORK_TIMEOUT);
    }

    @Override
    protected void onAsyncRequest(final HttpRequestBase request) {
        final File imgFile = getImageFile();

        if (!imgFile.exists()) {
            throw new IllegalStateException("Observation image file does not exist: " + imgFile.getAbsolutePath());
        }
    }

    private HttpEntity createMultipartEntity() {
        final File imageFile = getImageFile();

        return MultipartEntityBuilder.create()
                .addTextBody("observationId", mObservationId)
                .addTextBody("uuid", mImageUuid)
                .addBinaryBody("file", imageFile, ContentType.create("image/jpeg"), "")
                .build();
    }

    private File getImageFile() {
        return ImageUtils.getImageFile(getWorkContext().getContext(), mImageUuid);
    }
}
