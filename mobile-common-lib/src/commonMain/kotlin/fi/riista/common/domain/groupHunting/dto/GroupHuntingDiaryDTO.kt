package fi.riista.common.domain.groupHunting.dto

import fi.riista.common.domain.groupHunting.model.GroupHuntingDiary
import kotlinx.serialization.Serializable

@Serializable
data class GroupHuntingDiaryDTO(
    val harvests: List<GroupHuntingHarvestDTO>,
    val observations: List<GroupHuntingObservationDTO>,
    val rejectedHarvests: List<GroupHuntingHarvestDTO>,
    val rejectedObservations: List<GroupHuntingObservationDTO>,
)

internal fun GroupHuntingDiaryDTO.toGroupHuntingDiary(): GroupHuntingDiary {
    return GroupHuntingDiary(
        harvests = harvests.mapNotNull { it.toGroupHuntingHarvest() },
        observations = observations.mapNotNull { it.toGroupHuntingObservation() },
        rejectedHarvests = rejectedHarvests.mapNotNull { it.toGroupHuntingHarvest()?.copy(rejected = true) },
        rejectedObservations = rejectedObservations.mapNotNull { it.toGroupHuntingObservation()?.copy(rejected = true) },
    )
}
