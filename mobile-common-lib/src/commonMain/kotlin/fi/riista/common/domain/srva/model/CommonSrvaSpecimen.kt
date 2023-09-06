package fi.riista.common.domain.srva.model

import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.model.BackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class CommonSrvaSpecimen(
    val gender: BackendEnum<Gender>,
    val age: BackendEnum<GameAge>,
) {
    fun isEmpty() = this == EMPTY_SRVA_SPECIMEN
}

private val EMPTY_SRVA_SPECIMEN = CommonSrvaSpecimen(
    gender = BackendEnum.create(null),
    age = BackendEnum.create(null),
)

internal fun Iterable<CommonSrvaSpecimen>.keepNonEmpty() = filter { !it.isEmpty() }

internal fun CommonSrvaSpecimen.toCommonSpecimenData(): CommonSpecimenData {
    return CommonSpecimenData(
        remoteId = null,
        revision = null,
        gender = gender,
        age = age,
        stateOfHealth = null,
        marking = null,
        lengthOfPaw = null,
        widthOfPaw = null,
        weight = null,
        weightEstimated = null,
        weightMeasured = null,
        fitnessClass = null,
        antlersLost = null,
        antlersType = null,
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
}