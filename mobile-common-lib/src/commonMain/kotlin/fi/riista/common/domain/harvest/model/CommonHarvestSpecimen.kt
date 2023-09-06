package fi.riista.common.domain.harvest.model

import fi.riista.common.domain.dto.HarvestSpecimenDTO
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.GameAntlersType
import fi.riista.common.domain.model.GameFitnessClass
import fi.riista.common.domain.model.Gender
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

typealias CommonHarvestSpecimenId = Long

@Serializable
data class CommonHarvestSpecimen(
    val id: CommonHarvestSpecimenId?,
    val rev: Int?,
    val gender: BackendEnum<Gender>,
    val age: BackendEnum<GameAge>,
    val weight: Double?,
    val weightEstimated: Double?,
    val weightMeasured: Double?,
    val fitnessClass: BackendEnum<GameFitnessClass>,
    val antlersLost: Boolean?,
    val antlersType: BackendEnum<GameAntlersType>,
    val antlersWidth: Int?,
    val antlerPointsLeft: Int?,
    val antlerPointsRight: Int?,
    val antlersGirth: Int?,
    val antlersLength: Int?,
    val antlersInnerWidth: Int?,
    val antlerShaftWidth: Int?,
    val notEdible: Boolean?,
    val alone: Boolean?,
    val additionalInfo: String?,
) {
    fun isEmpty() = this == EMPTY_HARVEST_SPECIMEN
}

private val EMPTY_HARVEST_SPECIMEN = CommonHarvestSpecimen(
    id = null,
    rev = null,
    gender = BackendEnum.create(null),
    age = BackendEnum.create(null),
    weight = null,
    weightEstimated = null,
    weightMeasured = null,
    fitnessClass = BackendEnum.create(null),
    antlersLost = null,
    antlersType = BackendEnum.create(null),
    antlersWidth = null,
    antlerPointsLeft = null,
    antlerPointsRight = null,
    antlersGirth = null,
    antlersLength = null,
    antlersInnerWidth = null,
    antlerShaftWidth = null,
    notEdible = null,
    alone = null,
    additionalInfo = null,
)

internal fun Iterable<CommonHarvestSpecimen>.keepNonEmpty() = filter { !it.isEmpty() }

internal fun CommonHarvestSpecimen.toHarvestSpecimenDTO() : HarvestSpecimenDTO {
    return HarvestSpecimenDTO(
        id = id,
        rev = rev,
        gender = gender.rawBackendEnumValue,
        age = age.rawBackendEnumValue,
        weight = weight,
        weightEstimated = weightEstimated,
        weightMeasured = weightMeasured,
        fitnessClass = fitnessClass.rawBackendEnumValue,
        antlersLost = antlersLost,
        antlersType = antlersType.rawBackendEnumValue,
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

internal fun CommonHarvestSpecimen.toCommonSpecimenData() =
    CommonSpecimenData(
        remoteId = id,
        revision = rev,
        gender = gender.rawBackendEnumValue?.toBackendEnum(),
        age = age.rawBackendEnumValue?.toBackendEnum(),
        stateOfHealth = null,
        marking = null,
        lengthOfPaw = null,
        widthOfPaw = null,
        weight = weight,
        weightEstimated = weightEstimated,
        weightMeasured = weightMeasured,
        fitnessClass = fitnessClass.rawBackendEnumValue?.toBackendEnum(),
        antlersLost = antlersLost,
        antlersType = antlersType.rawBackendEnumValue?.toBackendEnum(),
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

internal fun CommonSpecimenData.toCommonHarvestSpecimen() =
    CommonHarvestSpecimen(
        id = remoteId,
        rev = revision,
        gender = gender ?: BackendEnum.create(null),
        age = age ?: BackendEnum.create(null),
        weight = weight,
        weightEstimated = weightEstimated,
        weightMeasured = weightMeasured,
        fitnessClass = fitnessClass ?: BackendEnum.create(null),
        antlersLost = antlersLost,
        antlersType = antlersType ?: BackendEnum.create(null),
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
