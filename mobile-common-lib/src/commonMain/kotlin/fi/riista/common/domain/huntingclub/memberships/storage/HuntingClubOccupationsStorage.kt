package fi.riista.common.domain.huntingclub.memberships.storage

import fi.riista.common.domain.huntingclub.memberships.HuntingClubOccupationsProvider
import fi.riista.common.domain.model.Occupation

internal interface HuntingClubOccupationsStorage: HuntingClubOccupationsProvider {
    suspend fun replaceOccupations(username: String, occupations: List<Occupation>)
}
