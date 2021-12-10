package fi.riista.common.dto

import fi.riista.common.model.PersonWithHunterNumber
import kotlinx.serialization.Serializable

@Serializable
data class PersonWithHunterNumberDTO(
    val id: Long,
    val rev: Int,
    val byName: String,
    val lastName: String,
    /**
     * Hunter number is allowed to be null. In most cases it won't be but it is possible
     * that the person has stopped hunting and thus the hunter number has been cleared.
     */
    val hunterNumber: HunterNumberDTO? = null,
    val extendedName: String? = null,
)

fun PersonWithHunterNumberDTO.toPersonWithHunterNumber(): PersonWithHunterNumber {
    return PersonWithHunterNumber(
            id = id,
            rev = rev,
            byName = byName,
            lastName = lastName,
            hunterNumber = hunterNumber,
            extendedName = extendedName,
    )
}
