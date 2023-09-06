package fi.riista.common.domain.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum

enum class StateAcceptedToHarvestPermit(
    override val rawBackendEnumValue: String,
) : RepresentsBackendEnum {
    PROPOSED("PROPOSED"),
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED"),
    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<StateAcceptedToHarvestPermit> = value.toBackendEnum()
    }
}
