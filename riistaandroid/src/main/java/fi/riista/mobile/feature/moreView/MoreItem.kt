package fi.riista.mobile.feature.moreView

enum class MoreItemType {
    MY_DETAILS,
    GALLERY,
    CONTACT_DETAILS,
    SHOOTING_TESTS,
    SETTINGS,
    HUNTING_DIRECTOR,
    HUNTING_CONTROL,
    EVENT_SEARCH,
    MAGAZINE,
    SEASONS,
    LOGOUT,
    ;
}

data class MoreItem(
    val type: MoreItemType,
    val iconResource: Int,
    val title: String,
    val opensInBrowser: Boolean = false,
)
