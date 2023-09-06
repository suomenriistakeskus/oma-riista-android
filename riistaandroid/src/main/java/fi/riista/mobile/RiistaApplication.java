package fi.riista.mobile;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import net.danlew.android.joda.JodaTimeAndroid;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import fi.riista.common.RiistaSDK;
import fi.riista.common.RiistaSdkBuilder;
import fi.riista.common.RiistaSdkBuilderKt;
import fi.riista.common.remoteSettings.RemoteSettingsDTO;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.di.AppInjector;
import fi.riista.mobile.srva.SrvaDatabase;
import fi.riista.mobile.storage.StorageDatabase;
import fi.riista.mobile.utils.AppPreferences;
import fi.riista.mobile.utils.BuildInfo;
import fi.riista.mobile.utils.Credentials;
import fi.riista.mobile.utils.CredentialsStore;
import fi.riista.mobile.utils.JsonUtils;
import fi.riista.mobile.utils.LocaleUtil;
import fi.riista.mobile.utils.UserInfoStore;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.application.WorkApplication;
import fi.vincit.androidutilslib.config.AndroidUtilsLibConfig;
import fi.vincit.androidutilslib.util.JsonSerializator;

public class RiistaApplication extends WorkApplication implements HasAndroidInjector {

    public static boolean sDidCheckVersion = false;

    private static RiistaApplication sInstance;

    @Inject
    DispatchingAndroidInjector<Object> dispatchingAndroidInjector;

    @Inject
    AppLifecycleHandler appLifecycleHandler;

    @Inject
    CredentialsStore credentialsStore;

    @Inject
    UserInfoStore userInfoStore;

    @Inject
    ObjectMapper objectMapper;

    public static RiistaApplication getInstance() {
        return sInstance;
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return dispatchingAndroidInjector;
    }

    // TODO Should not exist here but was introduced because DI is not yet implemented everywhere.
    public CredentialsStore getCredentialsStore() {
        return credentialsStore;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Required by Android-specialized version of Joda-Time library.
        JodaTimeAndroid.init(this);

        AppInjector.init(this);

        // Set server base address. On test builds user can change it.
        final String prefServerAddress = AppPreferences.getServerBaseAddress(this);
        if (BuildInfo.isTestBuild() && prefServerAddress != null) {
            AppConfig.initializeBaseAddress(prefServerAddress);
        } else {
            AppConfig.initializeBaseAddress(BuildConfig.SERVER_ADDRESS);
        }

        // RiistaSDK may utilize species information immediately after launch. This is the
        // case when app is killed in the background and we're creating a new app instance
        // which is then restored directly to group hunting activities/fragments
        // -> we don't go to MainActivity which would typically refresh SpeciesInformation
        SpeciesInformation.refreshInfo(this);

        // initialize after injecting dependencies. RiistaSDK utilizes e.g. credentialsStore
        initializeRiistaSDK();

        registerActivityLifecycleCallbacks(appLifecycleHandler);

        sInstance = this;

        adjustBitmapCache();

        // Register Joda-enabled mapper before most other initialization dependent on JSON serialization.
        JsonSerializator.setDefaultMapper(objectMapper);
        JsonUtils.setMapper(objectMapper);

        SrvaDatabase.init(this, userInfoStore);
        StorageDatabase.init(this, userInfoStore);
    }

    private void initializeRiistaSDK() {
        PackageInfo packageInfo = Utils.getAppPackageInfo(this);

        final String versionName = packageInfo != null ? packageInfo.versionName : BuildConfig.VERSION_NAME;
        final int versionCode = packageInfo != null ? packageInfo.versionCode : BuildConfig.VERSION_CODE;

        final RiistaSdkBuilder builder = RiistaSdkBuilder.with(
                        versionName,
                        String.valueOf(versionCode),
                        AppConfig.getBaseAddress(),
                        this::recordExceptionToCrashlytics
                )
                .registerApplicationContext(getApplicationContext());

        RiistaSdkBuilderKt.setAllowRedirectsToAbsoluteHosts(builder, BuildInfo.isTestBuild())
                .initializeRiistaSDK();

        final Credentials credentials = credentialsStore.get();
        if (credentials != null) {
            RiistaSDK.INSTANCE.setLoginCredentials(credentials.getUsername(), credentials.getPassword());
        }

        RemoteConfig.fetchAndActivate(() -> {
            final RemoteSettingsDTO remoteSettings = RiistaSDK.remoteSettings()
                    .parseRemoteSettingsJson(RemoteConfig.getRemoteSettingsForRiistaSdk());
            if (remoteSettings != null) {
                // At this point remote settings can be changed before giving them to SDK
                final RemoteSettingsDTO updatedSettings = RemoteSettingsUpdater.updateRiistaSdkSettings(remoteSettings);
                RiistaSDK.remoteSettings().updateWithRemoteSettings(updatedSettings);
            }

            RiistaSDK.groupHuntingIntroMessageHandler()
                    .parseMessageFromJson(RemoteConfig.getGroupHuntingIntroMessage());

            RiistaSDK.getAppStartupMessageHandler()
                    .parseAppStartupMessageFromJson(RemoteConfig.getAppStartupMessage());

            RiistaSDK.getMapTileVersions().parseMapTileVersions(RemoteConfig.getMapTileVersions());

            RiistaSDK.getCommonFileProvider().removeTemporaryFiles();
        });
    }

    private void recordExceptionToCrashlytics(final Throwable throwable, final String message) {
        if (message != null) {
            FirebaseCrashlytics.getInstance().log(message);
        }
        FirebaseCrashlytics.getInstance().recordException(throwable);
    }

    private void adjustBitmapCache() {
        // Increase default in-memory bitmap cache size.
        AndroidUtilsLibConfig.Cache.Bitmap.DEFAULT_BITMAP_CACHE_SIZE = 1048576 * 5;
        AndroidUtilsLibConfig.Cache.Bitmap.DEFAULT_MAX_BITMAP_SIZE = 1024 * 512;
    }

    @Override
    protected void attachBaseContext(final Context base) {
        super.attachBaseContext(LocaleUtil.setupLocale(base));
    }
}
