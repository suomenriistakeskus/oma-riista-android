package fi.riista.common.domain.groupHunting.model

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.model.Organization
import fi.riista.common.model.LocalizedString

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