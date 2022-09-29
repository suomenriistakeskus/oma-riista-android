package fi.riista.mobile.sync;

import androidx.annotation.NonNull;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.ArrayList;
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
        SrvaDatabase.getInstance().loadEventsWithLocalImages(events -> sendImages(events, listener));
    }

    private void sendImages(final List<SrvaEvent> remainingEvents, final SrvaSyncListener listener) {
        if (remainingEvents.isEmpty()) {
            listener.onFinish();
            return;
        }

        final SrvaEvent currentEvent = remainingEvents.remove(0);
        final List<LocalImage> remainingEventImages = new ArrayList<>(currentEvent.localImages);
        sendImages(remainingEvents, currentEvent, remainingEventImages, listener);
    }

    private void sendImages(final List<SrvaEvent> remainingEvents,
                            final SrvaEvent currentEvent,
                            final List<LocalImage> remainingEventImages,
                            final SrvaSyncListener listener) {
        if (remainingEventImages.isEmpty()) {
            sendImages(remainingEvents, listener);
            return;
        }

        if (currentEvent.remoteId == null) {
            // event has not been sent yet, cannot send images for it
            // -> continue sending images from next available event
            sendImages(remainingEvents, listener);
            return;
        }

        final LocalImage currentImage = remainingEventImages.remove(0);
        if (currentEvent.imageIds.contains(currentImage.serverId)) {
            // image has already been sent, try next image
            sendImages(remainingEvents, currentEvent, remainingEventImages, listener);
            return;
        }

        // This local image has not been sent to the server yet
        final PostSrvaImageTask task = new PostSrvaImageTask(syncWorkContext, currentEvent, currentImage) {
            boolean imageUploaded = false;

            @Override
            protected void onFinishText(final String text) {
                imageUploaded = true;
                Utils.LogMessage("Image sent: " + currentImage.localPath);
            }

            @Override
            protected void onError() {
                Utils.LogMessage("Image sending failed: " + getHttpStatusCode() +
                        ", uuid = " + currentImage.serverId);
            }

            @Override
            protected void onEnd() {
                if (!imageUploaded) {
                    sendImages(remainingEvents, currentEvent, remainingEventImages, listener);
                    return;
                }

                // update event so that we know afterwards that image has been sent
                // - insert as first item as the latest added image seems to be the first when images
                //   are received from backend
                currentEvent.imageIds.add(0, currentImage.serverId);

                SrvaDatabase.getInstance().saveEvent(currentEvent, new SaveListener() {
                    @Override
                    public void onSaved(final long id) {
                        sendImages(remainingEvents, currentEvent, remainingEventImages, listener);
                    }

                    @Override
                    public void onError() {
                        sendImages(remainingEvents, currentEvent, remainingEventImages, listener);
                    }
                });
            }
        };
        task.start();
    }
}
