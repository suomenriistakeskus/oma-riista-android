package fi.riista.common.domain.groupHunting.dto

import fi.riista.common.domain.groupHunting.model.HuntingGroupPermit
import fi.riista.common.dto.LocalDateDTO
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.dto.toLocalDate
import fi.riista.common.model.LocalDatePeriod
import fi.riista.common.util.letWith
import kotlinx.serialization.Serializable

@Serializable
data class HuntingGroupDTO(
    val id: Long,
    val clubId: Long,
    val speciesCode: Int,
    val huntingYear: Int,
    // permit dates i.e. when the permit is valid and hunting is allowed
    val beginDate: LocalDateDTO? = null,
    val endDate: LocalDateDTO? = null,
    val beginDate2: LocalDateDTO? = null,
    val endDate2: LocalDateDTO? = null,
    val permitNumber: String,
    val name: LocalizedStringDTO,
)

fun HuntingGroupDTO.createPermit(): HuntingGroupPermit {
    return HuntingGroupPermit(
            permitNumber = permitNumber,
            validityPeriods = listOfNotNull(
                    beginDate?.toLocalDate()?.letWith(endDate?.toLocalDate()) { start, end ->
                        LocalDatePeriod(start, end)
                    },
                    beginDate2?.toLocalDate()?.letWith(endDate2?.toLocalDate()) { start, end ->
                        LocalDatePeriod(start, end)
                    },
            )
    )
}