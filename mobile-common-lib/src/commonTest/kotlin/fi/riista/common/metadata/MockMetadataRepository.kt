package fi.riista.common.metadata

internal class MockMetadataRepository : MetadataRepository {
    var savedMetadatas = mutableMapOf<MetadataSpecification, String>()

    override fun getMetadataJson(specification: MetadataSpecification): String? {
        return savedMetadatas[specification]
    }

    override suspend fun saveMetadataJson(specification: MetadataSpecification, metadataJson: String) {
        savedMetadatas[specification] = metadataJson
    }
}
