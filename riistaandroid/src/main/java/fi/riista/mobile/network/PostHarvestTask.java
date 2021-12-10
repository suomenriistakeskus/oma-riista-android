package fi.riista.mobile.network;

import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.HarvestDatabase;
import fi.riista.mobile.database.HarvestDbHelper;
import fi.riista.mobile.database.HarvestImageUpdate;
import fi.riista.mobile.models.DeerHuntingType;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.GameLog;
import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.models.GreySealHuntingMethod;
import fi.riista.mobile.models.HarvestSpecimen;
import fi.riista.mobile.utils.CookieStoreSingleton;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.OperationCounter;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;

import static java.util.Objects.requireNonNull;

/**
 * Send harvest entry to server
 */
public abstract class PostHarvestTask extends TextTask {

    private static final String TAG = "PostHarvestTask";

    private final HarvestDatabase mHarvestDatabase;
    private final GameHarvest mHarvest;
    private final int mHarvestSpecVersion;

    // TODO Remove `harvestSpecVersion` parameter when deer pilot 2020 is over.
    protected PostHarvestTask(final WorkContext workContext,
                              final HarvestDatabase harvestDatabase,
                              final GameHarvest harvest,
                              final int harvestSpecVersion) {
        super(workContext);

        setCookieStore(CookieStoreSingleton.INSTANCE.getCookieStore());

        mHarvestDatabase = harvestDatabase;
        mHarvest = harvest;
        mHarvestSpecVersion = requireNonNull(harvestSpecVersion);

        if (harvest.mPendingOperation == HarvestDbHelper.UpdateType.DELETE) {
            setBaseUrl(AppConfig.getBaseUrl() + "/gamediary/harvest/" + mHarvest.mId);
            setHttpMethod(HttpMethod.DELETE);
        } else if (!harvest.mRemote) {
            setBaseUrl(AppConfig.getBaseUrl() + "/gamediary/harvest");
            setHttpMethod(HttpMethod.POST);
            setJsonEntity(getRequestData(harvest, false));
        } else {
            setBaseUrl(AppConfig.getBaseUrl() + "/gamediary/harvest/" + mHarvest.mId);
            setHttpMethod(HttpMethod.PUT);
            setJsonEntity(getRequestData(harvest, true));
        }
    }

    private Map<String, Object> getRequestData(final GameHarvest event, final boolean edit) {
        final Map<String, Object> object = new HashMap<>();

        object.put("type", GameLog.TYPE_HARVEST);
        object.put("harvestSpecVersion", mHarvestSpecVersion);

        if (edit) {
            object.put("rev", event.mRev);
        } else if (event.mMobileClientRefId != 0) {
            object.put("mobileClientRefId", event.mMobileClientRefId);
        }

        object.put("gameSpeciesCode", event.mSpeciesID);

        final Map<String, Object> geoLocation = new HashMap<>();
        geoLocation.put("source", mHarvest.mLocationSource);
        geoLocation.put("latitude", mHarvest.mCoordinates.first);
        geoLocation.put("longitude", mHarvest.mCoordinates.second);
        geoLocation.put("accuracy", mHarvest.mAccuracy);

        if (mHarvest.mHasAltitude) {
            geoLocation.put("altitude", mHarvest.mAltitude);
        }

        object.put("geoLocation", geoLocation);
        object.put("pointOfTime", DateTimeUtils.formatDate(mHarvest.mTime.getTime()));
        object.put("amount", event.mAmount);
        object.put("description", event.mDescription);

        if (event.mPermitNumber != null) {
            object.put("permitNumber", mHarvest.mPermitNumber);
        }

        if (event.mDeerHuntingType != null) {
            object.put("deerHuntingType", mHarvest.mDeerHuntingType.name());
            object.put("deerHuntingOtherTypeDescription", mHarvest.mDeerHuntingOtherTypeDescription);
        }
        if (event.mFeedingPlace != null) {
            object.put("feedingPlace", event.mFeedingPlace);
        }
        if (event.mHuntingMethod != null) {
            object.put("huntingMethod", event.mHuntingMethod);
        }
        if (event.mTaigaBeanGoose != null) {
            object.put("taigaBeanGoose", event.mTaigaBeanGoose);
        }

        final List<Map<String, Object>> specimenData =
                event.mSpecimen != null && event.mAmount < GameLog.SPECIMEN_DETAILS_MAX
                        ? getSpecimenData(event)
                        : new ArrayList<>(0);

        object.put("specimens", specimenData);

        return object;
    }

    private List<Map<String, Object>> getSpecimenData(final GameHarvest harvest) {
        final List<Map<String, Object>> specimenList = new ArrayList<>(harvest.mSpecimen.size());

        for (final HarvestSpecimen specimen : harvest.mSpecimen) {
            specimenList.add(JsonUtils.objectToMap(specimen));
        }

        return specimenList;
    }

    @Override
    protected void onAsyncStreamError(final InputStream stream) throws Exception {
        // Log the server error message.
        Log.d(TAG, IOUtils.toString(stream, StandardCharsets.UTF_8));
    }

    @Override
    protected void onError() {
        final int statusCode = getHttpStatusCode();

        if (statusCode == -1) {
            mHarvestDatabase.updateHarvest(mHarvest, false);
            onHarvestWaitingDelivery();

        } else if (statusCode != 204) {
            Log.d(TAG, "Sending event failed with http status code " + statusCode);

            if (statusCode == 409) {
                onHarvestOutdated();
            } else {
                onHarvestSendingFailed();
            }
        } else {
            mHarvestDatabase.markHarvestAsRemote(mHarvest);
            onHarvestSent();
        }

        onTextCompletion();
        onImageCompletion(false, 0);
    }

