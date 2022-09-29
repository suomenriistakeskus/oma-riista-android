package fi.riista.common.domain.observation.ui.modify

import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.ObservationCategory
import fi.riista.common.domain.model.ObservationType
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDateTime

sealed class ModifyObservationIntent {
    class ChangeLocation(
        val location: ETRMSGeoLocation,
        val locationChangedAfterUserInteraction: Boolean,
    ): ModifyObservationIntent()
    class ChangeSpecies(val species: Species): ModifyObservationIntent()
    class SetEntityImage(val image: EntityImage): ModifyObservationIntent()
    class ChangeDateAndTime(val dateAndTime: LocalDateTime): ModifyObservationIntent()
    class ChangeSpecimenAmount(val specimenAmount: Int?): ModifyObservationIntent()
    class ChangeSpecimenData(val specimenData: SpecimenFieldDataContainer): ModifyObservationIntent()
    class ChangeObservationCategory(val observationCategory: BackendEnum<ObservationCategory>): ModifyObservationIntent()
    class ChangeObservationType(val observationType: BackendEnum<ObservationType>): ModifyObservationIntent()
    class ChangeDeerHuntingType(val deerHuntingType: BackendEnum<DeerHuntingType>): ModifyObservationIntent()
    class ChangeDeerHuntingOtherTypeDescription(val deerHuntingOtherTypeDescription: String): ModifyObservationIntent()

    class ChangeMooselikeMaleAmount(val mooselikeMaleAmount: Int?): ModifyObservationIntent()
    class ChangeMooselikeFemaleAmount(val mooselikeFemaleAmount: Int?): ModifyObservationIntent()
    class ChangeMooselikeFemale1CalfAmount(val mooselikeFemale1CalfAmount: Int?): ModifyObservationIntent()
    class ChangeMooselikeFemale2CalfsAmount(val mooselikeFemale2CalfsAmount: Int?): ModifyObservationIntent()
    class ChangeMooselikeFemale3CalfsAmount(val mooselikeFemale3CalfsAmount: Int?): ModifyObservationIntent()
    class ChangeMooselikeFemale4CalfsAmount(val mooselikeFemale4CalfsAmount: Int?): ModifyObservationIntent()
    class ChangeMooselikeCalfAmount(val mooselikeCalfAmount: Int?): ModifyObservationIntent()
    class ChangeMooselikeUnknownSpecimenAmount(val mooselikeUnknownSpecimenAmount: Int?): ModifyObservationIntent()

    class ChangeVerifiedByCarnivoreAuthority(val verifiedByCarnivoreAuthority: Boolean?): ModifyObservationIntent()
    class ChangeObserverName(val observerName: String): ModifyObservationIntent()
    class ChangeObserverPhoneNumber(val observerPhoneNumber: String): ModifyObservationIntent()
    class ChangeOfficialAdditionaInformation(val officialAdditionalInformation: String): ModifyObservationIntent()
    class ChangeDescription(val description: String): ModifyObservationIntent()
}
