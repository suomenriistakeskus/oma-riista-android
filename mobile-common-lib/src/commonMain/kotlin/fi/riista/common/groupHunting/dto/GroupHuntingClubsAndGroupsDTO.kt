package fi.riista.common.groupHunting.dto

import fi.riista.common.dto.OrganizationDTO
import kotlinx.serialization.Serializable

@Serializable
data class GroupHuntingClubsAndGroupsDTO(
    val clubs: List<OrganizationDTO>,
    val groups: List<HuntingGroupDTO>,
)