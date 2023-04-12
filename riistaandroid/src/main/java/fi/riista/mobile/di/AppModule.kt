package fi.riista.mobile.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import dagger.Module
import dagger.Provides
import fi.riista.mobile.database.HarvestDatabase
import fi.riista.mobile.database.PermitManager
import fi.riista.mobile.database.SpeciesInformation
import fi.riista.mobile.database.SpeciesResolver
import fi.riista.mobile.database.room.MetsahallitusPermitDAO
import fi.riista.mobile.database.room.Migrations.MIGRATION_1_2
import fi.riista.mobile.database.room.RiistaDatabase
import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_CONTEXT_NAME
import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME
import fi.riista.mobile.network.AppDownloadManager
import fi.riista.mobile.sync.AppSync
import fi.riista.mobile.utils.Authenticator
import fi.riista.mobile.utils.AuthenticatorImpl
import fi.riista.mobile.utils.BackgroundOperationStatus
import fi.riista.mobile.utils.CookieStoreSingleton
import fi.riista.mobile.utils.CredentialsStore
import fi.riista.mobile.utils.CredentialsStoreImpl
import fi.riista.mobile.utils.LogoutHelper
import fi.riista.mobile.utils.ROOM_DATABASE_NAME
import fi.riista.mobile.utils.UserInfoConverter
import fi.riista.mobile.utils.UserInfoStore
import fi.riista.mobile.utils.UserInfoStoreImpl
import fi.vincit.androidutilslib.application.WorkApplication
import fi.vincit.androidutilslib.context.WorkContext
import fi.vincit.androidutilslib.network.SynchronizedCookieStore
import fi.vincit.androidutilslib.util.JsonSerializator
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {

    @Provides
    fun application(application: WorkApplication): Application = application

    @Provides
    @Named(APPLICATION_CONTEXT_NAME)
    fun applicationContext(application: WorkApplication): Context = application.applicationContext

    @Provides
    @Named(APPLICATION_WORK_CONTEXT_NAME)
    fun applicationWorkContext(application: WorkApplication): WorkContext = application.workContext

    @Singleton
    @Provides
    fun db(@Named(APPLICATION_CONTEXT_NAME) appContext: Context): RiistaDatabase {
        return Room
                .databaseBuilder(appContext, RiistaDatabase::class.java, ROOM_DATABASE_NAME)
                .addMigrations(MIGRATION_1_2)

                // Will delete all data from Room database table if a migration is missing!
                //.fallbackToDestructiveMigration()

                .build()
    }

    @Singleton
    @Provides
    fun metsahallitusPermitDAO(db: RiistaDatabase): MetsahallitusPermitDAO {
        return db.metsahallitusPermitDao()
    }


    @Singleton
    @Provides
    fun defaultObjectMapper(): ObjectMapper {
        val mapper: ObjectMapper = JsonSerializator.createDefaultMapper()
        mapper.registerModule(JodaModule())
        return mapper
    }

    @Singleton
    @Provides
    fun speciesResolver() = SpeciesResolver { SpeciesInformation.getSpecies(it) }

    @Singleton
    @Provides
    fun cookieStore(): SynchronizedCookieStore = CookieStoreSingleton.INSTANCE.cookieStore

    @Singleton
    @Provides
    fun credentialsStore(@Named(APPLICATION_CONTEXT_NAME) appContext: Context): CredentialsStore {
        return CredentialsStoreImpl(appContext)
    }

    @Singleton
    @Provides
    fun userInfoStore(@Named(APPLICATION_CONTEXT_NAME) appContext: Context,
                      userInfoConverter: UserInfoConverter): UserInfoStore {

        return UserInfoStoreImpl(appContext, userInfoConverter)
    }

    @Singleton
    @Provides
    fun authenticator(
        @Named(APPLICATION_WORK_CONTEXT_NAME) appWorkContext: WorkContext,
        credentialsStore: CredentialsStore,
        userInfoStore: UserInfoStore,
    ): Authenticator {

        return AuthenticatorImpl(
            appWorkContext,
            credentialsStore,
            userInfoStore,
        )
    }

    @Singleton
    @Provides
    fun appDownloadManager(@Named(APPLICATION_CONTEXT_NAME) appContext: Context): AppDownloadManager {
        return AppDownloadManager(appContext)
    }

    @Singleton
    @Provides
    fun backgroundOperationStatus(): BackgroundOperationStatus {
        return BackgroundOperationStatus()
    }

    @Singleton
    @Provides
    fun logoutHelper(
        appSync: AppSync,
        harvestDatabase: HarvestDatabase,
        credentialsStore: CredentialsStore,
        permitManager: PermitManager,
    ): LogoutHelper {
        return LogoutHelper(
            appSync = appSync,
            harvestDatabase = harvestDatabase,
            credentialsStore = credentialsStore,
            permitManager = permitManager,
        )
    }
}
