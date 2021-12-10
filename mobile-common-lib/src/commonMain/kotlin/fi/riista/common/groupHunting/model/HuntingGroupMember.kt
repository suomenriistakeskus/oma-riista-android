package fi.riista.common.groupHunting.model

import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.OccupationTypeDTO
import fi.riista.common.model.HunterNumber
import fi.riista.common.model.PersonWithHunterNumber

data class HuntingGroupMember(
    val id: Long,
    val occupationType: OccupationTypeDTO,
    val personId: Long,
    val firstName: String,
    val lastName: String,
    val hunterNumber: HunterNumber?,
    val beginDate: LocalDateDTO?,
    val endDate: LocalDateDTO?,
)

fun List<HuntingGroupMember>.getMember(person: PersonWithHunterNumber?): HuntingGroupMember? {
    return person?.id?.let { personId ->
        find { it.personId == personId }
    }
}

fun List<HuntingGroupMember>.isMember(person: PersonWithHunterNumber?): Boolean {
    return getMember(person) != null
}