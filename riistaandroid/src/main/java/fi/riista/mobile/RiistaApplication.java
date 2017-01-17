package fi.riista.mobile;

import android.content.Context;
import android.support.multidex.MultiDex;

import fi.riista.mobile.observation.ObservationDatabase;
import fi.riista.mobile.observation.ObservationMetadataHelper;
import fi.riista.mobile.srva.SrvaDatabase;
import fi.riista.mobile.srva.SrvaParametersHelper;
import fi.vincit.androidutilslib.application.WorkApplication;
import fi.vincit.androidutilslib.config.AndroidUtilsLibConfig;

public class RiistaApplication extends WorkApplication {

    private static RiistaApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        adjustBitmapCache();

        ObservationMetadataHelper.init(getWorkContext());
        ObservationDatabase.init(this);
        SrvaDatabase.init(this);

        SrvaParametersHelper.init(getWorkContext());
    }

    public static RiistaApplication getInstance() {
        return sInstance;
    }

    private void adjustBitmapCache() {
        //Increase default in-memory bitmap cache size
        AndroidUtilsLibConfig.Cache.Bitmap.DEFAULT_BITMAP_CACHE_SIZE = 1048576 * 5;
        AndroidUtilsLibConfig.Cache.Bitmap.DEFAULT_MAX_BITMAP_SIZE = 1024 * 512;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
