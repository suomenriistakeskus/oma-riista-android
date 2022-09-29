package fi.riista.common.domain.observation.model

import fi.riista.common.domain.observation.metadata.dto.ObservationSpecimenFieldNameDTO
import fi.riista.common.logging.getLogger

enum class ObservationSpecimenField(
    /**
     * Field name in observation metadata.
     */
    val metadataFieldName: String
) {
    // order is the preferred display order in UI

    GENDER(metadataFieldName = "gender"),
    AGE(metadataFieldName = "age"),
    STATE_OF_HEALTH(metadataFieldName = "state"),
    MARKING(metadataFieldName = "marking"),
    WIDTH_OF_PAW(metadataFieldName = "widthOfPaw"),
    LENGTH_OF_PAW(metadataFieldName = "lengthOfPaw"),
    ;

    companion object {
        fun create(metadataFieldName: ObservationSpecimenFieldNameDTO): ObservationSpecimenField? {
            return values()
                .firstOrNull { it.metadataFieldName == metadataFieldName }
                .also { field ->
                    if (field == null) {
                        logger.w { "Failed to convert $metadataFieldName to ObservationSpecimenField" }
                    }
                }
        }

        private val logger by getLogger(ObservationSpecimenField::class)
    }
}
