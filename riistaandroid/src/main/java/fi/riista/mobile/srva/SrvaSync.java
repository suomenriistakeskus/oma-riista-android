package fi.riista.mobile.srva;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.List;

import fi.riista.mobile.RiistaApplication;
import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.network.DeleteSrvaEventTask;
import fi.riista.mobile.network.FetchSrvasTask;
import fi.riista.mobile.network.PostSrvaEventTask;
import fi.riista.mobile.network.PostSrvaImageTask;
import fi.riista.mobile.srva.SrvaDatabase.SrvaEventListener;
import fi.riista.mobile.utils.BaseDatabase.SaveListener;
import fi.riista.mobile.utils.Utils;

public class SrvaSync {

    public interface SrvaSyncListener {
        void onFinish();
    }

    public void sync(final SrvaSyncListener listener) {
        //Delete events from server
        deleteEvents(new SrvaSyncListener() {
            @Override
            public void onFinish() {
                //Send modified events to server
                sendEvents(new SrvaSyncListener() {
                    @Override
                    public void onFinish() {
                        //Fetch server events
                        fetchEvents(new SrvaSyncListener() {
                            @Override
                            public void onFinish() {
                                //Send images
                                sendImages(listener);
                            }
                        });
                    }
                });
            }
        });
    }

    private void deleteEvents(final SrvaSyncListener listener) {
        SrvaDatabase.getInstance().loadDeletedRemoteEvents(new SrvaEventListener() {
            @Override
            public void onEvents(List<SrvaEvent> events) {
                deleteEvent(events, listener);
            }
        });
    }

    private void deleteEvent(final List<SrvaEvent> events, final SrvaSyncListener listener) {
        if (events.isEmpty()) {
            listener.onFinish();
            return;
        }

        final SrvaEvent event = events.remove(0);

        DeleteSrvaEventTask task = new DeleteSrvaEventTask(RiistaApplication.getInstance().getWorkContext(), event) {
            @Override
            protected void onFinishText(String text) {
            }

            @Override
            protected void onError() {
                int statusCode = getHttpStatusCode();
                if (statusCode != 204) {
                    Utils.LogMessage("Can't delete event: " + event.remoteId + ", " + statusCode);
                }
            }

            @Override
            protected void onEnd() {
                deleteEvent(events, listener);
            }
        };
        task.start();
    }

    private void fetchEvents(final SrvaSyncListener listener) {
        FetchSrvasTask task = new FetchSrvasTask(RiistaApplication.getInstance().getWorkContext()) {
            @Override
            protected void onFinishObjects(List<SrvaEvent> results) {
                SrvaDatabase.getInstance().handleReceivedEvents(results);
            }

            @Override
            protected void onEnd() {
                listener.onFinish();
            }
        };
        task.start();
    }

    private void sendEvents(final SrvaSyncListener listener) {
        SrvaDatabase.getInstance().loadModifiedEvents(new SrvaEventListener() {
            @Override
            public void onEvents(List<SrvaEvent> events) {
                postEvent(events, listener);
            }
        });
    }

    private void postEvent(final List<SrvaEvent> events, final SrvaSyncListener listener) {
        if (events.isEmpty()) {
            listener.onFinish();
            return;
        }

        final SrvaEvent localEvent = events.remove(0);

        PostSrvaEventTask task = new PostSrvaEventTask(RiistaApplication.getInstance().getWorkContext(), localEvent) {
            private SrvaEvent mResult = null;

            @Override
            protected void onFinishObject(SrvaEvent result) {
                //Successful upload
                mResult = result;
                mResult.copyLocalAttributes(localEvent);
                mResult.modified = false;
            }

            @Override
            protected void onError() {
                Utils.LogMessage("SRVA send error: " + getHttpStatusCode());
            }

            @Override
            protected void onAsyncStreamError(InputStream stream) throws Exception {
                Utils.LogMessage("SRVA send error: " + IOUtils.toString(stream, "UTF-8"));
            }

            @Override
            protected void onEnd() {
                if (mResult != null) {
                    SrvaDatabase.getInstance().saveEvent(mResult, new SaveListener() {
                        @Override
                        public void onSaved(long id) {
                            postEvent(events, listener);
                        }

                        @Override
                        public void onError() {
                            postEvent(events, listener);
                        }
                    });
                } else {
                    postEvent(events, listener);
                }
            }
        };
        task.start();
    }

    private void sendImages(final SrvaSyncListener listener) {
        SrvaDatabase.getInstance().loadEventsWithLocalImages(new SrvaEventListener() {
            @Override
            public void onEvents(List<SrvaEvent> events) {
                sendImage(events, listener);
            }
        });
    }

    private void sendImage(final List<SrvaEvent> events, final SrvaSyncListener listener) {
        if (events.isEmpty()) {
            listener.onFinish();
            return;
        }

        SrvaEvent event = events.get(0);
        if (event.remoteId != null && event.localImages.size() > 0) {
            //This server-backed observation has local images
            final LocalImage image = event.localImages.remove(0);

            if (!event.imageIds.contains(image.serverId)) {
                //This local image has not been sent to the server yet
                PostSrvaImageTask task = new PostSrvaImageTask(RiistaApplication.getInstance().getWorkContext(), event, image) {
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
                        sendImage(events, listener);
                    }
                };
                task.start();
            } else {
                //Try next image or observation
                sendImage(events, listener);
            }
        } else {
            //No images for this observation
            events.remove(0);

            sendImage(events, listener);
        }
    }
}
