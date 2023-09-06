package fi.riista.common.domain.groupHunting.ui.huntingDays.modify

import fi.riista.common.domain.constants.isMoose
import fi.riista.common.domain.groupHunting.GroupHuntingDayUpdateResponse
import fi.riista.common.domain.groupHunting.dto.GroupHuntingMethodTypeDTO
import fi.riista.common.domain.groupHunting.model.GroupHuntingDay
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.GroupHuntingMethodType
import fi.riista.common.domain.groupHunting.model.HuntingGroup
import fi.riista.common.domain.groupHunting.model.HuntingGroupId
import fi.riista.common.domain.groupHunting.model.HuntingGroupPermit
import fi.riista.common.domain.groupHunting.validation.GroupHuntingDayValidator.Error
import fi.riista.common.domain.groupHunting.validation.validate
import fi.riista.common.logging.getLogger
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.Entity
import fi.riista.common.model.HoursAndMinutes
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.LocalTime
import fi.riista.common.model.Revision
import fi.riista.common.model.changeDate
import fi.riista.common.model.minus
import fi.riista.common.model.plus
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.HasUnreproducibleState
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.DataFieldProducer
import fi.riista.common.ui.dataField.DataFieldProducerProxy
import fi.riista.common.ui.dataField.DateAndTimeField
import fi.riista.common.ui.dataField.EnumStringListFieldFactory
import fi.riista.common.ui.dataField.FieldRequirement
import fi.riista.common.ui.dataField.IntField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.Padding
import fi.riista.common.ui.dataField.SelectDurationField
import fi.riista.common.ui.intent.IntentHandler
import fi.riista.common.util.LocalDateTimeProvider
import kotlinx.serialization.Serializable

