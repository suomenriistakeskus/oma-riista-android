package fi.riista.common.util

import kotlinx.datetime.Clock
import kotlin.random.Random

internal fun generateMobileClientRefId(): Long {
    val now = Clock.System.now()
    val secondsSinceEpoch = now.toEpochMilliseconds() / 1000L
    return (secondsSinceEpoch shl 32) + Random.nextInt()
}
