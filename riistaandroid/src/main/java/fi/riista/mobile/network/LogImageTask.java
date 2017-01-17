package fi.riista.mobile.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import java.io.ByteArrayOutputStream;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.models.LogImage;
import fi.riista.mobile.utils.ImageUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.TextTask;
import fi.vincit.httpclientandroidlib.HttpEntity;
import fi.vincit.httpclientandroidlib.entity.ContentType;
import fi.vincit.httpclientandroidlib.entity.mime.MultipartEntityBuilder;

/**
 * Send harvest entry image to server
 */
public class LogImageTask extends TextTask {

    private LogImage mImage;
    private OperationType mOperationType;

    // For loading image to memory just before request is run.
    private int mDiaryEntryId;
    private Context mContext;

    public LogImageTask(WorkContext context, OperationType type, int gameDiaryEntryId, LogImage image) {
        super(context);
        mImage = image;
        mDiaryEntryId = gameDiaryEntryId;
        mContext = context.getContext();
        mOperationType = type;
        setCookieStore(GameDatabase.getInstance().getCookieStore());
        if (type == OperationType.ADD) {
            setBaseUrl(AppConfig.BASE_URL + "/gamediary/image/uploadforharvest");
            setHttpMethod(HttpMethod.POST);
        } else if (type == OperationType.DELETE) {
            setBaseUrl(AppConfig.BASE_URL + "/gamediary/image/" + mImage.uuid);
            setHttpMethod(HttpMethod.DELETE);
        }
    }

    @Override
    protected void onAsyncRun() throws Exception {
        if (mOperationType == OperationType.ADD) {
            try {
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();

                Bitmap bitmap = ImageUtils.getBitmapForUpload(mContext, mImage.uri);
                ByteArrayOutputStream blob = new ByteArrayOutputStream();

                assert bitmap != null;
                bitmap.compress(CompressFormat.JPEG, 100, blob);
                bitmap.recycle();

                byte[] bitmapdata = blob.toByteArray();
                blob.close();

                entityBuilder.addTextBody("harvestId", Integer.valueOf(mDiaryEntryId).toString());
                entityBuilder.addTextBody("uuid", mImage.uuid);
                entityBuilder.addBinaryBody("file", bitmapdata, ContentType.create("image/jpeg"), mImage.uri.getLastPathSegment());

                HttpEntity entity = entityBuilder.build();
                this.setHttpEntity(entity);
            } catch (OutOfMemoryError e) {
                Utils.LogMessage("LogImageTask", "Out of Memory");
                this.cancel();
            }
        }

        super.onAsyncRun();
    }

    @Override
    protected void onError() {
        if (getHttpStatusCode() != 204) {
            if (mOperationType == OperationType.DELETE) {
                // If the image being removed doesn't exist, remove it locally too
                GameDatabase.getInstance().markImageAsSent(mImage, mOperationType);
            }
        } else {
            GameDatabase.getInstance().markImageAsSent(mImage, mOperationType);
        }
    }

    @Override
    protected void onFinishText(String text) {
        GameDatabase.getInstance().markImageAsSent(mImage, mOperationType);
    }

    public enum OperationType {
        ADD,
        DELETE
    }
}
