package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.GameObservation;
import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.utils.ImageUtils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;
import fi.vincit.httpclientandroidlib.HttpEntity;
import fi.vincit.httpclientandroidlib.entity.ContentType;
import fi.vincit.httpclientandroidlib.entity.mime.MultipartEntityBuilder;

public abstract class PostObservationImageTask extends TextTask {
    protected PostObservationImageTask(WorkContext workContext, GameObservation observation, LocalImage image) {
        super(workContext);

        setCookieStore(GameDatabase.getInstance().getCookieStore());
        setHttpMethod(HttpMethod.POST);
        setHttpEntity(createImageEntity(observation, image));
        setBaseUrl(AppConfig.BASE_URL + "/gamediary/image/uploadforobservation");
    }

    private HttpEntity createImageEntity(GameObservation observation, LocalImage image) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        builder.addTextBody("observationId", "" + observation.remoteId);
        builder.addTextBody("uuid", image.serverId);
        builder.addBinaryBody("file", ImageUtils.getImageFile(getWorkContext().getContext(), "" + image.serverId),
                ContentType.create("image/jpeg"), "");

        return builder.build();
    }
}
