package fi.riista.common.domain.huntingclub.clubs.storage

import fi.riista.common.domain.model.Organization
import fi.riista.common.domain.model.OrganizationId

internal interface HuntingClubProvider {
    fun findByRemoteId(organizationId: OrganizationId): Organization?
    fun findByOfficialCode(officialCode: String): Organization?
}
