package fi.riista.mobile.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.msebera.android.httpclient.entity.StringEntity;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.WorkAsyncTask;
import fi.vincit.androidutilslib.task.WorkAsyncTask.TaskListener;

import static java.util.Objects.requireNonNull;

public class JsonUtils {

    private static final String ENCODING = "UTF-8";

    private static ObjectMapper sMapper;

    public static void setMapper(@NonNull final ObjectMapper objectMapper) {
        sMapper = requireNonNull(objectMapper);
    }

    private static ObjectMapper getMapper() {
        if (sMapper == null) {
            throw new IllegalStateException("JsonUtils must be initialized with ObjectMapper instance before use");
        }

        return sMapper;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> objectToMap(final Object object) {
        return getMapper().convertValue(object, Map.class);
    }

    public static String objectToJson(final Object object) {
        try {
            return getMapper().writeValueAsString(object);
        } catch (final JsonProcessingException e) {
            Utils.LogMessage("JSON error: " + e.getMessage());
        }
        return null;
    }

    public static <T> List<T> jsonToList(final String json, final Class<T> klass) {
        final ObjectMapper mapper = getMapper();
        final CollectionType ct = mapper.getTypeFactory().constructCollectionType(ArrayList.class, klass);

        try {
            return mapper.readValue(json, ct);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static <T> Set<T> jsonToSet(final String json, final Class<T> klass) {
        final ObjectMapper mapper = getMapper();
        final CollectionType ct = mapper.getTypeFactory().constructCollectionType(HashSet.class, klass);

        try {
            return mapper.readValue(json, ct);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return new HashSet<>();
    }

    public static <T> T jsonToObject(final String json, final Class<T> klass) {
        return jsonToObject(json, klass, false);
    }

    public static <T> T jsonToObject(final String json, final Class<T> klass, final boolean returnNullOnException) {
        try {
            return getMapper().readValue(json, klass);
        } catch (final IOException e) {
            if (returnNullOnException) {
                return null;
            }
            throw new RuntimeException(e);
        }
    }

    public static <T> T jsonToObject(final InputStream stream, final Class<T> klass) throws IOException {
        return getMapper().readValue(stream, klass);
    }

    public static StringEntity createJsonStringEntity(final Object object) {
        final String json = objectToJson(object);

        final StringEntity entity = json != null
                ? new StringEntity(json, ENCODING)
                : new StringEntity("", ENCODING); // Let the network task fail later
        entity.setContentType("application/json; charset=" + ENCODING);

        return entity;
    }

    public static void writeToFileAsync(final WorkContext workContext, final Object object, final String fileName) {
        writeToFileAsync(workContext, object, fileName, null);
    }

    public static void writeToFileAsync(final WorkContext workContext,
                                        final Object object,
                                        final String fileName,
                                        final TaskListener listener) {

        final WorkAsyncTask task = new WorkAsyncTask(workContext) {
            @Override
            protected void onAsyncRun() throws IOException {
                final Context context = workContext.getContext();

                try (final FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
                    final String json = objectToJson(object);
                    IOUtils.write(json, outputStream, ENCODING);
                }
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
