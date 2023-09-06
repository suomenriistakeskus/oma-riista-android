package fi.riista.common.domain.harvest.model

enum class HarvestReportingType {
    BASIC,
    SEASON,
    PERMIT,

    // in backend there's also hunting day reporting type but that's not currently
    // applicable on mobile
}
