package fi.riista.common.metadata

import co.touchlab.stately.concurrency.AtomicReference

abstract class MetadataMemoryCache<Metadata : Any> internal constructor(
    private val metadataSpecification: MetadataSpecification,
    private val metadataRepository: MetadataRepository,
) {
    private var _cachedMetadata = AtomicReference<Metadata?>(null)
    val cachedMetadata: Metadata?
        get() {
            return when (val currentMetadata = _cachedMetadata.get()) {
                null -> {
                    val storedMetadata: Metadata? = metadataRepository
                        .getMetadataJson(metadataSpecification)
                        ?.deserializeJsonToMetadata()

                    _cachedMetadata.set(storedMetadata)

                    storedMetadata
                }
                else -> currentMetadata
            }
        }

    fun storeMetadata(metadata: Metadata) {
        if (cachedMetadata == metadata) {
            return
        }

        _cachedMetadata.set(metadata)
        metadata.serializeMetadataToJson()?.let { metadataJson ->
            metadataRepository.saveMetadataJson(
                specification = metadataSpecification,
                metadataJson = metadataJson
            )
        }
    }

    protected abstract fun String.deserializeJsonToMetadata(): Metadata?
    protected abstract fun Metadata.serializeMetadataToJson(): String?
}