package fi.riista.mobile.network;

import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.metadata.ObservationMetadata;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.JsonObjectTask;

public abstract class FetchMetadataTask extends JsonObjectTask<ObservationMetadata> {
    protected FetchMetadataTask(WorkContext workContext, String url) {
        super(workContext, ObservationMetadata.class);

        setCookieStore(GameDatabase.getInstance().getCookieStore());
        setBaseUrl(url);
    }
}
