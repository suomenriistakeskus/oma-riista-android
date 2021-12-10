package fi.riista.common.groupHunting

import fi.riista.common.model.HunterNumber

interface GroupHuntingAvailabilityResolver {
    /**
     * Is the group hunting functionality enabled for the specified hunter?
     */
    fun isGroupHuntingFunctionalityEnabledFor(hunterNumber: HunterNumber?): Boolean
}