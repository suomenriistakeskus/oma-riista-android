package fi.riista.mobile.models;

import android.location.Location;
import android.util.Pair;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import fi.riista.mobile.utils.MapUtils;

public class GeoLocation implements Serializable {
    @JsonProperty("latitude")
    public int latitude;

    @JsonProperty("longitude")
    public int longitude;

    @JsonProperty("source")
    public String source;

    @JsonProperty("accuracy")
    public Double accuracy;

    @JsonProperty("altitude")
    public Double altitude;

    @JsonProperty("altitudeAccuracy")
    public Double altitudeAccuracy;

    public static GeoLocation fromLocation(Location location) {
        Pair<Long, Long> coords = MapUtils.WGS84toETRSTM35FIN(location.getLatitude(), location.getLongitude());

        GeoLocation geoLocation = new GeoLocation();
        geoLocation.latitude = coords.first.intValue();
        geoLocation.longitude = coords.second.intValue();
        if (location.hasAccuracy()) {
            geoLocation.accuracy = (double) location.getAccuracy();
        }
        if (location.hasAltitude()) {
            geoLocation.altitude = location.getAltitude();
        }
        return geoLocation;
    }

    public Location toLocation() {
        Pair<Double, Double> coords = MapUtils.ETRMStoWGS84(latitude, longitude);

        Location location = new Location("");
        location.setLatitude(coords.first);
        location.setLongitude(coords.second);
        if (accuracy != null) {
            location.setAccuracy(accuracy.floatValue());
        }
        if (altitude != null) {
            location.setAltitude(altitude.floatValue());
        }
        return location;
    }
}