abstract class ModifyGroupHuntingDayController(
    protected val currentTimeProvider: LocalDateTimeProvider,
    private val stringProvider: StringProvider,
    private val startDateIsReadonly: Boolean,
) : ControllerWithLoadableModel<ModifyGroupHuntingDayViewModel>(),
    IntentHandler<ModifyGroupHuntingDayIntent>,
    HasUnreproducibleState<ModifyGroupHuntingDayController.HuntingDayData> {

    private lateinit var dataFieldProducers: List<DataFieldProducer<ProducerInput, GroupHuntingDayField>>

    val eventDispatchers: ModifyGroupHuntingDayEventDispatcher =
        ModifyGroupHuntingDayEventToIntentMapper(intentHandler = this)

    private val huntingMethodFieldFactory =
        EnumStringListFieldFactory.create<GroupHuntingMethodType>(stringProvider)

    protected var restoredHuntingDay: GroupHuntingDay? = null

    init {
        initializeDataFieldProducers()
    }

    /**
     * Saves (creates or updates) the hunting day.
     */
    abstract suspend fun saveHuntingDay(): GroupHuntingDayUpdateResponse

    /**
     * Gets the hunting group.
     */
    protected abstract fun getHuntingGroup(): HuntingGroup?

    /**
     * Creates a new [GroupHuntingDay] based on given [huntingDay] data that contains only those
     * fields that should be saved.
     *
     * By having this function we can collect all user entered values to the viewmodel
     * hunting day and strip unnecessary fields when saving the hunting day.
     */
    protected fun createHuntingDayToBeSaved(huntingDay: GroupHuntingDay): GroupHuntingDay {
        // save the field just in case as if the enum value is unknown to us
        val saveNumberOfHounds = huntingDay.huntingMethod.requiresHound() ?: true

        return GroupHuntingDay(
                id = huntingDay.id,
                type = huntingDay.type,
                rev = huntingDay.rev,
                huntingGroupId = huntingDay.huntingGroupId,
                startDateTime = huntingDay.startDateTime,
                endDateTime = huntingDay.endDateTime,
                breakDurationInMinutes = huntingDay.breakDurationInMinutes,
                snowDepth = huntingDay.snowDepth,
                huntingMethod = huntingDay.huntingMethod,
                numberOfHunters = huntingDay.numberOfHunters,
                numberOfHounds = when (saveNumberOfHounds) {
                    true -> huntingDay.numberOfHounds
                    false -> null
                },
                createdBySystem = huntingDay.createdBySystem,
        )
    }

    override fun handleIntent(intent: ModifyGroupHuntingDayIntent) {
        val viewModel = viewModelLoadStatus.value.loadedViewModel
                ?: run {
                    logger.w { "Failed to obtain loaded viewmodel for handling intent" }
                    return
                }

        val huntingGroup = getHuntingGroup()
                ?: run {
                    logger.w { "Failed to obtain hunting group for handling intent" }
                    return
                }

        val currentHuntingDay = viewModel.huntingDay
        val updatedHuntingDay = when (intent) {
            is ModifyGroupHuntingDayIntent.ChangeStartDateTime -> {
                createHuntingDayWithStartDateTime(currentHuntingDay,
                                                  huntingGroup,
                                                  intent.startDateAndTime)
                    .let { huntingDay ->
                        if (startDateIsReadonly) {
                            huntingDay
                        } else {
                            // start date can be moved, move end date with it
                            moveHuntingDayEndDateToSameDayAsStart(huntingDay, huntingGroup)
                        }
                    }
            }
            is ModifyGroupHuntingDayIntent.ChangeEndDateTime ->
                createHuntingDayWithEndDateTime(currentHuntingDay, huntingGroup, intent.endDateAndTime)
            is ModifyGroupHuntingDayIntent.ChangeNumberOfHunters ->
                currentHuntingDay.copy(numberOfHunters = intent.numberOfHunters)
            is ModifyGroupHuntingDayIntent.ChangeHuntingMethod ->
                currentHuntingDay.copy(huntingMethod = intent.huntingMethod)
            is ModifyGroupHuntingDayIntent.ChangeNumberOfHounds ->
                currentHuntingDay.copy(numberOfHounds = intent.numberOfHounds)
            is ModifyGroupHuntingDayIntent.ChangeSnowDepth ->
                currentHuntingDay.copy(snowDepth = intent.snowDepth)
            is ModifyGroupHuntingDayIntent.ChangeBreakDuration ->
                currentHuntingDay.copy(breakDurationInMinutes = intent.breakDuration.toTotalMinutes())
        }

        updateViewModel(ViewModelLoadStatus.Loaded(
                viewModel = createViewModel(updatedHuntingDay, huntingGroup)
        ))
    }

    private fun createHuntingDayWithStartDateTime(
        huntingDay: GroupHuntingDay,
        huntingGroup: HuntingGroup,
        startDateTime: LocalDateTime,
    ): GroupHuntingDay {
        val startDate = if (startDateIsReadonly) {
            // It is possible that the hunting day start date cannot be changed.
            // This is the case when we're editing an existing hunting day.
            huntingDay.startDateTime.date
        } else {
            startDateTime.date
        }

        var newStartDateTime = LocalDateTime(
                date = startDate,
                time = startDateTime.time
        )

        // restrict new start datetime to be within permit
        getMinimumStartDateTime(huntingGroup.permit).let { minStartDateTime ->
            newStartDateTime = newStartDateTime.coerceAtLeast(minStartDateTime)
        }
        newStartDateTime = newStartDateTime.coerceAtMost(
                getMaximumStartDateTime(huntingDay, huntingGroup.permit)
        )

        // hunting day id needs to also be updated if day is a locally created day
        val huntingDayId = GroupHuntingDayId.local(date = newStartDateTime.date)
            .takeIf { huntingDay.type.isLocal() }
                ?: huntingDay.id

        return huntingDay.copy(
                id = huntingDayId,
                startDateTime = newStartDateTime
        )
    }

    private fun moveHuntingDayEndDateToSameDayAsStart(
        huntingDay: GroupHuntingDay,
        huntingGroup: HuntingGroup,
    ): GroupHuntingDay {
        return createHuntingDayWithEndDateTime(
                huntingDay = huntingDay,
                huntingGroup = huntingGroup,
                endDateTime = huntingDay.endDateTime.changeDate(huntingDay.startDateTime.date)
        )
    }

    private fun createHuntingDayWithEndDateTime(
        huntingDay: GroupHuntingDay,
        huntingGroup: HuntingGroup,
        endDateTime: LocalDateTime,
    ): GroupHuntingDay {
        // restrict new end datetime to be within permit
        var newEndDateTime = endDateTime.coerceAtLeast(getMinimumEndDateTime(huntingDay))
        getMaximumEndDateTime(huntingGroup.permit).let { maxEndDateTime ->
            newEndDateTime = newEndDateTime.coerceAtMost(maxEndDateTime)
        }

        return huntingDay.copy(endDateTime = newEndDateTime)
    }

    protected fun createViewModel(huntingDay: GroupHuntingDay, huntingGroup: HuntingGroup):
            ModifyGroupHuntingDayViewModel {

        val producerInput = ProducerInput(
                huntingDay = huntingDay,
                huntingGroup = huntingGroup,
                includeHuntingDayDetails = huntingGroup.speciesCode.isMoose(),
                minStartDateTime = getMinimumStartDateTime(huntingGroup.permit),
                maxStartDateTime = getMaximumStartDateTime(huntingDay, huntingGroup.permit),
                minEndDateTime = getMinimumEndDateTime(huntingDay),
                maxEndDateTime = getMaximumEndDateTime(huntingGroup.permit),
                validationErrors = huntingDay.validate(huntingGroup.permit, currentTimeProvider)
        )

        return ModifyGroupHuntingDayViewModel(
                huntingDay = huntingDay,
                fields = dataFieldProducers.mapNotNull { producer ->
                    producer.produceDataField(producerInput)
                },
                huntingDayCanBeSaved = producerInput.validationErrors.isEmpty()
        )
    }

    private fun getMinimumStartDateTime(permit: HuntingGroupPermit): LocalDateTime {
        val now = currentTimeProvider.now()
        val earliestPermitDate = permit.earliestDate?.let {
            LocalDateTime(date = it, time = LocalTime(0, 0, 0))
        }

        return earliestPermitDate?.coerceAtMost(now) ?: now
    }

    private fun getMaximumStartDateTime(huntingDay: GroupHuntingDay, permit: HuntingGroupPermit): LocalDateTime {
        return getMaximumEndDateTime(permit).minus(minutes = huntingDay.breakDurationInMinutes ?: 0)
    }

    private fun getMinimumEndDateTime(huntingDay: GroupHuntingDay): LocalDateTime {
        return huntingDay.startDateTime.plus(minutes = huntingDay.breakDurationInMinutes ?: 0)
    }

    private fun getMaximumEndDateTime(permit: HuntingGroupPermit): LocalDateTime {
        val now = currentTimeProvider.now()
        val lastPermitDate = permit.lastDate?.let {
            LocalDateTime(date = it, time = LocalTime(23, 59, 0))
        }

        return lastPermitDate?.coerceAtMost(now) ?: now
    }

    override fun getUnreproducibleState(): HuntingDayData? {
        return viewModelLoadStatus.value.loadedViewModel
            ?.huntingDay
            ?.let {
                HuntingDayData(
                        id = it.id,
                        type = it.type,
                        rev = it.rev,
                        huntingGroupId = it.huntingGroupId,
                        startDateTime = it.startDateTime,
                        endDateTime = it.endDateTime,
                        breakDurationInMinutes = it.breakDurationInMinutes,
                        snowDepth = it.snowDepth,
                        huntingMethod = it.huntingMethod.rawBackendEnumValue,
                        numberOfHunters = it.numberOfHunters,
                        numberOfHounds = it.numberOfHounds,
                        createdBySystem = it.createdBySystem,
                )
            }

    }

    override fun restoreUnreproducibleState(state: HuntingDayData) {
        restoredHuntingDay = GroupHuntingDay(
                id = state.id,
                type = state.type,
                rev = state.rev,
                huntingGroupId = state.huntingGroupId,
                startDateTime = state.startDateTime,
                endDateTime = state.endDateTime,
                breakDurationInMinutes = state.breakDurationInMinutes,
                snowDepth = state.snowDepth,
                huntingMethod = state.huntingMethod.toBackendEnum(),
                numberOfHunters = state.numberOfHunters,
                numberOfHounds = state.numberOfHounds,
                createdBySystem = state.createdBySystem,
        )
    }

    private fun initializeDataFieldProducers() {
        dataFieldProducers = listOf(
                { input: ProducerInput ->
                    DateAndTimeField(
                            id = GroupHuntingDayField.START_DATE_AND_TIME,
                            dateAndTime = input.huntingDay.startDateTime
                    ) {
                        label = stringProvider.getString(RR.string.group_hunting_day_label_start_date_and_time)
                        readOnlyDate = startDateIsReadonly // don't allow changing the date of the start date
                        readOnlyTime = false // but time can be changed
                        requirementStatus = FieldRequirement.required()
                        minDateTime = input.minStartDateTime
                        maxDateTime = input.maxStartDateTime
                    }
                },
                { input: ProducerInput ->
                    DateAndTimeField(
                            id = GroupHuntingDayField.END_DATE_AND_TIME,
                            dateAndTime = input.huntingDay.endDateTime
                    ) {
                        label = stringProvider.getString(RR.string.group_hunting_day_label_end_date_and_time)
                        requirementStatus = FieldRequirement.required()
                        readOnly = false
                        minDateTime = input.minEndDateTime
                        maxDateTime = input.maxEndDateTime
                    }
                },
                { input: ProducerInput ->
                    val errorString = when {
                        input.validationErrors.contains(Error.DATES_NOT_WITHIN_PERMIT) -> {
                            // indicate the error even though recovery might be impossible in the mobile
                            RR.string.group_hunting_day_error_dates_not_within_permit
                        }
                        input.validationErrors.contains(Error.INVALID_DAYS_UNTIL_END) ->
                            RR.string.error_date_not_allowed
                        else -> null
                    }

                    if (errorString != null) {
                        LabelField(
                                id = GroupHuntingDayField.DATE_TIME_ERROR,
                                text = stringProvider.getString(errorString),
                                type = LabelField.Type.ERROR
                        ) {
                            paddingTop = Padding.NONE
                        }
                    } else {
                        null
                    }
                },
                { input: ProducerInput ->
                    if (!input.includeHuntingDayDetails) {
                        return@listOf null
                    }

                    IntField(
                            id = GroupHuntingDayField.NUMBER_OF_HUNTERS,
                            value = input.huntingDay.numberOfHunters
                    ) {
                        label = stringProvider.getString(RR.string.group_hunting_day_label_number_of_hunters)
                        readOnly = false
                        requirementStatus = FieldRequirement.required()
                    }
                },
                { input: ProducerInput ->
                    if (!input.includeHuntingDayDetails) {
                        return@listOf null
                    }

                    val fieldRequirementStatus = FieldRequirement.required()
                    huntingMethodFieldFactory.create(
                            fieldId = GroupHuntingDayField.HUNTING_METHOD,
                            currentEnumValue = input.huntingDay.huntingMethod,
                            allowEmptyValue = fieldRequirementStatus.isRequired().not()
                    ) {
                        label = stringProvider.getString(RR.string.group_hunting_day_label_hunting_method)
                        readOnly = false
                        requirementStatus = fieldRequirementStatus
                    }
                },
                { input: ProducerInput ->
                    if (!input.includeHuntingDayDetails) {
                        return@listOf null
                    }

                    val displayNumberOfHounds = input.huntingDay.huntingMethod.requiresHound()
                    if (displayNumberOfHounds != false) {
                        IntField(
                                id = GroupHuntingDayField.NUMBER_OF_HOUNDS,
                                value = input.huntingDay.numberOfHounds
                        ) {
                            label =
                                stringProvider.getString(RR.string.group_hunting_day_label_number_of_hounds)
                            readOnly = false
                            // only required if we are certain that number of hounds should be displayed
                            requirementStatus = if (displayNumberOfHounds == true) {
                                FieldRequirement.required()
                            } else {
                                FieldRequirement.voluntary()
                            }
                        }
                    } else {
                        null
                    }
                },
                { input: ProducerInput ->
                    if (!input.includeHuntingDayDetails) {
                        return@listOf null
                    }

                    IntField(
                            id = GroupHuntingDayField.SNOW_DEPTH,
                            value = input.huntingDay.snowDepth
                    ) {
                        label = stringProvider.getString(RR.string.group_hunting_day_label_snow_depth_centimeters)
                        readOnly = false
                        requirementStatus = FieldRequirement.voluntary()
                    }
                },
                { input: ProducerInput ->
                    if (!input.includeHuntingDayDetails) {
                        return@listOf null
                    }

                    val breakDuration = HoursAndMinutes(input.huntingDay.breakDurationInMinutes ?: 0)
                    // Requirements for breaks:
                    // - 30 minutes is the shortest allowed break.
                    // - Don't allow break that equals the hunting day duration (i.e. there must be active hunting as well)
                    var possibleBreakDurations =
                        when (val durationInMinutes = input.huntingDay.durationInMinutes) {
                            // allow 'no breaks' option
                            0 -> listOf(HoursAndMinutes(0))
                            else -> {
                                (0 until durationInMinutes step 30)
                                    .map { minutes ->
                                        HoursAndMinutes(minutes)
                                    }
                            }
                        }

                    // add current selection if not already present and if valid (within possible alternatives)
                    if (possibleBreakDurations.isNotEmpty() &&
                            !possibleBreakDurations.contains(breakDuration) &&
                            breakDuration < possibleBreakDurations.last()) {
                        possibleBreakDurations = (possibleBreakDurations + breakDuration)
                            .sortedBy { it.toTotalMinutes() }
                    }

                    SelectDurationField(
                            id = GroupHuntingDayField.BREAK_DURATION,
                            value = breakDuration,
                            possibleValues = possibleBreakDurations,
                    ) {
                        label = stringProvider.getString(RR.string.group_hunting_day_label_break_duration_minutes)
                        zeroMinutesStringId = RR.string.group_hunting_day_no_breaks
                        readOnly = false
                        requirementStatus = FieldRequirement.voluntary()
                        paddingBottom = Padding.LARGE // extra padding for the last item
                    }
                }
        ).map { DataFieldProducerProxy(it) }
    }

    private data class ProducerInput(
        val huntingDay: GroupHuntingDay,
        val huntingGroup: HuntingGroup,
        val includeHuntingDayDetails: Boolean,
        val minStartDateTime: LocalDateTime?,
        val maxStartDateTime: LocalDateTime,
        val minEndDateTime: LocalDateTime,
        val maxEndDateTime: LocalDateTime?,
        val validationErrors: List<Error>,
    )


    /**
     * The hunting day data currently being modified. The main difference is
     * that the huntingMethod is stored as in raw backend value.
     */
    @Serializable
    data class HuntingDayData(
        val id: GroupHuntingDayId,
        override val type: Entity.Type,
        val rev: Revision?,
        val huntingGroupId: HuntingGroupId,
        val startDateTime: LocalDateTime,
        val endDateTime: LocalDateTime,
        val breakDurationInMinutes: Int?,
        val snowDepth: Int?,
        val huntingMethod: GroupHuntingMethodTypeDTO?,
        val numberOfHunters: Int?,
        val numberOfHounds: Int?,
        val createdBySystem: Boolean,
    ): Entity

    protected fun BackendEnum<GroupHuntingMethodType>.requiresHound(): Boolean? {
        return value?.requiresHound
    }

    companion object {
        private val logger by getLogger(ModifyGroupHuntingDayController::class)
    }
}
