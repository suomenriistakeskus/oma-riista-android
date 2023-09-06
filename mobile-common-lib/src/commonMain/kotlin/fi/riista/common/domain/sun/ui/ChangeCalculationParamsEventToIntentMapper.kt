package fi.riista.common.domain.sun.ui

import fi.riista.common.ui.dataField.LocalDateEventDispatcher
import fi.riista.common.ui.dataField.LocationEventDispatcher

class ChangeCalculationParamsEventToIntentMapper(
    private val intentHandler: SunriseAndSunsetTimesController,
): ChangeCalculationParamsEventDispatcher {
    override val localDateEventDispatcher = LocalDateEventDispatcher<SunriseAndSunsetField> { fieldId, localDate ->
        val intent = when (fieldId) {
            SunriseAndSunsetField.DATE -> ChangeCalculationParamsIntent.ChangeDate(
                localDate = localDate
            )
            else -> throw createUnexpectedEventException(fieldId, "LocalDate", localDate)
        }

        intentHandler.handleIntent(intent)
    }

    override val locationEventDispatcher = LocationEventDispatcher<SunriseAndSunsetField> { fieldId, location ->
        val intent = when (fieldId) {
            SunriseAndSunsetField.LOCATION -> ChangeCalculationParamsIntent.ChangeLocation(
                location = location,
                locationChangedAfterUserInteraction = true,
            )
            else -> throw createUnexpectedEventException(fieldId, "Location", location)
        }

        intentHandler.handleIntent(intent)
    }

    private fun createUnexpectedEventException(
        fieldId: SunriseAndSunsetField,
        eventType: String,
        newValue: Any?,
    ): RuntimeException {
        return RuntimeException("Unexpected event of type $eventType for field $fieldId (newValue: $newValue)")
    }
}
