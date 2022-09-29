package fi.riista.common.domain.observation.ui.modify

import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.ui.dataField.*

/**
 * An event dispatcher for [CommonObservationField] related events.
 *
 * For more information about purpose of the event dispatchers,
 * see the file: DataFieldEventDispatchers.kt
 */
interface ModifyObservationEventDispatcher {
    val locationEventDispatcher: LocationEventDispatcher<CommonObservationField>
    val speciesEventDispatcher: SpeciesEventDispatcher<CommonObservationField>
    val imageEventDispatcher: EntityImageDispatcher
    val specimenEventDispatcher: SpecimenDataEventDispatcher<CommonObservationField>
    val localDateTimeEventDispatcher: LocalDateTimeEventDispatcher<CommonObservationField>
    val stringEventDispatcher: StringEventDispatcher<CommonObservationField>
    val stringWithIdEventDispatcher: StringWithIdEventDispatcher<CommonObservationField>
    val intEventDispatcher: IntEventDispatcher<CommonObservationField>
}