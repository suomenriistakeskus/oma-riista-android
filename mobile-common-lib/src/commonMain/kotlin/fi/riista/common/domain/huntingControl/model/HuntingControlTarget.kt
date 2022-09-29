package fi.riista.common.domain.huntingControl.model

import fi.riista.common.domain.model.OrganizationId

sealed class HuntingControlTarget

data class HuntingControlRhyTarget(
    override val rhyId: OrganizationId,
) : HuntingControlTarget(), IdentifiesRhy

data class HuntingControlEventTarget(
    override val rhyId: OrganizationId,
    override val eventId: HuntingControlEventId,
) : HuntingControlTarget(), IdentifiesHuntingControlEvent, IdentifiesRhy

fun IdentifiesRhy.createTargetForEvent(eventId: HuntingControlEventId): HuntingControlEventTarget {
    return HuntingControlEventTarget(
        rhyId = rhyId,
        eventId = eventId,
    )
}
