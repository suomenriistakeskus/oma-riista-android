package fi.riista.mobile.network;

import fi.riista.mobile.models.observation.metadata.ObservationMetadata;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;

public abstract class FetchMetadataTask extends JsonObjectTask<ObservationMetadata> {
    protected FetchMetadataTask(WorkContext workContext, String url) {
        super(workContext, ObservationMetadata.class);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());
        setBaseUrl(url);
    }
}
