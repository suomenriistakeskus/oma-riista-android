package fi.riista.common.domain.huntingclub.ui

data class ListHuntingClubsViewModel(
    val items: List<HuntingClubViewModel>,
    val hasOpenInvitations: Boolean,
)
