package fi.riista.common.domain.model

import fi.riista.common.domain.observation.model.ObservationSpecimenMarking
import fi.riista.common.domain.observation.model.ObservationSpecimenState
import fi.riista.common.model.BackendEnum

internal fun CommonSpecimenData.Companion.createForTests(
    remoteId: Long? = null,
    revision: Int? = null,
    gender: BackendEnum<Gender>? = null,
    age: BackendEnum<GameAge>? = null,
    stateOfHealth: BackendEnum<ObservationSpecimenState>? = null,
    marking: BackendEnum<ObservationSpecimenMarking>? = null,
    lengthOfPaw: Double? = null,
    widthOfPaw: Double? = null,
    weight: Double? = null,
    weightEstimated: Double? = null,
    weightMeasured: Double? = null,
    fitnessClass: BackendEnum<GameFitnessClass>? = null,
    antlersLost: Boolean? = null,
    antlersType: BackendEnum<GameAntlersType>? = null,
    antlersWidth: Int? = null,
    antlerPointsLeft: Int? = null,
    antlerPointsRight: Int? = null,
    antlersGirth: Int? = null,
    antlersLength: Int? = null,
    antlersInnerWidth: Int? = null,
    antlerShaftWidth: Int? = null,
    notEdible: Boolean? = null,
    alone: Boolean? = null,
    additionalInfo: String? = null,
): CommonSpecimenData {
    return CommonSpecimenData(
        remoteId = remoteId,
        revision = revision,
        gender = gender,
        age = age,
        stateOfHealth = stateOfHealth,
        marking = marking,
        lengthOfPaw = lengthOfPaw,
        widthOfPaw = widthOfPaw,
        weight = weight,
        weightEstimated = weightEstimated,
        weightMeasured = weightMeasured,
        fitnessClass = fitnessClass,
        antlersLost = antlersLost,
        antlersType = antlersType,
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