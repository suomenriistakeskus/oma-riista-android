package fi.riista.common.domain.model

import fi.riista.common.domain.dto.PersonWithHunterNumberDTO
import fi.riista.common.model.Revision
import kotlinx.serialization.Serializable

typealias PersonWithHunterNumberId = Long

@Serializable
data class PersonWithHunterNumber(
    val id: PersonWithHunterNumberId,
    val rev: Revision,
    val byName: String,
    val lastName: String,
    /**
     * Hunter number is allowed to be null. In most cases it won't be but it is possible
     * that the person has stopped hunting and thus the hunter number has been cleared.
     */
    val hunterNumber: HunterNumber?,
    val extendedName: String? = null,
)

fun PersonWithHunterNumber.toPersonWithHunterNumberDTO(): PersonWithHunterNumberDTO {
    return PersonWithHunterNumberDTO(
            id = id,
            rev = rev,
            byName = byName,
            lastName = lastName,
            hunterNumber = hunterNumber,
            extendedName = extendedName,
    )
}
