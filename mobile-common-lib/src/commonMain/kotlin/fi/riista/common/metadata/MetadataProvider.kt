package fi.riista.common.metadata

import fi.riista.common.database.DatabaseDriverFactory
import fi.riista.common.domain.observation.metadata.ObservationMetadataProvider
import fi.riista.common.domain.observation.metadata.model.ObservationMetadata
import fi.riista.common.domain.srva.metadata.SrvaMetadataProvider
import fi.riista.common.domain.srva.metadata.model.SrvaMetadata
import fi.riista.common.domain.userInfo.CurrentUserContextProvider
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.network.sync.SynchronizationService
import fi.riista.common.preferences.Preferences
import fi.riista.common.util.LocalDateTimeProvider

interface MetadataProvider {
    val srvaMetadata: SrvaMetadata
    val observationMetadata: ObservationMetadata
}

/**
 * A class that acts as an access point to various metadata (e.g. SRVA metadata) as
 * well as an entry point to updating metadata.
 */
internal class DefaultMetadataProvider internal constructor(
    private val databaseDriverFactory: DatabaseDriverFactory,
    private val backendApiProvider: BackendApiProvider,
    private val preferences: Preferences,
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val synchronizationService: SynchronizationService,
    private val currentUserContextProvider: CurrentUserContextProvider,
): MetadataProvider {
    override val srvaMetadata: SrvaMetadata
        get() = srvaMetadataProvider.metadata
    override val observationMetadata: ObservationMetadata
        get() = observationMetadataProvider.metadata


    private val metadataRepository: MetadataRepository by lazy {
        MetadataDatabaseRepository(databaseDriverFactory = databaseDriverFactory)
    }

    private val srvaMetadataProvider: SrvaMetadataProvider by lazy {
        SrvaMetadataProvider(
            metadataRepository = metadataRepository,
            backendApiProvider = backendApiProvider,
            currentUserContextProvider = currentUserContextProvider,
            preferences = preferences,
            localDateTimeProvider = localDateTimeProvider,
        )
    }

    private val observationMetadataProvider: ObservationMetadataProvider by lazy {
        ObservationMetadataProvider(
            metadataRepository = metadataRepository,
            backendApiProvider = backendApiProvider,
            currentUserContextProvider = currentUserContextProvider,
            preferences = preferences,
            localDateTimeProvider = localDateTimeProvider,
        )
    }

    internal fun initialize() {
        synchronizationService.registerSynchronizationContext(synchronizationContext = srvaMetadataProvider)
        synchronizationService.registerSynchronizationContext(synchronizationContext = observationMetadataProvider)
    }
}
