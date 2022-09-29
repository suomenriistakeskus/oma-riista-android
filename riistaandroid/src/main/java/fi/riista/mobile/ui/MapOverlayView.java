package fi.riista.mobile.ui;

import static fi.riista.mobile.EntryMapView.MAP_ZOOM_LEVEL_MAX;
import static fi.riista.mobile.EntryMapView.MAP_ZOOM_LEVEL_MIN;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import fi.riista.mobile.EntryMapView;
import fi.riista.mobile.R;
import fi.riista.mobile.models.AreaMap;
import fi.riista.mobile.utils.AppPreferences;
import fi.riista.mobile.utils.UiUtils;

public class MapOverlayView extends LinearLayout {

    public interface MapViewerInterface {
        /**
         * Toggle map view fullscreen mode
         *
         * @return Is map fullscreen afterwards
         */
        boolean onExpandCollapse();

        void setMeasuring(final boolean isMeasuring);
    }

    public static final String NO_AREA_ID = "NO_AREA_ID";

    public MapViewerInterface fragment;
    private Location mCurrentGpsLocation;
    private List<Location> mMeasurePoints;
    private Polyline mMeasureLine;
    private EntryMapView mMapView;
    private View mMapScaleView;
    private TextView mMapScaleTextView;
    private View mMapMeasureContainer;
    private ImageButton mMapMeasureButton;
    private TextView mMapMeasureTextView;
    private View mShowMapControlsView;
    private ImageButton mShowMapControlsButton;
    private ImageButton mFullscreenButton;
    private View mMapControlsContainer;
    private ImageButton mZoomInButton;
    private ImageButton mZoomOutButton;
    private boolean isControlsVisible;

