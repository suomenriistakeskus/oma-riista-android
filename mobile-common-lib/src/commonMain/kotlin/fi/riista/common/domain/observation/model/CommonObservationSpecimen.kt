package fi.riista.common.domain.observation.model

import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.observation.dto.CommonObservationSpecimenDTO
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class CommonObservationSpecimen(
    val remoteId: Long?,
    val revision: Int?,
    val gender: BackendEnum<Gender>,
    val age: BackendEnum<GameAge>,
    val stateOfHealth: BackendEnum<ObservationSpecimenState>,
    val marking: BackendEnum<ObservationSpecimenMarking>,
    val widthOfPaw: Double?,
    val lengthOfPaw: Double?,
) {
    fun isEmpty() = this == EMPTY_OBSERVATION_SPECIMEN
}

private val EMPTY_OBSERVATION_SPECIMEN = CommonObservationSpecimen(
    remoteId = null,
    revision = null,
    gender = BackendEnum.create(null),
    age = BackendEnum.create(null),
    stateOfHealth = BackendEnum.create(null),
    marking = BackendEnum.create(null),
    widthOfPaw = null,
    lengthOfPaw = null,
)

internal fun Iterable<CommonObservationSpecimen>.keepNonEmpty() = filter { !it.isEmpty() }

internal fun CommonObservationSpecimen.toObservationSpecimenDTO() =
    CommonObservationSpecimenDTO(
        id = remoteId,
        rev = revision,
        gender = gender.rawBackendEnumValue,
        age = age.rawBackendEnumValue,
        state = stateOfHealth.rawBackendEnumValue,
        marking = marking.rawBackendEnumValue,
        lengthOfPaw = lengthOfPaw,
        widthOfPaw = widthOfPaw,
    )

internal fun CommonObservationSpecimen.toCommonSpecimenData() =
    CommonSpecimenData(
        remoteId = remoteId,
        revision = revision,
        gender = gender.rawBackendEnumValue?.toBackendEnum(),
        age = age.rawBackendEnumValue?.toBackendEnum(),
        stateOfHealth = stateOfHealth.rawBackendEnumValue?.toBackendEnum(),
        marking = marking.rawBackendEnumValue?.toBackendEnum(),
        lengthOfPaw = lengthOfPaw,
        widthOfPaw = widthOfPaw,
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
