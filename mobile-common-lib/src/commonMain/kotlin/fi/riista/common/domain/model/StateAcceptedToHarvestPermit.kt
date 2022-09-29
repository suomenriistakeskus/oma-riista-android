package fi.riista.common.domain.model

import fi.riista.common.model.RepresentsBackendEnum

enum class StateAcceptedToHarvestPermit(override val rawBackendEnumValue: String) : RepresentsBackendEnum {
    PROPOSED("PROPOSED"),
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED"),
    ;
}
