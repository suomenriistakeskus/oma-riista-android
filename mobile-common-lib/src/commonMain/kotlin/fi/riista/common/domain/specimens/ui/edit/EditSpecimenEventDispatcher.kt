package fi.riista.common.domain.specimens.ui.edit

import fi.riista.common.domain.specimens.ui.SpecimenFieldId
import fi.riista.common.ui.dataField.AgeEventDispatcher
import fi.riista.common.ui.dataField.GenderEventDispatcher
import fi.riista.common.ui.dataField.StringWithIdEventDispatcher

/**
 * An event dispatcher for [SpecimenFieldId] related events.
 *
 * For more information about purpose of the event dispatchers,
 * see the file: DataFieldEventDispatchers.kt
 */
interface EditSpecimenEventDispatcher {
    val genderEventDispatcher: GenderEventDispatcher<SpecimenFieldId>
    val ageEventDispatcher: AgeEventDispatcher<SpecimenFieldId>
    val stringWithIdDispatcher: StringWithIdEventDispatcher<SpecimenFieldId>
}