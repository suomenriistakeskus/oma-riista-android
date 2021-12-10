package fi.riista.mobile.utils

import fi.riista.common.RiistaSDK
import fi.riista.mobile.riistaSdkHelpers.isDuringHuntingSeason
import org.joda.time.LocalDate

object HarvestSeasonUtil {

    @JvmStatic
    fun isInsideHuntingSeason(date: LocalDate, gameSpeciesCode: Int): Boolean {
        return RiistaSDK.harvestSeasons.isDuringHuntingSeason(gameSpeciesCode, date)
    }

}