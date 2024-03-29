package fi.riista.mobile.models;

import android.net.Uri;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class LocalImage implements Serializable {

    @JsonProperty("serverId")
    public String serverId;

    @JsonProperty("localPath")
    public String localPath;

    public GameLogImage toGameLogImage() {
        GameLogImage image = new GameLogImage(Uri.parse(localPath));
        image.uuid = serverId;
        return image;
    }

    public static LocalImage fromGameLogImage(GameLogImage image) {
        LocalImage info = new LocalImage();
        if (image.uri != null) {
            info.localPath = image.uri.getPath();
        }
        info.serverId = image.uuid;
        return info;
    }
}
