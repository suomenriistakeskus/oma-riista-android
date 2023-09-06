package fi.riista.common.domain.harvest.model

import fi.riista.common.domain.model.HarvestReportState
import fi.riista.common.domain.model.StateAcceptedToHarvestPermit
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR
import fi.riista.common.ui.dataField.IndicatorColor

enum class HarvestState(
    override val resourcesStringId: RR.string
): LocalizableEnum {
    REPORT_REQUIRED(RR.string.harvest_report_required),
    REPORT_SENT_FOR_APPROVAL(RR.string.harvest_report_state_sent_for_approval),
    REPORT_APPROVED(RR.string.harvest_report_state_approved),
    REPORT_REJECTED(RR.string.harvest_report_state_rejected),
    PERMIT_ACCEPTED(RR.string.harvest_permit_accepted),
    PERMIT_PROPOSED(RR.string.harvest_permit_proposed),
    PERMIT_REJECTED(RR.string.harvest_permit_rejected),
    ;

    companion object {
        fun combinedState(
            harvestReportState: HarvestReportState?,
            stateAcceptedToHarvestPermit: StateAcceptedToHarvestPermit?,
            harvestReportRequired: Boolean?
        ): HarvestState? {
            return harvestReportState?.asHarvestState()
                ?: stateAcceptedToHarvestPermit?.asHarvestState()
                ?: when (harvestReportRequired) {
                    true -> REPORT_REQUIRED
                    false,
                    null -> null
                }
        }
    }
}

val HarvestState.indicatorColor: IndicatorColor
    get() {
        return when (this) {
            HarvestState.REPORT_SENT_FOR_APPROVAL,
            HarvestState.PERMIT_PROPOSED -> IndicatorColor.YELLOW
            HarvestState.REPORT_APPROVED,
            HarvestState.PERMIT_ACCEPTED -> IndicatorColor.GREEN
            HarvestState.REPORT_REQUIRED,
            HarvestState.REPORT_REJECTED,
            HarvestState.PERMIT_REJECTED -> IndicatorColor.RED
        }
    }

private fun HarvestReportState.asHarvestState(): HarvestState =
    when (this) {
        HarvestReportState.SENT_FOR_APPROVAL -> HarvestState.REPORT_SENT_FOR_APPROVAL
        HarvestReportState.APPROVED -> HarvestState.REPORT_APPROVED
        HarvestReportState.REJECTED -> HarvestState.REPORT_REJECTED
    }

private fun StateAcceptedToHarvestPermit.asHarvestState(): HarvestState =
    when (this) {
        StateAcceptedToHarvestPermit.PROPOSED -> HarvestState.PERMIT_PROPOSED
        StateAcceptedToHarvestPermit.ACCEPTED -> HarvestState.PERMIT_ACCEPTED
        StateAcceptedToHarvestPermit.REJECTED -> HarvestState.PERMIT_REJECTED
    }
