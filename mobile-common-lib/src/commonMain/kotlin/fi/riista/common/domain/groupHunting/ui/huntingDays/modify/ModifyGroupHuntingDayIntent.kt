package fi.riista.common.domain.groupHunting.ui.huntingDays.modify

import fi.riista.common.domain.groupHunting.model.GroupHuntingMethodType
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.HoursAndMinutes
import fi.riista.common.model.LocalDateTime

sealed class ModifyGroupHuntingDayIntent {
    class ChangeStartDateTime(val startDateAndTime: LocalDateTime): ModifyGroupHuntingDayIntent()
    class ChangeEndDateTime(val endDateAndTime: LocalDateTime): ModifyGroupHuntingDayIntent()
    class ChangeNumberOfHunters(val numberOfHunters: Int?): ModifyGroupHuntingDayIntent()
    class ChangeHuntingMethod(val huntingMethod: BackendEnum<GroupHuntingMethodType>): ModifyGroupHuntingDayIntent()
    class ChangeNumberOfHounds(val numberOfHounds: Int?): ModifyGroupHuntingDayIntent()
    class ChangeSnowDepth(val snowDepth: Int?): ModifyGroupHuntingDayIntent()
    class ChangeBreakDuration(val breakDuration: HoursAndMinutes): ModifyGroupHuntingDayIntent()
}
