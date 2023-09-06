package fi.riista.common.domain.shootingTest.model

typealias ShootingTestOfficialId = Long

data class CommonShootingTestOfficial(
    val id: ShootingTestOfficialId?,
    val shootingTestEventId: ShootingTestEventId?,
    val occupationId: Long,
    val personId: Long,
    val firstName: String?,
    val lastName: String?,
    val shootingTestResponsible: Boolean,
)
