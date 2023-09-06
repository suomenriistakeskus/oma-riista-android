package fi.riista.common.domain.huntingControl.ui.hunterInfo

import fi.riista.common.ui.intent.IntentHandler

internal class HunterInfoEventToIntentMapper(
    val intentHandler: IntentHandler<HunterInfoIntent>,
) : HunterInfoDispatcher {
    override fun dispatchHunterNumber(number: String) {
        intentHandler.handleIntent(HunterInfoIntent.SearchByHunterNumber(number))
    }

    override fun dispatchSsn(number: String) {
        intentHandler.handleIntent(HunterInfoIntent.SearchBySsn(number))
    }
}

internal class HunterInfoIntEventToIntentMapper(
    private val intentHandler: IntentHandler<HunterInfoIntent>
) : HunterInfoIntEventDispatcher {

    override fun dispatchIntChanged(fieldId: HunterInfoField, value: Int?) {
        val intent = when (fieldId.type) {
            HunterInfoField.Type.ENTER_HUNTER_NUMBER -> HunterInfoIntent.ChangeHunterNumber(value)
            else -> {
                 throw RuntimeException("Unexpected event of type Int? for field $fieldId (newValue: $value)")
            }
        }
        intentHandler.handleIntent(intent)
    }
}

internal class HunterInfoActionEventToIntentMapper(
    private val intentHandler: IntentHandler<HunterInfoIntent>
) : HunterInfoActionEventDispatcher {
    override fun dispatchEvent(fieldId: HunterInfoField) {
        val intent = when (fieldId.type) {
            HunterInfoField.Type.RESET_BUTTON -> HunterInfoIntent.Reset
            HunterInfoField.Type.RETRY_BUTTON -> HunterInfoIntent.Retry
            else -> {
                throw RuntimeException("Unexpected event of type Action for field $fieldId")
            }
        }
        intentHandler.handleIntent(intent)
    }
}
