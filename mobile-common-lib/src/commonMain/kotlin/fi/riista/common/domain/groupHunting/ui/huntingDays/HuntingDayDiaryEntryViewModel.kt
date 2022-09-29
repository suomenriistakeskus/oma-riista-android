package fi.riista.common.domain.groupHunting.ui.huntingDays

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.groupHunting.model.AcceptStatus
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.GroupHuntingObservation
import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.model.LocalDateTime

interface HuntingDayDiaryEntryViewModel {
    val id: Long
    val speciesCode: SpeciesCode
    val pointOfTime: LocalDateTime
    val type: Type
    val amount: Int
    val acceptStatus: AcceptStatus

    /**
     * The deer hunting type if available. Allows grouping harvests / observations.
     */
    val deerHuntingType: DeerHuntingType?

    enum class Type {
        HARVEST,
        OBSERVATION
    }
}

data class HuntingDayHarvestViewModel(
    override val id: Long,
    override val speciesCode: SpeciesCode,
    override val pointOfTime: LocalDateTime,
    override val amount: Int,
    override val acceptStatus: AcceptStatus,
    override val deerHuntingType: DeerHuntingType?
): HuntingDayDiaryEntryViewModel {
    override val type = HuntingDayDiaryEntryViewModel.Type.HARVEST
}

data class HuntingDayObservationViewModel(
    override val id: Long,
    override val speciesCode: SpeciesCode,
    override val pointOfTime: LocalDateTime,
    override val amount: Int,
    override val acceptStatus: AcceptStatus,
    override val deerHuntingType: DeerHuntingType?
): HuntingDayDiaryEntryViewModel {
    override val type = HuntingDayDiaryEntryViewModel.Type.OBSERVATION
}

internal fun GroupHuntingHarvest.toHuntingDayHarvestViewModel(): HuntingDayHarvestViewModel {
    return HuntingDayHarvestViewModel(
            id = id,
            speciesCode = gameSpeciesCode,
            pointOfTime = pointOfTime,
            amount = amount
                    ?: specimens.size.takeIf { it > 0 }
                    ?: 1, // there must be at least one harvest
            acceptStatus = acceptStatus,
            deerHuntingType = deerHuntingType.value,
    )
}

internal fun GroupHuntingObservation.toHuntingDayObservationViewModel(): HuntingDayObservationViewModel {
    return HuntingDayObservationViewModel(
            id = id,
            speciesCode = gameSpeciesCode,
            pointOfTime = pointOfTime,
            // it is ok that the amount is 0. This e.g. the case when observation type
            // is something that doesn't allow figuring out the amount (e.g. feeding marks or scat etc)
            amount = totalSpecimenAmount
                    ?: amount
                    ?: specimens.size,
            acceptStatus = acceptStatus,
            deerHuntingType = deerHuntingType.value,
    )
}