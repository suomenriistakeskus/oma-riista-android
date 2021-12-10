package fi.riista.common.ui.intent

/**
 * An interface for intent handlers.
 *
 * Intents form the I-part of the MVI-architecture (Model-View-Intent) and are meant
 * to be the way to update the model. The model changes are then reflected on the View side
 * by updating the UI.
 *
 * [IntentType] will probably be a sealed class in most cases thus making it easier to
 * encapsulate both intent type (the class) and intent parameters (data stored in instance).
 */
interface IntentHandler<IntentType> {
    fun handleIntent(intent: IntentType)
}