    @Override
    protected void onFinishText(final String text) {
        if (this.getHttpMethod() == HttpMethod.DELETE) {
            onHarvestSent();

            onTextCompletion();
            return;
        }

        try {
            // Text part of sending succeeded, continue with images
            final JSONObject object = new JSONObject(text);

            mHarvest.mHarvestSpecVersion = object.getInt("harvestSpecVersion");

            mHarvest.mId = object.getInt("id");

            mHarvest.mRemote = true;
            mHarvest.mRev = object.getInt("rev");
            mHarvest.mCanEdit = object.getBoolean("canEdit");

            mHarvest.mSpeciesID = object.getInt("gameSpeciesCode");

            // Skip saving location and time fields.

            mHarvest.mAmount = object.getInt("amount");
            mHarvest.mDescription = !object.isNull("description") ? object.getString("description") : null;

            mHarvest.mHarvestReportRequired = object.getBoolean("harvestReportRequired");
            mHarvest.mHarvestReportDone = object.getBoolean("harvestReportDone");
            mHarvest.mHarvestReportState = !object.isNull("harvestReportState") ? object.getString("harvestReportState") : null;

            mHarvest.mStateAcceptedToHarvestPermit = !object.isNull("stateAcceptedToHarvestPermit") ? object.getString("stateAcceptedToHarvestPermit") : null;
            mHarvest.mPermitNumber = !object.isNull("permitNumber") ? object.getString("permitNumber") : null;
            mHarvest.mPermitType = !object.isNull("permitType") ? object.getString("permitType") : null;

            mHarvest.mDeerHuntingType = !object.isNull("deerHuntingType")
                    ? DeerHuntingType.valueOf(object.getString("deerHuntingType"))
                    : null;
            mHarvest.mDeerHuntingOtherTypeDescription = !object.isNull("deerHuntingOtherTypeDescription")
                    ? object.getString("deerHuntingOtherTypeDescription")
                    : null;
            mHarvest.mFeedingPlace = !object.isNull("feedingPlace") ? object.getBoolean("feedingPlace") : null;
            mHarvest.mHuntingMethod = !object.isNull("huntingMethod")
                    ? GreySealHuntingMethod.fromString(object.getString("huntingMethod"))
                    : null;
            mHarvest.mTaigaBeanGoose = !object.isNull("taigaBeanGoose") ? object.getBoolean("taigaBeanGoose") : null;

            // Skip null check since field must contain a list
            final String specimenData = object.getString("specimens");
            mHarvest.mSpecimen = JsonUtils.jsonToList(specimenData, HarvestSpecimen.class);

            onHarvestSent();

            // Pending changes are changes that already exist in database (pending insertion or deletion).
            final HarvestImageUpdate pendingChanges = mHarvestDatabase.getImageUpdateFromUnsentHarvest(mHarvest);

            // New changes are changes that are not yet in database.
            final HarvestImageUpdate newChanges = mHarvestDatabase.updateHarvest(mHarvest, false);

            final HarvestImageUpdate imageUpdate = pendingChanges.merge(newChanges);

            postHarvestImages(imageUpdate, new PostHarvestImageCallback() {
                @Override
                public void finish(final boolean errors) {
                    if (!errors) {
                        mHarvestDatabase.markHarvestAsSent(mHarvest);
                    }

                    // TODO Is amount of changes properly derived?
                    onImageCompletion(errors, imageUpdate.getNumberOfImageChanges());
                }
            });

        } catch (final JSONException e) {
            Log.e(TAG, e.getMessage());
            onImageCompletion(true, 0);
        }

        onTextCompletion();
    }

    protected void onHarvestSent() {
        // Override this method
    }

    protected void onHarvestSendingFailed() {
        // Override this method
    }

    protected void onHarvestOutdated() {
        // Override this method
    }

    protected void onHarvestWaitingDelivery() {
        // Override this method
    }

    // This method needs to be called after the text operation ends, called after onError, onFinishText and eventSent
    protected void onTextCompletion() {
        // Override this method
    }

    // This methods needs to be called after images have been sent
    protected void onImageCompletion(final boolean errors, final int sentImageUpdates) {
        // Override this method
    }

    private void postHarvestImages(final HarvestImageUpdate imageUpdate, final PostHarvestImageCallback callback) {
        final WorkContext workContext = getWorkContext();
        final OperationCounter counter = new OperationCounter(imageUpdate.getNumberOfImageChanges());
        final int harvestId = mHarvest.mId;

        if (counter.getTotalImageOperations() == 0 && callback != null) {
            callback.finish(false);
            return;
        }

        if (imageUpdate.hasDeletedImages()) {
            for (final GameLogImage deletedImage : imageUpdate.getDeletedImages()) {
                final DeleteHarvestImageTask task =
                        new DeleteHarvestImageTask(workContext, mHarvestDatabase, deletedImage);
                task.start();

                if (counter.incrementOperationsDone() && callback != null) {
                    callback.finish(counter.getErrors());
                }
            }
        }

        if (imageUpdate.hasAddedImages()) {
            for (final GameLogImage addedImage : imageUpdate.getAddedImages()) {
                final PostHarvestImageTask task =
                        new PostHarvestImageTask(workContext, mHarvestDatabase, harvestId, addedImage) {

                    @Override
                    protected void onFinishText(final String text) {
                        super.onFinishText(text);

                        if (counter.incrementOperationsDone() && callback != null) {
                            callback.finish(counter.getErrors());
                        }
                    }

                    @Override
                    protected void onError() {
                        super.onError();

                        if (getHttpStatusCode() >= 400) {
                            counter.setErrors(true);
                        }

                        if (counter.incrementOperationsDone() && callback != null) {
                            callback.finish(counter.getErrors());
                        }
                    }
                };
                task.start();
            }
        }
    }
}
