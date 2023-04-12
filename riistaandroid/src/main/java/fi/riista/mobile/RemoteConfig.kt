package fi.riista.mobile

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import fi.riista.common.reactive.AppObservable
import fi.riista.common.reactive.Observable

interface RemoteConfigFetchedListener {
    fun remoteConfigFetched()
}

object RemoteConfig {

    /**
     * Has the remote config been fetched?
     */
    val remoteConfigFetched: AppObservable<Boolean> = AppObservable(initialValue = false)

    init {
        val remoteConfig = Firebase.remoteConfig
        if (BuildConfig.FLAVOR == "dev") {
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0
            }
            remoteConfig.setConfigSettingsAsync(configSettings)
        }
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    @JvmStatic
    fun fetchAndActivate(listener: RemoteConfigFetchedListener) {
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener {
            listener.remoteConfigFetched()

            remoteConfigFetched.set(true)
        }
    }

    @JvmStatic
    val remoteSettingsForRiistaSdk: String
        get() = Firebase.remoteConfig["riista_sdk_remote_settings"].asString()

    @JvmStatic
    val appStartupMessage: String
        get() = Firebase.remoteConfig["app_startup_message"].asString()

    @JvmStatic
    val harvestSeasonOverrides: String
        get() = Firebase.remoteConfig["harvest_season_overrides"].asString()

    @JvmStatic
    val groupHuntingIntroMessage: String
        get() = Firebase.remoteConfig["group_hunting_intro_message"].asString()

    @JvmStatic
    val ssnPattern: String
        get() = Firebase.remoteConfig["ssn_pattern"].asString()
}
