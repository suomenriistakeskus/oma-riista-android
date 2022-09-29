package fi.riista.mobile.feature.groupHunting.harvests

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.domain.groupHunting.ui.GroupHarvestField
import fi.riista.common.domain.groupHunting.ui.groupHarvest.modify.ModifyGroupHarvestController
import fi.riista.common.domain.groupHunting.ui.groupHarvest.modify.ModifyGroupHarvestViewModel
import fi.riista.common.model.*
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.common.ui.dataField.*
import fi.riista.mobile.R
import fi.riista.mobile.activity.MapViewerActivity
import fi.riista.mobile.activity.SelectStringWithIdActivity
import fi.riista.mobile.database.SpeciesResolver
import fi.riista.mobile.feature.groupHunting.DataFieldPageFragment
import fi.riista.mobile.feature.groupHunting.dataFields.viewHolder.*
import fi.riista.mobile.feature.groupHunting.huntingDays.select.SelectGroupHuntingDayActivity
import fi.riista.mobile.pages.MapExternalIdProvider
import fi.riista.mobile.riistaSdkHelpers.determineViewHolderType
import fi.riista.mobile.riistaSdkHelpers.fromJodaDateTime
import fi.riista.mobile.riistaSdkHelpers.registerLabelFieldViewHolderFactories
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime
import fi.riista.mobile.ui.DateTimePickerFragment
import fi.riista.mobile.ui.NoChangeAnimationsItemAnimator
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.*
import fi.riista.mobile.ui.showDatePickerFragment
import fi.riista.mobile.utils.MapUtils
import kotlinx.coroutines.*
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * A fragment for either approving or rejecting proposed [GroupHuntingHarvest]
 */
