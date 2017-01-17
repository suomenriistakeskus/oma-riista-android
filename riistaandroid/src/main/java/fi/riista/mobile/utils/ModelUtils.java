package fi.riista.mobile.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fi.riista.mobile.models.LocalImage;
import fi.riista.mobile.models.LogImage;

public class ModelUtils {

    public static List<LogImage> combineImages(Long entryId, List<String> imageIds, List<LocalImage> localImages) {
        ArrayList<LogImage> results = new ArrayList<>();

        //Combine images from both sources.
        for (String imageId : imageIds) {
            results.add(new LogImage(imageId));
        }
        for (LocalImage info : localImages) {
            if (info.serverId != null && imageIds.contains(info.serverId)) {
                //Already exists on the server
            } else {
                results.add(info.toLogImage());
            }
        }
        if (entryId != null) {
            for (LogImage image : results) {
                image.diaryEntryId = entryId.intValue();
            }
        }

        Collections.reverse(results); //Locals first

        return results;
    }
}
