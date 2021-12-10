package fi.riista.mobile.di

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import fi.riista.mobile.RiistaApplication
import fi.vincit.androidutilslib.application.WorkApplication
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    ActivitiesModule::class,
    FragmentModule::class
])
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: WorkApplication): Builder

        fun build(): AppComponent
    }

    fun inject(application: RiistaApplication)
}
