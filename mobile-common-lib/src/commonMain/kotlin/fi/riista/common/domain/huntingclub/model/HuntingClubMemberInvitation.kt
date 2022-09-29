package fi.riista.common.domain.huntingclub.model

import fi.riista.common.domain.model.OccupationType
import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.PersonContactInfo
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDateTime

typealias HuntingClubMemberInvitationId = Long

data class HuntingClubMemberInvitation(
    val id: HuntingClubMemberInvitationId,
    val rev: Int,
    val clubId: Long, // Id of the hunting club
    val personId: Long, // Id of the invited person
    val club: Organization, // More information about hunting club
    val person: PersonContactInfo, // More information about invited person
    val occupationType: BackendEnum<OccupationType>?,
    val userRejectedTime: LocalDateTime?,
)
