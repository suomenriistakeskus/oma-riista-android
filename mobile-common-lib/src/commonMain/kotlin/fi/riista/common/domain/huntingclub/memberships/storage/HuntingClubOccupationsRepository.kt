package fi.riista.common.domain.huntingclub.memberships.storage

import fi.riista.common.database.DatabaseWriteContext
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.huntingclub.DbOccupation
import fi.riista.common.domain.huntingclub.ListUserOccupationsWithOrganizations
import fi.riista.common.domain.huntingclub.clubs.storage.HuntingClubStorage
import fi.riista.common.domain.model.Occupation
import fi.riista.common.domain.model.Organization
import fi.riista.common.dto.toLocalDate
import fi.riista.common.model.LocalizedString
import fi.riista.common.model.toBackendEnum
import kotlinx.coroutines.withContext

internal class HuntingClubOccupationsRepository(
    database: RiistaDatabase,
    private val clubStorage: HuntingClubStorage
): HuntingClubOccupationsStorage {
    private val membershipQueries = database.dbOccupationQueries

    override fun getOccupations(username: String): List<Occupation> {
        return membershipQueries.listUserOccupationsWithOrganizations(username).executeAsList()
            .map { it.toOccupation() }
    }

    override suspend fun replaceOccupations(
        username: String,
        occupations: List<Occupation>
    ) = withContext(DatabaseWriteContext) {
        // first ensure all organizations are stored
        clubStorage.addOrganizationsIfNotExists(
            organizations = occupations.map { it.organisation }
        )

        membershipQueries.transaction {
            membershipQueries.deleteUserOccupations(username = username)

            occupations.mapNotNull { it.toDbOccupation(username = username) }
                .forEach {
                    membershipQueries.insertOccupation(
                        occupation_remote_id = it.occupation_remote_id,
                        username = it.username,
                        occupation_type = it.occupation_type,
                        occupation_name_fi = it.occupation_name_fi,
                        occupation_name_sv = it.occupation_name_sv,
                        occupation_name_en = it.occupation_name_en,
                        occupation_begin_date = it.occupation_begin_date,
                        occupation_end_date = it.occupation_end_date,
                        organization_id = it.organization_id,
                    )
                }
        }
    }
}

internal fun ListUserOccupationsWithOrganizations.toOccupation(): Occupation {
    val organization = Organization(
        id = organization_remote_id,
        name = LocalizedString(
            fi = organization_name_fi,
            sv = organization_name_sv,
            en = organization_name_en,
        ),
        officialCode = organization_official_code
    )

    return Occupation(
        id = occupation_remote_id,
        occupationType = occupation_type.toBackendEnum(),
        name = LocalizedString(
            fi = occupation_name_fi,
            sv = occupation_name_sv,
            en = occupation_name_en
        ),
        beginDate = occupation_begin_date?.toLocalDate(),
        endDate = occupation_end_date?.toLocalDate(),
        organisation = organization,
    )
}

private fun Occupation.toDbOccupation(username: String): DbOccupation? {
    val occupationTypeString = occupationType.rawBackendEnumValue ?: return null

    return DbOccupation(
        occupation_remote_id = id,
        username = username,
        occupation_type = occupationTypeString,
        occupation_name_fi = name.fi,
        occupation_name_sv = name.sv,
        occupation_name_en = name.en,
        occupation_begin_date = beginDate?.toStringISO8601(),
        occupation_end_date = endDate?.toStringISO8601(),
        organization_id = organisation.id,
    )
}
