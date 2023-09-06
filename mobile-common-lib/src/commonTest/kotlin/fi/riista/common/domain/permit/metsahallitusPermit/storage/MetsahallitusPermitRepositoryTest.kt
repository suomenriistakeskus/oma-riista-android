package fi.riista.common.domain.permit.metsahallitusPermit.storage

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.permit.metsahallitusPermit.model.CommonMetsahallitusPermit
import fi.riista.common.helpers.createDatabaseDriverFactory
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalizedString
import kotlin.test.Test
import kotlin.test.assertEquals

class MetsahallitusPermitRepositoryTest {
    @Test
    fun `conversion to DbMetsahallitusPermit and back`() {
        val permit = CommonMetsahallitusPermit(
            permitIdentifier = "permitIdentifier",
            permitType = LocalizedString(
                fi = "permitType_fi",
                sv = "permitType_sv",
                en = "permitType_en"
            ),
            permitName = LocalizedString(
                fi = "permitName_fi",
                sv = "permitName_sv",
                en = "permitName_en"
            ),
            areaNumber = "2",
            areaName = LocalizedString(
                fi = "areaName_fi",
                sv = "areaName_sv",
                en = "areaName_en"
            ),
            beginDate = LocalDate(2023, 5, 30),
            endDate = LocalDate(2023, 5, 31),
            harvestFeedbackUrl = LocalizedString(
                fi = "harvestFeedbackUrl_fi",
                sv = "harvestFeedbackUrl_sv",
                en = "harvestFeedbackUrl_en"
            ),
        )

        assertEquals(
            expected = permit,
            actual = permit.toDbMetsahallitusPermit("Pentti").toCommonMetsahallitusPermit()
        )
    }

    @Test
    fun `getting single permit should succeed`() = runBlockingTest {
        val repository = getRepository()
        val username = "user"

        repository.replacePermits(
            username = username,
            permits = listOf(
                createPermit("foo"),
                createPermit("bar"),
            )
        )

        repository.getPermit(username, permitIdentifier = "foo")
    }

    private fun createPermit(permitIdentifier: String): CommonMetsahallitusPermit {
        return CommonMetsahallitusPermit(
            permitIdentifier = permitIdentifier,
            permitType = LocalizedString(
                fi = "permitType_fi_$permitIdentifier",
                sv = "permitType_sv_$permitIdentifier",
                en = "permitType_en_$permitIdentifier"
            ),
            permitName = LocalizedString(
                fi = "permitName_fi_$permitIdentifier",
                sv = "permitName_sv_$permitIdentifier",
                en = "permitName_en_$permitIdentifier"
            ),
            areaNumber = "2",
            areaName = LocalizedString(
                fi = "areaName_fi_$permitIdentifier",
                sv = "areaName_sv_$permitIdentifier",
                en = "areaName_en_$permitIdentifier"
            ),
            beginDate = LocalDate(2023, 5, 30),
            endDate = LocalDate(2023, 5, 31),
            harvestFeedbackUrl = LocalizedString(
                fi = "harvestFeedbackUrl_fi_$permitIdentifier",
                sv = "harvestFeedbackUrl_sv_$permitIdentifier",
                en = "harvestFeedbackUrl_en_$permitIdentifier"
            ),
        )
    }

    private fun getRepository(): MetsahallitusPermitRepository {
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())
        return MetsahallitusPermitRepository(database)
    }
}
