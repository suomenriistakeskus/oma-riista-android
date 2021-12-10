package fi.riista.common.groupHunting.ui.huntingDays.modify

import fi.riista.common.resources.toBackendEnum
import fi.riista.common.ui.dataField.HoursAndMinutesEventDispatcher
import fi.riista.common.ui.dataField.IntEventDispatcher
import fi.riista.common.ui.dataField.LocalDateTimeEventDispatcher
import fi.riista.common.ui.dataField.StringWithIdEventDispatcher
import fi.riista.common.ui.intent.IntentHandler

internal class ModifyGroupHuntingDayEventToIntentMapper(
    private val intentHandler: IntentHandler<ModifyGroupHuntingDayIntent>,
) : ModifyGroupHuntingDayEventDispatcher {
    override val intEventDispatcher =
        IntEventDispatcher<GroupHuntingDayField> { fieldId, value ->
            val intent = when (fieldId) {
                GroupHuntingDayField.NUMBER_OF_HUNTERS ->
                    ModifyGroupHuntingDayIntent.ChangeNumberOfHunters(value)
                GroupHuntingDayField.NUMBER_OF_HOUNDS ->
                    ModifyGroupHuntingDayIntent.ChangeNumberOfHounds(value)
                GroupHuntingDayField.SNOW_DEPTH ->
                    ModifyGroupHuntingDayIntent.ChangeSnowDepth(value)
                else -> throw createUnexpectedEventException(fieldId, "Int?", value)
            }

            intentHandler.handleIntent(intent)
        }

    override val dateTimeEventDispatcher =
        LocalDateTimeEventDispatcher<GroupHuntingDayField> { fieldId, value ->
            val intent = when (fieldId) {
                GroupHuntingDayField.START_DATE_AND_TIME ->
                    ModifyGroupHuntingDayIntent.ChangeStartDateTime(value)
                GroupHuntingDayField.END_DATE_AND_TIME ->
                    ModifyGroupHuntingDayIntent.ChangeEndDateTime(value)
                else -> throw createUnexpectedEventException(fieldId, "LocalDateTime", value)
            }

            intentHandler.handleIntent(intent)
        }

    override val stringWithIdDispatcher =
        StringWithIdEventDispatcher<GroupHuntingDayField> { fieldId, newValue ->
            val intent = when (fieldId) {
                GroupHuntingDayField.HUNTING_METHOD ->
                    ModifyGroupHuntingDayIntent.ChangeHuntingMethod(newValue.toBackendEnum())
                else -> throw createUnexpectedEventException(fieldId, "StringWithId", newValue)
            }

            intentHandler.handleIntent(intent)
        }

    override val durationEventDispatcher =
        HoursAndMinutesEventDispatcher<GroupHuntingDayField> { fieldId, value ->
            val intent = when (fieldId) {
                GroupHuntingDayField.BREAK_DURATION ->
                    ModifyGroupHuntingDayIntent.ChangeBreakDuration(value)
                else -> throw createUnexpectedEventException(fieldId, "HoursAndMinutes", value)
            }

            intentHandler.handleIntent(intent)
        }

    private fun createUnexpectedEventException(
        fieldId: GroupHuntingDayField,
        eventType: String,
        value: Any?,
    ): RuntimeException {
        return RuntimeException("Unexpected event of type $eventType for field $fieldId (value: $value)")
    }

}