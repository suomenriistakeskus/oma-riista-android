package fi.riista.common.domain.srva.metadata

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.srva.metadata.model.SrvaEventCategory
import fi.riista.common.domain.srva.metadata.model.SrvaMetadata
import fi.riista.common.domain.srva.model.SrvaEventResult
import fi.riista.common.domain.srva.model.SrvaEventType
import fi.riista.common.metadata.MetadataMemoryCache
import fi.riista.common.metadata.MetadataRepository
import fi.riista.common.metadata.MetadataSpecification
import fi.riista.common.model.toBackendEnum
import fi.riista.common.domain.srva.model.CommonSrvaTypeDetail
import fi.riista.common.domain.srva.model.SrvaEventResultDetail
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import kotlinx.serialization.Serializable

class SrvaMetadataCache internal constructor(
    metadataSpecification: MetadataSpecification,
    metadataRepository: MetadataRepository,
): MetadataMemoryCache<SrvaMetadata>(metadataSpecification, metadataRepository) {
    internal constructor(metadataRepository: MetadataRepository)
            : this(
        metadataSpecification = MetadataSpecification(
            metadataType = MetadataRepository.MetadataType.SRVA,
            metadataSpecVersion = Constants.SRVA_SPEC_VERSION.toLong(),
            metadataJsonFormatVersion = CachedSrvaMetadata.METADATA_JSON_FORMAT_VERSION
        ),
        metadataRepository = metadataRepository
    )

    override fun String.deserializeJsonToMetadata(): SrvaMetadata? {
        return deserializeFromJson<CachedSrvaMetadata>()?.toSrvaMetadata()
    }

    override fun SrvaMetadata.serializeMetadataToJson(): String? {
        return toCachedFormat().serializeToJson()
    }
}


/**
 * A custom format for storing SRVA metadata to cache (= repository == database).
 * Keep the format separate from [SrvaMetadata] in order to not accidentally
 * prevent stored (db) jsons from being
 */
@Serializable
private data class CachedSrvaMetadata(
    val speciesCodes: List<SpeciesCode>,
    val ages: List<String>,
    val genders: List<String>,
    val eventCategories: List<CachedSrvaEventCategory>,
) {
    companion object {
        // format version, remember to bump if making changes
        const val METADATA_JSON_FORMAT_VERSION = 1L
    }
}

@Serializable
private data class CachedSrvaEventCategory(
    val categoryType: String?,
    val possibleEventTypes: List<String>,
    val possibleEventTypeDetails: Map<String, List<CachedSrvaEventTypeDetail>>,
    val possibleEventResults: List<String>,
    val possibleEventResultDetails: Map<String, List<String>>,
    val possibleMethods: List<String>,
)

@Serializable
private data class CachedSrvaEventTypeDetail(
    val detailType: String,
    val speciesCodes: List<SpeciesCode>,
)

private fun SrvaMetadata.toCachedFormat() =
    CachedSrvaMetadata(
        speciesCodes = species.map { it.speciesCode },
        ages = ages.mapNotNull { it.rawBackendEnumValue },
        genders = genders.mapNotNull { it.rawBackendEnumValue },
        eventCategories = eventCategories.map { it.toCachedFormat() },
    )

private fun SrvaEventCategory.toCachedFormat() =
    CachedSrvaEventCategory(
        categoryType = categoryType.rawBackendEnumValue,
        possibleEventTypes = possibleEventTypes.mapNotNull { it.rawBackendEnumValue },
        possibleEventTypeDetails = possibleEventTypeDetails.mapNotNull { entry ->
            entry.key.rawBackendEnumValue?.let { eventType ->
                eventType to entry.value.mapNotNull { detail ->
                    detail.detailType.rawBackendEnumValue?.let {
                        CachedSrvaEventTypeDetail(detailType = it, speciesCodes = detail.speciesCodes)
                    }
                }
            }
        }.toMap(),
        possibleEventResults = possibleEventResults.mapNotNull { it.rawBackendEnumValue },
        possibleEventResultDetails = possibleEventResultDetails.mapNotNull { entry ->
            entry.key.rawBackendEnumValue?.let { resultType ->
                resultType to entry.value.mapNotNull { it.rawBackendEnumValue }
            }
        }.toMap(),
        possibleMethods = possibleMethods.mapNotNull { it.rawBackendEnumValue },
    )

private fun CachedSrvaMetadata.toSrvaMetadata() =
    SrvaMetadata(
        species = speciesCodes.map { Species.Known(it) },
        ages = ages.map { it.toBackendEnum() },
        genders = genders.map { it.toBackendEnum() },
        eventCategories = eventCategories.map { it.toSrvaEventCategory() },
    )

private fun CachedSrvaEventCategory.toSrvaEventCategory() =
    SrvaEventCategory(
        categoryType = categoryType.toBackendEnum(),
        possibleEventTypes = possibleEventTypes.map { it.toBackendEnum() },
        possibleEventTypeDetails = possibleEventTypeDetails.map { entry ->
            entry.key.toBackendEnum<SrvaEventType>() to entry.value.map {
                CommonSrvaTypeDetail(
                    detailType = it.detailType.toBackendEnum(),
                    speciesCodes = it.speciesCodes,
                )
            }
        }.toMap(),
        possibleEventResults = possibleEventResults.map { it.toBackendEnum() },
        possibleEventResultDetails = possibleEventResultDetails.map { entry ->
            entry.key.toBackendEnum<SrvaEventResult>() to entry.value.map {
                it.toBackendEnum<SrvaEventResultDetail>()
            }
        }.toMap(),
        possibleMethods = possibleMethods.map { it.toBackendEnum() },
    )
