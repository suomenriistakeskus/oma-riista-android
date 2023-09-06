package fi.riista.common.domain.huntingclub.selectableForEntries

import fi.riista.common.domain.model.Organization

/**
 * A provider for accessing user hunting clubs.
 *
 * For example when creating/updating a harvest, user can select a hunting club for which the harvest
 * will be logged / recorded. This provider implementation can allow getting those.
 */
internal interface UserHuntingClubsProvider {
    fun getClubs(username: String): List<Organization>
    fun findClub(username: String, clubOfficialCode: String): Organization?
}
