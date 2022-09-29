package fi.riista.common.domain.model

typealias HunterNumber = String

internal const val HUNTER_NUMBER_LENGTH = 8

fun HunterNumber.isHunterNumberValid(): Boolean {
    if (!isHunterNumberCorrectLength()) {
        return false
    }
    if (!this.all { it in '0'..'9' }) {
        return false
    }
    val checkSum = this[this.length - 1]
    return charToInt(checkSum) == calculateChecksum(this.dropLast(1))
}

fun HunterNumber.isHunterNumberCorrectLength(): Boolean {
    if (this.length != HUNTER_NUMBER_LENGTH) {
        return false
    }
    return true
}

internal fun calculateChecksum(s: String): Int {
    var sum = 0
    val weights = listOf(7, 3, 1, 7, 3, 1, 7)
    var w = 0
    for (i in s.length - 1 downTo 0) {
        sum += charToInt(s[i]) * weights[w++]
    }
    val remainder = sum % 10
    return if (remainder == 0) {
        0
    } else {
        10 - remainder
    }
}

private fun charToInt(c: Char): Int {
    return c.code - '0'.code
}
