package fi.riista.common.extensions

import android.os.Bundle
import fi.riista.common.domain.huntingControl.model.HuntingControlEventTarget
import fi.riista.common.domain.huntingControl.model.HuntingControlRhyTarget
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.util.BundleWrapper
import fi.riista.common.util.WrappedBundle

fun HuntingControlRhyTarget.saveToBundle(bundle: Bundle, prefix: String) {
    saveToBundle(WrappedBundle(bundle), prefix)
}

internal fun HuntingControlRhyTarget.saveToBundle(bundle: BundleWrapper, prefix: String) {
    bundle.putRhyId(prefix, rhyId)
}

fun Bundle.loadHuntingControlRhyTarget(prefix: String): HuntingControlRhyTarget? {
    return WrappedBundle(this).loadHuntingControlRhyTarget(prefix)
}

internal fun BundleWrapper.loadHuntingControlRhyTarget(prefix: String): HuntingControlRhyTarget? {
    val rhyId = getRhyIdOrNull(prefix)

    return if (rhyId != null) {
        HuntingControlRhyTarget(
            rhyId = rhyId,
        )
    } else {
        null
    }
}

fun HuntingControlEventTarget.saveToBundle(bundle: Bundle, prefix: String) {
    saveToBundle(WrappedBundle(bundle), prefix)
}

internal fun HuntingControlEventTarget.saveToBundle(bundle: BundleWrapper, prefix: String) {
    bundle.putRhyId(prefix, rhyId)
    bundle.putHuntingControlEventId(prefix, eventId)
}

fun Bundle.loadHuntingControlEventTarget(prefix: String): HuntingControlEventTarget? {
    return WrappedBundle(this).loadHuntingControlEventTarget(prefix)
}

internal fun BundleWrapper.loadHuntingControlEventTarget(prefix: String): HuntingControlEventTarget? {
    val rhyId = getRhyIdOrNull(prefix)
    val eventId = getHuntingControlEventIdOrNull(prefix)

    return if (rhyId != null && eventId != null) {
        HuntingControlEventTarget(
            rhyId = rhyId,
            eventId = eventId,
        )
    } else {
        null
    }
}

private fun BundleWrapper.putRhyId(prefix: String, rhyId: OrganizationId) {
    putLong(keyForRhyId(prefix), rhyId)
}

private fun BundleWrapper.getRhyIdOrNull(prefix: String): OrganizationId? {
    return getLongOrNull(keyForRhyId(prefix))
}

private fun BundleWrapper.putHuntingControlEventId(prefix: String, clubId: OrganizationId) {
    putLong(keyForHuntingControlEventId(prefix), clubId)
}

private fun BundleWrapper.getHuntingControlEventIdOrNull(prefix: String): OrganizationId? {
    return getLongOrNull(keyForHuntingControlEventId(prefix))
}

private fun keyForRhyId(prefix: String) = "${prefix}_key_rhy_id"
private fun keyForHuntingControlEventId(prefix: String) = "${prefix}_key_hunting_control_event_id"
