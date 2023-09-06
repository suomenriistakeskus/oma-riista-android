package fi.riista.common.domain.huntingclub.memberships

import fi.riista.common.domain.model.Occupation

interface HuntingClubOccupationsProvider {
    fun getOccupations(username: String): List<Occupation>
}
