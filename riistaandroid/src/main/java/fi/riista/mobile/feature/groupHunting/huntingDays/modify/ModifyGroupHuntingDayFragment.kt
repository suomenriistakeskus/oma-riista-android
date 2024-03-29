package fi.riista.mobile.feature.groupHunting.huntingDays.modify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import fi.riista.common.domain.groupHunting.GroupHuntingDayUpdateResponse
import fi.riista.common.domain.groupHunting.model.GroupHuntingDay
import fi.riista.common.domain.groupHunting.ui.huntingDays.modify.GroupHuntingDayField
import fi.riista.common.domain.groupHunting.ui.huntingDays.modify.ModifyGroupHuntingDayController
import fi.riista.common.domain.groupHunting.ui.huntingDays.modify.ModifyGroupHuntingDayViewModel
import fi.riista.common.model.HoursAndMinutes
import fi.riista.common.model.LocalDateTime
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DateAndTimeField
import fi.riista.common.ui.dataField.IntField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.SelectDurationField
import fi.riista.common.ui.dataField.StringListField
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.DataFieldPageFragment
import fi.riista.mobile.feature.groupHunting.huntingDays.ViewOrEditGroupHuntingDayFragmentManager
import fi.riista.mobile.riistaSdkHelpers.determineViewHolderType
import fi.riista.mobile.riistaSdkHelpers.fromJodaDateTime
import fi.riista.mobile.riistaSdkHelpers.registerLabelFieldViewHolderFactories
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.ui.DateTimePickerFragment
import fi.riista.mobile.ui.DurationPickerFragment
import fi.riista.mobile.ui.NoChangeAnimationsItemAnimator
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.ChoiceViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderType
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderTypeResolver
import fi.riista.mobile.ui.dataFields.viewHolder.DateTimePickerFragmentLauncher
import fi.riista.mobile.ui.dataFields.viewHolder.DurationPickerFragmentLauncher
import fi.riista.mobile.ui.dataFields.viewHolder.EditableDateAndTimeViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.IntFieldViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.SelectDurationViewHolder
import fi.riista.mobile.ui.registerDatePickerFragmentResultListener
import fi.riista.mobile.ui.registerDurationPickerFragmentResultListener
import fi.riista.mobile.ui.showDatePickerFragment
import fi.riista.mobile.ui.showDurationPickerFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.joda.time.DateTime

/**
 * A fragment for modifying (e.g. editing) a [GroupHuntingDay]
 */
