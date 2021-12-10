package fi.riista.mobile.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fi.riista.mobile.activity.EditActivity
import fi.riista.mobile.activity.HarvestActivity
import fi.riista.mobile.activity.LoginActivity
import fi.riista.mobile.activity.MainActivity
import fi.riista.mobile.activity.MapAreaListActivity
import fi.riista.mobile.activity.MapSettingsActivity
import fi.riista.mobile.activity.ObservationSpecimensActivity
import fi.riista.mobile.activity.ShootingTestMainActivity

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
    abstract fun bindHarvestActivity(): HarvestActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindEditActivity(): EditActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindObservationSpecimensActivity(): ObservationSpecimensActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindMapAreaListActivity(): MapAreaListActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindMapSettingsActivity(): MapSettingsActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindShootingTestMainActivity(): ShootingTestMainActivity

}
