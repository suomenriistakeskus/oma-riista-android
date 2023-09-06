package fi.riista.common.domain.model

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.constants.isDeer
import fi.riista.common.domain.constants.isMoose
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

fun Species.isMoose(): Boolean = isSpecies(SpeciesCodes.MOOSE_ID)
fun Species.isWildBoar(): Boolean = isSpecies(SpeciesCodes.WILD_BOAR_ID)
fun Species.isGreySeal(): Boolean = isSpecies(SpeciesCodes.GREY_SEAL_ID)
fun Species.isBeanGoose(): Boolean = isSpecies(SpeciesCodes.BEAN_GOOSE_ID)

internal fun Species.isSpecies(speciesCode: SpeciesCode): Boolean {
    return when (this) {
        is Species.Known -> this.speciesCode == speciesCode
        Species.Other -> false
        Species.Unknown -> false
    }
}