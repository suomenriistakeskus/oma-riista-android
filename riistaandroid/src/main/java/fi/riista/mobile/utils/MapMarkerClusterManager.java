package fi.riista.mobile.utils;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.LinkedList;
import java.util.List;

public class MapMarkerClusterManager<T extends ClusterItem> extends ClusterManager<T> {

    public MapMarkerClusterManager(final Context context, final GoogleMap map) {
        super(context, map);
    }

    public boolean itemsInSameLocation(final Cluster<T> cluster) {
        final LinkedList<T> items = new LinkedList<>(cluster.getItems());
        final T item = items.remove(0);

        final double longitude = item.getPosition().longitude;
        final double latitude = item.getPosition().latitude;

        for (final T t : items) {
            if (Double.compare(longitude, t.getPosition().longitude) != 0 && Double.compare(latitude, t.getPosition().latitude) != 0) {
                return false;
            }
        }

        return true;
    }

    public void removeItems(final List<T> items) {
        for (final T item : items) {
            removeItem(item);
        }
    }
}
