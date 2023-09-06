package fi.riista.common.domain.huntingclub.clubs.storage

import fi.riista.common.domain.model.Organization

internal interface HuntingClubStorage: HuntingClubProvider {
    suspend fun addOrganizationsIfNotExists(organizations: List<Organization>)
}
