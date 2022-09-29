package fi.riista.common.domain.groupHunting.ui.huntingDays.modify

import fi.riista.common.domain.groupHunting.model.GroupHuntingDay
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class ModifyGroupHuntingDayViewModel(
    internal val huntingDay: GroupHuntingDay,
    override val fields: DataFields<GroupHuntingDayField>,
    val huntingDayCanBeSaved: Boolean,
) : DataFieldViewModel<GroupHuntingDayField>()