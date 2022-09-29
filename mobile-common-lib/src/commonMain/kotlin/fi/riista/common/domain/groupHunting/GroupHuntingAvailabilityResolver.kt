package fi.riista.common.domain.groupHunting

import fi.riista.common.domain.model.HunterNumber

interface GroupHuntingAvailabilityResolver {
    /**
     * Is the group hunting functionality enabled for the specified hunter?
     */
    fun isGroupHuntingFunctionalityEnabledFor(hunterNumber: HunterNumber?): Boolean
}