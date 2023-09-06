package fi.riista.mobile.feature.moreView

enum class MoreItemType {
    // More view
    MY_DETAILS,
    GALLERY,
    CONTACT_DETAILS,
    SHOOTING_TESTS,
    SETTINGS,
    HUNTING_DIRECTOR,
    HUNTING_CONTROL,
    SUNRISE_AND_SUNSET,
    EVENT_SEARCH,
    MAGAZINE,
    SEASONS,
    ABOUT,
    LOGOUT,

    // About view
    PRIVACY_STATEMENT,
    TERMS_OF_SERVICE,
    ACCESSIBILITY,
    LICENSES,
    ;
}

data class MoreItem(
    val type: MoreItemType,
    val iconResource: Int,
    val title: String,
    val opensInBrowser: Boolean = false,
)
