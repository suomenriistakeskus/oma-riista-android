package fi.riista.common.extensions

import android.os.Bundle
import fi.riista.common.groupHunting.model.*
import fi.riista.common.model.OrganizationId
import fi.riista.common.model.StringWithId
import fi.riista.common.util.BundleWrapper
import fi.riista.common.util.WrappedBundle
import fi.riista.common.util.letWith


// Hunting club target

fun GroupHuntingClubTarget.saveToBundle(bundle: Bundle, prefix: String) {
    saveToBundle(WrappedBundle(bundle), prefix)
}

internal fun GroupHuntingClubTarget.saveToBundle(bundle: BundleWrapper, prefix: String) {
    bundle.putClubId(prefix, clubId)
}

fun Bundle.loadGroupHuntingClubTarget(prefix: String): GroupHuntingClubTarget? {
    return WrappedBundle(this).loadGroupHuntingClubTarget(prefix)
}

internal fun BundleWrapper.loadGroupHuntingClubTarget(prefix: String): GroupHuntingClubTarget? {
    return getClubIdOrNull(prefix)?.let { GroupHuntingClubTarget(clubId = it) }
}


// Hunting group target

fun HuntingGroupTarget.saveToBundle(bundle: Bundle, prefix: String) {
    saveToBundle(WrappedBundle(bundle), prefix)
}

internal fun HuntingGroupTarget.saveToBundle(bundle: BundleWrapper, prefix: String) {
    bundle.putClubId(prefix, clubId)
    bundle.putHuntingGroupId(prefix, huntingGroupId)
}

fun Bundle.loadHuntingGroupTarget(prefix: String): HuntingGroupTarget? {
    return WrappedBundle(this).loadHuntingGroupTarget(prefix)
}

internal fun BundleWrapper.loadHuntingGroupTarget(prefix: String): HuntingGroupTarget? {
    return getClubIdOrNull(prefix)
        ?.letWith(getHuntingGroupIdOrNull(prefix)) { clubId, huntingGroupId ->
            HuntingGroupTarget(clubId, huntingGroupId)
        }
}


// Group hunting harvest

fun GroupHuntingHarvestTarget.saveToBundle(bundle: Bundle, prefix: String) {
    saveToBundle(WrappedBundle(bundle), prefix)
}

internal fun GroupHuntingHarvestTarget.saveToBundle(bundle: BundleWrapper, prefix: String) {
    bundle.putClubId(prefix, clubId)
    bundle.putHuntingGroupId(prefix, huntingGroupId)
    bundle.putGroupHuntingHarvestId(prefix, harvestId)
}

fun Bundle.loadGroupHuntingHarvestTarget(prefix: String): GroupHuntingHarvestTarget? {
    return WrappedBundle(this).loadGroupHuntingHarvestTarget(prefix)
}

internal fun BundleWrapper.loadGroupHuntingHarvestTarget(prefix: String): GroupHuntingHarvestTarget? {
    val clubId = getClubIdOrNull(prefix)
    val huntingGroupId = getHuntingGroupIdOrNull(prefix)
    val harvestId = getGroupHuntingHarvestIdOrNull(prefix)

    return if (clubId != null && huntingGroupId != null && harvestId != null) {
        GroupHuntingHarvestTarget(clubId, huntingGroupId, harvestId)
    } else {
        null
    }
}


// Group hunting observation

fun GroupHuntingObservationTarget.saveToBundle(bundle: Bundle, prefix: String) {
    saveToBundle(WrappedBundle(bundle), prefix)
}

internal fun GroupHuntingObservationTarget.saveToBundle(bundle: BundleWrapper, prefix: String) {
    bundle.putClubId(prefix, clubId)
    bundle.putHuntingGroupId(prefix, huntingGroupId)
    bundle.putGroupHuntingObservationId(prefix, observationId)
}

fun Bundle.loadGroupHuntingObservationTarget(prefix: String): GroupHuntingObservationTarget? {
    return WrappedBundle(this).loadGroupHuntingObservationTarget(prefix)
}

internal fun BundleWrapper.loadGroupHuntingObservationTarget(prefix: String): GroupHuntingObservationTarget? {
    val clubId = getClubIdOrNull(prefix)
    val huntingGroupId = getHuntingGroupIdOrNull(prefix)
    val observationId = getGroupHuntingObservationIdOrNull(prefix)

    return if (clubId != null && huntingGroupId != null && observationId != null) {
        GroupHuntingObservationTarget(clubId, huntingGroupId, observationId)
    } else {
        null
    }
}


// Group hunting day

fun GroupHuntingDayTarget.saveToBundle(bundle: Bundle, prefix: String) {
    saveToBundle(WrappedBundle(bundle), prefix)
}

