package fi.riista.common.domain.specimens.ui.edit

import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.observation.model.ObservationSpecimenMarking
import fi.riista.common.domain.observation.model.ObservationSpecimenState
import fi.riista.common.domain.specimens.ui.SpecimenFieldId
import fi.riista.common.model.BackendEnum

sealed class EditSpecimenIntent {
    abstract val fieldId: SpecimenFieldId

    class ChangeGender(override val fieldId: SpecimenFieldId, val gender: Gender?): EditSpecimenIntent()
    class ChangeAge(override val fieldId: SpecimenFieldId, val age: BackendEnum<GameAge>): EditSpecimenIntent()
    class ChangeStateOfHealth(
        override val fieldId: SpecimenFieldId,
        val stateOfHealth: BackendEnum<ObservationSpecimenState>?,
    ): EditSpecimenIntent()
    class ChangeSpecimenMarking(
        override val fieldId: SpecimenFieldId,
        val marking: BackendEnum<ObservationSpecimenMarking>?,
    ): EditSpecimenIntent()

    class ChangeWidthOfPaw(
        override val fieldId: SpecimenFieldId,
        val widthOfPawMillimeters: Int?,
    ): EditSpecimenIntent()
    class ChangeLengthOfPaw(
        override val fieldId: SpecimenFieldId,
        val lengthOfPawMillimeters: Int?,
    ): EditSpecimenIntent()
}
