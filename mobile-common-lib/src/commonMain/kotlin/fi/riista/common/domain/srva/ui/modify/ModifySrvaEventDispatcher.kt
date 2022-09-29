package fi.riista.common.domain.srva.ui.modify

import fi.riista.common.domain.srva.ui.SrvaEventField
import fi.riista.common.ui.dataField.*

/**
 * An event dispatcher for [SrvaEventField] related events.
 *
 * For more information about purpose of the event dispatchers,
 * see the file: DataFieldEventDispatchers.kt
 */
interface ModifySrvaEventDispatcher {
    val locationEventDispatcher: LocationEventDispatcher<SrvaEventField>
    val speciesEventDispatcher: SpeciesEventDispatcher<SrvaEventField>
    val imageEventDispatcher: EntityImageDispatcher
    val specimenEventDispatcher: SpecimenDataEventDispatcher<SrvaEventField>
    val localDateTimeEventDispatcher: LocalDateTimeEventDispatcher<SrvaEventField>
    val stringEventDispatcher: StringEventDispatcher<SrvaEventField>
    val stringWithIdEventDispatcher: StringWithIdEventDispatcher<SrvaEventField>
    val booleanEventDispatcher: BooleanEventDispatcher<SrvaEventField>
    val intEventDispatcher: IntEventDispatcher<SrvaEventField>
}