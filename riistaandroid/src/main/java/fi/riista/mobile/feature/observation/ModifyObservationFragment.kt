package fi.riista.mobile.feature.observation

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
import fi.riista.common.domain.observation.ObservationOperationResponse
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.domain.observation.ui.modify.ModifyObservationController
import fi.riista.common.domain.observation.ui.modify.ModifyObservationViewModel
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
import fi.riista.common.ui.dataField.BooleanField
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DateAndTimeField
import fi.riista.common.ui.dataField.DoubleField
import fi.riista.common.ui.dataField.IntField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.LocationField
import fi.riista.common.ui.dataField.SpeciesField
import fi.riista.common.ui.dataField.SpecimenField
import fi.riista.common.ui.dataField.StringField
import fi.riista.common.ui.dataField.StringListField
import fi.riista.mobile.R
import fi.riista.mobile.activity.ChooseSpeciesActivity
import fi.riista.mobile.activity.MapViewerActivity
import fi.riista.mobile.activity.SelectStringWithIdActivity
import fi.riista.mobile.database.SpeciesResolver
import fi.riista.mobile.feature.groupHunting.DataFieldPageFragment
import fi.riista.mobile.feature.specimens.SpecimensActivity
import fi.riista.mobile.models.Species
import fi.riista.mobile.riistaSdkHelpers.determineViewHolderType
import fi.riista.mobile.riistaSdkHelpers.fromJodaDateTime
import fi.riista.mobile.riistaSdkHelpers.registerLabelFieldViewHolderFactories
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime
import fi.riista.mobile.sync.AppSync
import fi.riista.mobile.sync.PreventAppSyncWhileModifyingSynchronizableEntry
import fi.riista.mobile.sync.SyncConfig
import fi.riista.mobile.ui.CanIndicateBusy
import fi.riista.mobile.ui.DateTimePickerFragment
import fi.riista.mobile.ui.NoChangeAnimationsItemAnimator
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.ChoiceViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.ChoiceViewLauncher
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderType
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderTypeResolver
import fi.riista.mobile.ui.dataFields.viewHolder.DateTimePickerFragmentLauncher
import fi.riista.mobile.ui.dataFields.viewHolder.EditableDateAndTimeViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.EditableTextViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.InstructionsViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.IntFieldViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.LocationOnMapViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.MapOpener
import fi.riista.mobile.ui.dataFields.viewHolder.ReadOnlyTextViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.SelectSpeciesAndImageViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.SpeciesSelectionLauncher
import fi.riista.mobile.ui.dataFields.viewHolder.SpecimensActivityLauncher
import fi.riista.mobile.ui.dataFields.viewHolder.SpecimensViewHolder
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
 * A fragment for either creating or editing [CommonObservation]
 */
