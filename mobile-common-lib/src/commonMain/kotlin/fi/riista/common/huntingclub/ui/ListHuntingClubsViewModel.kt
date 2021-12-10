package fi.riista.common.huntingclub.ui

data class ListHuntingClubsViewModel(
    val items: List<HuntingClubViewModel>,
    val hasOpenInvitations: Boolean,
)
