package fi.riista.common.domain.huntingclub.ui

import fi.riista.common.domain.huntingclub.model.HuntingClubMemberInvitationId

sealed class HuntingClubViewModel {
    abstract val id: Long

    data class HuntingClub(
        override val id: Long,
        val name: String,
        val officialCode: String,
    ): HuntingClubViewModel()

    data class Invitation(
        override val id: Long,
        val name: String,
        val officialCode: String,
        val invitationId: HuntingClubMemberInvitationId,
    ): HuntingClubViewModel()

    data class Header(
        override val id: Long,
        val text: String
    ): HuntingClubViewModel()
}
