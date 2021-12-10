package fi.riista.mobile.sync;

import androidx.annotation.NonNull;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.List;

import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.network.DeleteSrvaEventTask;
import fi.riista.mobile.network.FetchSrvasTask;
import fi.riista.mobile.network.PostSrvaEventTask;
import fi.riista.mobile.network.PostSrvaImageTask;
import fi.riista.mobile.srva.SrvaDatabase;
import fi.riista.mobile.utils.BaseDatabase.SaveListener;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;

import static java.util.Objects.requireNonNull;

public class SrvaSync {

    public interface SrvaSyncListener {
        void onFinish();
    }

    private final WorkContext syncWorkContext;

    public SrvaSync(@NonNull final WorkContext syncWorkContext) {
        this.syncWorkContext = requireNonNull(syncWorkContext);
    }

    public void sync(final SrvaSyncListener listener) {
        // Delete events from server
        deleteEvents(() -> {
            // Send modified events to server
            sendEvents(() -> {
                // Fetch server events
                fetchEvents(() -> {
                    // Send images
                    sendImages(listener);
                });
            });
        });
    }

    private void deleteEvents(final SrvaSyncListener listener) {
        SrvaDatabase.getInstance().loadDeletedRemoteEvents(events -> deleteEvent(events, listener));
    }

    private void deleteEvent(final List<SrvaEvent> events, final SrvaSyncListener listener) {
        if (events.isEmpty()) {
            listener.onFinish();
            return;
        }

        final SrvaEvent event = events.remove(0);

        final DeleteSrvaEventTask task = new DeleteSrvaEventTask(syncWorkContext, event) {
            @Override
            protected void onFinishText(final String text) {
            }

            @Override
            protected void onError() {
                final int statusCode = getHttpStatusCode();

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
        final FetchSrvasTask task = new FetchSrvasTask(syncWorkContext) {
            @Override
            protected void onFinishObjects(final List<SrvaEvent> results) {
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
        SrvaDatabase.getInstance().loadModifiedEvents(events -> postEvent(events, listener));
    }

    private void postEvent(final List<SrvaEvent> events, final SrvaSyncListener listener) {
        if (events.isEmpty()) {
            listener.onFinish();
            return;
        }

        final SrvaEvent localEvent = events.remove(0);

        final PostSrvaEventTask task = new PostSrvaEventTask(syncWorkContext, localEvent) {
            private SrvaEvent mResult = null;

            @Override
            protected void onFinishObject(final SrvaEvent result) {
                // Successful upload
                mResult = result;
                mResult.copyLocalAttributes(localEvent);
                mResult.modified = false;
            }

            @Override
            protected void onError() {
                Utils.LogMessage("SRVA send error: " + getHttpStatusCode());
            }

            @Override
            protected void onAsyncStreamError(final InputStream stream) throws Exception {
                Utils.LogMessage("SRVA send error: " + IOUtils.toString(stream, "UTF-8"));
            }

            @Override
            protected void onEnd() {
                if (mResult != null) {
                    SrvaDatabase.getInstance().saveEvent(mResult, new SaveListener() {
                        @Override
                        public void onSaved(final long id) {
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
        SrvaDatabase.getInstance().loadEventsWithLocalImages(events -> sendImage(events, listener));
    }

    private void sendImage(final List<SrvaEvent> events, final SrvaSyncListener listener) {
        if (events.isEmpty()) {
            listener.onFinish();
            return;
        }

        final SrvaEvent event = events.get(0);

        if (event.remoteId != null && event.localImages.size() > 0) {
            // This server-backed observation has local images
            final LocalImage image = event.localImages.remove(0);

            if (!event.imageIds.contains(image.serverId)) {
                // This local image has not been sent to the server yet
                final PostSrvaImageTask task = new PostSrvaImageTask(syncWorkContext, event, image) {
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
                        sendImage(events, listener);
                    }
                };
                task.start();
            } else {
                // Try next image or observation
                sendImage(events, listener);
            }
        } else {
            // No images for this observation
            events.remove(0);

            sendImage(events, listener);
        }
    }
}
