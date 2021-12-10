package fi.riista.common.domain.season

import fi.riista.common.model.*

/**
 * Copied from application side + converted to kotlin.
 */
object HardCodedSeasons {
    fun isInsideHuntingSeason(date: LocalDate, speciesCode: SpeciesCode): Boolean {
        val huntingYear = date.getHuntingYear()

        return speciesCode == SpeciesCodes.BEAN_GOOSE_ID && isBeanGooseSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.BEAR_ID && isBearSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.COMMON_EIDER_ID && isCommonEiderSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.COOT_ID && isCootSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.GARGANEY_ID && isGarganeySeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.GOOSANDER_ID && isGoosanderSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.GREYLAG_GOOSE_ID && isGreylagGooseSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.GREY_SEAL_ID && isGreySealSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.LONG_TAILED_DUCK_ID && isLongTailedDuckSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.PINTAIL_ID && isPintailSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.POCHARD_ID && isPochardSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.POLECAT_ID && huntingYear >= 2017 ||
                speciesCode == SpeciesCodes.RED_BREASTED_MERGANSER_ID && isRedBreastedMerganserSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.RINGED_SEAL_ID && isRingedSealSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.ROE_DEER_ID && isRoeDeerSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.SHOVELER_ID && isSholeverSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.TUFTED_DUCK_ID && isTuftedDuckSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.WIGEON_ID && isWigeonSeason(date, huntingYear) ||
                speciesCode == SpeciesCodes.WILD_BOAR_ID && huntingYear >= 2017
    }

    // Metsähanhi
    private fun isBeanGooseSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Starting from 2017
        if (huntingYear < 2017) {
            return false
        }

        // 20.8 - 27.8 and 1.10. - 30.11.
        return isDateInRange(date, ld(huntingYear, 8, 20), ld(huntingYear, 8, 27)) ||
                isDateInRange(date, ld(huntingYear, 10, 1), ld(huntingYear, 11, 30))
    }

    // Karhu
    private fun isBearSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Starting from 2017
        if (huntingYear < 2017) {
            return false
        }

        // 20.8. - 31.10.
        return isDateInRange(date, ld(huntingYear, 8, 20), ld(huntingYear, 10, 31))
    }

    // Haahka
    private fun isCommonEiderSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Starting from 2019
        if (huntingYear < 2019) {
            return false
        }

        // 1.6. - 15.6.
        return isDateInRange(date, ld(huntingYear + 1, 6, 1), ld(huntingYear + 1, 6, 15))
    }

    // Nokikana
    private fun isCootSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Starting from 2020
        if (huntingYear < 2020) {
            return false
        }

        // 20.8. — 31.12.
        return isDateInRange(date, ld(huntingYear, 8, 20), ld(huntingYear, 12, 31))
    }

    // Heinätavi
    private fun isGarganeySeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Starting from 2020
        if (huntingYear < 2020) {
            return false
        }

        // 20.8. — 31.12.
        return isDateInRange(date, ld(huntingYear, 8, 20), ld(huntingYear, 12, 31))
    }

    // Isokoskelo
    private fun isGoosanderSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Starting from 2020
        if (huntingYear < 2020) {
            return false
        }

        // 1.9. — 31.12.
        return isDateInRange(date, ld(huntingYear, 9, 1), ld(huntingYear, 12, 31))
    }

    // Merihanhi
    private fun isGreylagGooseSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Starting from 2021
        if (huntingYear < 2021) {
            return false
        }

        // 10.8. — 31.12.
        return isDateInRange(date, ld(huntingYear, 8, 10), ld(huntingYear, 12, 31))
    }

    // Halli
    private fun isGreySealSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Starting from 2017
        if (huntingYear < 2017) {
            return false
        }

        // 1.8. - 31.12. and 16.4. - 31.7.
        return isDateInRange(date, ld(huntingYear, 8, 1), ld(huntingYear, 12, 31)) ||
                isDateInRange(date, ld(huntingYear + 1, 4, 16), ld(huntingYear + 1, 7, 31))
    }

    // Alli
    private fun isLongTailedDuckSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Starting from 2020
        if (huntingYear < 2020) {
            return false
        }

        // 1.9. — 31.12.
        return isDateInRange(date, ld(huntingYear, 9, 1), ld(huntingYear, 12, 31))
    }

    // Jouhisorsa
    private fun isPintailSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Starting from 2020
        if (huntingYear < 2020) {
            return false
        }

        // 20.8. — 31.12.
        return isDateInRange(date, ld(huntingYear, 8, 20), ld(huntingYear, 12, 31))
    }

    // Punasotka
    private fun isPochardSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Pochard is protected starting from 2020.
        return false
    }

    // Tukkakoskelo
    private fun isRedBreastedMerganserSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Red-breasted merganser is protected starting from 2020.
        return false
    }

    // Itämerennorppa
    private fun isRingedSealSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Starting from 2021
        if (huntingYear < 2021) {
            return false
        }

        // 1.8. - 31.12. and 16.4. - 31.7.
        return isDateInRange(date, ld(huntingYear, 8, 1), ld(huntingYear, 12, 31)) ||
                isDateInRange(date, ld(huntingYear + 1, 4, 16), ld(huntingYear + 1, 7, 31))
    }

    // Metsäkauris
    private fun isRoeDeerSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Starting from 2017
        if (huntingYear < 2017) {
            return false
        }

        // 1.9. - 15.2. and 16.5. - 15.6.
        return isDateInRange(date, ld(huntingYear, 9, 1), ld(huntingYear + 1, 2, 15)) ||
                isDateInRange(date, ld(huntingYear + 1, 5, 16), ld(huntingYear + 1, 6, 15))
    }

    // Lapasorsa
    private fun isSholeverSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Starting from 2020
        if (huntingYear < 2020) {
            return false
        }

        // 20.8. — 31.12.
        return isDateInRange(date, ld(huntingYear, 8, 20), ld(huntingYear, 12, 31))
    }

    // Tukkasotka
    private fun isTuftedDuckSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Starting from 2020
        if (huntingYear < 2020) {
            return false
        }

        // 20.8. — 31.12.
        return isDateInRange(date, ld(huntingYear, 8, 20), ld(huntingYear, 12, 31))
    }

    // Haapana
    private fun isWigeonSeason(date: LocalDate, huntingYear: HuntingYear): Boolean {
        // Starting from 2020
        if (huntingYear < 2020) {
            return false
        }

        // 20.8. — 31.12.
        return isDateInRange(date, ld(huntingYear, 8, 20), ld(huntingYear, 12, 31))
    }

    private fun isDateInRange(date: LocalDate, minDate: LocalDate, maxDate: LocalDate): Boolean {
        return date in minDate..maxDate
    }

    private fun ld(year: Int, month: Int, day: Int) = LocalDate(year, month, day)
}