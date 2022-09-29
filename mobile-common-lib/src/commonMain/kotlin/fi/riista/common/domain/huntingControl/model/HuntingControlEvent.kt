package fi.riista.common.domain.huntingControl.model

import fi.riista.common.domain.model.OrganizationId
import fi.riista.common.model.*

typealias HuntingControlEventId = Long

data class HuntingControlEvent(
    val localId: Long,
    val remoteId: HuntingControlEventId?,
    val specVersion: Int,
    val rev: Revision?,
    val mobileClientRefId: Long?,
    val rhyId: OrganizationId,
    val eventType: BackendEnum<HuntingControlEventType>,
    val status: BackendEnum<HuntingControlEventStatus>,
    val inspectors: List<HuntingControlEventInspector>,
    val cooperationTypes: List<BackendEnum<HuntingControlCooperationType>>,
    val otherParticipants: String?,
    val geoLocation: ETRMSGeoLocation,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val wolfTerritory: Boolean,
    val description: String?,
    val locationDescription: String?,
    val proofOrderCount: Int,
    val customerCount: Int,
    val canEdit: Boolean,
    val modified: Boolean,
    val attachments: List<HuntingControlAttachment>,
)
