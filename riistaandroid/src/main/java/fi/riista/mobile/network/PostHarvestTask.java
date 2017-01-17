package fi.riista.mobile.network;

import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.DiaryDataSource;
import fi.riista.mobile.database.DiaryHelper;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.database.LogImageSendCompletion;
import fi.riista.mobile.database.LogImageUpdate;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.Specimen;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

/**
 * Send harvest entry to server
 */
public class PostHarvestTask extends TextTask {

    private GameHarvest mEvent = null;

    protected PostHarvestTask(WorkContext workContext, final GameHarvest event, boolean isNew) {
        super(workContext);
        setCookieStore(GameDatabase.getInstance().getCookieStore());
        mEvent = event;

        if (event.mPendingOperation == DiaryHelper.UpdateType.DELETE) {
            setBaseUrl(AppConfig.BASE_URL + "/gamediary/harvest/" + mEvent.mId);
            setHttpMethod(HttpMethod.DELETE);
        } else if (isNew) {
            setBaseUrl(AppConfig.BASE_URL + "/gamediary/harvest");
            setHttpMethod(HttpMethod.POST);
            Map<String, Object> object = getRequestData(event, false);
            this.setJsonEntity(object);
        } else {
            setBaseUrl(AppConfig.BASE_URL + "/gamediary/harvest/" + mEvent.mId);
            setHttpMethod(HttpMethod.PUT);
            Map<String, Object> object = getRequestData(event, true);
            this.setJsonEntity(object);
        }
    }

    private Map<String, Object> getRequestData(GameHarvest event, boolean edit) {
        Map<String, Object> object = new HashMap<>();
        object.put("harvestSpecVersion", AppConfig.HARVEST_SPEC_VERSION);
        object.put("type", event.mType);
        DateFormat df = new SimpleDateFormat(DiaryDataSource.ISO_8601);
        object.put("pointOfTime", df.format(mEvent.mTime.getTime()));
        object.put("gameSpeciesCode", event.mSpeciesID);
        object.put("amount", event.mAmount);
        if (!edit && event.mMobileClientRefId != 0) {
            object.put("mobileClientRefId", event.mMobileClientRefId);
        }

        Map<String, Object> geoLocation = new HashMap<>();
        geoLocation.put("source", mEvent.mLocationSource);
        geoLocation.put("latitude", mEvent.mCoordinates.first);
        geoLocation.put("longitude", mEvent.mCoordinates.second);
        geoLocation.put("accuracy", mEvent.mAccuracy);
        if (mEvent.mHasAltitude) {
            geoLocation.put("altitude", mEvent.mAltitude);
        }
        object.put("geoLocation", geoLocation);

        if (event.mSpecimen != null && event.mAmount < GameHarvest.SPECIMEN_DETAILS_MAX) {
            object.put("specimens", getSpecimenData(event));
        }

        if (event.mPermitNumber != null) {
            object.put("permitNumber", mEvent.mPermitNumber);
        }

        object.put("description", event.mMessage);
        if (edit) {
            object.put("rev", event.mRev);
        }
        return object;
    }

    private List<Map<String, Object>> getSpecimenData(GameHarvest event) {
        List<Map<String, Object>> specimenList = new ArrayList<>();

        for (Specimen specimen : event.mSpecimen) {
            specimenList.add(JsonUtils.objectToMap(specimen));
        }

        return specimenList;
    }

    @Override
    protected void onAsyncStreamError(InputStream stream) throws Exception {
        //Log the server error message
        Utils.LogMessage("" + IOUtils.toString(stream, "UTF-8"));
    }

    @Override
    protected void onError() {
        if (getHttpStatusCode() == -1) {
            GameDatabase.getInstance().editLocalEvent(mEvent, false);
            eventWaitingDelivery();
        } else if (getHttpStatusCode() != 204) {
            Log.d(Utils.class.getSimpleName(), "Sending event failed with http status code " + getHttpStatusCode());
            if (getHttpStatusCode() == 409) {
                eventOutdated();
            } else {
                eventSendingFailed();
            }
        } else {
            GameDatabase.getInstance().markEventAsRemote(mEvent);
            eventSent();
        }
        textCompletion();
        imageCompletion(false, 0);
    }

    @Override
    protected void onFinishText(String text) {
        JSONObject object;
        try {
            // Text part of sending succeeded, continue with images
            object = new JSONObject(text);
            mEvent.mId = object.getInt("id");
            mEvent.mRev = object.getInt("rev");
            mEvent.mRemote = true;
            mEvent.mHarvestReportRequired = object.getBoolean("harvestReportRequired");
            mEvent.mHarvestReportDone = object.getBoolean("harvestReportDone");
            mEvent.mHarvestReportState = !object.isNull("harvestReportState") ? object.getString("harvestReportState") : null;
            mEvent.mStateAcceptedToHarvestPermit = !object.isNull("stateAcceptedToHarvestPermit") ? object.getString("stateAcceptedToHarvestPermit") : null;
            mEvent.mCanEdit = object.getBoolean("canEdit");
            eventSent();

            final LogImageUpdate imageUpdate = new LogImageUpdate();
            imageUpdate.addedImages = new ArrayList<>();
            imageUpdate.deletedImages = new ArrayList<>();

            // Pending changes are changes that already exist in database (pending insertion or deletion)
            LogImageUpdate pendingChanges = GameDatabase.getInstance().getImageUpdateFromUnsentEvent(mEvent);
            // New changes are changes that are not yet in database
            LogImageUpdate newChanges = GameDatabase.getInstance().editLocalEvent(mEvent, false);
            imageUpdate.addedImages.addAll(pendingChanges.addedImages);
            imageUpdate.addedImages.addAll(newChanges.addedImages);
            imageUpdate.deletedImages.addAll(pendingChanges.deletedImages);
            imageUpdate.deletedImages.addAll(newChanges.deletedImages);

            Utils.sendImages(getWorkContext(), mEvent.mId, imageUpdate, new LogImageSendCompletion() {
                @Override
                public void finish(boolean errors) {
                    if (!errors) {
                        GameDatabase.getInstance().markEventAsSent(mEvent);
                    }
                    imageCompletion(errors, imageUpdate.addedImages.size());
                }
            });

        } catch (JSONException e) {
            Log.e(Utils.class.getSimpleName(), e.getMessage());
            imageCompletion(true, 0);
        }
        textCompletion();
    }

    protected void eventSent() {
        // Override this method
    }

    protected void eventSendingFailed() {
        // Override this method
    }

    protected void eventOutdated() {
        // Override this method
    }

    protected void eventWaitingDelivery() {
        // Override this method
    }

    // This method needs to be called after the text operation ends, called after onError, onFinishText and eventSent
    protected void textCompletion() {
        // Override this method
    }

    // This methods needs to be called after images have been sent
    protected void imageCompletion(boolean errors, int sentImages) {
        // Override this method
    }
}
