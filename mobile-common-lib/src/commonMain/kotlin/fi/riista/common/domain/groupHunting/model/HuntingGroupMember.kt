package fi.riista.common.domain.groupHunting.model

import fi.riista.common.domain.model.HunterNumber
import fi.riista.common.domain.model.OccupationType
import fi.riista.common.domain.model.PersonWithHunterNumber
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.LocalDate

data class HuntingGroupMember(
    val id: Long,
    val occupationType: BackendEnum<OccupationType>,
    val personId: Long,
    val firstName: String,
    val lastName: String,
    val hunterNumber: HunterNumber?,
    val beginDate: LocalDate?,
    val endDate: LocalDate?,
)

fun List<HuntingGroupMember>.getMember(person: PersonWithHunterNumber?): HuntingGroupMember? {
    return person?.id?.let { personId ->
        find { it.personId == personId }
    }
}

fun List<HuntingGroupMember>.isMember(person: PersonWithHunterNumber?): Boolean {
    return getMember(person) != null
}