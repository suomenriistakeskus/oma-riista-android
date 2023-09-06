package fi.riista.common.domain.shootingTest.model

import fi.riista.common.model.RepresentsBackendEnum

enum class ShootingTestRegistrationStatus(
    override val rawBackendEnumValue: String,
): RepresentsBackendEnum {
    HUNTING_PAYMENT_DONE("HUNTING_PAYMENT_DONE"),
    HUNTING_PAYMENT_NOT_DONE("HUNTING_PAYMENT_NOT_DONE"),
    IN_PROGRESS("IN_PROGRESS"),
    COMPLETED("COMPLETED"),
    DISQUALIFIED_AS_OFFICIAL("DISQUALIFIED_AS_OFFICIAL"),
    NO_HUNTER_NUMBER("NO_HUNTER_NUMBER"),
    HUNTING_BAN("HUNTING_BAN"),
    FOREIGN_HUNTER("FOREIGN_HUNTER"),
    ;
}
