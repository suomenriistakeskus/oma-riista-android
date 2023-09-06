package fi.riista.common.domain.permit.metsahallitusPermit.dto

import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalizedString
import kotlin.test.Test
import kotlin.test.assertEquals

class CommonMetsahallitusPermitDTOTest {
    @Test
    fun `conversion to CommonMetsahallitusPermit`() {
        val dto = CommonMetsahallitusPermitDTO(
            permitIdentifier = "permitIdentifier",
            permitType = LocalizedStringDTO(
                fi = "permitType_fi",
                sv = "permitType_sv",
                en = "permitType_en"
            ),
            permitName = LocalizedStringDTO(
                fi = "permitName_fi",
                sv = "permitName_sv",
                en = "permitName_en"
            ),
            areaNumber = "2",
            areaName = LocalizedStringDTO(
                fi = "areaName_fi",
                sv = "areaName_sv",
                en = "areaName_en"
            ),
            beginDate = "2023-05-30",
            endDate = "2023-05-31",
            harvestFeedbackUrl = LocalizedStringDTO(
                fi = "harvestFeedbackUrl_fi",
                sv = "harvestFeedbackUrl_sv",
                en = "harvestFeedbackUrl_en"
            ),
        )

        with (dto.toCommonMetsahallitusPermit()) {
            assertEquals(
                expected = "permitIdentifier",
                actual = permitIdentifier
            )
            assertEquals(
                expected = LocalizedString(
                    fi = "permitType_fi",
                    sv = "permitType_sv",
                    en = "permitType_en"
                ),
                actual = permitType
            )
            assertEquals(
                expected = LocalizedString(
                    fi = "permitName_fi",
                    sv = "permitName_sv",
                    en = "permitName_en"
                ),
                actual = permitName
            )
            assertEquals(
                expected = "2",
                actual = areaNumber
            )
            assertEquals(
                expected = LocalizedString(
                    fi = "areaName_fi",
                    sv = "areaName_sv",
                    en = "areaName_en"
                ),
                actual = areaName
            )
            assertEquals(
                expected = LocalDate(2023, 5, 30),
                actual = beginDate
            )
            assertEquals(
                expected = LocalDate(2023, 5, 31),
                actual = endDate
            )
            assertEquals(
                expected = LocalizedString(
                    fi = "harvestFeedbackUrl_fi",
                    sv = "harvestFeedbackUrl_sv",
                    en = "harvestFeedbackUrl_en"
                ),
                actual = harvestFeedbackUrl
            )
        }
    }
}