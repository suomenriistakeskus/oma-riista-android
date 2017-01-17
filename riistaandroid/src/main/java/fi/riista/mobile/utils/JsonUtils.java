package fi.riista.mobile.utils;

import android.content.Context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fi.riista.mobile.RiistaApplication;
import fi.vincit.androidutilslib.task.WorkAsyncTask;
import fi.vincit.androidutilslib.task.WorkAsyncTask.TaskListener;
import fi.vincit.androidutilslib.util.JsonSerializator;
import fi.vincit.httpclientandroidlib.entity.StringEntity;

public class JsonUtils {

    private static ObjectMapper sMapper = JsonSerializator.createDefaultMapper();

    private static final String ENCODING = "UTF-8";

    @SuppressWarnings("unchecked")
    public static Map<String, Object> objectToMap(Object object) {
        return sMapper.convertValue(object, Map.class);
    }

    public static String objectToJson(Object object) {
        String json = null;
        try {
            json = sMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            Utils.LogMessage("JSON error: " + e.getMessage());
        }
        return json;
    }

    public static <T> List<T> jsonToList(String json, Class<T> klass) {
        CollectionType ct = sMapper.getTypeFactory().constructCollectionType(ArrayList.class, klass);
        try {
            return sMapper.readValue(json, ct);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static <T> T jsonToObject(String json, Class<T> klass) {
        try {
            return sMapper.readValue(json, klass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static StringEntity createJsonStringEntity(Object object) {
        String json = objectToJson(object);
        if (json == null) {
            json = ""; //Let the network task fail later
        }

        StringEntity entity = new StringEntity(json, ENCODING);
        entity.setContentType("application/json; charset=" + ENCODING);
        return entity;
    }

    public static void writeToFileAsync(Object object, final String fileName, TaskListener listener) {
        final String json = objectToJson(object);
        final Context context = RiistaApplication.getInstance();

        WorkAsyncTask task = new WorkAsyncTask(RiistaApplication.getInstance().getWorkContext()) {
            @Override
            protected void onAsyncRun() throws Exception {
                FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                IOUtils.write(json, outputStream, ENCODING);
                outputStream.close();
            }

            @Override
            protected void onFinish() {
                Utils.LogMessage("Wrote file to " + fileName);
            }

            @Override
            protected void onError() {
                Utils.LogMessage("Error writing file to " + fileName + ": " + getError().getMessage());
            }
        };
        if (listener != null) {
            task.addTaskListener(listener);
        }
        task.startSerial();
    }
}
