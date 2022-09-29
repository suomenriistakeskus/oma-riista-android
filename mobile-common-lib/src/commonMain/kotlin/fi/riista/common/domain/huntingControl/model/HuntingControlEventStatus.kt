package fi.riista.common.domain.huntingControl.model

import fi.riista.common.model.RepresentsBackendEnum

enum class HuntingControlEventStatus(
    override val rawBackendEnumValue: String,
): RepresentsBackendEnum {
    PROPOSED("PROPOSED"),
    REJECTED("REJECTED"),
    ACCEPTED("ACCEPTED"),
    ACCEPTED_SUBSIDIZED("ACCEPTED_SUBSIDIZED"),
}