internal fun GroupHuntingDayTarget.saveToBundle(bundle: BundleWrapper, prefix: String) {
    bundle.putClubId(prefix, clubId)
    bundle.putHuntingGroupId(prefix, huntingGroupId)
    bundle.putGroupHuntingDayId(prefix, huntingDayId)
}

fun Bundle.loadGroupHuntingDayTarget(prefix: String): GroupHuntingDayTarget? {
    return WrappedBundle(this).loadGroupHuntingDayTarget(prefix)
}

internal fun BundleWrapper.loadGroupHuntingDayTarget(prefix: String): GroupHuntingDayTarget? {
    val clubId = getClubIdOrNull(prefix)
    val huntingGroupId = getHuntingGroupIdOrNull(prefix)
    val huntingDayId = getGroupHuntingDayIdOrNull(prefix)

    return if (clubId != null && huntingGroupId != null && huntingDayId != null) {
        GroupHuntingDayTarget(clubId, huntingGroupId, huntingDayId)
    } else {
        null
    }
}

// StringWithId

fun StringWithId.saveToBundle(bundle: Bundle, prefix: String) {
    saveToBundle(WrappedBundle(bundle), prefix)
}

internal fun StringWithId.saveToBundle(bundle: BundleWrapper, prefix: String) {
    bundle.putStringWithId(prefix, this)
}

fun Bundle.loadStringWithId(prefix: String): StringWithId? {
    return WrappedBundle(this).loadStringWithId(prefix)
}

internal fun BundleWrapper.loadStringWithId(prefix: String): StringWithId? {
    return getStringWithId(prefix)
}

// Helpers

private fun BundleWrapper.putClubId(prefix: String, clubId: OrganizationId) {
    putLong(keyForClubId(prefix), clubId)
}

private fun BundleWrapper.getClubIdOrNull(prefix: String): OrganizationId? {
    return getLongOrNull(keyForClubId(prefix))
}

private fun BundleWrapper.putHuntingGroupId(prefix: String, clubId: OrganizationId) {
    putLong(keyForHuntingGroupId(prefix), clubId)
}

private fun BundleWrapper.getHuntingGroupIdOrNull(prefix: String): OrganizationId? {
    return getLongOrNull(keyForHuntingGroupId(prefix))
}

private fun BundleWrapper.putGroupHuntingHarvestId(prefix: String, harvestId: GroupHuntingHarvestId) {
    putLong(keyForGroupHuntingHarvestId(prefix), harvestId)
}

private fun BundleWrapper.getGroupHuntingHarvestIdOrNull(prefix: String): GroupHuntingHarvestId? {
    return getLongOrNull(keyForGroupHuntingHarvestId(prefix))
}

private fun BundleWrapper.putGroupHuntingObservationId(prefix: String, observationId: GroupHuntingObservationId) {
    putLong(keyForGroupHuntingObservationId(prefix), observationId)
}

private fun BundleWrapper.getGroupHuntingObservationIdOrNull(prefix: String): GroupHuntingObservationId? {
    return getLongOrNull(keyForGroupHuntingObservationId(prefix))
}

private fun BundleWrapper.putGroupHuntingDayId(prefix: String, huntingDayId: GroupHuntingDayId) {
    putLong(keyForGroupHuntingDayId(prefix), huntingDayId.toLong())
}

private fun BundleWrapper.getGroupHuntingDayIdOrNull(prefix: String): GroupHuntingDayId? {
    return getLongOrNull(keyForGroupHuntingDayId(prefix))
        ?.let { GroupHuntingDayId.fromLong(it) }
}

private fun BundleWrapper.putStringWithId(prefix: String, stringWithId: StringWithId) {
    putLong(keyForStringWithIdId(prefix), stringWithId.id)
    putString(keyForStringWithIdString(prefix), stringWithId.string)
}

private fun BundleWrapper.getStringWithId(prefix: String): StringWithId? {
    val id = getLongOrNull(keyForStringWithIdId(prefix))
    val string = getString(keyForStringWithIdString(prefix))
    if (id != null && string != null) {
        return StringWithId(id = id, string = string)
    }
    return null
}

private fun keyForClubId(prefix: String) = "${prefix}_key_club_id"
private fun keyForHuntingGroupId(prefix: String) = "${prefix}_key_hunting_group_id"
private fun keyForGroupHuntingHarvestId(prefix: String) = "${prefix}_key_group_hunting_harvest_id"
private fun keyForGroupHuntingObservationId(prefix: String) = "${prefix}_key_group_hunting_observation_id"
private fun keyForGroupHuntingDayId(prefix: String) = "${prefix}_key_group_hunting_day_id"
private fun keyForStringWithIdId(prefix: String) = "${prefix}_key_string_with_id_id"
private fun keyForStringWithIdString(prefix: String) = "${prefix}_key_string_with_id_string"
