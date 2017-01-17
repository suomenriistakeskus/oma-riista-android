package fi.riista.mobile.pages;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.activity.BaseActivity;
import fi.riista.mobile.models.LogImage;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.view.WebImageView;

public class ImageViewer extends PageFragment {

    public static String IMAGES = "images";
    private static int pagerMarginPixels = 20;

    private ImageViewerInterface mInterface = null;
    private List<LogImage> mImages = null;
    private ViewPager mPager = null;
    private ImageAdapter mAdapter = null;
    private int mDiaryEntryId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_imageviewer, container, false);
        mPager = (ViewPager) view.findViewById(R.id.imagepager);
        mPager.setPageMargin(pagerMarginPixels);
        if (mInterface != null) {
            mInterface.ImageViewerViewCreated(this);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity a = getActivity();
        if (a != null && isMenuVisible())
            a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Activity a = getActivity();
        if (a != null)
            a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        Activity a = getActivity();
        if (a != null) {
            if (menuVisible) {
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            } else {
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    public void setImages(List<LogImage> images, int currentImageIndex) {
        mImages = images;
        mAdapter = new ImageAdapter(mPager, mImages);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(currentImageIndex);
    }

    public void setDelegate(ImageViewerInterface delegate) {
        mInterface = delegate;
    }

    void setTitleForImage(int index) {
        String imageTitleFormat = getActivity().getResources().getString(R.string.imageViewTemplate);
        String title = String.format(imageTitleFormat, String.valueOf(index + 1), String.valueOf(mImages.size()));
        setViewTitle(title);
    }

    public interface ImageViewerInterface {
        void ImageViewerViewCreated(ImageViewer viewer);

        void imageSelected(int diaryEntryId);
    }

    private class ImageAdapter extends PagerAdapter {

        private List<LogImage> mImages = null;

        ImageAdapter(ViewPager pager, List<LogImage> images) {
            mImages = images;
        }

        @Override
        public Object instantiateItem(View container, int position) {
            BaseActivity mainActivity = (BaseActivity) ImageViewer.this.getActivity();
            WebImageView imageView = new WebImageView(ImageViewer.this.getActivity());
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setAdjustViewBounds(true);
            Utils.setupImage(mainActivity.getWorkContext(), imageView, mImages.get(position), container.getMeasuredWidth(), container.getMeasuredWidth(), "b");
            ((ViewPager) container).addView(imageView);
            return imageView;
        }

        @Override
        public int getCount() {
            return mImages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return ((ImageView) object) == view;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            setTitleForImage(position);
            int diaryEntryId = mImages.get(position).diaryEntryId;
            if (mInterface != null && mDiaryEntryId != diaryEntryId) {
                mInterface.imageSelected(diaryEntryId);
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }
    }
}
