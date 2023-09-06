package fi.riista.common.domain.huntingclub.selectableForEntries

import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.ui.controller.HasUnreproducibleState

/**
 * Manages the hunting clubs that are selectable for entries.
 *
 * The idea is that each time e.g. harvest edit / create is started a new [HuntingClubsSelectableForEntries]
 * instance should be created.
 */
interface HuntingClubsSelectableForEntries {
    /**
     * Gets the hunting clubs that are available to be selected for an entry (e.g. harvest)
     */
    fun getClubsSelectableForEntries(): List<Organization>

    /**
     * Tries to find a selectable club that has the given [organizationId].
     */
    fun findSelectableClub(organizationId: OrganizationId): Organization?

    /**
     * Search for a hunting club that has the given official code.
     */
    suspend fun searchClubByOfficialCode(officialCode: String): Organization?

    /**
     * Gets the hunting clubs that have not yet been persisted to permanent storage.
     *
     * This allows storing these e.g. to unreproducible state (see [HasUnreproducibleState]) and
     * not losing them if memory is released while app is on background (e.g. activity is destroyed).
     */
    fun getNonPersistentClubs(): List<Organization>

    /**
     * Restores the non-persistent clubs e.g. after activity has been destroyed
     */
    fun restoreNonPersistentClubs(clubs: List<Organization>)

    interface Factory {
        /**
         * Create a new [HuntingClubsSelectableForEntries] instance using this method.
         */
        fun create(): HuntingClubsSelectableForEntries
    }
}