abstract class ModifyGroupHuntingDayFragment<M : ModifyGroupHuntingDayFragment.Manager>
    : DataFieldPageFragment<GroupHuntingDayField>()
    , DataFieldViewHolderTypeResolver<GroupHuntingDayField>
    , DurationPickerFragmentLauncher<GroupHuntingDayField>
    , DurationPickerFragment.Listener
    , DateTimePickerFragmentLauncher<GroupHuntingDayField>
    , DateTimePickerFragment.Listener
{

    interface Manager: ViewOrEditGroupHuntingDayFragmentManager {

        /**
         * Cancels modifying (e.g. editing) the hunting day.
         */
        fun cancelHuntingDayModification()

        /**
         * To be called when starting to save a hunting day. Allows the parent
         * activity to display loading indicators.
         */
        fun onSavingHuntingDay()

        /**
         * To be called when saving hunting day has completed either successfully or
         * with an error.
         *
         * [indicatorsDismissed] block will be called once any loading indicators have
         * been dismissed.
         */
        fun onHuntingDaySaveCompleted(success: Boolean, indicatorsDismissed: () -> Unit = {})
    }

    private lateinit var adapter: DataFieldRecyclerViewAdapter<GroupHuntingDayField>
    private lateinit var saveButton: MaterialButton

    protected lateinit var manager: M
    private lateinit var controller: ModifyGroupHuntingDayController
    private val disposeBag = DisposeBag()

    private var saveScope: CoroutineScope? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_group_hunting_day, container, false)
        setViewTitle(R.string.group_hunting_hunting_day)

        controller = getControllerFromManager()

        savedInstanceState?.let {
           controller.restoreFromBundle(it, CONTROLLER_STATE_PREFIX)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_data_fields)!!
        recyclerView.adapter = createAdapter(viewHolderTypeResolver = this)
            .also { adapter ->
                this.adapter = adapter
                registerViewHolderFactories(adapter)
            }
        recyclerView.itemAnimator = NoChangeAnimationsItemAnimator()

        view.findViewById<MaterialButton>(R.id.btn_cancel)?.let { btn ->
            btn.setOnClickListener {
                manager.cancelHuntingDayModification()
            }
        }

        saveButton = view.findViewById<MaterialButton>(R.id.btn_save)!!
            .also { btn ->
                btn.setOnClickListener {
                    saveHuntingDay()
                }
            }

        registerDatePickerFragmentResultListener(DATE_PICKER_REQUEST_CODE)
        registerDurationPickerFragmentResultListener(DURATION_PICKER_REQUEST_CODE)
        return view
    }

    protected abstract fun getControllerFromManager(): ModifyGroupHuntingDayController

    private fun saveHuntingDay() {
        manager.onSavingHuntingDay()

        val scope = MainScope()

        scope.launch {
            val saveResponse = controller.saveHuntingDay()

            // allow cancellation to take effect i.e don't continue updating UI
            // if saveScope has been cancelled
            yield()

            when (saveResponse) {
                is GroupHuntingDayUpdateResponse.Updated -> {
                    manager.onHuntingDaySaveCompleted(true)
                }
                is GroupHuntingDayUpdateResponse.Failed,
                GroupHuntingDayUpdateResponse.Error -> {
                    manager.onHuntingDaySaveCompleted(false) {
                        if (!isResumed) {
                            return@onHuntingDaySaveCompleted
                        }

                        AlertDialogFragment.Builder(
                            requireContext(),
                            AlertDialogId.MODIFY_GROUP_HUNTING_DAY_FRAGMENT_HUNTING_DAY_SAVE_FAILED,
                        )
                            .setMessage(R.string.group_hunting_day_save_failed_generic)
                            .setPositiveButton(R.string.ok)
                            .build()
                            .show(requireActivity().supportFragmentManager)
                    }
                }
            }
        }
        saveScope = scope
    }

    override fun resolveViewHolderType(dataField: DataField<GroupHuntingDayField>): DataFieldViewHolderType {
        return when (dataField) {
            is LabelField -> dataField.determineViewHolderType()
            is DateAndTimeField -> DataFieldViewHolderType.EDITABLE_DATE_AND_TIME
            is StringListField -> DataFieldViewHolderType.SELECTABLE_STRING
            is IntField -> DataFieldViewHolderType.INT
            is SelectDurationField -> DataFieldViewHolderType.SELECTABLE_DURATION
            else -> {
                throw IllegalArgumentException("Unexpected DataField type: ${dataField::class.simpleName}")
            }
        }
    }

    private fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<GroupHuntingDayField>) {
        adapter.apply {
            registerLabelFieldViewHolderFactories(linkActionEventDispatcher = null)
            registerViewHolderFactory(
                EditableDateAndTimeViewHolder.Factory(
                    pickerLauncher = this@ModifyGroupHuntingDayFragment
            ))
            registerViewHolderFactory(ChoiceViewHolder.Factory(controller.eventDispatchers.stringWithIdDispatcher))
            registerViewHolderFactory(IntFieldViewHolder.Factory(controller.eventDispatchers.intEventDispatcher))
            registerViewHolderFactory(
                SelectDurationViewHolder.Factory(
                    pickerLauncher = this@ModifyGroupHuntingDayFragment
            ))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller.saveToBundle(outState, CONTROLLER_STATE_PREFIX)
    }

    override fun showDurationPickerFragment(
        fieldId: GroupHuntingDayField,
        possibleDurations: List<HoursAndMinutes>,
        selectedDuration: HoursAndMinutes
    ) {
        val durationPickerFragment = DurationPickerFragment.create(
            requestCode = DURATION_PICKER_REQUEST_CODE,
            fieldId = fieldId.toInt(),
            dialogTitle = requireContext().getString(R.string.group_hunting_day_select_break_duration),
            possibleDurations = possibleDurations,
            selectedDuration = selectedDuration,
        )

        showDurationPickerFragment(durationPickerFragment)
    }

    override fun onDurationSelected(fieldId: Int, duration: HoursAndMinutes) {
        GroupHuntingDayField.fromInt(fieldId)?.let {
            controller.eventDispatchers.durationEventDispatcher.dispatchHoursAndMinutesChanged(it, duration)
        }
    }

    override fun pickDateOrTime(
        fieldId: GroupHuntingDayField,
        pickMode: DateTimePickerFragment.PickMode,
        currentDateTime: LocalDateTime,
        minDateTime: LocalDateTime?,
        maxDateTime: LocalDateTime?
    ) {
        val datePickerFragment = DateTimePickerFragment.create(
                requestCode = DATE_PICKER_REQUEST_CODE,
                fieldId = fieldId.toInt(),
                pickMode = pickMode,
                selectedDateTime = currentDateTime.toJodaDateTime(),
                minDateTime = minDateTime?.toJodaDateTime(),
                maxDateTime = maxDateTime?.toJodaDateTime()
        )

        showDatePickerFragment(datePickerFragment)
    }

    override fun onDateTimeSelected(fieldId: Int, dateTime: DateTime) {
        GroupHuntingDayField.fromInt(fieldId)?.let {
            controller.eventDispatchers.dateTimeEventDispatcher.dispatchLocalDateTimeChanged(
                it, LocalDateTime.fromJodaDateTime(dateTime)
            )
        }
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { viewModelLoadStatus ->
            when (viewModelLoadStatus) {
                ViewModelLoadStatus.NotLoaded -> {}
                ViewModelLoadStatus.Loading -> manager.onViewModelLoading()
                ViewModelLoadStatus.LoadFailed -> manager.onViewModelLoadFailed()
                is ViewModelLoadStatus.Loaded -> {
                    manager.onViewModelLoaded()
                    displayViewModelData(viewModelLoadStatus.viewModel)
                }
            }
        }.disposeBy(disposeBag)

        loadHuntingDayIfNotLoaded()
    }

    private fun displayViewModelData(viewModel: ModifyGroupHuntingDayViewModel) {
        adapter.setDataFields(viewModel.fields)
        saveButton.isEnabled = viewModel.huntingDayCanBeSaved
    }

    override fun onPause() {
        super.onPause()

        saveScope?.cancel()
        disposeBag.disposeAll()
    }

    private fun loadHuntingDayIfNotLoaded() {
        val loadStatus = controller.viewModelLoadStatus.value
        if (loadStatus is ViewModelLoadStatus.Loaded) {
            return
        }

        MainScope().launch {
            controller.loadViewModel()
        }
    }

    companion object {
        private const val CONTROLLER_STATE_PREFIX = "MGHDF_controller"
        private const val DATE_PICKER_REQUEST_CODE = "MGHDF_date_picker_request_code"
        private const val DURATION_PICKER_REQUEST_CODE = "MGHDF_duration_picker_request_code"
    }
}
