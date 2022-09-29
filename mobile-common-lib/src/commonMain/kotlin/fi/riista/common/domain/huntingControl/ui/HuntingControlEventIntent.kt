package fi.riista.common.domain.huntingControl.ui

import fi.riista.common.domain.huntingControl.model.HuntingControlAttachment
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalTime
import fi.riista.common.model.StringWithId

sealed class HuntingControlEventIntent {
    class ChangeEventType(val newEvenType: StringWithId) : HuntingControlEventIntent()
    class ChangeLocation(
        val newLocation: ETRMSGeoLocation,
        val locationChangedAfterUserInteraction: Boolean,
    ) : HuntingControlEventIntent()

    class ChangeNumberOfCustomers(val numberOfCustomers: Int?) : HuntingControlEventIntent()
    class ChangeNumberOfProofOrders(val numberOfProofOrders: Int?): HuntingControlEventIntent()
    class ChangeLocationDescription(val newDescription: String) : HuntingControlEventIntent()
    class ChangeWolfTerritory(val newWolfTerritory: Boolean) : HuntingControlEventIntent()
    class ChangeDate(val newDate: LocalDate) : HuntingControlEventIntent()
    class ChangeStartTime(val newStartTime: LocalTime) : HuntingControlEventIntent()
    class ChangeEndTime(val newEndTime: LocalTime) : HuntingControlEventIntent()
    class ChangeOtherPartisipants(val newOtherPartisipants: String) : HuntingControlEventIntent()
    class ChangeInspectors(val newInspectors: List<StringWithId>) : HuntingControlEventIntent()
    class RemoveInspectors(val removeInspector: StringWithId) : HuntingControlEventIntent()
    class ToggleCooperationType(val toggledCooperationType: StringWithId) : HuntingControlEventIntent()
    class ChangeDescription(val newDescription: String) : HuntingControlEventIntent()
    class DeleteAttachment(val index: Int) : HuntingControlEventIntent()
    class AddAttachment(val newAttachment: HuntingControlAttachment): HuntingControlEventIntent()
}
