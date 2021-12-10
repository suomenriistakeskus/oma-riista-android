package fi.riista.common.model

import kotlin.jvm.JvmStatic

enum class HarvestReportState(override val rawBackendEnumValue: String): RepresentsBackendEnum {
    SENT_FOR_APPROVAL("SENT_FOR_APPROVAL"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    ;
}
