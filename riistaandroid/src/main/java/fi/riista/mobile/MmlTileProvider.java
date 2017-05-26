package fi.riista.mobile;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import fi.riista.mobile.utils.AppPreferences;

/**
 * Tile provider for fetching MML map tiles from Riista map server
 */
class MmlTileProvider implements TileProvider {

    private static final String REFERER_KEY = "Referer";
    private static final String REFERER_VALUE = "https://oma.riista.fi";
    private static final String MML_TOPOGRAPHIC_TILE_URL_FORMAT = "http://kartta.riista.fi/tms/1.0.0/maasto_mobile/EPSG_3857/%d/%d/%d.png";
    private static final String MML_AERIAL_TILE_URL_FORMAT = "http://kartta.riista.fi/tms/1.0.0/orto_mobile/EPSG_3857/%d/%d/%d.png";
    private static final String MML_BACKGROUND_TILE_URL_FORMAT = "http://kartta.riista.fi/tms/1.0.0/tausta_mobile/EPSG_3857/%d/%d/%d.png";

    private final int mWidth;
    private final int mHeight;

    // URL format for currently selected map type
    private String mUrlFormat = MML_TOPOGRAPHIC_TILE_URL_FORMAT;

    MmlTileProvider(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    void setMapType(AppPreferences.MapTileSource mapType) {
        if (mapType == AppPreferences.MapTileSource.MML_AERIAL) {
            mUrlFormat = MML_AERIAL_TILE_URL_FORMAT;
        } else if (mapType == AppPreferences.MapTileSource.MML_BACKGROUND) {
            mUrlFormat = MML_BACKGROUND_TILE_URL_FORMAT;
        } else {
            mUrlFormat = MML_TOPOGRAPHIC_TILE_URL_FORMAT;
        }
    }

    private synchronized URL getTileUrl(int x, int y, int zoom) {
        String s = String.format(mUrlFormat, zoom, x, tmsConvert(y, zoom));
        URL url;
        try {
            url = new URL(s);
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }

        return url;
    }

    private int tmsConvert(int y, int zoom) {
        return (1 << zoom) - y - 1;
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        URL tileUrl = this.getTileUrl(x, y, zoom);
        if (tileUrl == null) {
            return NO_TILE;
        } else {
            Tile tile;
            InputStream in = null;

            try {
                HttpURLConnection urlConnection = (HttpURLConnection) tileUrl.openConnection();
                urlConnection.setRequestProperty(REFERER_KEY, REFERER_VALUE);
                in = new BufferedInputStream(urlConnection.getInputStream());

                tile = new Tile(this.mWidth, this.mHeight, IOUtils.toByteArray(in));
            } catch (IOException e) {
                tile = null;
            } finally {
                if (in != null) try {
                    in.close();
                } catch (Exception ignored) {
                }
            }

            return tile;
        }
    }
}
