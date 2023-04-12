package fi.riista.mobile.feature.harvest

import android.app.Activity
import android.app.AlertDialog
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
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.harvest.HarvestOperationResponse
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.modify.ModifyHarvestAction
import fi.riista.common.domain.harvest.ui.modify.ModifyHarvestActionHandler
import fi.riista.common.domain.harvest.ui.modify.ModifyHarvestController
import fi.riista.common.domain.harvest.ui.modify.ModifyHarvestViewModel
import fi.riista.common.domain.model.PermitNumber
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.logging.getLogger
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.StringId
import fi.riista.common.model.StringWithId
import fi.riista.common.model.toBackendEnum
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.common.ui.dataField.AgeField
import fi.riista.common.ui.dataField.BooleanField
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DateAndTimeField
import fi.riista.common.ui.dataField.DoubleField
import fi.riista.common.ui.dataField.GenderField
import fi.riista.common.ui.dataField.InstructionsField
import fi.riista.common.ui.dataField.IntField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.LocationField
import fi.riista.common.ui.dataField.SpeciesField
import fi.riista.common.ui.dataField.SpecimenField
import fi.riista.common.ui.dataField.StringField
import fi.riista.common.ui.dataField.StringListField
import fi.riista.mobile.R
import fi.riista.mobile.activity.ChooseSpeciesActivity
import fi.riista.mobile.activity.HarvestPermitActivity
import fi.riista.mobile.activity.MapViewerActivity
import fi.riista.mobile.activity.SelectStringWithIdActivity
import fi.riista.mobile.database.SpeciesResolver
import fi.riista.mobile.feature.groupHunting.DataFieldPageFragment
import fi.riista.mobile.feature.specimens.SpecimensActivity
import fi.riista.mobile.models.Species
import fi.riista.mobile.riistaSdkHelpers.AppPermitProvider
import fi.riista.mobile.riistaSdkHelpers.determineViewHolderType
import fi.riista.mobile.riistaSdkHelpers.fromJodaDateTime
import fi.riista.mobile.riistaSdkHelpers.registerLabelFieldViewHolderFactories
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime
import fi.riista.mobile.sync.SyncConfig
import fi.riista.mobile.ui.CanIndicateBusy
import fi.riista.mobile.ui.DateTimePickerFragment
import fi.riista.mobile.ui.NoChangeAnimationsItemAnimator
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.*
import fi.riista.mobile.ui.registerDatePickerFragmentResultListener
import fi.riista.mobile.ui.showDatePickerFragment
import fi.riista.mobile.utils.ChangeImageHelper
import fi.riista.mobile.utils.EditUtils
import fi.riista.mobile.utils.MapUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * A fragment for either creating or editing [CommonHarvest]
 */
