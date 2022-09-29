package fi.riista.common.domain.huntingControl.ui

import fi.riista.common.domain.huntingControl.model.HuntingControlAttachment
import fi.riista.common.logging.getLogger
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalTime
import fi.riista.common.model.StringWithId
import fi.riista.common.ui.intent.IntentHandler

internal class HuntingControlEventStringWithIdEventToIntentMapper(
    private val intentHandler: IntentHandler<HuntingControlEventIntent>
) : HuntingControlEventStringWithIdEventDispatcher {

    override fun dispatchStringWithIdChanged(fieldId: HuntingControlEventField, value: List<StringWithId>) {
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.EVENT_TYPE -> {
                if (value.size == 1) {
                    HuntingControlEventIntent.ChangeEventType(value[0])
                } else {
                    logger.w { "Value ($value) missing from HuntingControlEventField $fieldId" }
                    return
                }
            }
            HuntingControlEventField.Type.INSPECTORS -> HuntingControlEventIntent.ChangeInspectors(value)
            else -> {
                logger.w { "HuntingControlEventField $fieldId has unexpected String value of $value" }
                return
            }
        }
        intentHandler.handleIntent(intent)
    }

    companion object {
        private val logger by getLogger(HuntingControlEventStringWithIdEventToIntentMapper::class)
    }
}

internal class HuntingControlEventStringWithIdClickEventToIntentMapper(
    private val intentHandler: IntentHandler<HuntingControlEventIntent>
) : HuntingControlEventStringWithIdClickEventDispatcher {

    override fun dispatchStringWithIdClicked(fieldId: HuntingControlEventField, value: StringWithId) {
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.COOPERATION -> HuntingControlEventIntent.ToggleCooperationType(value)
            HuntingControlEventField.Type.INSPECTOR_NAMES -> HuntingControlEventIntent.RemoveInspectors(value)
            else -> {
                logger.w { "HuntingControlEventField $fieldId has unexpected String value of $value" }
                return
            }
        }
        intentHandler.handleIntent(intent)
    }

    companion object {
        private val logger by getLogger(HuntingControlEventStringWithIdClickEventToIntentMapper::class)
    }
}

internal class HuntingControlEventGeoLocationEventToIntentMapper(
    private val intentHandler: IntentHandler<HuntingControlEventIntent>
) : HuntingControlEventLocationEventDispatcher {

    override fun dispatchLocationChanged(fieldId: HuntingControlEventField, value: ETRMSGeoLocation) {
        intentHandler.handleIntent(
            HuntingControlEventIntent.ChangeLocation(
                newLocation = value,
                locationChangedAfterUserInteraction = true,
            )
        )
    }
}

internal class HuntingControlEventIntEventToIntentMapper(
    private val intentHandler: IntentHandler<HuntingControlEventIntent>
) : HuntingControlEventIntEventDispatcher {

    override fun dispatchIntChanged(fieldId: HuntingControlEventField, value: Int?) {
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.NUMBER_OF_CUSTOMERS -> HuntingControlEventIntent.ChangeNumberOfCustomers(value)
            HuntingControlEventField.Type.NUMBER_OF_PROOF_ORDERS -> HuntingControlEventIntent.ChangeNumberOfProofOrders(value)
            else -> {
                logger.w { "HuntingControlEventField $fieldId has unexpected Int value of $value" }
                return
            }
        }
        intentHandler.handleIntent(intent)
    }

    companion object {
        private val logger by getLogger(HuntingControlEventIntEventToIntentMapper::class)
    }
}

