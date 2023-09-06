package fi.riista.common.domain.harvest.ui.modify

import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.ui.dataField.*

/**
 * An event dispatcher for [CommonHarvestField] related events.
 *
 * For more information about purpose of the event dispatchers,
 * see the file: DataFieldEventDispatchers.kt
 */
interface ModifyHarvestEventDispatcher {
    val speciesEventDispatcher: SpeciesEventDispatcher<CommonHarvestField>
    val imageEventDispatcher: EntityImageDispatcher
    val specimenEventDispatcher: SpecimenDataEventDispatcher<CommonHarvestField>
    val stringEventDispatcher: StringEventDispatcher<CommonHarvestField>
    val stringWithIdEventDispatcher: StringWithIdEventDispatcher<CommonHarvestField>
    val localDateTimeEventDispatcher: LocalDateTimeEventDispatcher<CommonHarvestField>
    val localTimeEventDispatcher: LocalTimeEventDispatcher<CommonHarvestField>
    val huntingDayEventDispatcher: HuntingDayIdEventDispatcher<CommonHarvestField>
    val genderEventDispatcher: GenderEventDispatcher<CommonHarvestField>
    val ageEventDispatcher: AgeEventDispatcher<CommonHarvestField>
    val locationEventDispatcher: LocationEventDispatcher<CommonHarvestField>
    val booleanEventDispatcher: BooleanEventDispatcher<CommonHarvestField>
    val doubleEventDispatcher: DoubleEventDispatcher<CommonHarvestField>
    val intEventDispatcher: IntEventDispatcher<CommonHarvestField>
    val linkActionEventDispatcher: ActionEventDispatcher<CommonHarvestField>
    val permitEventDispatcher: PermitEventDispatcher
}