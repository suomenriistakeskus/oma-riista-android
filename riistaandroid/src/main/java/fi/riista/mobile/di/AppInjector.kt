package fi.riista.mobile.di

import fi.riista.mobile.RiistaApplication

object AppInjector {

    @JvmStatic
    fun init(riistaApp: RiistaApplication) {
        val appComponent = DaggerAppComponent.builder()
                .application(riistaApp)
                .build()

        appComponent.inject(riistaApp)
    }
}
