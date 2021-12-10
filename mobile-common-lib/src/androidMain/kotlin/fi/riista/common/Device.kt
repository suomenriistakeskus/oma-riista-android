package fi.riista.common

actual class Device actual constructor() {
    // default values are for tests as on that side the android.os.Build functionality
    // provide null values
    actual val name: String = android.os.Build.DEVICE ?: "unknown"
    actual val osVersion: String = android.os.Build.VERSION.RELEASE ?: "unknown"
}