package fi.riista.common.groupHunting.ui.groupHarvest.modify

import fi.riista.common.groupHunting.ui.GroupHarvestField
import fi.riista.common.ui.dataField.*

/**
 * An event dispatcher for [GroupHarvestField] related events.
 *
 * For more information about purpose of the event dispatchers,
 * see the file: DataFieldEventDispatchers.kt
 */
interface ModifyGroupHarvestEventDispatcher {
    val stringEventDispatcher: StringEventDispatcher<GroupHarvestField>
    val stringWithIdEventDispatcher: StringWithIdEventDispatcher<GroupHarvestField>
    val localDateTimeEventDispatcher: LocalDateTimeEventDispatcher<GroupHarvestField>
    val localTimeEventDispatcher: LocalTimeEventDispatcher<GroupHarvestField>
    val huntingDayEventDispatcher: HuntingDayIdEventDispatcher<GroupHarvestField>
    val genderEventDispatcher: GenderEventDispatcher<GroupHarvestField>
    val ageEventDispatcher: AgeEventDispatcher<GroupHarvestField>
    val locationEventDispatcher: LocationEventDispatcher<GroupHarvestField>
    val booleanEventDispatcher: BooleanEventDispatcher<GroupHarvestField>
    val doubleEventDispatcher: DoubleEventDispatcher<GroupHarvestField>
    val intEventDispatcher: IntEventDispatcher<GroupHarvestField>
}