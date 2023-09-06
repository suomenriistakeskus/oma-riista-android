package fi.riista.common.domain.huntingControl.ui.modify

import fi.riista.common.domain.huntingControl.model.HuntingControlAttachment
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalTime
import fi.riista.common.model.StringWithId

sealed class ModifyHuntingControlEventIntent {
    class ChangeEventType(val newEvenType: StringWithId) : ModifyHuntingControlEventIntent()
    class ChangeLocation(
        val newLocation: ETRMSGeoLocation,
        val locationChangedAfterUserInteraction: Boolean,
    ) : ModifyHuntingControlEventIntent()

    class ChangeNumberOfCustomers(val numberOfCustomers: Int?) : ModifyHuntingControlEventIntent()
    class ChangeNumberOfProofOrders(val numberOfProofOrders: Int?): ModifyHuntingControlEventIntent()
    class ChangeLocationDescription(val newDescription: String) : ModifyHuntingControlEventIntent()
    class ChangeWolfTerritory(val newWolfTerritory: Boolean) : ModifyHuntingControlEventIntent()
    class ChangeDate(val newDate: LocalDate) : ModifyHuntingControlEventIntent()
    class ChangeStartTime(val newStartTime: LocalTime) : ModifyHuntingControlEventIntent()
    class ChangeEndTime(val newEndTime: LocalTime) : ModifyHuntingControlEventIntent()
    class ChangeOtherPartisipants(val newOtherPartisipants: String) : ModifyHuntingControlEventIntent()
    class ChangeInspectors(val newInspectors: List<StringWithId>) : ModifyHuntingControlEventIntent()
    class RemoveInspectors(val removeInspector: StringWithId) : ModifyHuntingControlEventIntent()
    class ToggleCooperationType(val toggledCooperationType: StringWithId) : ModifyHuntingControlEventIntent()
    class ChangeDescription(val newDescription: String) : ModifyHuntingControlEventIntent()
    class DeleteAttachment(val index: Int) : ModifyHuntingControlEventIntent()
    class AddAttachment(val newAttachment: HuntingControlAttachment): ModifyHuntingControlEventIntent()
}
