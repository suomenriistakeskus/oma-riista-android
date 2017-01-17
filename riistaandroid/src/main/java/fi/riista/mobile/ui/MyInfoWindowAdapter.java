package fi.riista.mobile.ui;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import fi.riista.mobile.LocationInterface;
import fi.riista.mobile.R;

/**
 * Info window adapter for google maps component
 * Used for displaying location quality information
 */
public class MyInfoWindowAdapter implements InfoWindowAdapter, LocationInterface {

    private final View contentView;
    private final int GOOD_ACCURACY = 50; // meters
    private Context mContext;
    private Location mLocation = null;

    public MyInfoWindowAdapter(Context context) {
        mContext = context;
        contentView = LayoutInflater.from(mContext).inflate(R.layout.view_gps_overlay, null);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        ImageView qualityImage = (ImageView) contentView.findViewById(R.id.loc_quality_image);
        TextView qualityText = (TextView) contentView.findViewById(R.id.loc_quality_text);
        if (mLocation != null && mLocation.getAccuracy() < GOOD_ACCURACY) {
            qualityImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_small_gps_good));
            qualityText.setText(mContext.getResources().getString(R.string.gps_good));
        } else if (mLocation != null) {
            qualityImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_small_gps_bad));
            qualityText.setText(mContext.getResources().getString(R.string.gps_bad));
        } else {
            qualityImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_small_no_gps));
            qualityText.setText(mContext.getResources().getString(R.string.gps_none));
        }

        return contentView;
    }

    @Override
    public void newLocation(Location location) {
        mLocation = location;
    }
}
