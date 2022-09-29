package fi.riista.common.domain.huntingControl.sync.model

import fi.riista.common.domain.huntingControl.model.*
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalTime

data class LoadHuntingControlEvent(
    val specVersion: Int,
    val id: HuntingControlEventId,
    val rev: Int,
    val mobileClientRefId: Long?,
    val eventType: BackendEnum<HuntingControlEventType>,
    val status: BackendEnum<HuntingControlEventStatus>,
    val inspectors: List<HuntingControlEventInspector>,
    val cooperationTypes: List<BackendEnum<HuntingControlCooperationType>>,
    val wolfTerritory: Boolean,
    val otherParticipants: String?,
    val geoLocation: ETRMSGeoLocation,
    val locationDescription: String?,
    val date: LocalDate,
    val beginTime: LocalTime,
    val endTime: LocalTime,
    val customers: Int,
    val proofOrders: Int,
    val description: String?,
    val attachments: List<HuntingControlAttachment>,
    val canEdit: Boolean,
)
