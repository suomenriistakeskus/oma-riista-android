package fi.riista.common.domain.shootingTest

import fi.riista.common.domain.OperationResult
import fi.riista.common.domain.OperationResultWithData
import fi.riista.common.domain.model.ShootingTestType
import fi.riista.common.domain.shootingTest.dto.OpenShootingTestEventDTO
import fi.riista.common.domain.shootingTest.dto.SelectedShootingTestTypesDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestAttemptCreateDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestAttemptDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestParticipantCreateDTO
import fi.riista.common.domain.shootingTest.dto.ShootingTestPaymentUpdateDTO
import fi.riista.common.domain.shootingTest.dto.UpdateShootingTestOfficialsDTO
import fi.riista.common.domain.shootingTest.dto.toCommonShootingTestAttempt
import fi.riista.common.domain.shootingTest.dto.toCommonShootingTestCalendarEvent
import fi.riista.common.domain.shootingTest.dto.toCommonShootingTestOfficial
import fi.riista.common.domain.shootingTest.dto.toCommonShootingTestParticipant
import fi.riista.common.domain.shootingTest.dto.toCommonShootingTestParticipantDetailed
import fi.riista.common.domain.shootingTest.dto.toShootingTestPerson
import fi.riista.common.domain.shootingTest.model.CalendarEventId
import fi.riista.common.domain.shootingTest.model.CommonShootingTestAttempt
import fi.riista.common.domain.shootingTest.model.CommonShootingTestCalendarEvent
import fi.riista.common.domain.shootingTest.model.CommonShootingTestOfficial
import fi.riista.common.domain.shootingTest.model.CommonShootingTestParticipant
import fi.riista.common.domain.shootingTest.model.CommonShootingTestParticipantDetailed
import fi.riista.common.domain.shootingTest.model.CommonShootingTestPerson
import fi.riista.common.domain.shootingTest.model.ShootingTestAttemptId
import fi.riista.common.domain.shootingTest.model.ShootingTestEventId
import fi.riista.common.domain.shootingTest.model.ShootingTestParticipantId
import fi.riista.common.domain.shootingTest.model.ShootingTestResult
import fi.riista.common.logging.getLogger
import fi.riista.common.model.Revision
import fi.riista.common.network.BackendApiProvider

