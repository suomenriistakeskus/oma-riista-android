package fi.riista.mobile.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.message.ChangeHarvestMessage;
import fi.riista.mobile.message.ChangeObservationMessage;
import fi.riista.mobile.models.LogImage;
import fi.riista.mobile.pages.ImageViewer;
import fi.riista.mobile.pages.ImageViewer.ImageViewerInterface;

public class ImageViewerActivity extends BaseActivity {

    public static final String EXTRA_IMAGES = "extra_images";
    public static final String EXTRA_UUID = "extra_uuid";
    public static final String EXTRA_ISHARVEST = "extra_isharvest";

    private boolean mIsHarvestImages = false;

    private static class ViewerImage implements Serializable {
        public String uuid;
        public String uri;
        int entryId;

        ViewerImage(LogImage logImage) {
            uuid = logImage.uuid;
            if (logImage.uri != null) {
                uri = logImage.uri.toString();
            }
            entryId = logImage.diaryEntryId;
        }

        LogImage toLogImage() {
            LogImage image;
            if (uri != null) {
                image = new LogImage(Uri.parse(uri));
            } else {
                image = new LogImage(uuid);
            }
            image.diaryEntryId = entryId;
            return image;
        }
    }

    public static Intent createIntent(Context context, List<LogImage> logImages, String selectedUuid) {
        ArrayList<ViewerImage> images = new ArrayList<>();
        for (LogImage image : logImages) {
            images.add(new ViewerImage(image));
        }

        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(EXTRA_IMAGES, images);
        intent.putExtra(EXTRA_UUID, selectedUuid);
        return intent;
    }

    private List<LogImage> mImages = new ArrayList<>();
    private int mSelectedImageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment);

        List<ViewerImage> images = (List<ViewerImage>) getIntent().getSerializableExtra(EXTRA_IMAGES);
        String selectedUuid = "" + getIntent().getStringExtra(EXTRA_UUID);

        mIsHarvestImages = getIntent().getBooleanExtra(EXTRA_ISHARVEST, false);

        for (ViewerImage image : images) {
            if (image.uuid.equals(selectedUuid)) {
                mSelectedImageIndex = mImages.size();
            }
            mImages.add(image.toLogImage());
        }

        final ImageViewer imageViewer = new ImageViewer();
        imageViewer.setDelegate(new ImageViewerInterface() {
            @Override
            public void imageSelected(int diaryEntryId) {
                if (mIsHarvestImages) {
                    getWorkContext().sendGlobalMessage(new ChangeHarvestMessage(diaryEntryId));
                } else {
                    getWorkContext().sendGlobalMessage(new ChangeObservationMessage(diaryEntryId));
                }
            }

            @Override
            public void ImageViewerViewCreated(ImageViewer viewer) {
                imageViewer.setImages(mImages, mSelectedImageIndex);
            }
        });

        getSupportFragmentManager().beginTransaction().add(R.id.layout_fragment_container, imageViewer).commit();
    }
}
