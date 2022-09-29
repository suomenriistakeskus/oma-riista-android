package fi.riista.common.domain.groupHunting.dto

import fi.riista.common.domain.dto.OrganizationDTO
import kotlinx.serialization.Serializable

@Serializable
data class GroupHuntingClubsAndGroupsDTO(
    val clubs: List<OrganizationDTO>,
    val groups: List<HuntingGroupDTO>,
)