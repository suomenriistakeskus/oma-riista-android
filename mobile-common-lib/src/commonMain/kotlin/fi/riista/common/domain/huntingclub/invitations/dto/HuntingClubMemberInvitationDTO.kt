package fi.riista.common.domain.huntingclub.invitations.dto

import fi.riista.common.domain.dto.OccupationTypeDTO
import fi.riista.common.domain.dto.OrganizationDTO
import fi.riista.common.domain.dto.PersonContactInfoDTO
import fi.riista.common.domain.dto.toOrganization
import fi.riista.common.domain.dto.toPersonContactInfo
import fi.riista.common.domain.huntingclub.invitations.model.HuntingClubMemberInvitation
import fi.riista.common.dto.LocalDateTimeDTO
import fi.riista.common.dto.toLocalDateTime
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class HuntingClubMemberInvitationDTO(
    val id: Long,
    val rev: Int,
    val clubId: Long, // Id of the hunting club
    val personId: Long, // Id of the invited person
    val club: OrganizationDTO, // More information about hunting club
    val person: PersonContactInfoDTO, // More information about invited person
    val occupationType: OccupationTypeDTO? = null,
    val userRejectedTime: LocalDateTimeDTO? = null,
)

fun HuntingClubMemberInvitationDTO.toHuntingClubMemberInvitation(): HuntingClubMemberInvitation {
    return HuntingClubMemberInvitation(
        id = id,
        rev = rev,
        clubId = clubId,
        personId = personId,
        club = club.toOrganization(),
        person = person.toPersonContactInfo(),
        occupationType = occupationType?.toBackendEnum(),
        userRejectedTime = userRejectedTime?.toLocalDateTime(),
    )
}
