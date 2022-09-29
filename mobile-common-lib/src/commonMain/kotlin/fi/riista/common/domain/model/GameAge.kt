package fi.riista.common.domain.model

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

/**
 * An enum representing age.
 *
 * Intentionally not [LocalizableEnum] as localization depends on species
 * (e.g.bear with the age of 1 to 2 years is a yearling cub)
 */
enum class GameAge(
    override val rawBackendEnumValue: String,
): RepresentsBackendEnum {
    ADULT("ADULT"),
    YOUNG("YOUNG"),
    UNKNOWN("UNKNOWN"),

    // observation specific values
    LESS_THAN_ONE_YEAR("LT1Y"),
    BETWEEN_ONE_AND_TWO_YEARS("_1TO2Y"),
    ;

    fun localizationKey(speciesCode: SpeciesCode): RR.string {
        return when (this) {
            ADULT -> RR.string.age_adult
            YOUNG -> RR.string.age_young
            UNKNOWN -> RR.string.age_unknown
            LESS_THAN_ONE_YEAR -> RR.string.age_less_than_one_year
            BETWEEN_ONE_AND_TWO_YEARS -> {
                when (speciesCode) {
                    SpeciesCodes.BEAR_ID -> RR.string.age_eraus
                    else -> RR.string.age_between_one_and_two_years
                }
            }
        }
    }

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<GameAge> = value.toBackendEnum()
    }
}

internal fun GameAge.localizationKey(species: Species): RR.string {
    val speciesCode = species.knownSpeciesCodeOrNull() ?: -1
    return localizationKey(speciesCode = speciesCode)
}
