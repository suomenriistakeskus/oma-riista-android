package fi.riista.common.domain.srva.sync.dto

import fi.riista.common.domain.srva.model.CommonSrvaMethod
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class SrvaMethodDTO(
    val name: String?,
    val isChecked: Boolean,
)

fun SrvaMethodDTO.toCommonSrvaMethod(): CommonSrvaMethod {
    return CommonSrvaMethod(
        type = name.toBackendEnum(),
        selected = isChecked,
    )
}

fun CommonSrvaMethod.toSrvaMethodDTO(): SrvaMethodDTO {
    return SrvaMethodDTO(
        name = type.rawBackendEnumValue,
        isChecked = selected,
    )
}