class ShootingTestContext internal constructor(
    backendApiProvider: BackendApiProvider,
): BackendApiProvider by backendApiProvider {

    suspend fun fetchShootingTestCalendarEvents() : OperationResultWithData<List<CommonShootingTestCalendarEvent>> {
        val response = backendAPI.fetchShootingTestCalendarEvents()
        response.onSuccess { statusCode, data ->
            return OperationResultWithData.Success(
                statusCode = statusCode,
                data = data.typed.map { it.toCommonShootingTestCalendarEvent() },
            )
        }
        response.onError { statusCode, exception ->
            logger.w { "Fetching shooting test calendar events failed: $statusCode (${exception?.message})" }
            return OperationResultWithData.Failure(statusCode = statusCode)
        }
        return OperationResultWithData.Failure(statusCode = null)
    }

    suspend fun fetchShootingTestCalendarEvent(
        calendarEventId: CalendarEventId,
    ): OperationResultWithData<CommonShootingTestCalendarEvent> {
        val response = backendAPI.fetchShootingTestCalendarEvent(calendarEventId)
        response.onSuccess { statusCode, data ->
            return OperationResultWithData.Success(
                statusCode = statusCode,
                data = data.typed.toCommonShootingTestCalendarEvent(),
            )
        }
        response.onError { statusCode, exception ->
            logger.w { "Fetching shooting test calendar event failed: $statusCode (${exception?.message})" }
            return OperationResultWithData.Failure(statusCode = statusCode)
        }
        return OperationResultWithData.Failure(statusCode = null)
    }

    suspend fun openShootingTestEvent(
        calendarEventId: CalendarEventId,
        shootingTestEventId: ShootingTestEventId?,
        occupationIds: List<Long>,
        responsibleOccupationId: Long?,
    ) : OperationResult {
        val dto = OpenShootingTestEventDTO(
            calendarEventId = calendarEventId,
            shootingTestEventId = shootingTestEventId,
            occupationIds = occupationIds,
            responsibleOccupationId = responsibleOccupationId,
        )
        val response = backendAPI.openShootingTestEvent(dto)
        response.onSuccessWithoutData { statusCode ->
            return OperationResult.Success(statusCode = statusCode)
        }
        response.onError { statusCode, exception ->
            logger.w { "Opening shooting test failed: $statusCode (${exception?.message})" }
            return OperationResult.Failure(statusCode = statusCode)
        }
        return OperationResult.Failure(statusCode = null)
    }

    suspend fun closeShootingTestEvent(shootingTestEventId: ShootingTestEventId): OperationResult {
        val response = backendAPI.closeShootingTestEvent(shootingTestEventId)
        response.onSuccessWithoutData { statusCode ->
            return OperationResult.Success(statusCode = statusCode)
        }
        response.onError { statusCode, exception ->
            logger.w { "Closing shooting test failed: $statusCode (${exception?.message})" }
            return OperationResult.Failure(statusCode = statusCode)
        }
        return OperationResult.Failure(statusCode = null)
    }

    suspend fun reopenShootingTestEvent(shootingTestEventId: ShootingTestEventId): OperationResult {
        val response = backendAPI.reopenShootingTestEvent(shootingTestEventId)
        response.onSuccessWithoutData { statusCode ->
            return OperationResult.Success(statusCode = statusCode)
        }
        response.onError { statusCode, exception ->
            logger.w { "Reopening shooting test failed: $statusCode (${exception?.message})" }
            return OperationResult.Failure(statusCode = statusCode)
        }
        return OperationResult.Failure(statusCode = null)
    }

    suspend fun fetchAvailableShootingTestOfficialsForEvent(
        shootingTestEventId: ShootingTestEventId,
    ): OperationResultWithData<List<CommonShootingTestOfficial>> {
        val response = backendAPI.fetchAvailableShootingTestOfficialsForEvent(shootingTestEventId)
        response.onSuccess { statusCode, data ->
            return OperationResultWithData.Success(
                statusCode = statusCode,
                data = data.typed.map { it.toCommonShootingTestOfficial() },
            )
        }
        response.onError { statusCode, exception ->
            logger.w { "Fetching available shooting test officials for event failed: $statusCode (${exception?.message})" }
            return OperationResultWithData.Failure(statusCode = statusCode)
        }
        return OperationResultWithData.Failure(statusCode = null)
    }

    suspend fun fetchSelectedShootingTestOfficialsForEvent(
        shootingTestEventId: ShootingTestEventId,
    ): OperationResultWithData<List<CommonShootingTestOfficial>> {
        val response = backendAPI.fetchSelectedShootingTestOfficialsForEvent(shootingTestEventId)
        response.onSuccess { statusCode, data ->
            return OperationResultWithData.Success(
                statusCode = statusCode,
                data = data.typed.map { it.toCommonShootingTestOfficial() },
            )
        }
        response.onError { statusCode, exception ->
            logger.w { "Fetching selected shooting test officials for event failed: $statusCode (${exception?.message})" }
            return OperationResultWithData.Failure(statusCode = statusCode)
        }
        return OperationResultWithData.Failure(statusCode = null)
    }

    suspend fun fetchAvailableShootingTestOfficialsForRhy(
        rhyId: Long,
    ): OperationResultWithData<List<CommonShootingTestOfficial>> {
        val response = backendAPI.fetchAvailableShootingTestOfficialsForRhy(rhyId)
        response.onSuccess { statusCode, data ->
            return OperationResultWithData.Success(
                statusCode = statusCode,
                data = data.typed.map { it.toCommonShootingTestOfficial() }
            )
        }
        response.onError { statusCode, exception ->
            logger.w { "Fetching available shooting test officials for RHY failed: $statusCode (${exception?.message})" }
            return OperationResultWithData.Failure(statusCode = statusCode)
        }
        return OperationResultWithData.Failure(statusCode = null)
    }

    suspend fun updateShootingTestOfficials(
        calendarEventId: CalendarEventId,
        shootingTestEventId: ShootingTestEventId,
        officialOccupationIds: List<Long>,
        responsibleOccupationId: Long?,
    ): OperationResult {
        val dto = UpdateShootingTestOfficialsDTO(
            calendarEventId = calendarEventId,
            shootingTestEventId = shootingTestEventId,
            occupationIds = officialOccupationIds,
            responsibleOccupationId = responsibleOccupationId,
        )
        val response = backendAPI.updateShootingTestOfficials(dto)
        response.onSuccessWithoutData { statusCode ->
            return OperationResult.Success(statusCode = statusCode)
        }
        response.onError { statusCode, exception ->
            logger.w { "Updating shooting test officials failed: $statusCode (${exception?.message})" }
            return OperationResult.Failure(statusCode = statusCode)
        }
        return OperationResult.Failure(statusCode = null)
    }

    suspend fun searchPersonBySsn(
        shootingTestEventId: ShootingTestEventId,
        ssn: String,
    ): OperationResultWithData<CommonShootingTestPerson> {
        val response = backendAPI.searchShootingTestPersonWithSsn(shootingTestEventId, ssn)
        response.onSuccess { statusCode, data ->
            return OperationResultWithData.Success(
                statusCode = statusCode,
                data = data.typed.toShootingTestPerson(),
            )
        }
        response.onError { statusCode, exception ->
            logger.w { "Search shooting test person with SSN failed: $statusCode (${exception?.message})" }
            return OperationResultWithData.Failure(statusCode = statusCode)
        }
        return OperationResultWithData.Failure(statusCode = null)
    }

    suspend fun searchPersonByHunterNumber(
        shootingTestEventId: ShootingTestEventId,
        hunterNumber: String,
    ): OperationResultWithData<CommonShootingTestPerson> {
        val response = backendAPI.searchShootingTestPersonWithHunterNumber(shootingTestEventId, hunterNumber)
        response.onSuccess { statusCode, data ->
            return OperationResultWithData.Success(
                statusCode = statusCode,
                data = data.typed.toShootingTestPerson(),
            )
        }
        response.onError { statusCode, exception ->
            logger.w { "Searching shooting test person with hunter number failed: $statusCode (${exception?.message})" }
            return OperationResultWithData.Failure(statusCode = statusCode)
        }
        return OperationResultWithData.Failure(statusCode = null)
    }

    suspend fun fetchShootingTestParticipants(
        shootingTestEventId: ShootingTestEventId,
    ): OperationResultWithData<List<CommonShootingTestParticipant>> {
        val response = backendAPI.fetchShootingTestParticipants(shootingTestEventId)
        response.onSuccess { statusCode, data ->
            return OperationResultWithData.Success(
                statusCode = statusCode,
                data.typed.map { it.toCommonShootingTestParticipant() },
            )
        }
        response.onError { statusCode, exception ->
            logger.w { "Fetching shooting test participants failed: $statusCode (${exception?.message})" }
            return OperationResultWithData.Failure(statusCode = statusCode)
        }
        return OperationResultWithData.Failure(statusCode = null)
    }

    suspend fun fetchShootingTestParticipant(
        participantId: ShootingTestParticipantId,
    ): OperationResultWithData<CommonShootingTestParticipant> {
        val response = backendAPI.fetchShootingTestParticipant(participantId)
        response.onSuccess { statusCode, data ->
            return OperationResultWithData.Success(
                statusCode = statusCode,
                data = data.typed.toCommonShootingTestParticipant()
            )
        }
        response.onError { statusCode, exception ->
            logger.w { "Fetching shooting test participant failed: $statusCode (${exception?.message})" }
            return OperationResultWithData.Failure(statusCode = statusCode)
        }
        return OperationResultWithData.Failure(statusCode = null)
    }

    suspend fun addShootingTestParticipant(
        shootingTestEventId: ShootingTestEventId,
        hunterNumber: String,
        mooseTestIntended: Boolean,
        bearTestIntended: Boolean,
        roeDeerTestIntended: Boolean,
        bowTestIntended: Boolean,
    ): OperationResult {
        val dto = ShootingTestParticipantCreateDTO(
            hunterNumber = hunterNumber,
            selectedTypes = SelectedShootingTestTypesDTO(
                mooseTestIntended = mooseTestIntended,
                bearTestIntended = bearTestIntended,
                roeDeerTestIntended = roeDeerTestIntended,
                bowTestIntended = bowTestIntended,
            )
        )
        val response = backendAPI.addShootingTestParticipant(shootingTestEventId, dto)
        response.onSuccessWithoutData { statusCode ->
            return OperationResult.Success(statusCode = statusCode)
        }
        response.onError { statusCode, exception ->
            logger.w { "Adding shooting test participant failed: $statusCode (${exception?.message})" }
            return OperationResult.Failure(statusCode = statusCode)
        }
        return OperationResult.Failure(statusCode = null)
    }

    suspend fun fetchShootingTestParticipantDetailed(
        participantId: ShootingTestParticipantId,
    ): OperationResultWithData<CommonShootingTestParticipantDetailed> {
        val response = backendAPI.fetchShootingTestParticipantDetailed(participantId)
        response.onSuccess { statusCode, data ->
            return OperationResultWithData.Success(
                statusCode = statusCode,
                data = data.typed.toCommonShootingTestParticipantDetailed()
            )
        }
        response.onError { statusCode, exception ->
            logger.w { "Fetching shooting test participant details failed: $statusCode (${exception?.message}" }
            return OperationResultWithData.Failure(statusCode = statusCode)
        }
        return OperationResultWithData.Failure(statusCode = null)
    }

    suspend fun fetchShootingTestAttempt(
        shootingTestAttemptId: ShootingTestAttemptId,
    ): OperationResultWithData<CommonShootingTestAttempt> {
        val response = backendAPI.fetchShootingTestAttempt(shootingTestAttemptId)
        response.onSuccess { statusCode, data ->
            return OperationResultWithData.Success(
                statusCode = statusCode,
                data = data.typed.toCommonShootingTestAttempt()
            )
        }
        response.onError { statusCode, exception ->
            logger.w { "Fetching shooting test attempt failed: $statusCode ${exception?.message}" }
            return OperationResultWithData.Failure(statusCode = statusCode)
        }
        return OperationResultWithData.Failure(statusCode = null)
    }

    suspend fun addShootingTestAttempt(
        participantId: ShootingTestParticipantId,
        participantRev: Revision,
        type: ShootingTestType,
        result: ShootingTestResult,
        hits: Int,
        note: String?,
    ): OperationResult {
        val dto = ShootingTestAttemptCreateDTO(
            participantId = participantId,
            participantRev = participantRev,
            type = type.rawBackendEnumValue,
            result = result.rawBackendEnumValue,
            hits = hits,
            note = note,
        )
        val response = backendAPI.addShootingTestAttempt(dto)
        response.onSuccessWithoutData { statusCode ->
            return OperationResult.Success(statusCode = statusCode)
        }
        response.onError { statusCode, exception ->
            logger.w { "Adding shooting test attempt failed: $statusCode (${exception?.message})" }
            return OperationResult.Failure(statusCode = statusCode)
        }
        return OperationResult.Failure(statusCode = null)
    }

    suspend fun updateShootingTestAttempt(
        id: Long,
        rev: Int,
        participantId: ShootingTestParticipantId,
        participantRev: Revision,
        type: ShootingTestType,
        result: ShootingTestResult,
        hits: Int,
        note: String?,
    ): OperationResult {
        val dto = ShootingTestAttemptDTO(
            id = id,
            rev = rev,
            participantId = participantId,
            participantRev = participantRev,
            type = type.rawBackendEnumValue,
            result = result.rawBackendEnumValue,
            hits = hits,
            note = note,
        )
        val response = backendAPI.updateShootingTestAttempt(dto)
        response.onSuccessWithoutData { statusCode ->
            return OperationResult.Success(statusCode = statusCode)
        }
        response.onError { statusCode, exception ->
            logger.w { "Updating shooting test attempt failed: $statusCode (${exception?.message})" }
            return OperationResult.Failure(statusCode = statusCode)
        }
        return OperationResult.Failure(statusCode = null)
    }

    suspend fun removeShootingTestAttempt(shootingTestAttemptId: ShootingTestAttemptId): OperationResult {
        val response = backendAPI.removeShootingTestAttempt(shootingTestAttemptId)
        response.onSuccessWithoutData { statusCode ->
            return OperationResult.Success(statusCode = statusCode)
        }
        response.onError { statusCode, exception ->
            logger.w { "Removing shooting test attempt failed: $statusCode (${exception?.message})" }
            return OperationResult.Failure(statusCode = statusCode)
        }
        return OperationResult.Failure(statusCode = null)
    }

    suspend fun updatePaymentStateForParticipant(
        participantId: ShootingTestParticipantId,
        participantRev: Revision,
        paidAttempts: Int,
        completed: Boolean,
    ): OperationResult {
        val dto = ShootingTestPaymentUpdateDTO(
            rev = participantRev,
            paidAttempts = paidAttempts,
            completed = completed,
        )
        val response = backendAPI.updateShootingTestPaymentForParticipant(
            participantId = participantId,
            paymentUpdateDTO = dto,
        )
        response.onSuccessWithoutData { statusCode ->
            return OperationResult.Success(statusCode = statusCode)
        }
        response.onError { statusCode, exception ->
            logger.w { "Updating shooting test payment failed: $statusCode (${exception?.message})" }
            return OperationResult.Failure(statusCode)
        }
        return OperationResult.Failure(statusCode = null)
    }

    suspend fun completeAllPaymentsForParticipant(
        participantId: ShootingTestParticipantId,
        participantRev: Revision,
    ): OperationResult {
        val response = backendAPI.completeAllPaymentsForParticipant(
            participantId = participantId,
            participantRev = participantRev,
        )
        response.onSuccessWithoutData { statusCode ->
            return OperationResult.Success(statusCode = statusCode)
        }
        response.onError { statusCode, exception ->
            logger.w { "Completing all payments for participant failed: $statusCode (${exception?.message})" }
            return OperationResult.Failure(statusCode = statusCode)
        }
        return OperationResult.Failure(statusCode = null)
    }

    companion object {
        private val logger by getLogger(ShootingTestContext::class)
    }
}
