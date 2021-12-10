package fi.riista.mobile

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

interface RemoteConfigFetchedListener {
    fun remoteConfigFetched()
}

object RemoteConfig {

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
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { listener.remoteConfigFetched() }
    }

    @JvmStatic
    val remoteSettingsForRiistaSdk: String
        get() = Firebase.remoteConfig["riista_sdk_remote_settings"].asString()

    @JvmStatic
    val harvestSeasonOverrides: String
        get() = Firebase.remoteConfig["harvest_season_overrides"].asString()

    @JvmStatic
    val groupHuntingIntroMessage: String
        get() = Firebase.remoteConfig["group_hunting_intro_message"].asString()
}
