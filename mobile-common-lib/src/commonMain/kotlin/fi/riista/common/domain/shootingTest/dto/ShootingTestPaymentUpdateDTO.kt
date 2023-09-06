package fi.riista.common.domain.shootingTest.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShootingTestPaymentUpdateDTO(
    val rev: Int,
    val paidAttempts: Int,
    val completed: Boolean,
)
