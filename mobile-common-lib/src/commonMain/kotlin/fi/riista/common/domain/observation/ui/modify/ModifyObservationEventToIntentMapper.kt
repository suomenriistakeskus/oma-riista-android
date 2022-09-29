package fi.riista.common.domain.observation.ui.modify

import fi.riista.common.domain.model.ObservationCategory
import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.TrueOrFalse
import fi.riista.common.resources.toBackendEnum
import fi.riista.common.ui.dataField.EntityImageDispatcher
import fi.riista.common.ui.dataField.IntEventDispatcher
import fi.riista.common.ui.dataField.LocalDateTimeEventDispatcher
import fi.riista.common.ui.dataField.LocationEventDispatcher
import fi.riista.common.ui.dataField.SpeciesEventDispatcher
import fi.riista.common.ui.dataField.SpecimenDataEventDispatcher
import fi.riista.common.ui.dataField.StringEventDispatcher
import fi.riista.common.ui.dataField.StringWithIdEventDispatcher
import fi.riista.common.ui.intent.IntentHandler

internal class ModifyObservationEventToIntentMapper(
    private val intentHandler: IntentHandler<ModifyObservationIntent>,
): ModifyObservationEventDispatcher {

    override val locationEventDispatcher = LocationEventDispatcher<CommonObservationField> { fieldId, value ->
        val intent = when (fieldId) {
            CommonObservationField.LOCATION -> ModifyObservationIntent.ChangeLocation(
                location = value,
                locationChangedAfterUserInteraction = true,
            )
            else -> throw createUnexpectedEventException(fieldId, "LocalDateTime", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val speciesEventDispatcher = SpeciesEventDispatcher<CommonObservationField> { fieldId, value ->
        val intent = when (fieldId) {
            CommonObservationField.SPECIES_AND_IMAGE -> ModifyObservationIntent.ChangeSpecies(
                species = value
            )
            else -> throw createUnexpectedEventException(fieldId, "Species", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val imageEventDispatcher = EntityImageDispatcher { image ->
        intentHandler.handleIntent(
            ModifyObservationIntent.SetEntityImage(image)
        )
    }

    override val specimenEventDispatcher = SpecimenDataEventDispatcher<CommonObservationField> { fieldId, value ->
        val intent = when (fieldId) {
            CommonObservationField.SPECIMENS -> ModifyObservationIntent.ChangeSpecimenData(
                specimenData = value
            )
            else -> throw createUnexpectedEventException(fieldId, "Specimen", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val localDateTimeEventDispatcher = LocalDateTimeEventDispatcher<CommonObservationField> { fieldId, value ->
        val intent = when (fieldId) {
            CommonObservationField.DATE_AND_TIME -> ModifyObservationIntent.ChangeDateAndTime(dateAndTime = value)
            else -> throw createUnexpectedEventException(fieldId, "LocalDateTime", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val stringWithIdEventDispatcher = StringWithIdEventDispatcher<CommonObservationField> { fieldId, value ->
        val selectedValue = value.firstOrNull() ?: kotlin.run {
            throw RuntimeException("Wrong number of values for field $fieldId (value: $value)")
        }

        val intent = when (fieldId) {
            CommonObservationField.OBSERVATION_CATEGORY ->
                ModifyObservationIntent.ChangeObservationCategory(observationCategory = selectedValue.toBackendEnum())
            CommonObservationField.WITHIN_MOOSE_HUNTING -> {
                val observationCategory: ObservationCategory? = when (selectedValue.toBackendEnum<TrueOrFalse>().value) {
                    TrueOrFalse.TRUE -> ObservationCategory.MOOSE_HUNTING
                    TrueOrFalse.FALSE -> ObservationCategory.NORMAL
                    null -> null
                }

                ModifyObservationIntent.ChangeObservationCategory(
                    observationCategory = BackendEnum.create(observationCategory)
                )
            }
            CommonObservationField.WITHIN_DEER_HUNTING -> {
                val observationCategory: ObservationCategory? = when (selectedValue.toBackendEnum<TrueOrFalse>().value) {
                    TrueOrFalse.TRUE -> ObservationCategory.DEER_HUNTING
                    TrueOrFalse.FALSE -> ObservationCategory.NORMAL
                    null -> null
                }

                ModifyObservationIntent.ChangeObservationCategory(
                    observationCategory = BackendEnum.create(observationCategory)
                )
            }
            CommonObservationField.OBSERVATION_TYPE ->
                ModifyObservationIntent.ChangeObservationType(observationType = selectedValue.toBackendEnum())
            CommonObservationField.DEER_HUNTING_TYPE ->
                ModifyObservationIntent.ChangeDeerHuntingType(deerHuntingType = selectedValue.toBackendEnum())
            CommonObservationField.TASSU_VERIFIED_BY_CARNIVORE_AUTHORITY ->
                ModifyObservationIntent.ChangeVerifiedByCarnivoreAuthority(
                    verifiedByCarnivoreAuthority = selectedValue.toBackendEnum<TrueOrFalse>().value?.booleanValue
                )

            else -> throw createUnexpectedEventException(fieldId, "StringWithId", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val stringEventDispatcher = StringEventDispatcher<CommonObservationField> { fieldId, value ->
        val intent = when (fieldId) {
            CommonObservationField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION ->
                ModifyObservationIntent.ChangeDeerHuntingOtherTypeDescription(deerHuntingOtherTypeDescription = value)
            CommonObservationField.TASSU_OBSERVER_NAME -> ModifyObservationIntent.ChangeObserverName(observerName = value)
            CommonObservationField.TASSU_OBSERVER_PHONENUMBER ->
                ModifyObservationIntent.ChangeObserverPhoneNumber(observerPhoneNumber = value)
            CommonObservationField.TASSU_OFFICIAL_ADDITIONAL_INFO ->
                ModifyObservationIntent.ChangeOfficialAdditionaInformation(officialAdditionalInformation = value)
            CommonObservationField.DESCRIPTION -> ModifyObservationIntent.ChangeDescription(description = value)
            else -> throw createUnexpectedEventException(fieldId, "String", value)
        }

        intentHandler.handleIntent(intent)
    }

    override val intEventDispatcher = IntEventDispatcher<CommonObservationField> { fieldId, value ->
        val intent = when (fieldId) {
            CommonObservationField.SPECIMEN_AMOUNT -> ModifyObservationIntent.ChangeSpecimenAmount(value)
            CommonObservationField.MOOSE_LIKE_MALE_AMOUNT ->
                ModifyObservationIntent.ChangeMooselikeMaleAmount(mooselikeMaleAmount = value)
            CommonObservationField.MOOSE_LIKE_FEMALE_AMOUNT ->
                ModifyObservationIntent.ChangeMooselikeFemaleAmount(mooselikeFemaleAmount = value)
            CommonObservationField.MOOSE_LIKE_FEMALE_1CALF_AMOUNT ->
                ModifyObservationIntent.ChangeMooselikeFemale1CalfAmount(mooselikeFemale1CalfAmount = value)
            CommonObservationField.MOOSE_LIKE_FEMALE_2CALFS_AMOUNT ->
                ModifyObservationIntent.ChangeMooselikeFemale2CalfsAmount(mooselikeFemale2CalfsAmount = value)
            CommonObservationField.MOOSE_LIKE_FEMALE_3CALFS_AMOUNT ->
                ModifyObservationIntent.ChangeMooselikeFemale3CalfsAmount(mooselikeFemale3CalfsAmount = value)
            CommonObservationField.MOOSE_LIKE_FEMALE_4CALFS_AMOUNT ->
                ModifyObservationIntent.ChangeMooselikeFemale4CalfsAmount(mooselikeFemale4CalfsAmount = value)
            CommonObservationField.MOOSE_LIKE_CALF_AMOUNT ->
                ModifyObservationIntent.ChangeMooselikeCalfAmount(mooselikeCalfAmount = value)
            CommonObservationField.MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT ->
                ModifyObservationIntent.ChangeMooselikeUnknownSpecimenAmount(mooselikeUnknownSpecimenAmount = value)
            else -> throw createUnexpectedEventException(fieldId, "String", value)
        }

        intentHandler.handleIntent(intent)
    }


    private fun createUnexpectedEventException(
        fieldId: CommonObservationField,
        eventType: String,
        value: Any?,
    ): RuntimeException {
        return RuntimeException("Unexpected event of type $eventType for field $fieldId (value: $value)")
    }
}

