package fi.riista.common.domain.observation.ui

import fi.riista.common.domain.observation.metadata.dto.ObservationFieldNameDTO
import fi.riista.common.logging.getLogger
import fi.riista.common.ui.dataField.DataFieldId

enum class CommonObservationField(
    val presence: Presence,
    /**
     * Field name in observation metadata.
     */
    val metadataFieldName: String?
): DataFieldId {
    LOCATION(presence = Presence.ALWAYS),
    SPECIES_AND_IMAGE(presence = Presence.ALWAYS),
    DATE_AND_TIME(presence = Presence.ALWAYS),
    OBSERVATION_TYPE(presence = Presence.ALWAYS),

    OBSERVATION_CATEGORY(metadataFieldName = "observationCategory"),
    WITHIN_MOOSE_HUNTING(metadataFieldName = "withinMooseHunting"),
    WITHIN_DEER_HUNTING(metadataFieldName = "withinDeerHunting"),
    DEER_HUNTING_TYPE(metadataFieldName = "deerHuntingType"),
    DEER_HUNTING_OTHER_TYPE_DESCRIPTION(metadataFieldName = "deerHuntingTypeDescription"),
    SPECIMEN_AMOUNT(metadataFieldName = "amount"),
    ERROR_SPECIMEN_AMOUNT_AT_LEAST_TWO(presence = Presence.ERROR),
    // depends on AMOUNT field i.e. should be displayed only if AMOUNT exists
    SPECIMENS(presence = Presence.DEPENDING_ON_METADATA, metadataFieldName = null),
    MOOSE_LIKE_MALE_AMOUNT(metadataFieldName = "mooselikeMaleAmount"),
    MOOSE_LIKE_FEMALE_AMOUNT(metadataFieldName = "mooselikeFemaleAmount"),
    MOOSE_LIKE_FEMALE_1CALF_AMOUNT(metadataFieldName = "mooselikeFemale1CalfAmount"),
    MOOSE_LIKE_FEMALE_2CALFS_AMOUNT(metadataFieldName = "mooselikeFemale2CalfsAmount"),
    MOOSE_LIKE_FEMALE_3CALFS_AMOUNT(metadataFieldName = "mooselikeFemale3CalfsAmount"),
    MOOSE_LIKE_FEMALE_4CALFS_AMOUNT(metadataFieldName = "mooselikeFemale4CalfsAmount"),
    MOOSE_LIKE_CALF_AMOUNT(metadataFieldName = "mooselikeCalfAmount"),
    MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT(metadataFieldName = "mooselikeUnknownSpecimenAmount"),

    // Tassu fields (large carnivore observations)

    // editable, depenging on metadata
    TASSU_VERIFIED_BY_CARNIVORE_AUTHORITY(metadataFieldName = "verifiedByCarnivoreAuthority"),
    TASSU_OBSERVER_NAME(metadataFieldName = "observerName"),
    TASSU_OBSERVER_PHONENUMBER(metadataFieldName = "observerPhoneNumber"),
    TASSU_OFFICIAL_ADDITIONAL_INFO(metadataFieldName = "officialAdditionalInfo"),

    // only to be displayed in read-only mode and only if there's a distance
    TASSU_IN_YARD_DISTANCE_TO_RESIDENCE(presence = Presence.DEPENDING_ON_CURRENT_VALUE),
    // only to be displayed in read-only mode and only observation contains litter
    TASSU_LITTER(presence = Presence.DEPENDING_ON_CURRENT_VALUE),
    // only to be displayed in read-only mode and only observation contains pack
    TASSU_PACK(presence = Presence.DEPENDING_ON_CURRENT_VALUE),

    DESCRIPTION(presence = Presence.ALWAYS),
    ;

    enum class Presence {
        /**
         * Field is always displayed / editable.
         */
        ALWAYS,

        /**
         * Field is only displayed for certain values.
         */
        DEPENDING_ON_CURRENT_VALUE,

        /**
         * Field is always displayed / editable based on current metadata.
         */
        DEPENDING_ON_METADATA,

        /**
         * Field is displayed when observation validation has failed.
         */
        ERROR,
    }

    constructor(presence: Presence):
            this(presence = presence, metadataFieldName = null)
    constructor(metadataFieldName: ObservationFieldNameDTO):
            this(presence = Presence.DEPENDING_ON_METADATA, metadataFieldName = metadataFieldName)

    override fun toInt() = ordinal

    companion object {
        fun fromInt(value: Int): CommonObservationField? {
            return values().getOrNull(value)
        }

        fun create(metadataFieldName: ObservationFieldNameDTO): CommonObservationField? {
            return values()
                .firstOrNull { it.metadataFieldName == metadataFieldName }
                .also { field ->
                    if (field == null) {
                        logger.w { "Failed to convert $metadataFieldName to ObservationField" }
                    }
                }
        }

        internal fun fieldsWithPresence(presence: Presence): List<CommonObservationField> =
            values().filter { it.presence == presence }

        private val logger by getLogger(CommonObservationField::class)
    }
}
