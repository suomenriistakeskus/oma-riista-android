package fi.riista.common.domain.model

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.isDeer
import kotlinx.serialization.Serializable

@Serializable
sealed class Species {
    @Serializable
    data class Known(val speciesCode: SpeciesCode): Species()

    @Serializable
    object Other: Species()

    @Serializable
    object Unknown: Species()

    fun knownSpeciesCodeOrNull(): SpeciesCode? {
        return if (this is Known) {
            speciesCode
        } else {
            null
        }
    }
}

fun Species.isDeer(): Boolean {
    return when (this) {
        is Species.Known -> speciesCode.isDeer()
        Species.Other -> false
        Species.Unknown -> false
    }
}