abstract class ModifyGroupHarvestFragment<
        Controller : ModifyGroupHarvestController,
        Manager : ModifyGroupHarvestFragment.BaseManager
        >
    : DataFieldPageFragment<GroupHarvestField>()
    , DataFieldViewHolderTypeResolver<GroupHarvestField>
    , MapOpener
    , MapExternalIdProvider
    , SelectHuntingDayLauncher<GroupHarvestField>
    , DateTimePickerFragmentLauncher<GroupHarvestField>
    , DateTimePickerFragment.Listener
    , ChoiceViewLauncher<GroupHarvestField> {

    interface BaseManager {
        val huntingGroupTarget: HuntingGroupTarget

        fun cancelHarvestOperation()
    }

    @Inject
    lateinit var speciesResolver: SpeciesResolver

    private lateinit var adapter: DataFieldRecyclerViewAdapter<GroupHarvestField>
    protected lateinit var saveButton: MaterialButton

    protected lateinit var manager: Manager
    protected lateinit var controller: Controller
    private val disposeBag = DisposeBag()

    private val locationRequestActivityResultLaunch = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val location = result.data?.getParcelableExtra<Location>(MapViewerActivity.RESULT_LOCATION)
            val source = result.data?.getStringExtra(MapViewerActivity.RESULT_LOCATION_SOURCE)

            if (location != null && source != null) {
                setLocation(location, source)
            }
        }
    }

    private val selectHuntingDayActivityResultLaunch = registerForActivityResult(StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            handleHuntingDaySelectionResult(data)
        }
    }

    private val selectStringWithIdActivityResultLaunch = registerForActivityResult(StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            handleSelectStringWithIdResult(data)
        }
    }

    // functionality required from subclass

    protected abstract fun onSaveButtonClicked()
    protected abstract fun getManagerFromContext(context: Context): Manager
    protected abstract fun getControllerFromManager(): Controller


    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        manager = getManagerFromContext(context)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_modify_proposed_group_harvest, container, false)

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
                manager.cancelHarvestOperation()
            }
        }

        saveButton = view.findViewById<MaterialButton>(R.id.btn_approve_proposed_harvest)!!
            .also { btn ->
                btn.setOnClickListener {
                    onSaveButtonClicked()
                }
            }

        return view
    }

    override fun resolveViewHolderType(dataField: DataField<GroupHarvestField>): DataFieldViewHolderType {
        return when (dataField) {
            is LabelField -> dataField.determineViewHolderType()
            is StringField -> when (dataField.settings.readOnly) {
                true -> DataFieldViewHolderType.READONLY_TEXT
                false -> DataFieldViewHolderType.EDITABLE_TEXT
            }
            is SpeciesField -> DataFieldViewHolderType.SPECIES_NAME_AND_ICON
            is GenderField -> DataFieldViewHolderType.EDITABLE_GENDER
            is DateAndTimeField -> DataFieldViewHolderType.EDITABLE_DATE_AND_TIME
            is HuntingDayAndTimeField -> DataFieldViewHolderType.SELECT_HUNTING_DAY_AND_TIME
            is AgeField -> DataFieldViewHolderType.EDITABLE_AGE
            is LocationField -> DataFieldViewHolderType.LOCATION_ON_MAP
            is BooleanField -> DataFieldViewHolderType.EDITABLE_BOOLEAN_AS_RADIO_TOGGLE
            is DoubleField -> DataFieldViewHolderType.EDITABLE_DOUBLE
            is StringListField -> DataFieldViewHolderType.SELECTABLE_STRING
            is IntField -> DataFieldViewHolderType.INT
            is InstructionsField -> DataFieldViewHolderType.INSTRUCTIONS
            else -> {
                throw IllegalArgumentException("Unexpected DataField type: ${dataField::class.simpleName}")
            }
        }
    }

    private fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<GroupHarvestField>) {
        adapter.apply {
            registerLabelFieldViewHolderFactories()
            registerViewHolderFactory(SpeciesNameAndIconViewHolder.Factory(speciesResolver))
            registerViewHolderFactory(
                LocationOnMapViewHolder.Factory(
                this@ModifyGroupHarvestFragment,
                mapExternalIdProvider = this@ModifyGroupHarvestFragment,
            ))
            registerViewHolderFactory(
                EditableTextViewHolder.Factory(
                eventDispatcher = controller.eventDispatchers.stringEventDispatcher
            ))
            registerViewHolderFactory(
                EditableGenderViewHolder.Factory(
                eventDispatcher = controller.eventDispatchers.genderEventDispatcher
            ))
            registerViewHolderFactory(
                EditableDateAndTimeViewHolder.Factory(
                pickerLauncher = this@ModifyGroupHarvestFragment
            ))
            registerViewHolderFactory(SelectHuntingDayAndTimeViewHolder.Factory(
                eventDispatcher = controller.eventDispatchers.localTimeEventDispatcher,
                selectHuntingDayLauncher = this@ModifyGroupHarvestFragment
            ))
            registerViewHolderFactory(
                EditableAgeViewHolder.Factory(
                eventDispatcher = controller.eventDispatchers.ageEventDispatcher
            ))
            registerViewHolderFactory(ReadOnlyTextViewHolder.Factory())
            registerViewHolderFactory(
                EditableBooleanAsRadioToggleViewHolder.Factory(
                eventDispatcher = controller.eventDispatchers.booleanEventDispatcher
            ))
            registerViewHolderFactory(
                EditableDoubleViewHolder.Factory(
                eventDispatcher = controller.eventDispatchers.doubleEventDispatcher
            ))
            registerViewHolderFactory(
                ChoiceViewHolder.Factory(
                eventDispatcher = controller.eventDispatchers.stringWithIdEventDispatcher,
                choiceViewLauncher = this@ModifyGroupHarvestFragment
            ))
            registerViewHolderFactory(
                IntFieldViewHolder.Factory(
                eventDispatcher = controller.eventDispatchers.intEventDispatcher
            ))
            registerViewHolderFactory(InstructionsViewHolder.Factory())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller.saveToBundle(outState, CONTROLLER_STATE_PREFIX)
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { viewModelLoadStatus ->
            updateBasedOnViewModel(viewModelLoadStatus)
        }.disposeBy(disposeBag)

        loadHarvestIfNotLoaded()
    }

    override fun onPause() {
        super.onPause()
        disposeBag.disposeAll()
    }

    private fun updateBasedOnViewModel(
        viewModelLoadStatus: ViewModelLoadStatus<ModifyGroupHarvestViewModel>) {

        when (viewModelLoadStatus) {
            ViewModelLoadStatus.NotLoaded,
            ViewModelLoadStatus.Loading,
            ViewModelLoadStatus.LoadFailed -> {
                adapter.setDataFields(listOf())
                saveButton.isEnabled = false
            }
            is ViewModelLoadStatus.Loaded -> {
                val viewModel = viewModelLoadStatus.viewModel
                adapter.setDataFields(viewModel.fields)
                saveButton.isEnabled = viewModel.harvestIsValid
            }
        }
    }

    private fun loadHarvestIfNotLoaded() {
        if (controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }

        MainScope().launch {
            controller.loadViewModel()
        }
    }

    override fun displayChoicesInSeparateView(
        fieldId: GroupHarvestField,
        mode: StringListField.Mode,
        choices: List<StringWithId>,
        selectedChoices: List<StringId>?,
        viewConfiguration: StringListField.ExternalViewConfiguration,
    ) {
        val intent = SelectStringWithIdActivity.getLaunchIntent(
            packageContext = requireContext(),
            fieldId = fieldId,
            mode = mode,
            possibleValues = choices,
            selectedValueIds = selectedChoices,
            configuration = viewConfiguration
        )

        selectStringWithIdActivityResultLaunch.launch(intent)
    }

    override fun pickDateOrTime(
        fieldId: GroupHarvestField,
        pickMode: DateTimePickerFragment.PickMode,
        currentDateTime: LocalDateTime,
        minDateTime: LocalDateTime?,
        maxDateTime: LocalDateTime?
    ) {
        val datePickerFragment = DateTimePickerFragment.create(
                dialogId = fieldId.toInt(),
                pickMode = pickMode,
                selectedDateTime = currentDateTime.toJodaDateTime(),
                minDateTime = minDateTime?.toJodaDateTime(),
                maxDateTime = maxDateTime?.toJodaDateTime()
        )

        showDatePickerFragment(datePickerFragment, fieldId.toInt())
    }

    override fun onDateTimeSelected(dialogId: Int, dateTime: DateTime) {
        GroupHarvestField.fromInt(dialogId)?.let { fieldId ->
            controller.eventDispatchers.localDateTimeEventDispatcher.dispatchLocalDateTimeChanged(
                    fieldId, LocalDateTime.fromJodaDateTime(dateTime))
        }
    }

    override fun openMap(location: Location) {
        val intent = Intent(context, MapViewerActivity::class.java)
        intent.putExtra(MapViewerActivity.EXTRA_EDIT_MODE, true)
        intent.putExtra(MapViewerActivity.EXTRA_START_LOCATION, location)
        intent.putExtra(MapViewerActivity.EXTRA_NEW, false)
        intent.putExtra(MapViewerActivity.EXTRA_EXTERNAL_ID, getMapExternalId())
        locationRequestActivityResultLaunch.launch(intent)
    }

    override fun getMapExternalId(): String? {
        return controller.getLoadedViewModelOrNull()?.huntingGroupArea?.externalId
    }

    override fun launchHuntingDaySelection(
        fieldId: GroupHarvestField,
        selectedHuntingDayId: GroupHuntingDayId?,
        preferredHuntingDayDate: LocalDate?,
    ) {
        val intent = SelectGroupHuntingDayActivity.getLaunchIntent(
                packageContext = requireContext(),
                huntingGroupTarget = manager.huntingGroupTarget,
                fieldId = fieldId.toInt(),
                selectedHuntingDayId = selectedHuntingDayId,
                preferredHuntingDayDate = preferredHuntingDayDate,
        )
        selectHuntingDayActivityResultLaunch.launch(intent)
    }

    private fun handleHuntingDaySelectionResult(data: Intent) {
        val fieldId = GroupHarvestField.fromInt(
                SelectGroupHuntingDayActivity.getFieldIdFromIntent(data)
        )
        val huntingDayId = SelectGroupHuntingDayActivity.getHuntingDayIdFromIntent(data)

        if (fieldId != null && huntingDayId != null) {
            controller.eventDispatchers.huntingDayEventDispatcher.dispatchHuntingDayChanged(fieldId, huntingDayId)
        }
    }

    private fun handleSelectStringWithIdResult(data: Intent) {
        val fieldId = GroupHarvestField.fromInt(
            SelectStringWithIdActivity.getFieldIdFromIntent(data)
        )
        val selectedValue = SelectStringWithIdActivity.getStringWithIdResulListFromIntent(data)

        if (fieldId != null && !selectedValue.isNullOrEmpty()) {
            controller.eventDispatchers.stringWithIdEventDispatcher
                .dispatchStringWithIdChanged(fieldId, selectedValue)
        }
    }

    private fun setLocation(location: Location, source: String) {
        val coordinates = MapUtils.WGS84toETRSTM35FIN(location.latitude, location.longitude)
        if (coordinates.first != null && coordinates.second != null) {
            val geoLocation = ETRMSGeoLocation(
                    coordinates.first.toInt(),
                    coordinates.second.toInt(),
                    source.toBackendEnum(),
                    // todo: consider passing accuracy and other values as well
                    null, null, null)
            controller.eventDispatchers.locationEventDispatcher.dispatchLocationChanged(GroupHarvestField.LOCATION, geoLocation)
        }
    }

    companion object {
        private const val CONTROLLER_STATE_PREFIX = "MGHF_controller"
    }
}
