package fi.riista.mobile.sync;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.models.observation.GameObservation;
import fi.riista.mobile.network.DeleteObservationTask;
import fi.riista.mobile.network.FetchObservationsTask;
import fi.riista.mobile.network.PostObservationImageTask;
import fi.riista.mobile.network.PostObservationTask;
import fi.riista.mobile.observation.ObservationDatabase;
import fi.riista.mobile.utils.BaseDatabase.DeleteListener;
import fi.riista.mobile.utils.BaseDatabase.SaveListener;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;

import static fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME;
import static java.util.Objects.requireNonNull;

@Singleton
public class ObservationSync {

    public interface ObservationSyncListener {
        void onFinish();
    }

    private final WorkContext mSyncWorkContext;
    private final ObservationDatabase mObservationDatabase;

    @Inject
    public ObservationSync(@NonNull @Named(APPLICATION_WORK_CONTEXT_NAME) final WorkContext appWorkContext,
                           @NonNull final ObservationDatabase observationDatabase) {

        mSyncWorkContext = requireNonNull(appWorkContext);
        mObservationDatabase = requireNonNull(observationDatabase);
    }

    public void sync(@Nullable final List<Integer> serverHuntingYears,
                     @NonNull final ObservationSyncListener listener) {

        requireNonNull(listener);

        final ArrayList<Integer> huntingYears = serverHuntingYears != null
                ? new ArrayList<>(serverHuntingYears)
                : new ArrayList<>();

        // Add local observation hunting years to the hunting years received from the server
        loadLocalObservationYears(huntingYears, () -> {
            // Delete any marked observations from the server
            deleteObservations(() -> {
                // Then send modified to server
                sendObservations(() -> {

                    // Then fetch new ones

                    final HashSet<Integer> failedHuntingYears = new HashSet<>();
                    final HashMap<Long, GameObservation> fetchedObservations = new HashMap<>();

                    fetchObservations(huntingYears, failedHuntingYears, fetchedObservations, () -> {
                        // Then send images
                        sendImages(listener);
                    });
                });
            });
        });
    }

    private void loadLocalObservationYears(final List<Integer> allHuntingYears, final ObservationSyncListener listener) {
        mObservationDatabase.loadObservationYears(localYears -> {
            for (final Integer localYear : localYears) {
                if (!allHuntingYears.contains(localYear)) {
                    allHuntingYears.add(localYear);
                }
            }

            listener.onFinish();
        });
    }

    private void deleteObservations(final ObservationSyncListener listener) {
        mObservationDatabase.loadDeletedRemoteObservations(observations -> deleteObservation(observations, listener));
    }

    private void deleteObservation(final List<GameObservation> observations, final ObservationSyncListener listener) {
        if (observations.isEmpty()) {
            listener.onFinish();
            return;
        }

        final GameObservation observation = observations.remove(0);

        final DeleteObservationTask task = new DeleteObservationTask(mSyncWorkContext, observation) {
            @Override
            protected void onFinishText(final String noContentResponse) {
            }

            @Override
            protected void onError() {
                final int statusCode = getHttpStatusCode();

                if (statusCode != 204) {
                    Utils.LogMessage("Can't delete observation: " + observation.remoteId + ", " + statusCode);
                }
            }

            @Override
            protected void onEnd() {
                deleteObservation(observations, listener);
            }
        };
        task.start();
    }

    private void sendObservations(final ObservationSyncListener listener) {
        mObservationDatabase.loadModifiedObservations(observations -> postObservation(observations, listener));
    }

    private void postObservation(final List<GameObservation> observations, final ObservationSyncListener listener) {
        if (observations.isEmpty()) {
            listener.onFinish();
            return;
        }

        final GameObservation localObservation = observations.remove(0);

        final PostObservationTask task = new PostObservationTask(mSyncWorkContext, localObservation) {
            private GameObservation mResult = null;

            @Override
            protected void onFinishObject(final GameObservation result) {
                // Successful upload
                mResult = result;
                mResult.copyLocalAttributes(localObservation);
                mResult.modified = false;
            }

            @Override
            protected void onError() {
                Utils.LogMessage("Observation POST error: " + getHttpStatusCode());
            }

            @Override
            protected void onAsyncStreamError(final InputStream stream) throws Exception {
                Utils.LogMessage("Observation POST error: " + IOUtils.toString(stream, StandardCharsets.UTF_8));
            }

            @Override
            protected void onEnd() {
                if (mResult != null) {
                    mObservationDatabase.saveObservation(mResult, new SaveListener() {
                        @Override
                        public void onSaved(final long id) {
                            postObservation(observations, listener);
                        }

                        @Override
                        public void onError() {
                            postObservation(observations, listener);
                        }
                    });
                } else {
                    postObservation(observations, listener);
                }
            }
        };
        task.start();
    }