internal class HuntingControlEventStringEventToIntentMapper(
    private val intentHandler: IntentHandler<HuntingControlEventIntent>
) : HuntingControlEventStringEventDispatcher {
    override fun dispatchStringChanged(fieldId: HuntingControlEventField, value: String) {
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.LOCATION_DESCRIPTION -> HuntingControlEventIntent.ChangeLocationDescription(value)
            HuntingControlEventField.Type.EVENT_DESCRIPTION -> HuntingControlEventIntent.ChangeDescription(value)
            HuntingControlEventField.Type.OTHER_PARTICIPANTS -> HuntingControlEventIntent.ChangeOtherPartisipants(value)
            else -> {
                logger.w { "HuntingControlEventField $fieldId has unexpected String value of $value" }
                return
            }
        }
        intentHandler.handleIntent(intent)
    }

    companion object {
        private val logger by getLogger(HuntingControlEventStringEventToIntentMapper::class)
    }
}

internal class HuntingControlEventBooleanEventToIntentMapper(
    private val intentHandler: IntentHandler<HuntingControlEventIntent>
) : HuntingControlEventBooleanEventDispatcher {
    override fun dispatchBooleanChanged(fieldId: HuntingControlEventField, value: Boolean) {
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.WOLF_TERRITORY -> HuntingControlEventIntent.ChangeWolfTerritory(value)
            else -> {
                logger.w { "HuntingControlEventField $fieldId has unexpected Boolean value of $value" }
                return
            }
        }
        intentHandler.handleIntent(intent)
    }

    companion object {
        private val logger by getLogger(HuntingControlEventBooleanEventToIntentMapper::class)
    }
}

internal class HuntingControlEventLocalDateEventToIntentMapper(
    private val intentHandler: IntentHandler<HuntingControlEventIntent>
) : HuntingControlEventLocalDateEventDispatcher {
    override fun dispatchLocalDateChanged(fieldId: HuntingControlEventField, value: LocalDate) {
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.DATE -> HuntingControlEventIntent.ChangeDate(value)
            else -> {
                logger.w { "HuntingControlEventField $fieldId has unexpected LocalDate value of $value" }
                return
            }
        }
        intentHandler.handleIntent(intent)
    }

    companion object {
        private val logger by getLogger(HuntingControlEventLocalDateEventToIntentMapper::class)
    }
}

internal class HuntingControlEventLocalTimeEventToIntentMapper(
    private val intentHandler: IntentHandler<HuntingControlEventIntent>
) : HuntingControlEventLocalTimeEventDispatcher {
    override fun dispatchLocalTimeChanged(fieldId: HuntingControlEventField, value: LocalTime) {
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.START_TIME -> HuntingControlEventIntent.ChangeStartTime(value)
            HuntingControlEventField.Type.END_TIME -> HuntingControlEventIntent.ChangeEndTime(value)
            else -> {
                logger.w { "HuntingControlEventField $fieldId has unexpected LocalTime value of $value" }
                return
            }
        }
        intentHandler.handleIntent(intent)
    }

    companion object {
        private val logger by getLogger(HuntingControlEventLocalTimeEventToIntentMapper::class)
    }
}

internal class HuntingControlActionEventToIntentMapper(
    private val intentHandler: IntentHandler<HuntingControlEventIntent>
) : HuntingControlAttachmentActionEventDispatcher {
    override fun dispatchEvent(fieldId: HuntingControlEventField) {
        val intent = when (fieldId.type) {
            HuntingControlEventField.Type.ATTACHMENT -> HuntingControlEventIntent.DeleteAttachment(fieldId.index)
            else -> {
                logger.w { "HuntingControlEventField $fieldId has unexpected type ${fieldId.type}" }
                return
            }
        }
        intentHandler.handleIntent(intent)
    }

    companion object {
        private val logger by getLogger(HuntingControlActionEventToIntentMapper::class)
    }
}

internal class HuntingControlAttachmentEventToIntentMapper(
    private val intentHandler: IntentHandler<HuntingControlEventIntent>
) : HuntingControlAttachmentEventDispatcher {
    override fun addAttachment(attachment: HuntingControlAttachment) {
        val intent = HuntingControlEventIntent.AddAttachment(attachment)
        intentHandler.handleIntent(intent)
    }
}
