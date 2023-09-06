package fi.riista.common.domain.specimens.ui.edit

import fi.riista.common.domain.specimens.ui.SpecimenFieldId
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.model.BackendEnum
import fi.riista.common.resources.toBackendEnum
import fi.riista.common.ui.dataField.AgeEventDispatcher
import fi.riista.common.ui.dataField.DoubleEventDispatcher
import fi.riista.common.ui.dataField.GenderEventDispatcher
import fi.riista.common.ui.dataField.StringWithIdEventDispatcher
import fi.riista.common.ui.intent.IntentHandler

internal class EditSpecimenEventToIntentMapper(
    private val intentHandler: IntentHandler<EditSpecimenIntent>,
): EditSpecimenEventDispatcher {

    override val genderEventDispatcher = GenderEventDispatcher<SpecimenFieldId> { fieldId, value ->
        intentHandler.handleIntent(EditSpecimenIntent.ChangeGender(fieldId, value))
    }

    override val ageEventDispatcher = AgeEventDispatcher<SpecimenFieldId> { fieldId, value ->
        intentHandler.handleIntent(EditSpecimenIntent.ChangeAge(
            fieldId = fieldId,
            age = BackendEnum.create(value)
        ))
    }

    override val stringWithIdDispatcher =
        StringWithIdEventDispatcher<SpecimenFieldId> { fieldId, newValue ->
            if (newValue.size != 1) {
                throw RuntimeException("Wrong number of values for field $fieldId (newValue: $newValue)")
            }
            val intent = when (fieldId.type) {
                SpecimenFieldType.AGE ->
                    EditSpecimenIntent.ChangeAge(
                        fieldId = fieldId,
                        age = newValue[0].toBackendEnum()
                    )
                SpecimenFieldType.STATE_OF_HEALTH ->
                    EditSpecimenIntent.ChangeStateOfHealth(
                        fieldId = fieldId,
                        stateOfHealth = newValue[0].toBackendEnum()
                    )
                SpecimenFieldType.MARKING ->
                    EditSpecimenIntent.ChangeSpecimenMarking(
                        fieldId = fieldId,
                        marking = newValue[0].toBackendEnum()
                    )
                SpecimenFieldType.WIDTH_OF_PAW ->
                    EditSpecimenIntent.ChangeWidthOfPaw(
                        fieldId = fieldId,
                        // implicit dependency: id is the width in millimeters, negative values are for clearing value
                        widthOfPawMillimeters = newValue[0].id.toInt().takeIf { it >= 0 }
                    )
                SpecimenFieldType.LENGTH_OF_PAW ->
                    EditSpecimenIntent.ChangeLengthOfPaw(
                        fieldId = fieldId,
                        // implicit dependency: id is the length in millimeters, negative values are for clearing value
                        lengthOfPawMillimeters = newValue[0].id.toInt().takeIf { it >= 0 }
                    )
                else -> throw createUnexpectedEventException(fieldId, "StringWithId", newValue)
            }

            intentHandler.handleIntent(intent)
        }

    override val doubleEventDispatcher = DoubleEventDispatcher<SpecimenFieldId> { fieldId, value ->
        val intent = when (fieldId.type) {
            SpecimenFieldType.WEIGHT ->
                EditSpecimenIntent.ChangeWeight(
                    fieldId = fieldId,
                    weight = value
                )
            else -> throw createUnexpectedEventException(fieldId, "Double", value)
        }

        intentHandler.handleIntent(intent)
    }


    private fun createUnexpectedEventException(
        fieldId: SpecimenFieldId,
        eventType: String,
        value: Any?,
    ): RuntimeException {
        return RuntimeException("Unexpected event of type $eventType for field $fieldId (value: $value)")
    }
}

