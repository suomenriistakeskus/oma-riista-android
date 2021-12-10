package fi.riista.common

enum class PlatformName(val platformNameString: String) {
    IOS("ios"),
    ANDROID("android"),
}

expect class Platform() {
    val name: PlatformName
}