    private void fetchObservations(final List<Integer> remainingHuntingYears,
                                   final Set<Integer> failedHuntingYears,
                                   final Map<Long, GameObservation> fetchedObservationsByRemoteId,
                                   final ObservationSyncListener listener) {

        if (remainingHuntingYears.isEmpty()) {
            checkDeletedObservations(failedHuntingYears, fetchedObservationsByRemoteId, listener);
            return;
        }

        final int huntingYear = remainingHuntingYears.remove(0);

        final FetchObservationsTask task = new FetchObservationsTask(mSyncWorkContext, huntingYear) {
            @Override
            protected void onFinishObjects(final List<GameObservation> results) {
                for (final GameObservation observation : results) {
                    fetchedObservationsByRemoteId.put(observation.remoteId, observation);
                }

                mObservationDatabase.handleReceivedObservations(results);
            }

            @Override
            protected void onError() {
                failedHuntingYears.add(huntingYear);
            }

            @Override
            protected void onEnd() {
                fetchObservations(remainingHuntingYears, failedHuntingYears, fetchedObservationsByRemoteId, listener);
            }
        };
        task.start();
    }

    private void checkDeletedObservations(final Set<Integer> failedHuntingYears,
                                          final Map<Long, GameObservation> fetchedObservationsByRemoteId,
                                          final ObservationSyncListener listener) {

        mObservationDatabase.loadAllObservations(observations -> {
            final ArrayList<GameObservation> deleted = new ArrayList<>();

            // Go through all our local observations.
            for (final GameObservation observation : observations) {
                final Long remoteId = observation.remoteId;

                if (remoteId != null && !fetchedObservationsByRemoteId.containsKey(remoteId)) {

                    // This observation is in the server but we did not receive it this time.
                    // It might have been deleted from the server.
                    final int huntingYear =
                            DateTimeUtils.getHuntingYearForCalendar(observation.toDateTime().toCalendar(null));

                    if (!failedHuntingYears.contains(huntingYear)) {
                        // Year sync was successful, so it is missing because it was deleted.
                        deleted.add(observation);
                    }
                }
            }

            forceDelete(deleted, listener);
        });
    }

    private void forceDelete(final List<GameObservation> observations, final ObservationSyncListener listener) {
        if (observations.isEmpty()) {
            listener.onFinish();
            return;
        }

        final GameObservation observation = observations.remove(0);

        Utils.LogMessage("Server deleted observation: " + observation.remoteId);

        mObservationDatabase.deleteObservation(observation, true, new DeleteListener() {
            @Override
            public void onError() {
                forceDelete(observations, listener);
            }

            @Override
            public void onDelete() {
                forceDelete(observations, listener);
            }
        });
    }

    private void sendImages(final ObservationSyncListener listener) {
        mObservationDatabase.loadObservationsWithLocalImages(observations -> sendImage(observations, listener));
    }

    private void sendImage(final List<GameObservation> observations, final ObservationSyncListener listener) {
        if (observations.isEmpty()) {
            listener.onFinish();
            return;
        }

        final GameObservation observation = observations.get(0);

        if (observation.remoteId != null && observation.localImages.size() > 0) {
            // This server-backed observation has local images
            final LocalImage image = observation.localImages.remove(0);

            if (!observation.imageIds.contains(image.serverId)) {
                // This local image has not been sent to the server yet
                final PostObservationImageTask task = new PostObservationImageTask(mSyncWorkContext, observation, image) {
                    @Override
                    protected void onFinishText(final String text) {
                        Utils.LogMessage("Image sent: " + image.localPath);
                    }

                    @Override
                    protected void onError() {
                        Utils.LogMessage("Image sending failed: " + getHttpStatusCode());
                    }

                    @Override
                    protected void onEnd() {
                        sendImage(observations, listener);
                    }
                };
                task.start();
            } else {
                // Try next image or observation
                sendImage(observations, listener);
            }
        } else {
            // No images for this observation
            observations.remove(0);

            sendImage(observations, listener);
        }
    }
}
