package fi.riista.common.domain.huntingControl.ui.modify

import fi.riista.common.domain.huntingControl.ui.HuntingControlEventField
import fi.riista.common.ui.dataField.ActionEventDispatcher
import fi.riista.common.ui.dataField.AttachmentEventDispatcher
import fi.riista.common.ui.dataField.BooleanEventDispatcher
import fi.riista.common.ui.dataField.IntEventDispatcher
import fi.riista.common.ui.dataField.LocalDateEventDispatcher
import fi.riista.common.ui.dataField.LocalTimeEventDispatcher
import fi.riista.common.ui.dataField.LocationEventDispatcher
import fi.riista.common.ui.dataField.StringEventDispatcher
import fi.riista.common.ui.dataField.StringWithIdClickEventDispatcher
import fi.riista.common.ui.dataField.StringWithIdEventDispatcher
import fi.riista.common.ui.intent.IntentHandler

internal class ModifyHuntingControlEventEventToIntentMapper(
    private val intentHandler: IntentHandler<ModifyHuntingControlEventIntent>
) : ModifyHuntingControlEventEventDispatcher {

    override val stringWithIdEventDispatcher = StringWithIdEventDispatcher<HuntingControlEventField> { fieldId, value ->
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.EVENT_TYPE -> {
                if (value.size == 1) {
                    ModifyHuntingControlEventIntent.ChangeEventType(value[0])
                } else {
                    throw RuntimeException("Wrong number of values for field $fieldId (value: $value)")
                }
            }
            HuntingControlEventField.Type.INSPECTORS -> ModifyHuntingControlEventIntent.ChangeInspectors(value)
            else -> throw createUnexpectedEventException(fieldId, "StringWithId", value)
        }
        intentHandler.handleIntent(intent)
    }

    override val stringWithIdClickEventDispatcher = StringWithIdClickEventDispatcher<HuntingControlEventField> { fieldId, value ->
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.COOPERATION -> ModifyHuntingControlEventIntent.ToggleCooperationType(value)
            HuntingControlEventField.Type.INSPECTOR_NAMES -> ModifyHuntingControlEventIntent.RemoveInspectors(value)
            else -> throw createUnexpectedEventException(fieldId, "StringWithIdClickEvent", value)
        }
        intentHandler.handleIntent(intent)
    }

    override val locationEventDispatcher = LocationEventDispatcher<HuntingControlEventField>  { _, value ->
        intentHandler.handleIntent(
            ModifyHuntingControlEventIntent.ChangeLocation(
                newLocation = value,
                locationChangedAfterUserInteraction = true,
            )
        )
    }

    override val intEventDispatcher = IntEventDispatcher<HuntingControlEventField> { fieldId, value ->
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.NUMBER_OF_CUSTOMERS -> ModifyHuntingControlEventIntent.ChangeNumberOfCustomers(value)
            HuntingControlEventField.Type.NUMBER_OF_PROOF_ORDERS -> ModifyHuntingControlEventIntent.ChangeNumberOfProofOrders(value)
            else -> throw createUnexpectedEventException(fieldId, "IntEvent", value)
        }
        intentHandler.handleIntent(intent)
    }

    override val stringEventDispatcher =  StringEventDispatcher<HuntingControlEventField> { fieldId, value ->
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.LOCATION_DESCRIPTION -> ModifyHuntingControlEventIntent.ChangeLocationDescription(
                value
            )
            HuntingControlEventField.Type.EVENT_DESCRIPTION -> ModifyHuntingControlEventIntent.ChangeDescription(value)
            HuntingControlEventField.Type.OTHER_PARTICIPANTS -> ModifyHuntingControlEventIntent.ChangeOtherPartisipants(value)
            else -> throw createUnexpectedEventException(fieldId, "StringEvent", value)
        }
        intentHandler.handleIntent(intent)
    }

    override val booleanEventDispatcher = BooleanEventDispatcher<HuntingControlEventField> { fieldId, value ->
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.WOLF_TERRITORY -> ModifyHuntingControlEventIntent.ChangeWolfTerritory(value)
            else -> throw createUnexpectedEventException(fieldId, "BooleanEvent", value)
        }
        intentHandler.handleIntent(intent)
    }

    override val localDateEventDispatcher = LocalDateEventDispatcher<HuntingControlEventField> { fieldId, value ->
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.DATE -> ModifyHuntingControlEventIntent.ChangeDate(value)
            else -> throw createUnexpectedEventException(fieldId, "LocalDateEvent", value)
        }
        intentHandler.handleIntent(intent)
    }

    override val localTimeEventDispatcher = LocalTimeEventDispatcher<HuntingControlEventField> { fieldId, value ->
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.START_TIME -> ModifyHuntingControlEventIntent.ChangeStartTime(value)
            HuntingControlEventField.Type.END_TIME -> ModifyHuntingControlEventIntent.ChangeEndTime(value)
            else -> throw createUnexpectedEventException(fieldId, "LocalTimeEvent", value)
        }
        intentHandler.handleIntent(intent)
    }

    override val attachmentActionEventDispatcher = ActionEventDispatcher<HuntingControlEventField> { fieldId ->
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.ATTACHMENT -> ModifyHuntingControlEventIntent.DeleteAttachment(fieldId.index)
            else -> throw createUnexpectedEventException(fieldId, "ActionEvent")
        }
        intentHandler.handleIntent(intent)
    }

    override val attachmentEventDispatcher = AttachmentEventDispatcher<HuntingControlEventField> { attachment ->
        val intent = ModifyHuntingControlEventIntent.AddAttachment(attachment)
        intentHandler.handleIntent(intent)
    }

    private fun createUnexpectedEventException(
        fieldId: HuntingControlEventField,
        eventType: String,
        newValue: Any? = null,
    ): RuntimeException {
        return RuntimeException("Unexpected event of type $eventType for field $fieldId (newValue: $newValue)")
    }
}