    public MapOverlayView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.view_map_overlay, this);

        init();
    }

    private static String formatDistance(final float meters) {
        if (meters > 0.0f) {
            if (meters > 1000.0f) {
                final float kms = meters / 1000.0f;
                return String.format("%.1f km", kms);
            }

            return (int) meters + " m";
        }

        return "0 m";
    }

    private void init() {
        mMapScaleView = findViewById(R.id.map_scale);
        mMapScaleTextView = findViewById(R.id.map_scale_text);
        mMapMeasureContainer = findViewById(R.id.container_map_measure);
        mMapMeasureTextView = findViewById(R.id.map_measure_text);
        mMapControlsContainer = findViewById(R.id.container_map_controls);

        isControlsVisible = !AppPreferences.getHideMapControls(getContext());

        mShowMapControlsView = findViewById(R.id.container_show_map_controls);
        mShowMapControlsButton = findViewById(R.id.btn_show_map_controls);
        mShowMapControlsButton.setOnClickListener(view -> {
            if (isControlsVisible) {
                isControlsVisible = false;
                animateMapControlsOut();
            } else {
                isControlsVisible = true;
                animateMapControlsIn();
            }
        });

        findViewById(R.id.btn_overlay_center).setOnClickListener(view -> {
            if (mCurrentGpsLocation != null) {
                mMapView.animateCameraTo(mCurrentGpsLocation);
            }
        });
        mZoomInButton = findViewById(R.id.btn_zoom_in);
        mZoomInButton.setOnClickListener(view -> mMapView.zoomBy(1.0f));
        mZoomOutButton = findViewById(R.id.btn_zoom_out);
        mZoomOutButton.setOnClickListener(view -> mMapView.zoomBy(-1.0f));

        mFullscreenButton = findViewById(R.id.btn_full_screen);
        mFullscreenButton.setOnClickListener(view -> {
            if (fragment.onExpandCollapse()) {
                mFullscreenButton.setImageDrawable(
                        ResourcesCompat.getDrawable(getResources(), R.drawable.ic_collapse, null)
                );
            } else {
                mFullscreenButton.setImageDrawable(
                        ResourcesCompat.getDrawable(getResources(), R.drawable.ic_expand, null)
                );
            }
        });

        mMapMeasureButton = findViewById(R.id.btn_measure);
        mMapMeasureButton.setOnClickListener(view -> {
            if (mMeasurePoints == null) {
                startMeasuring();
            } else {
                stopMeasuring();
            }
        });

        findViewById(R.id.btn_measure_add).setOnClickListener(view -> {
            if (mMeasurePoints != null) {
                addMeasurementPoint();
            }
        });

        findViewById(R.id.btn_measure_remove).setOnClickListener(view -> {
            if (mMeasurePoints != null) {
                removeMeasurementPoint();
            }
        });
    }

    public void setMapView(final EntryMapView view) {
        mMapView = view;
    }

    public void setCurrentGpsLocation(final Location location) {
        mCurrentGpsLocation = location;
    }

    private void updateZoomButtonStatuses() {
        float zoomLevel = mMapView.getCameraZoomLevel();
        mZoomInButton.setEnabled(zoomLevel < MAP_ZOOM_LEVEL_MAX);
        mZoomOutButton.setEnabled(zoomLevel > MAP_ZOOM_LEVEL_MIN);
    }

    private void startMeasuring() {
        fragment.setMeasuring(true);

        mMapMeasureContainer.setVisibility(View.VISIBLE);
        mMapMeasureButton.setColorFilter(Color.argb(255, 0, 200, 0), PorterDuff.Mode.SRC_ATOP);

        mMeasurePoints = new ArrayList<>();
        addMeasurementPoint();
    }

    private void stopMeasuring() {
        fragment.setMeasuring(false);

        if (mMeasureLine != null) {
            mMeasureLine.remove();
            mMeasureLine = null;
        }
        mMeasurePoints = null;

        mMapMeasureContainer.setVisibility(View.GONE);
        mMapMeasureButton.clearColorFilter();
    }

    private void removeMeasurementPoint() {
        if (mMeasurePoints != null && mMeasurePoints.size() > 0) {
            mMeasurePoints.remove(mMeasurePoints.size() - 1);

            updateMeasureUi();

            if (mMeasurePoints.isEmpty()) {
                stopMeasuring();
            }
        }
    }

    private void addMeasurementPoint() {
        mMeasurePoints.add(mMapView.getCameraLocation());

        updateMeasureUi();
    }

    public void updateCameraMoved() {
        updateMeasureUi();
        updateMapScaleText();
        updateZoomButtonStatuses();
    }

    private void updateMeasureUi() {
        if (mMeasurePoints == null) {
            return;
        }

        final ArrayList<Location> points = new ArrayList<>(mMeasurePoints);
        points.add(mMapView.getCameraLocation());

        float total = 0.0f;

        for (int i = 1; i < points.size(); i++) {
            final Location a = points.get(i - 1);
            final Location b = points.get(i);

            final float[] results = new float[1];
            Location.distanceBetween(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude(), results);
            total += results[0];
        }

        final ArrayList<LatLng> linePoints = new ArrayList<>(points.size());

        for (final Location location : points) {
            linePoints.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }

        if (mMeasureLine == null) {
            mMeasureLine = mMapView.getMap().addPolyline(new PolylineOptions()
                    .zIndex(100)
                    .width(UiUtils.dipToPixels(getContext(), 5))
                    .color(Color.RED));
        }
        mMeasureLine.setPoints(linePoints);

        mMapMeasureTextView.setText(formatDistance(total));
    }

    private void updateMapScaleText() {
        final float distance = mMapView.calculateScale(mMapScaleView.getWidth());
        mMapScaleTextView.setText(formatDistance(distance));
    }

    public void updateMapSettings() {
        mMapView.setLocationVisible(AppPreferences.getShowUserMapLocation(getContext()));
//        setActiveClubArea(AppPreferences.getSelectedClubAreaMapId(getContext()));
    }

    public void updateMhMooseAreaVisibility() {
        final Set<AreaMap> mooseCodes = AppPreferences.getSelectedMhMooseAreaMapIds(getContext());
        final Iterator<AreaMap> mooseIter = mooseCodes != null ? mooseCodes.iterator() : null;

        mMapView.setShowMhMooseLayer(
                mooseCodes != null && mooseCodes.size() > 0,
                mooseIter != null && mooseIter.hasNext() ? mooseIter.next().getNumber() : "");
    }

    public void updateMhPienriistaAreaVisibility() {
        final Set<AreaMap> pienriistaCodes = AppPreferences.getSelectedMhPienriistaAreasMapIds(getContext());
        final Iterator<AreaMap> pienriistaIter = pienriistaCodes != null ? pienriistaCodes.iterator() : null;

        mMapView.setShowMhPienriistaLayer(
                pienriistaCodes != null && pienriistaCodes.size() > 0,
                pienriistaIter != null && pienriistaIter.hasNext() ? pienriistaIter.next().getNumber() : "");
    }

    private void animateMapControlsIn() {
        final TranslateAnimation buttonAnim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.3f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        buttonAnim.setDuration(200);
        buttonAnim.setFillAfter(true);

        final TranslateAnimation controlsAnim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        controlsAnim.setDuration(200);
        controlsAnim.setFillAfter(true);

        mShowMapControlsButton.setImageResource(R.drawable.ic_menu_hide);
        final float scale = getResources().getDisplayMetrics().density;
        mShowMapControlsButton.setPadding((int) (12 * scale + 0.5f),
                mShowMapControlsButton.getPaddingTop(),
                mShowMapControlsButton.getPaddingRight(),
                mShowMapControlsButton.getPaddingBottom());

        mShowMapControlsView.startAnimation(buttonAnim);
        mMapControlsContainer.startAnimation(controlsAnim);
    }

    private void animateMapControlsOut() {
        final TranslateAnimation buttonAnim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.3f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        buttonAnim.setDuration(200);
        buttonAnim.setFillAfter(true);

        final TranslateAnimation controlsAnim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        controlsAnim.setDuration(200);
        controlsAnim.setFillAfter(true);

        mShowMapControlsButton.setImageResource(R.drawable.ic_menu_expand);
        mShowMapControlsButton.setPadding(0,
                mShowMapControlsButton.getPaddingTop(),
                mShowMapControlsButton.getPaddingRight(),
                mShowMapControlsButton.getPaddingBottom());

        mShowMapControlsView.startAnimation(buttonAnim);
        mMapControlsContainer.startAnimation(controlsAnim);
    }
}
