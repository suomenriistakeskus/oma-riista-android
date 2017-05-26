package fi.riista.mobile;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class AppLifecycleHandler implements Application.ActivityLifecycleCallbacks {
    private int resumed;
    private int paused;
    private int started;
    private int stopped;

    private static AppLifecycleHandler sInstance;

    public static AppLifecycleHandler getInstance() {
        if (sInstance == null) {
            sInstance = new AppLifecycleHandler();
        }

        return sInstance;
    }

    private AppLifecycleHandler() {
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumed;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
    }

    public boolean isApplicationVisible() {
        return started > stopped;
    }

    public boolean isApplicationInForeground() {
        return resumed > paused;
    }
}
