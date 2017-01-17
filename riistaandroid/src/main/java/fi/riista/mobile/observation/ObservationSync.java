package fi.riista.mobile.observation;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import fi.riista.mobile.RiistaApplication;
import fi.riista.mobile.models.GameObservation;
import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.network.DeleteObservationTask;
import fi.riista.mobile.network.FetchObservationsTask;
import fi.riista.mobile.network.PostObservationImageTask;
import fi.riista.mobile.network.PostObservationTask;
import fi.riista.mobile.observation.ObservationDatabase.ObservationYearsListener;
import fi.riista.mobile.observation.ObservationDatabase.ObservationsListener;
import fi.riista.mobile.utils.BaseDatabase.DeleteListener;
import fi.riista.mobile.utils.BaseDatabase.SaveListener;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.Utils;

public class ObservationSync {

    public interface ObservationSyncListener {
        void onFinish();
    }

    private HashMap<Long, GameObservation> mReceived = new HashMap<>();
    private HashSet<Integer> mFailedYears = new HashSet<>();

    public void sync(List<Integer> serverYears, final ObservationSyncListener listener) {
        final List<Integer> years = new ArrayList<>();
        if (serverYears != null) {
            years.addAll(serverYears);
        }

        //Add local observation years to the years received from the server
        loadLocalObservationYears(years, new ObservationSyncListener() {
            @Override
            public void onFinish() {
                //Delete any marked observations from the server
                deleteObservations(new ObservationSyncListener() {
                    @Override
                    public void onFinish() {
                        //Then send modified to server
                        sendObservations(new ObservationSyncListener() {
                            @Override
                            public void onFinish() {
                                //Then fetch new ones
                                fetchYears(years, new ObservationSyncListener() {
                                    @Override
                                    public void onFinish() {
                                        //Then send images
                                        sendImages(listener);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void loadLocalObservationYears(final List<Integer> allYears, final ObservationSyncListener listener) {
        ObservationDatabase.getInstance().loadObservationYears(new ObservationYearsListener() {
            @Override
            public void onYears(List<Integer> localYears) {
                for (Integer year : localYears) {
                    if (!allYears.contains(year)) {
                        allYears.add(year);
                    }
                }
                listener.onFinish();
            }
        });
    }

    private void deleteObservations(final ObservationSyncListener listener) {
        ObservationDatabase.getInstance().loadDeletedRemoteObservations(new ObservationsListener() {
            @Override
            public void onObservations(List<GameObservation> observations) {
                deleteObservation(observations, listener);
            }
        });
    }

    private void deleteObservation(final List<GameObservation> observations, final ObservationSyncListener listener) {
        if (observations.isEmpty()) {
            listener.onFinish();
            return;
        }

        final GameObservation observation = observations.remove(0);

        DeleteObservationTask task = new DeleteObservationTask(RiistaApplication.getInstance().getWorkContext(), observation) {
            @Override
            protected void onFinishText(String text) {
            }

            @Override
            protected void onError() {
                int statusCode = getHttpStatusCode();
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
        ObservationDatabase.getInstance().loadModifiedObservations(new ObservationsListener() {
            @Override
            public void onObservations(List<GameObservation> observations) {
                postObservation(observations, listener);
            }
        });
    }

    private void postObservation(final List<GameObservation> observations, final ObservationSyncListener listener) {
        if (observations.isEmpty()) {
            listener.onFinish();
            return;
        }

        final GameObservation localObservation = observations.remove(0);

        PostObservationTask task = new PostObservationTask(RiistaApplication.getInstance().getWorkContext(), localObservation) {
            private GameObservation mResult = null;

            @Override
            protected void onFinishObject(GameObservation result) {
                //Successful upload
                mResult = result;
                mResult.copyLocalAttributes(localObservation);
                mResult.modified = false;
            }

            @Override
            protected void onError() {
                Utils.LogMessage("Observation POST error: " + getHttpStatusCode());
            }

            @Override
            protected void onAsyncStreamError(InputStream stream) throws Exception {
                Utils.LogMessage("Observation POST error: " + IOUtils.toString(stream, "UTF-8"));
            }

            @Override
            protected void onEnd() {
                if (mResult != null) {
                    ObservationDatabase.getInstance().saveObservation(mResult, new SaveListener() {
                        @Override
                        public void onSaved(long id) {
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

    private void fetchYears(final List<Integer> years, final ObservationSyncListener listener) {
        if (years.isEmpty()) {
            checkDeletedObservations(listener);
            return;
        }

        final int year = years.remove(0);

        FetchObservationsTask task = new FetchObservationsTask(RiistaApplication.getInstance().getWorkContext(), year) {
            @Override
            protected void onFinishObjects(List<GameObservation> results) {
                for (GameObservation observation : results) {
                    mReceived.put(observation.remoteId, observation);
                }
                ObservationDatabase.getInstance().handleReceivedObservations(results);
            }

            @Override
            protected void onError() {
                mFailedYears.add(year);
            }

            @Override
            protected void onEnd() {
                fetchYears(years, listener);
            }
        };
        task.start();
    }

    private void checkDeletedObservations(final ObservationSyncListener listener) {
        ObservationDatabase.getInstance().loadAllObservations(new ObservationsListener() {
            @Override
            public void onObservations(List<GameObservation> observations) {
                ArrayList<GameObservation> deleted = new ArrayList<>();

                //Go through all our local observations.
                for (GameObservation observation : observations) {
                    if (observation.remoteId != null && !mReceived.containsKey(observation.remoteId)) {
                        //This observation is in the server but we did not receive it this time.
                        //It might have been deleted from the server.
                        int gameYear = DateTimeUtils.getSeasonStartYearFromDate(observation.toDateTime().toCalendar(null));
                        if (!mFailedYears.contains(gameYear)) {
                            //Year sync was successful, so it is missing because it was deleted.
                            deleted.add(observation);
                        }
                    }
                }
                forceDelete(deleted, listener);
            }
        });
    }

    private void forceDelete(final List<GameObservation> observations, final ObservationSyncListener listener) {
        if (observations.isEmpty()) {
            listener.onFinish();
            return;
        }

        final GameObservation observation = observations.remove(0);

        Utils.LogMessage("Server deleted observation: " + observation.remoteId);

        ObservationDatabase.getInstance().deleteObservation(observation, true, new DeleteListener() {
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
        ObservationDatabase.getInstance().loadObservationsWithLocalImages(new ObservationsListener() {
            @Override
            public void onObservations(List<GameObservation> observations) {
                sendImage(observations, listener);
            }
        });
    }

    private void sendImage(final List<GameObservation> observations, final ObservationSyncListener listener) {
        if (observations.isEmpty()) {
            listener.onFinish();
            return;
        }

        GameObservation observation = observations.get(0);
        if (observation.remoteId != null && observation.localImages.size() > 0) {
            //This server-backed observation has local images
            final LocalImage image = observation.localImages.remove(0);

            if (!observation.imageIds.contains(image.serverId)) {
                //This local image has not been sent to the server yet
                PostObservationImageTask task = new PostObservationImageTask(RiistaApplication.getInstance().getWorkContext(), observation, image) {
                    @Override
                    protected void onFinishText(String text) {
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
                //Try next image or observation
                sendImage(observations, listener);
            }
        } else {
            //No images for this observation
            observations.remove(0);

            sendImage(observations, listener);
        }
    }
}
