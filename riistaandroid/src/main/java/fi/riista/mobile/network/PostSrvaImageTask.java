package fi.riista.mobile.network;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.utils.ImageUtils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;
import fi.vincit.httpclientandroidlib.HttpEntity;
import fi.vincit.httpclientandroidlib.entity.ContentType;
import fi.vincit.httpclientandroidlib.entity.mime.MultipartEntityBuilder;

public abstract class PostSrvaImageTask extends TextTask {
    protected PostSrvaImageTask(WorkContext workContext, SrvaEvent event, LocalImage image) {
        super(workContext);

        setCookieStore(GameDatabase.getInstance().getCookieStore());
        setHttpMethod(HttpMethod.POST);
        setHttpEntity(createImageEntity(event, image));
        setBaseUrl(AppConfig.BASE_URL + "/srva/image/upload");
    }

    private HttpEntity createImageEntity(SrvaEvent event, LocalImage image) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        builder.addTextBody("srvaEventId", "" + event.remoteId);
        builder.addTextBody("uuid", image.serverId);
        builder.addBinaryBody("file", ImageUtils.getImageFile(getWorkContext().getContext(), "" + image.serverId),
                ContentType.create("image/jpeg"), "");

        return builder.build();
    }
}
