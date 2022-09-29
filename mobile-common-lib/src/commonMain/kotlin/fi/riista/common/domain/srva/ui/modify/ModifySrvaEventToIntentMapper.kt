package fi.riista.common.domain.srva.ui.modify

import fi.riista.common.domain.srva.ui.SrvaEventField
import fi.riista.common.resources.toBackendEnum
import fi.riista.common.ui.dataField.*
import fi.riista.common.ui.intent.IntentHandler

internal class ModifySrvaEventToIntentMapper(
    private val intentHandler: IntentHandler<ModifySrvaEventIntent>,
): ModifySrvaEventDispatcher {

    override val locationEventDispatcher = LocationEventDispatcher<SrvaEventField> { fieldId, value ->
        val intent = when (fieldId.type) {
            SrvaEventField.Type.LOCATION -> ModifySrvaEventIntent.ChangeLocation(
                location = value,
                locationChangedAfterUserInteraction = true,
            )
            else -> throw createUnexpectedEventException(fieldId, "LocalDateTime", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val speciesEventDispatcher = SpeciesEventDispatcher<SrvaEventField> { fieldId, value ->
        val intent = when (fieldId.type) {
            SrvaEventField.Type.SPECIES_CODE -> ModifySrvaEventIntent.ChangeSpecies(
                species = value
            )
            else -> throw createUnexpectedEventException(fieldId, "Species", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val imageEventDispatcher = EntityImageDispatcher { image ->
        intentHandler.handleIntent(
            ModifySrvaEventIntent.SetEntityImage(image)
        )
    }

    override val specimenEventDispatcher = SpecimenDataEventDispatcher<SrvaEventField> { fieldId, value ->
        val intent = when (fieldId.type) {
            SrvaEventField.Type.SPECIMEN -> ModifySrvaEventIntent.ChangeSpecimenData(
                specimenData = value
            )
            else -> throw createUnexpectedEventException(fieldId, "Specimen", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val localDateTimeEventDispatcher = LocalDateTimeEventDispatcher<SrvaEventField> { fieldId, value ->
        val intent = when (fieldId.type) {
            SrvaEventField.Type.DATE_AND_TIME -> ModifySrvaEventIntent.ChangeDateAndTime(dateAndTime = value)
            else -> throw createUnexpectedEventException(fieldId, "LocalDateTime", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val stringWithIdEventDispatcher = StringWithIdEventDispatcher<SrvaEventField> { fieldId, value ->
        val selectedValue = value.firstOrNull() ?: kotlin.run {
            throw RuntimeException("Wrong number of values for field $fieldId (value: $value)")
        }

        val intent = when (fieldId.type) {
            SrvaEventField.Type.EVENT_CATEGORY -> ModifySrvaEventIntent.ChangeEventCategory(selectedValue.toBackendEnum())
            SrvaEventField.Type.EVENT_TYPE -> ModifySrvaEventIntent.ChangeEventType(selectedValue.toBackendEnum())
            SrvaEventField.Type.EVENT_TYPE_DETAIL -> ModifySrvaEventIntent.ChangeEventTypeDetail(selectedValue.toBackendEnum())
            SrvaEventField.Type.EVENT_RESULT_DETAIL -> ModifySrvaEventIntent.ChangeEventResultDetail(selectedValue.toBackendEnum())
            SrvaEventField.Type.EVENT_RESULT -> ModifySrvaEventIntent.ChangeEventResult(selectedValue.toBackendEnum())
            else -> throw createUnexpectedEventException(fieldId, "StringWithId", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val stringEventDispatcher = StringEventDispatcher<SrvaEventField> { fieldId, value ->
        val intent = when (fieldId.type) {
            SrvaEventField.Type.OTHER_SPECIES_DESCRIPTION -> ModifySrvaEventIntent.ChangeOtherSpeciesDescription(value)
            SrvaEventField.Type.DEPORTATION_ORDER_NUMBER -> ModifySrvaEventIntent.ChangeDeportationOrderNumber(value)
            SrvaEventField.Type.EVENT_OTHER_TYPE_DESCRIPTION -> ModifySrvaEventIntent.ChangeOtherEventTypeDescription(value)
            SrvaEventField.Type.EVENT_OTHER_TYPE_DETAIL_DESCRIPTION -> ModifySrvaEventIntent.ChangeOtherEventTypeDetailDescription(value)
            SrvaEventField.Type.OTHER_METHOD_DESCRIPTION -> ModifySrvaEventIntent.ChangeOtherMethodDescription(value)
            SrvaEventField.Type.DESCRIPTION -> ModifySrvaEventIntent.ChangeDescription(value)
            else -> throw createUnexpectedEventException(fieldId, "String", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val booleanEventDispatcher = BooleanEventDispatcher<SrvaEventField> { fieldId, value ->
        val intent = when (fieldId.type) {
            SrvaEventField.Type.METHOD_ITEM -> ModifySrvaEventIntent.ChangeMethodSelectionStatus(
                methodIndex = fieldId.index,
                selected = value
            )
            else -> throw createUnexpectedEventException(fieldId, "Boolean?", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val intEventDispatcher = IntEventDispatcher<SrvaEventField> { fieldId, value ->
        val intent = when (fieldId.type) {
            SrvaEventField.Type.SPECIMEN_AMOUNT -> ModifySrvaEventIntent.ChangeSpecimenAmount(value)
            SrvaEventField.Type.PERSON_COUNT -> ModifySrvaEventIntent.ChangePersonCount(value)
            SrvaEventField.Type.HOURS_SPENT -> ModifySrvaEventIntent.ChangeHoursSpent(value)
            else -> throw createUnexpectedEventException(fieldId, "Int?", value)
        }

        intentHandler.handleIntent(intent)
    }

    private fun createUnexpectedEventException(
        fieldId: SrvaEventField,
        eventType: String,
        value: Any?,
    ): RuntimeException {
        return RuntimeException("Unexpected event of type $eventType for field $fieldId (value: $value)")
    }
}

