package fi.riista.common.domain.groupHunting.ui.diary

import kotlinx.serialization.Serializable

/**
 * Filter diary events by type (harvest/observation) and/or by accept status (proposed/accepted/rejected)
 */
@Serializable
data class DiaryFilter(val eventType: EventType, val acceptStatus: AcceptStatus) {

    enum class EventType {
        OBSERVATIONS,
        HARVESTS,
        ALL,
    }

    enum class AcceptStatus {
        ACCEPTED,
        PROPOSED,
        REJECTED,
        ALL,
    }
}