abstract class ModifyObservationFragment<
        Controller : ModifyObservationController,
        Manager : ModifyObservationFragment.BaseManager
        >
    : DataFieldPageFragment<CommonObservationField>()
    , DataFieldViewHolderTypeResolver<CommonObservationField>
    , MapOpener
    , SpeciesSelectionLauncher<CommonObservationField>
    , DateTimePickerFragmentLauncher<CommonObservationField>
    , DateTimePickerFragment.Listener
    , ChoiceViewLauncher<CommonObservationField>
    , SpecimensActivityLauncher<CommonObservationField> {

    interface BaseManager: CanIndicateBusy {
        fun cancelObservationOperation()
    }

    @Inject
    lateinit var speciesResolver: SpeciesResolver

    @Inject
    lateinit var appSync: AppSync

    @Inject
    lateinit var syncConfig: SyncConfig

    private lateinit var adapter: DataFieldRecyclerViewAdapter<CommonObservationField>
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
    private lateinit var appSyncPreventer: PreventAppSyncWhileModifyingSynchronizableEntry

    // functionality required from subclass
    protected abstract fun notifyManagerAboutSuccessfulSave(observation: CommonObservation)
    protected abstract fun getManagerFromContext(context: Context): Manager


    private fun onSaveButtonClicked() {
        manager.indicateBusy()

        val scope = MainScope()

        scope.launch {
            val result = controller.saveObservation(updateToBackend = syncConfig.isAutomatic())

            // allow cancellation to take effect i.e don't continue updating UI
            // if saveScope has been cancelled
            yield()

            if (!isResumed) {
                return@launch
            }

            val networkResponse = result.networkSaveResponse
            val databaseResponse = result.databaseSaveResponse

            if (networkResponse is ObservationOperationResponse.NetworkFailure &&
                networkResponse.statusCode == CONFLICT_STATUS_CODE
            ) {
                onSaveFailed(
                    networkStatusCode = networkResponse.statusCode,
                    debugMessage = networkResponse.errorMessage
                )
            } else if (databaseResponse is ObservationOperationResponse.Success) {
                notifyManagerAboutSuccessfulSave(observation = databaseResponse.observation)
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

        appSyncPreventer = PreventAppSyncWhileModifyingSynchronizableEntry(appSync, observedFragment = this)
        parentFragmentManager.registerFragmentLifecycleCallbacks(appSyncPreventer, false)

        changeImageHelper.initialize(fragment = this) { image ->
            controller.eventDispatchers.imageEventDispatcher.setEntityImage(image)
        }
    }

    override fun onDetach() {
        super.onDetach()
        parentFragmentManager.unregisterFragmentLifecycleCallbacks(appSyncPreventer)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_modify_observation, container, false)

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
                manager.cancelObservationOperation()
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

    override fun resolveViewHolderType(dataField: DataField<CommonObservationField>): DataFieldViewHolderType {
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
            is BooleanField -> DataFieldViewHolderType.BOOLEAN_AS_CHECKBOX
            is DoubleField -> DataFieldViewHolderType.EDITABLE_DOUBLE
            is StringListField -> DataFieldViewHolderType.SELECTABLE_STRING
            is IntField -> DataFieldViewHolderType.INT
            else -> {
                throw IllegalArgumentException("Unexpected DataField type: ${dataField::class.simpleName}")
            }
        }
    }

    private fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<CommonObservationField>) {
        adapter.apply {
            registerLabelFieldViewHolderFactories(linkActionEventDispatcher = null)
            registerViewHolderFactory(
                SelectSpeciesAndImageViewHolder.Factory(
                    speciesResolver = speciesResolver,
                    speciesSelectionLauncher = this@ModifyObservationFragment,
                    entityImageActionLauncher = changeImageHelper,
                    entryType = SelectSpeciesAndImageViewHolder.EntryType.OBSERVATION,
                ))
            registerViewHolderFactory(
                LocationOnMapViewHolder.Factory(
                    mapOpener = this@ModifyObservationFragment,
                    mapExternalIdProvider = null,
                ))
            registerViewHolderFactory(
                EditableDateAndTimeViewHolder.Factory(
                    pickerLauncher = this@ModifyObservationFragment,
                ))
            registerViewHolderFactory(
                SpecimensViewHolder.Factory(
                    activityLauncher = this@ModifyObservationFragment,
                ))
            registerViewHolderFactory(
                EditableTextViewHolder.Factory(
                    eventDispatcher = controller.eventDispatchers.stringEventDispatcher,
                ))
            registerViewHolderFactory(ReadOnlyTextViewHolder.Factory())
            registerViewHolderFactory(
                ChoiceViewHolder.Factory(
                    eventDispatcher = controller.eventDispatchers.stringWithIdEventDispatcher,
                    choiceViewLauncher = this@ModifyObservationFragment,
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

        loadObservationIfNotLoaded()
    }

    override fun onPause() {
        super.onPause()
        disposeBag.disposeAll()
        saveScope?.cancel()
        saveScope = null
    }

    private fun updateBasedOnViewModel(
        viewModelLoadStatus: ViewModelLoadStatus<ModifyObservationViewModel>,
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
                saveButton.isEnabled = viewModel.observationIsValid
            }
        }
    }

    private fun loadObservationIfNotLoaded() {
        if (controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }

        MainScope().launch {
            controller.loadViewModel()
        }
    }

    override fun displayChoicesInSeparateView(
        fieldId: CommonObservationField,
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
        fieldId: CommonObservationField,
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
        CommonObservationField.fromInt(fieldId)?.let { fieldId ->
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
        val fieldId = CommonObservationField.fromInt(
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
                fieldId = CommonObservationField.LOCATION,
                value = geoLocation
            )
        }
    }

    override fun launchSpeciesSelection(fieldId: CommonObservationField, selectableSpecies: SpeciesField.SelectableSpecies) {
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

        val fieldId = CommonObservationField.fromInt(
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
        fieldId: CommonObservationField,
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
        val fieldId = CommonObservationField.fromInt(
            SpecimensActivity.getFieldIdFromIntent(data)
        )
        val specimenData = SpecimensActivity.getSpecimenDataFromIntent(data)

        if (fieldId != null) {
            controller.eventDispatchers.specimenEventDispatcher
                .dispatchSpecimenDataChanged(fieldId, specimenData)
        }
    }

    companion object {
        private const val PREFIX = "ModifyObservationFragment"
        private const val CONTROLLER_STATE_PREFIX = "${PREFIX}_controller"
        private const val CONFLICT_STATUS_CODE = 409
        private const val DATE_PICKER_REQUEST_CODE = "MOF_date_picker_request_code"

        private val logger by getLogger(ModifyObservationFragment::class)
    }
}
