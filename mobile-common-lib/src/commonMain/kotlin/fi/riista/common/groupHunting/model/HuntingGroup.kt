package fi.riista.common.groupHunting.model

import fi.riista.common.model.*

typealias HuntingGroupId = Long

data class HuntingGroup(
    val id: HuntingGroupId,
    val club: Organization,
    val speciesCode: SpeciesCode,
    val huntingYear: HuntingYear,
    val permit: HuntingGroupPermit,
    val name: LocalizedString,
)

fun HuntingGroup.getTarget() = HuntingGroupTarget(
        clubId = club.id, huntingGroupId = id
)