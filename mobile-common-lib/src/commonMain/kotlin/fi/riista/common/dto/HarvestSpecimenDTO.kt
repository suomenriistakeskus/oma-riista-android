package fi.riista.common.dto

import fi.riista.common.model.HarvestSpecimen
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class HarvestSpecimenDTO(
    val id: Long? = null,
    val rev: Int? = null,
    val gender: GenderDTO? = null,
    val age: GameAgeDTO? = null,
    val weight: Double? = null,
    val weightEstimated: Double? = null,
    val weightMeasured: Double? = null,
    val fitnessClass: GameFitnessClassDTO? = null,
    val antlersLost: Boolean? = null,
    val antlersType: GameAntlersTypeDTO? = null,
    val antlersWidth: Int? = null,
    val antlerPointsLeft: Int? = null,
    val antlerPointsRight: Int? = null,
    val antlersGirth: Int? = null,
    val antlersLength: Int? = null,
    val antlersInnerWidth: Int? = null,
    val antlerShaftWidth: Int? = null,
    val notEdible: Boolean? = null,
    val alone: Boolean? = null,
    val additionalInfo: String? = null,
)

internal fun HarvestSpecimenDTO.toHarvestSpecimen() : HarvestSpecimen {
    return HarvestSpecimen(
            id = id,
            rev = rev,
            gender = gender.toBackendEnum(),
            age = age.toBackendEnum(),
            weight = weight,
            weightEstimated = weightEstimated,
            weightMeasured = weightMeasured,
            fitnessClass = fitnessClass.toBackendEnum(),
            antlersLost = antlersLost,
            antlersType = antlersType.toBackendEnum(),
            antlersWidth = antlersWidth,
            antlerPointsLeft = antlerPointsLeft,
            antlerPointsRight = antlerPointsRight,
            antlersGirth = antlersGirth,
            antlersLength = antlersLength,
            antlersInnerWidth = antlersInnerWidth,
            antlerShaftWidth = antlerShaftWidth,
            notEdible = notEdible,
            alone = alone,
            additionalInfo = additionalInfo,
    )
}
