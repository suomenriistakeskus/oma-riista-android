package fi.riista.mobile.models;

import android.net.Uri;
import android.text.TextUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class GameLogImage implements Serializable {

    public ImageType type;
    public String uuid = "";
    public transient Uri uri;
    public int imageStatus = 0;

    public GameLogImage(Uri imageUri) {
        type = ImageType.URI;
        uri = imageUri;
    }

    public GameLogImage(String imageUUID) {
        type = ImageType.UUID;
        uuid = imageUUID;
    }

    public enum ImageType {
        URI, // local image
        UUID // remote image identified by uuid
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();

        String input = in.readUTF();
        if (!TextUtils.isEmpty(input)) {
            uri = Uri.parse(input);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        out.writeUTF(uri != null ? uri.toString() : "");
    }
}
