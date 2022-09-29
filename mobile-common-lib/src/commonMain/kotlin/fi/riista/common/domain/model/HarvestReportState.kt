package fi.riista.common.domain.model

import fi.riista.common.model.RepresentsBackendEnum

enum class HarvestReportState(override val rawBackendEnumValue: String): RepresentsBackendEnum {
    SENT_FOR_APPROVAL("SENT_FOR_APPROVAL"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    ;
}
