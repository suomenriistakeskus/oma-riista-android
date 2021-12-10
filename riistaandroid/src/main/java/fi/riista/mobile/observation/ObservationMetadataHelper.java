package fi.riista.mobile.observation;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.models.observation.metadata.ObservationMetadata;
import fi.riista.mobile.models.observation.metadata.ObservationSpecimenMetadata;
import fi.riista.mobile.network.FetchMetadataTask;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.NetworkTask;

import static fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME;
import static java.util.Objects.requireNonNull;

@Singleton
public class ObservationMetadataHelper {

    private static final String CACHE_FILE = "observationmetadata_" + AppConfig.OBSERVATION_SPEC_VERSION + ".json";

    private final WorkContext mAppWorkContext;
    private final ObjectMapper mObjectMapper;

    private ObservationMetadata mMetadata;

    @Inject
    public ObservationMetadataHelper(@NonNull @Named(APPLICATION_WORK_CONTEXT_NAME) final WorkContext appWorkContext,
                                     @NonNull final ObjectMapper objectMapper) {

        mAppWorkContext = requireNonNull(appWorkContext);
        mObjectMapper = requireNonNull(objectMapper);
    }

    public void fetchMetadata() {
        final String metadataUrl = NetworkTask.SCHEME_INTERNAL + CACHE_FILE;

        final FetchMetadataTask task = new FetchMetadataTask(mAppWorkContext, metadataUrl) {
            @Override
            protected void onFinishObject(final ObservationMetadata result) {
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
        final String url = AppConfig.getBaseUrl() + "/gamediary/observation/metadata/" + AppConfig.OBSERVATION_SPEC_VERSION;

        final FetchMetadataTask task = new FetchMetadataTask(mAppWorkContext, url) {
            @Override
            protected void onFinishObject(final ObservationMetadata result) {
                Utils.LogMessage("Got server metadata: " + result.lastModified);

                mMetadata = result;

                JsonUtils.writeToFileAsync(mAppWorkContext, mMetadata, CACHE_FILE);
            }

            @Override
            protected void onError() {
                Utils.LogMessage("Metadata fetch error: " + getError().getMessage());
            }
        };
        task.start();
    }

    public void loadFallbackMetadata() {
        Utils.LogMessage(this.getClass().getSimpleName(), "Use fallback");

        final String filename =
                String.format(Locale.getDefault(), "obs_meta_%d.json", AppConfig.OBSERVATION_SPEC_VERSION);

        try (final InputStream inputStream = mAppWorkContext.getContext().getAssets().open(filename)) {

            mMetadata = mObjectMapper.readValue(inputStream, ObservationMetadata.class);

        } catch (final IOException e) {
            Utils.LogMessage(ObservationMetadataHelper.class.getSimpleName(), e.getMessage());
        }
    }

    public boolean hasMetadata() {
        return mMetadata != null;
    }

    public ObservationSpecimenMetadata getMetadataForSpecies(final int speciesCode) {
        if (mMetadata != null) {
            for (final ObservationSpecimenMetadata specimen : mMetadata.speciesList) {
                if (specimen.gameSpeciesCode == speciesCode) {
                    return specimen;
                }
            }
        }
        return null;
    }
}
