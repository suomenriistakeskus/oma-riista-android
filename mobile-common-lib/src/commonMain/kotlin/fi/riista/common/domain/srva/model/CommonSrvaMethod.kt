package fi.riista.common.domain.srva.model

import fi.riista.common.model.BackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class CommonSrvaMethod(
    val type: BackendEnum<SrvaMethodType>,
    val selected: Boolean,
)

fun BackendEnum<SrvaMethodType>.toCommonSrvaMethod(selected: Boolean = false) =
    CommonSrvaMethod(
        type = this,
        selected = selected,
    )

internal val List<CommonSrvaMethod>.selectedMethods: List<BackendEnum<SrvaMethodType>>
    get() {
        return this.mapNotNull {
            when (it.selected) {
                true -> it.type
                else -> null
            }
        }
    }
