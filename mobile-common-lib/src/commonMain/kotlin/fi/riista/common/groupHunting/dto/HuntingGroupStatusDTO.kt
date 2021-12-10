package fi.riista.common.groupHunting.dto

import fi.riista.common.groupHunting.model.HuntingGroupStatus
import kotlinx.serialization.Serializable

@Serializable
data class HuntingGroupStatusDTO(
    val canCreateHuntingDay: Boolean,
    val canCreateHarvest: Boolean,
    val canCreateObservation: Boolean,
    val canEditDiaryEntry: Boolean,
    val canEditHuntingDay: Boolean,
)

fun HuntingGroupStatusDTO.toHuntingGroupStatus() =
    // explicitly use named parameters in order to make sure each flag is stored correctly
    // - parameter types are the same which would make it easy to store flag incorrectly
    HuntingGroupStatus(canCreateHuntingDay = canCreateHuntingDay,
                       canCreateHarvest = canCreateHarvest,
                       canCreateObservation = canCreateObservation,
                       canEditDiaryEntry = canEditDiaryEntry,
                       canEditHuntingDay = canEditHuntingDay)