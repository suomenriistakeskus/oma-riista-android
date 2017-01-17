package fi.riista.mobile.observation;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.metadata.ObservationMetadata;
import fi.riista.mobile.models.metadata.ObservationSpecimenMetadata;
import fi.riista.mobile.network.FetchMetadataTask;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.NetworkTask;

public class ObservationMetadataHelper {

    private static final String CACHE_FILE = "observationmetadata_" + AppConfig.OBSERVATION_SPEC_VERSION + ".json";

    private static ObservationMetadataHelper sInstance;

    public static void init(WorkContext context) {
        sInstance = new ObservationMetadataHelper(context);
    }

    public static ObservationMetadataHelper getInstance() {
        return sInstance;
    }

    private WorkContext mAppWorkContext;
    private ObservationMetadata mMetadata;

    private ObservationMetadataHelper(WorkContext workContext) {
        mAppWorkContext = workContext;
    }

    public void fetchMetadata() {
        FetchMetadataTask task = new FetchMetadataTask(mAppWorkContext, NetworkTask.SCHEME_INTERNAL + CACHE_FILE) {
            @Override
            protected void onFinishObject(ObservationMetadata result) {
                Utils.LogMessage("Got cached metadata: " + result.lastModified);
                mMetadata = result;
            }

            @Override
            protected void onError() {
                Utils.LogMessage("Metadata cache error: " + getError().getMessage());
            }

            @Override
            protected void onEnd() {
                fetchServerMetadata();
            }
        };
        task.start();
    }

    private void fetchServerMetadata() {
        String url = AppConfig.BASE_URL + "/gamediary/observation/metadata/" + AppConfig.OBSERVATION_SPEC_VERSION;

        FetchMetadataTask task = new FetchMetadataTask(mAppWorkContext, url) {
            @Override
            protected void onFinishObject(ObservationMetadata result) {
                Utils.LogMessage("Got server metadata: " + result.lastModified);

                mMetadata = result;

                JsonUtils.writeToFileAsync(mMetadata, CACHE_FILE, null);
            }

            @Override
            protected void onError() {
                Utils.LogMessage("Metadata fetch error: " + getError().getMessage());
            }
        };
        task.start();
    }

    public boolean hasMetadata() {
        return mMetadata != null;
    }

    public ObservationSpecimenMetadata getMetadataForSpecies(int speciesCode) {
        if (mMetadata != null) {
            for (ObservationSpecimenMetadata specimen : mMetadata.speciesList) {
                if (specimen.gameSpeciesCode == speciesCode) {
                    return specimen;
                }
            }
        }
        return null;
    }
}
