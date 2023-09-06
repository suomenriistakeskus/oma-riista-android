package fi.riista.common.domain.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class HarvestReportState(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
): RepresentsBackendEnum, LocalizableEnum {
    SENT_FOR_APPROVAL("SENT_FOR_APPROVAL", RR.string.harvest_report_state_sent_for_approval),
    APPROVED("APPROVED", RR.string.harvest_report_state_approved),
    REJECTED("REJECTED", RR.string.harvest_report_state_rejected),
    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<HarvestReportState> = value.toBackendEnum()
    }
}
