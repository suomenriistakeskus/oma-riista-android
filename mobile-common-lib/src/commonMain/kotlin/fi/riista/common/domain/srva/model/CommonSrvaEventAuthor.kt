package fi.riista.common.domain.srva.model

import fi.riista.common.domain.dto.PersonWithHunterNumberDTO
import kotlinx.serialization.Serializable

@Serializable
data class CommonSrvaEventAuthor(
    val id: Long,
    val revision: Long,
    val byName: String?,
    val lastName: String?,
)

fun CommonSrvaEventAuthor.toPersonWithHunterNumberDTO(): PersonWithHunterNumberDTO {
    return PersonWithHunterNumberDTO(
        id = id,
        rev = revision.toInt(),
        byName = byName ?: "",
        lastName = lastName ?: "",
    )
}
