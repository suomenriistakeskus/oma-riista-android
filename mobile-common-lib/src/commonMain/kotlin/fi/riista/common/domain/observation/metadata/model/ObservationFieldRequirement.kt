package fi.riista.common.domain.observation.metadata.model

import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.ui.dataField.FieldRequirement

enum class ObservationFieldRequirement(
    override val rawBackendEnumValue: String
): RepresentsBackendEnum {
    YES("YES"),
    NO("NO"),
    VOLUNTARY("VOLUNTARY"),
    VOLUNTARY_CARNIVORE_AUTHORITY("VOLUNTARY_CARNIVORE_AUTHORITY"),
    ;

    fun toFieldRequirement(isCarnivoreAuthority: Boolean): FieldRequirement? {
        return when (this) {
            YES -> FieldRequirement.required()
            NO -> null
            VOLUNTARY -> FieldRequirement.voluntary()
            VOLUNTARY_CARNIVORE_AUTHORITY -> if (isCarnivoreAuthority) {
                FieldRequirement.voluntary()
            } else {
                null
            }
        }
    }
}
