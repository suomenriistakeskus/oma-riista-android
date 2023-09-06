package fi.riista.common.domain.permit.metsahallitusPermit.storage

import fi.riista.common.database.DatabaseWriteContext
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.permit.DbMetsahallitusPermit
import fi.riista.common.domain.permit.metsahallitusPermit.model.CommonMetsahallitusPermit
import fi.riista.common.dto.toLocalDate
import fi.riista.common.model.LocalizedString
import kotlinx.coroutines.withContext

internal class MetsahallitusPermitRepository(
    database: RiistaDatabase
): MetsahallitusPermitStorage {
    private val permitQueries = database.dbMetsahallitusPermitQueries

    override fun hasPermits(username: String): Boolean {
        return permitQueries.hasPermits(username = username).executeAsOne()
    }

    override fun getAllPermits(username: String): List<CommonMetsahallitusPermit> {
        return permitQueries.listPermits(
            username = username
        ).executeAsList().map { it.toCommonMetsahallitusPermit() }
    }

    override fun getPermit(username: String, permitIdentifier: String): CommonMetsahallitusPermit? {
        return permitQueries.selectPermit(
            username = username,
            permit_identifier = permitIdentifier
        ).executeAsOneOrNull()?.toCommonMetsahallitusPermit()
    }

    override suspend fun replacePermits(
        username: String,
        permits: List<CommonMetsahallitusPermit>,
    ) = withContext(DatabaseWriteContext) {
        permitQueries.transaction {
            permitQueries.deleteUserPermits(username = username)

            permits.map { it.toDbMetsahallitusPermit(username) }
                .forEach { dbPermit ->
                    permitQueries.insertPermit(
                        username = dbPermit.username,
                        permit_identifier = dbPermit.permit_identifier,
                        permit_type_fi = dbPermit.permit_type_fi,
                        permit_type_sv = dbPermit.permit_type_sv,
                        permit_type_en = dbPermit.permit_type_en,
                        permit_name_fi = dbPermit.permit_name_fi,
                        permit_name_sv = dbPermit.permit_name_sv,
                        permit_name_en = dbPermit.permit_name_en,
                        area_number = dbPermit.area_number,
                        area_name_fi = dbPermit.area_name_fi,
                        area_name_sv = dbPermit.area_name_sv,
                        area_name_en = dbPermit.area_name_en,
                        begin_date = dbPermit.begin_date,
                        end_date = dbPermit.end_date,
                        harvest_feedback_url_fi = dbPermit.harvest_feedback_url_fi,
                        harvest_feedback_url_sv = dbPermit.harvest_feedback_url_sv,
                        harvest_feedback_url_en = dbPermit.harvest_feedback_url_en,
                    )
                }
        }
    }
}

internal fun DbMetsahallitusPermit.toCommonMetsahallitusPermit() =
    CommonMetsahallitusPermit(
        permitIdentifier = permit_identifier,
        permitType = LocalizedString(
            fi = permit_type_fi,
            sv = permit_type_sv,
            en = permit_type_en
        ),
        permitName = LocalizedString(
            fi = permit_name_fi,
            sv = permit_name_sv,
            en = permit_name_en
        ),
        areaNumber = area_number,
        areaName =  LocalizedString(
            fi = area_name_fi,
            sv = area_name_sv,
            en = area_name_en
        ),
        beginDate = begin_date?.toLocalDate(),
        endDate = end_date?.toLocalDate(),
        harvestFeedbackUrl =  LocalizedString(
            fi = harvest_feedback_url_fi,
            sv = harvest_feedback_url_sv,
            en = harvest_feedback_url_en
        ),
    )

internal fun CommonMetsahallitusPermit.toDbMetsahallitusPermit(username: String) =
    DbMetsahallitusPermit(
        username = username,
        permit_identifier = permitIdentifier,
        permit_type_fi = permitType.fi,
        permit_type_sv = permitType.sv,
        permit_type_en = permitType.en,
        permit_name_fi = permitName.fi,
        permit_name_sv = permitName.sv,
        permit_name_en = permitName.en,
        area_number = areaNumber,
        area_name_fi = areaName.fi,
        area_name_sv = areaName.sv,
        area_name_en = areaName.en,
        begin_date = beginDate?.toStringISO8601(),
        end_date = endDate?.toStringISO8601(),
        harvest_feedback_url_fi = harvestFeedbackUrl?.fi,
        harvest_feedback_url_sv = harvestFeedbackUrl?.sv,
        harvest_feedback_url_en = harvestFeedbackUrl?.en,
    )
