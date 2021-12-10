package fi.riista.common.groupHunting.validation

import fi.riista.common.groupHunting.model.GroupHuntingDay
import fi.riista.common.groupHunting.model.HuntingGroupPermit
import fi.riista.common.groupHunting.model.isWithinPermit
import fi.riista.common.logging.getLogger
import fi.riista.common.model.LocalDatePeriod
import fi.riista.common.model.daysUntil
import fi.riista.common.util.LocalDateTimeProvider

object GroupHuntingDayValidator {
    enum class Error {
        START_NOT_BEFORE_END,
        DATES_NOT_WITHIN_PERMIT,
        INVALID_DAYS_UNTIL_END,
        ENDS_IN_FUTURE,
        NO_ACTIVE_HUNTING,
        INVALID_NUMBER_OF_HUNTERS,
        MISSING_HUNTING_METHOD,
        INVALID_NUMBER_OF_HOUNDS,
    }

    private val logger by getLogger(GroupHuntingDayValidator::class)

    fun validate(
        huntingDay: GroupHuntingDay,
        permit: HuntingGroupPermit,
        currentTimeProvider: LocalDateTimeProvider
    ): List<Error> {
        return with(huntingDay) {
            listOfNotNull(
                    Error.START_NOT_BEFORE_END.takeUnless { startDateTime < endDateTime },
                    Error.DATES_NOT_WITHIN_PERMIT.takeUnless {
                        LocalDatePeriod(startDateTime.date, endDateTime.date).isWithinPermit(permit)
                    },
                    Error.INVALID_DAYS_UNTIL_END.takeUnless {
                        startDateTime.date.daysUntil(endDateTime.date) in 0..1
                    },
                    Error.ENDS_IN_FUTURE.takeUnless { endDateTime <= currentTimeProvider.now() },
                    Error.NO_ACTIVE_HUNTING.takeUnless { activeHuntingDurationInMinutes > 0 },
                    Error.INVALID_NUMBER_OF_HUNTERS.takeUnless { (numberOfHunters ?: 0) > 0 },
                    Error.MISSING_HUNTING_METHOD.takeUnless { huntingMethod.rawBackendEnumValue != null },
                    Error.INVALID_NUMBER_OF_HOUNDS.takeUnless {
                        val houndRequired = huntingMethod.value?.requiresHound ?: false
                        (!houndRequired || (numberOfHounds ?: 0) > 0)
                    }
            )
        }.also {
            if (it.isEmpty()) {
                logger.v { "GroupHuntingDay is valid."}
            } else {
                logger.v { "GroupHuntingDay validation errors: $it" }
            }
        }
    }
}


fun GroupHuntingDay.isValid(
    permit: HuntingGroupPermit,
    currentTimeProvider: LocalDateTimeProvider): Boolean {
    return validate(permit, currentTimeProvider).isEmpty()
}

fun GroupHuntingDay.validate(permit: HuntingGroupPermit, currentTimeProvider: LocalDateTimeProvider) =
    GroupHuntingDayValidator.validate(this, permit, currentTimeProvider)