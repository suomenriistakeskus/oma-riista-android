package fi.riista.mobile.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fi.riista.mobile.activity.MainActivity
import fi.riista.mobile.activity.MapAreaListActivity
import fi.riista.mobile.activity.MapSettingsActivity
import fi.riista.mobile.activity.ShootingTestMainActivity
import fi.riista.mobile.feature.harvest.HarvestActivity
import fi.riista.mobile.feature.login.LoginActivity
import fi.riista.mobile.feature.observation.ObservationActivity
import fi.riista.mobile.feature.srva.SrvaActivity
import fi.riista.mobile.feature.unregister.UnregisterUserAccountActivity

@Module
abstract class ActivitiesModule {

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindLoginActivity(): LoginActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindObservationActivity(): ObservationActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindHarvestActivity(): HarvestActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindSrvaActivity(): SrvaActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindMapAreaListActivity(): MapAreaListActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindMapSettingsActivity(): MapSettingsActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindShootingTestMainActivity(): ShootingTestMainActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindUnregisterUserAccountActivity(): UnregisterUserAccountActivity
}
