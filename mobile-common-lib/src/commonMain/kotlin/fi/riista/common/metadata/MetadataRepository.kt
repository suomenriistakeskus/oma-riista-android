package fi.riista.common.metadata

import fi.riista.common.database.DatabaseWriteContext
import fi.riista.common.database.DatabaseDriverFactory
import fi.riista.common.database.RiistaDatabase
import kotlinx.coroutines.withContext

internal data class MetadataSpecification(
    val metadataType: MetadataRepository.MetadataType,
    val metadataSpecVersion: Long,

    // allows updating internal DTO <-> json conversion without bumping spec version
    val metadataJsonFormatVersion: Long,
)

internal interface MetadataRepository {
    enum class MetadataType {
        SRVA,
        OBSERVATION,
    }

    fun getMetadataJson(specification: MetadataSpecification): String?
    suspend fun saveMetadataJson(specification: MetadataSpecification, metadataJson: String)
}


internal class MetadataDatabaseRepository(
    databaseDriverFactory: DatabaseDriverFactory
): MetadataRepository {
    private val database = RiistaDatabase(
        driver = databaseDriverFactory.createDriver(),
    )

    private val metadataQueries = database.dbMetadataQueries

    override fun getMetadataJson(specification: MetadataSpecification): String? {
        val metadataList = metadataQueries.selectMetadata(
            metadata_type = specification.metadataType.metadataTypeKey,
            metadata_spec_version = specification.metadataSpecVersion,
            metadata_format_version = specification.metadataJsonFormatVersion,
        ).executeAsList()

        return metadataList.firstOrNull()?.metadata_json
    }

    override suspend fun saveMetadataJson(
        specification: MetadataSpecification,
        metadataJson: String,
    ) = withContext(DatabaseWriteContext) {
        metadataQueries.transaction {
            val currentMetadata = getMetadataJson(specification)
            if (currentMetadata == null) {
                metadataQueries.insertMetadata(
                    metadata_type = specification.metadataType.metadataTypeKey,
                    metadata_spec_version = specification.metadataSpecVersion,
                    metadata_format_version = specification.metadataJsonFormatVersion,
                    metadata_json = metadataJson
                )
            } else if (currentMetadata != metadataJson) {
                metadataQueries.updateMetadata(
                    metadata_type = specification.metadataType.metadataTypeKey,
                    metadata_spec_version = specification.metadataSpecVersion,
                    metadata_format_version = specification.metadataJsonFormatVersion,
                    metadata_json = metadataJson
                )
            }
        }
    }

    // for database
    private val MetadataRepository.MetadataType.metadataTypeKey: String
        get() {
            return when (this) {
                MetadataRepository.MetadataType.SRVA -> "SRVA"
                MetadataRepository.MetadataType.OBSERVATION -> "OBSERVATION"
            }
        }
}
