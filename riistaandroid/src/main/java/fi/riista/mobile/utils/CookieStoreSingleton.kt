package fi.riista.mobile.utils

import fi.vincit.androidutilslib.network.SynchronizedCookieStore

// Enum is considered to be the most concise and safe way to write a singleton (see Effective Java
// book by Joshua Block).
enum class CookieStoreSingleton {
    INSTANCE;

    val cookieStore = SynchronizedCookieStore()
}
