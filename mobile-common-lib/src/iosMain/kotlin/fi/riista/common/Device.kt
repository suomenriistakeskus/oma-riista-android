package fi.riista.common

import platform.UIKit.UIDevice

actual class Device actual constructor() {
    // not really actual device information such as "iPhone SE" as that is not easily
    // available. Consider adding real device name only if needed
    actual val name: String = UIDevice.currentDevice.model
    actual val osVersion: String = UIDevice.currentDevice.systemVersion
}