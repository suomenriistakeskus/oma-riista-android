package fi.riista.common.domain.shootingTest.model

import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class ShootingTestResult(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string
): RepresentsBackendEnum, LocalizableEnum {
    QUALIFIED("QUALIFIED", RR.string.shooting_test_result_qualified),
    UNQUALIFIED("UNQUALIFIED", RR.string.shooting_test_result_unqualified),
    TIMED_OUT("TIMED_OUT", RR.string.shooting_test_result_timed_out),
    REBATED("REBATED", RR.string.shooting_test_result_rebated),
    ;
}
