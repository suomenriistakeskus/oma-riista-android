package fi.riista.common.domain.groupHunting.dto

import fi.riista.common.domain.groupHunting.model.HuntingGroupStatus
import kotlinx.serialization.Serializable

@Serializable
data class HuntingGroupStatusDTO(
    val canCreateHuntingDay: Boolean,
    val canCreateHarvest: Boolean,
    val canCreateObservation: Boolean,
    val canEditDiaryEntry: Boolean,
    val canEditHuntingDay: Boolean,

    // todo: remove optionality once backend implementation is guaranteed to exist in production
    val canEditHarvest: Boolean? = null,
    val canEditObservation: Boolean? = null,
    val huntingFinished: Boolean? = null,
)

fun HuntingGroupStatusDTO.toHuntingGroupStatus() =
    // explicitly use named parameters in order to make sure each flag is stored correctly
    // - parameter types are the same which would make it easy to store flag incorrectly
    HuntingGroupStatus(
        canCreateHuntingDay = canCreateHuntingDay,
        canCreateHarvest = canCreateHarvest,
        canCreateObservation = canCreateObservation,
        canEditDiaryEntry = canEditDiaryEntry,
        canEditHuntingDay = canEditHuntingDay,
        canEditHarvest = canEditHarvest ?: canEditDiaryEntry, // todo: remove default when no longer needed
        canEditObservation = canEditObservation ?: canEditDiaryEntry, // todo: remove default when no longer needed
        huntingFinished = huntingFinished ?: false, // todo: remove default when no longer needed
    )