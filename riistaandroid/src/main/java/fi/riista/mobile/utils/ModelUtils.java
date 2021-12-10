package fi.riista.mobile.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fi.riista.mobile.models.GameLogImage;
import fi.riista.mobile.models.LocalImage;

public class ModelUtils {

    public static List<GameLogImage> combineImages(final List<String> imageIds, final List<LocalImage> localImages) {
        final ArrayList<GameLogImage> results = new ArrayList<>();

        // Combine images from both sources.
        for (final String imageId : imageIds) {
            results.add(new GameLogImage(imageId));
        }

        for (final LocalImage info : localImages) {
            if (info.serverId == null || !imageIds.contains(info.serverId)) {
                results.add(info.toGameLogImage());
            }
        }

        Collections.reverse(results); // Locals first

        return results;
    }
}