abstract class ModifyHarvestFragment<
        Controller : ModifyHarvestController,
        Manager : ModifyHarvestFragment.BaseManager
        >
    : DataFieldPageFragment<CommonHarvestField>()
    , DataFieldViewHolderTypeResolver<CommonHarvestField>
    , ModifyHarvestActionHandler
    , MapOpener
    , SpeciesSelectionLauncher<CommonHarvestField>
    , DateTimePickerFragmentLauncher<CommonHarvestField>
    , DateTimePickerFragment.Listener
    , ChoiceViewLauncher<CommonHarvestField>
    , SpecimensActivityLauncher<CommonHarvestField> {

    interface BaseManager : CanIndicateBusy {
        fun cancelHarvestOperation()
    }

    @Inject
    lateinit var speciesResolver: SpeciesResolver

    @Inject
    lateinit var permitProvider: AppPermitProvider

    @Inject
    lateinit var syncConfig: SyncConfig

    private lateinit var adapter: DataFieldRecyclerViewAdapter<CommonHarvestField>
    private lateinit var saveButton: MaterialButton

    protected lateinit var manager: Manager
    protected abstract val controller: Controller

    private val disposeBag = DisposeBag()
    private var saveScope: CoroutineScope? = null

    private val locationRequestActivityResultLaunch = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val location = result.data?.getParcelableExtra<Location>(MapViewerActivity.RESULT_LOCATION)
            val source = result.data?.getStringExtra(MapViewerActivity.RESULT_LOCATION_SOURCE)

            if (location != null && source != null) {
                setLocation(location, source)
            }
        }
    }

    private val selectPermitActivityResultLaunch = registerForActivityResult(StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            handleSelectedPermit(data)
        }
    }

    private val selectSpeciesActivityResultLaunch = registerForActivityResult(StartActivityForResult() ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleSelectedSpecies(data = result.data)
        }
    }

    private val selectStringWithIdActivityResultLaunch = registerForActivityResult(StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            handleSelectStringWithIdResult(data)
        }
    }

    private val editSpecimensActivityResultLaunch = registerForActivityResult(StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            handleSpecimenEditResult(data)
        }
    }

    private val changeImageHelper = ChangeImageHelper()

    // functionality required from subclass

    protected abstract fun notifyManagerAboutSuccessfulSave(harvest: CommonHarvest)
    protected abstract fun getManagerFromContext(context: Context): Manager


    private fun onSaveButtonClicked() {
        manager.indicateBusy()

        val scope = MainScope()

        scope.launch {
            val result = controller.saveHarvest(updateToBackend = syncConfig.isAutomatic())

            // allow cancellation to take effect i.e don't continue updating UI
            // if saveScope has been cancelled
            yield()

            if (!isResumed) {
                return@launch
            }

            val networkResponse = result.networkSaveResponse
            val databaseResponse = result.databaseSaveResponse

            if (networkResponse is HarvestOperationResponse.NetworkFailure &&
                networkResponse.statusCode == CONFLICT_STATUS_CODE
            ) {
                onSaveFailed(
                    networkStatusCode = networkResponse.statusCode,
                    debugMessage = networkResponse.errorMessage
                )
            } else if (databaseResponse is HarvestOperationResponse.Success) {
                notifyManagerAboutSuccessfulSave(harvest = databaseResponse.harvest)
            } else {
                onSaveFailed(networkStatusCode = null, debugMessage = result.errorMessage)
            }
        }

        saveScope = scope
    }

    private fun onSaveFailed(networkStatusCode: Int?, debugMessage: String?) {
        debugMessage?.let {
            logger.d { "Save failed: $it" }
        }

        val message = when (networkStatusCode) {
            CONFLICT_STATUS_CODE -> R.string.eventoutdated
            else -> R.string.eventeditfailed
        }

        manager.hideBusyIndicators {
            AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .create()
                .show()
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        manager = getManagerFromContext(context)

        changeImageHelper.initialize(fragment = this) { image ->
            controller.eventDispatchers.imageEventDispatcher.setEntityImage(image)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_modify_harvest, container, false)

        controller.modifyHarvestActionHandler = this

        savedInstanceState?.let {
            changeImageHelper.restoreFromSavedInstanceState(savedInstanceState)
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

        saveButton = view.findViewById<MaterialButton>(R.id.btn_save)!!
            .also { btn ->
                btn.setOnClickListener {
                    onSaveButtonClicked()
                }
            }

        registerDatePickerFragmentResultListener(DATE_PICKER_REQUEST_CODE)
        return view
    }

    override fun resolveViewHolderType(dataField: DataField<CommonHarvestField>): DataFieldViewHolderType {
        return when (dataField) {
            is LabelField -> dataField.determineViewHolderType()
            is StringField -> when (dataField.settings.readOnly) {
                true -> DataFieldViewHolderType.READONLY_TEXT
                false -> DataFieldViewHolderType.EDITABLE_TEXT
            }
            is LocationField -> DataFieldViewHolderType.LOCATION_ON_MAP
            is SpeciesField -> DataFieldViewHolderType.SPECIES_NAME_AND_ICON
            is DateAndTimeField -> DataFieldViewHolderType.EDITABLE_DATE_AND_TIME
            is SpecimenField -> DataFieldViewHolderType.SPECIMEN
            is GenderField -> DataFieldViewHolderType.EDITABLE_GENDER
            is AgeField -> DataFieldViewHolderType.EDITABLE_AGE
            is BooleanField -> {
                when (dataField.settings.appearance) {
                    BooleanField.Appearance.YES_NO_BUTTONS -> DataFieldViewHolderType.EDITABLE_BOOLEAN_AS_RADIO_TOGGLE
                    BooleanField.Appearance.CHECKBOX -> DataFieldViewHolderType.BOOLEAN_AS_CHECKBOX
                }
            }
            is DoubleField -> DataFieldViewHolderType.EDITABLE_DOUBLE
            is StringListField -> DataFieldViewHolderType.SELECTABLE_STRING
            is IntField -> DataFieldViewHolderType.INT
            is InstructionsField -> DataFieldViewHolderType.INSTRUCTIONS
            else -> {
                throw IllegalArgumentException("Unexpected DataField type: ${dataField::class.simpleName}")
            }
        }
    }

    private fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<CommonHarvestField>) {
        adapter.apply {
            registerLabelFieldViewHolderFactories(
                linkActionEventDispatcher = controller.eventDispatchers.linkActionEventDispatcher
            )
            registerViewHolderFactory(
                SelectSpeciesAndImageViewHolder.Factory(
                    speciesResolver = speciesResolver,
                    speciesSelectionLauncher = this@ModifyHarvestFragment,
                    entityImageActionLauncher = changeImageHelper,
                    entryType = SelectSpeciesAndImageViewHolder.EntryType.HARVEST,
                ))
            registerViewHolderFactory(
                LocationOnMapViewHolder.Factory(
                    mapOpener = this@ModifyHarvestFragment,
                    mapExternalIdProvider = null,
                ))
            registerViewHolderFactory(
                EditableDateAndTimeViewHolder.Factory(
                    pickerLauncher = this@ModifyHarvestFragment,
                ))
            registerViewHolderFactory(
                SpecimensViewHolder.Factory(
                    activityLauncher = this@ModifyHarvestFragment,
                ))
            registerViewHolderFactory(
                EditableGenderViewHolder.Factory(
                    eventDispatcher = controller.eventDispatchers.genderEventDispatcher,
                )
            )
            registerViewHolderFactory(
                EditableAgeViewHolder.Factory(
                    eventDispatcher = controller.eventDispatchers.ageEventDispatcher,
                )
            )
            registerViewHolderFactory(
                EditableBooleanAsRadioToggleViewHolder.Factory(
                    eventDispatcher = controller.eventDispatchers.booleanEventDispatcher
                ))
            registerViewHolderFactory(
                BooleanAsCheckboxViewHolder.Factory(
                    eventDispatcher = controller.eventDispatchers.booleanEventDispatcher
                ))
            registerViewHolderFactory(
                EditableTextViewHolder.Factory(
                    eventDispatcher = controller.eventDispatchers.stringEventDispatcher,
                ))
            registerViewHolderFactory(ReadOnlyTextViewHolder.Factory())
            registerViewHolderFactory(
                ChoiceViewHolder.Factory(
                    eventDispatcher = controller.eventDispatchers.stringWithIdEventDispatcher,
                    choiceViewLauncher = this@ModifyHarvestFragment,
            ))
            registerViewHolderFactory(
                EditableDoubleViewHolder.Factory(
                    eventDispatcher = controller.eventDispatchers.doubleEventDispatcher
                ))
            registerViewHolderFactory(
                IntFieldViewHolder.Factory(
                    eventDispatcher = controller.eventDispatchers.intEventDispatcher,
            ))
            registerViewHolderFactory(InstructionsViewHolder.Factory())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        changeImageHelper.saveInstanceState(outState)
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
        saveScope?.cancel()
        saveScope = null
    }

    private fun updateBasedOnViewModel(
        viewModelLoadStatus: ViewModelLoadStatus<ModifyHarvestViewModel>,
    ) {
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

    override fun handleModifyHarvestAction(action: ModifyHarvestAction) {
        when (action) {
            is ModifyHarvestAction.SelectPermit -> launchPermitSelection(permitNumber = action.currentPermitNumber)
        }
    }

    private fun launchPermitSelection(permitNumber: PermitNumber?) {
        val intent = Intent(context, HarvestPermitActivity::class.java).also {
            if (permitNumber.isNullOrBlank().not()) {
                it.putExtra(HarvestPermitActivity.EXTRA_PERMIT_NUMBER, permitNumber)
            }
        }

        selectPermitActivityResultLaunch.launch(intent)
    }

    private fun handleSelectedPermit(data: Intent) {
        val permit = data.getStringExtra(HarvestPermitActivity.RESULT_PERMIT_NUMBER)
            ?.let { permitNumber ->
                permitProvider.getPermit(permitNumber)
            }
            ?: kotlin.run {
                logger.d { "Failed to find a permit for permit number. Not selecting permit!" }
                return
            }
        val speciesCode: SpeciesCode? =
            data.getIntExtra(HarvestPermitActivity.RESULT_PERMIT_SPECIES, -1).takeIf { it != -1 }

        controller.eventDispatchers.permitEventDispatcher.selectPermit(
            permit = permit,
            speciesCode = speciesCode
        )
    }

    override fun displayChoicesInSeparateView(
        fieldId: CommonHarvestField,
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
        fieldId: CommonHarvestField,
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
        CommonHarvestField.fromInt(fieldId)?.let { fieldId ->
            controller.eventDispatchers.localDateTimeEventDispatcher.dispatchLocalDateTimeChanged(
                    fieldId, LocalDateTime.fromJodaDateTime(dateTime))
        }
    }

    override fun openMap(location: Location) {
        val intent = Intent(context, MapViewerActivity::class.java)
        intent.putExtra(MapViewerActivity.EXTRA_EDIT_MODE, true)
        intent.putExtra(MapViewerActivity.EXTRA_START_LOCATION, location)
        intent.putExtra(MapViewerActivity.EXTRA_NEW, false)
        intent.putExtra(MapViewerActivity.EXTRA_SHOW_ITEMS, false)
        locationRequestActivityResultLaunch.launch(intent)
    }

    private fun handleSelectStringWithIdResult(data: Intent) {
        val fieldId = CommonHarvestField.fromInt(
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
            controller.eventDispatchers.locationEventDispatcher.dispatchLocationChanged(
                fieldId = CommonHarvestField.LOCATION,
                value = geoLocation
            )
        }
    }

    override fun launchSpeciesSelection(fieldId: CommonHarvestField, selectableSpecies: SpeciesField.SelectableSpecies) {
        EditUtils.startSpeciesSelection(
            parentFragment = this,
            activityResultLauncher = selectSpeciesActivityResultLaunch,
            fieldId = fieldId,
            selectableSpecies = selectableSpecies,
        )
    }

    private fun handleSelectedSpecies(data: Intent?) {
        if (data == null) {
            return
        }

        val fieldId = CommonHarvestField.fromInt(
            value = data.getIntExtra(ChooseSpeciesActivity.EXTRA_FIELD_ID, -1)
        ) ?: kotlin.run {
            logger.d { "Failed to obtain field id from species selection results" }
            return
        }

        val species = (data.getSerializableExtra(ChooseSpeciesActivity.RESULT_SPECIES) as? Species)
            ?.let { species ->
                if (species.mId != -1) {
                    fi.riista.common.domain.model.Species.Known(speciesCode = species.mId)
                } else {
                    fi.riista.common.domain.model.Species.Other
                }
            }
            ?: kotlin.run {
                logger.d { "Failed to obtain species from species selection results" }
                return
            }

        controller.eventDispatchers.speciesEventDispatcher.dispatchSpeciesChanged(
            fieldId = fieldId,
            value = species,
        )
    }

    override fun viewSpecimens(
        fieldId: CommonHarvestField,
        mode: SpecimensActivity.Mode,
        specimenData: SpecimenFieldDataContainer,
    ) {
        val launchIntent = SpecimensActivity.getLaunchIntentForMode(
            packageContext = requireContext(),
            mode = mode,
            fieldId = fieldId.toInt(),
            specimenData = specimenData
        )

        editSpecimensActivityResultLaunch.launch(launchIntent)
    }

    private fun handleSpecimenEditResult(data: Intent) {
        val fieldId = CommonHarvestField.fromInt(
            SpecimensActivity.getFieldIdFromIntent(data)
        )
        val specimenData = SpecimensActivity.getSpecimenDataFromIntent(data)

        if (fieldId != null) {
            controller.eventDispatchers.specimenEventDispatcher
                .dispatchSpecimenDataChanged(fieldId, specimenData)
        }
    }

    companion object {
        private const val PREFIX = "ModifyHarvestFragment"
        private const val CONTROLLER_STATE_PREFIX = "${PREFIX}_controller"
        private const val CONFLICT_STATUS_CODE = 409
        private const val DATE_PICKER_REQUEST_CODE = "MHF_date_picker_request_code"

        private val logger by getLogger(ModifyHarvestFragment::class)
    }
}
