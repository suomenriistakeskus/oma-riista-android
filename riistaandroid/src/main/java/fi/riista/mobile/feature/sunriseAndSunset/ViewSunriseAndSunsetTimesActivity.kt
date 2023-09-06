package fi.riista.mobile.feature.sunriseAndSunset

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationListener
import fi.riista.common.domain.sun.ui.SunriseAndSunsetTimesController
import fi.riista.common.domain.sun.ui.SunriseAndSunsetTimesViewModel
import fi.riista.common.domain.sun.ui.SunriseAndSunsetField
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.LocalDate
import fi.riista.common.model.toBackendEnum
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.common.ui.dataField.AgeField
import fi.riista.common.ui.dataField.AttachmentField
import fi.riista.common.ui.dataField.BooleanField
import fi.riista.common.ui.dataField.ButtonField
import fi.riista.common.ui.dataField.ChipField
import fi.riista.common.ui.dataField.CustomUserInterfaceField
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DateAndTimeField
import fi.riista.common.ui.dataField.DateField
import fi.riista.common.ui.dataField.DoubleField
import fi.riista.common.ui.dataField.GenderField
import fi.riista.common.ui.dataField.HarvestField
import fi.riista.common.ui.dataField.HuntingDayAndTimeField
import fi.riista.common.ui.dataField.InstructionsField
import fi.riista.common.ui.dataField.IntField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.LocationField
import fi.riista.common.ui.dataField.ObservationField
import fi.riista.common.ui.dataField.SelectDurationField
import fi.riista.common.ui.dataField.SpeciesField
import fi.riista.common.ui.dataField.SpecimenField
import fi.riista.common.ui.dataField.StringField
import fi.riista.common.ui.dataField.StringListField
import fi.riista.common.ui.dataField.TimespanField
import fi.riista.common.util.toETRMSGeoLocation
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.activity.MapViewerActivity
import fi.riista.mobile.feature.huntingControl.toLocalDateTime
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.riistaSdkHelpers.determineViewHolderType
import fi.riista.mobile.riistaSdkHelpers.fromJodaLocalDate
import fi.riista.mobile.riistaSdkHelpers.registerLabelFieldViewHolderFactories
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime
import fi.riista.mobile.ui.DateTimePickerFragment
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderType
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderTypeResolver
import fi.riista.mobile.ui.dataFields.viewHolder.DatePickerFragmentLauncher
import fi.riista.mobile.ui.dataFields.viewHolder.DateViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.LocationOnMapViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.MapOpener
import fi.riista.mobile.ui.dataFields.viewHolder.ReadOnlySingleLineTextViewHolder
import fi.riista.mobile.ui.registerDatePickerFragmentResultListener
import fi.riista.mobile.utils.MapUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class ViewSunriseAndSunsetTimesActivity
    : BaseActivity()
    , DataFieldViewHolderTypeResolver<SunriseAndSunsetField>
    , DatePickerFragmentLauncher<SunriseAndSunsetField>
    , DateTimePickerFragment.Listener
    , MapOpener, LocationListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DataFieldRecyclerViewAdapter<SunriseAndSunsetField>

    private lateinit var controller: SunriseAndSunsetTimesController
    private val disposeBag = DisposeBag()

    private val locationRequestActivityResultLaunch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val location = result.data?.getParcelableExtra<Location>(MapViewerActivity.RESULT_LOCATION)
            val source = result.data?.getStringExtra(MapViewerActivity.RESULT_LOCATION_SOURCE)

            if (location != null && source != null) {
                setLocation(location, source)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sunrise_and_sunset)
        setCustomTitle(getString(R.string.sunrise_and_sunset_title))

        controller = SunriseAndSunsetTimesController(
            stringProvider = ContextStringProviderFactory.createForContext(this),
        )

        if (savedInstanceState != null) {
            controller.restoreFromBundle(savedInstanceState, PREFIX_CONTROLLER_STATE)
        }

        registerDatePickerFragmentResultListener(DATE_PICKER_REQUEST_CODE)

        recyclerView = findViewById(R.id.rv_data_fields)
        recyclerView.adapter = DataFieldRecyclerViewAdapter(viewHolderTypeResolver = this).apply {
            registerViewHolderFactory(LocationOnMapViewHolder.Factory(this@ViewSunriseAndSunsetTimesActivity))
            registerViewHolderFactory(DateViewHolder.Factory(this@ViewSunriseAndSunsetTimesActivity))
            registerViewHolderFactory(ReadOnlySingleLineTextViewHolder.Factory())
            registerLabelFieldViewHolderFactories(linkActionEventDispatcher = null)
        }.also {
            adapter = it
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        controller.saveToBundle(outState, PREFIX_CONTROLLER_STATE)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { loadStatus ->
            when (loadStatus) {
                ViewModelLoadStatus.NotLoaded,
                ViewModelLoadStatus.Loading,
                ViewModelLoadStatus.LoadFailed -> {
                    // nop
                }
                is ViewModelLoadStatus.Loaded -> onViewModelLoaded(loadStatus.viewModel)
            }
        }.disposeBy(disposeBag)

        loadViewModelIfNotLoaded()

        if (controller.locationCanBeUpdatedAutomatically) {
            locationClient.addListener(this)
        }
    }

    override fun onPause() {
        super.onPause()
        disposeBag.disposeAll()
        locationClient.removeListener(this)
    }

    private fun onViewModelLoaded(viewModel: SunriseAndSunsetTimesViewModel) {
        adapter.setDataFields(viewModel.fields)
    }

    private fun loadViewModelIfNotLoaded() {
        if (controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }
        loadViewModel()
    }

    private fun loadViewModel() {
        MainScope().launch {
            controller.loadViewModel(refresh = false)
        }
    }

    override fun resolveViewHolderType(dataField: DataField<SunriseAndSunsetField>): DataFieldViewHolderType {
        return when (dataField) {
            is StringField -> DataFieldViewHolderType.READONLY_TEXT_SINGLE_LINE
            is LocationField -> DataFieldViewHolderType.LOCATION_ON_MAP
            is DateField -> DataFieldViewHolderType.DATE
            is LabelField -> dataField.determineViewHolderType()
            is StringListField,
            is SpecimenField,
            is InstructionsField,
            is IntField,
            is DoubleField,
            is BooleanField,
            is SpeciesField,
            is DateAndTimeField,
            is GenderField,
            is AgeField,
            is SelectDurationField,
            is HuntingDayAndTimeField,
            is HarvestField,
            is ObservationField,
            is TimespanField,
            is AttachmentField,
            is ButtonField,
            is ChipField,
            is CustomUserInterfaceField -> {
                throw RuntimeException("Unexpected type!")
            }
        }
    }

    override fun openMap(location: Location) {
        val intent = Intent(this, MapViewerActivity::class.java)
        intent.putExtra(MapViewerActivity.EXTRA_EDIT_MODE, true)
        intent.putExtra(MapViewerActivity.EXTRA_START_LOCATION, location)
        intent.putExtra(MapViewerActivity.EXTRA_NEW, false)
        intent.putExtra(MapViewerActivity.EXTRA_SHOW_ITEMS, false)
        locationRequestActivityResultLaunch.launch(intent)
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
                fieldId = SunriseAndSunsetField.LOCATION,
                value = geoLocation
            )
        }
    }

    override fun onLocationChanged(location: Location) {
        val locationChanged = controller.trySelectCurrentUserLocation(
            location = location.toETRMSGeoLocation(GeoLocationSource.GPS_DEVICE)
        )

        if (!locationChanged) {
            locationClient.removeListener(this)
        }
    }

    override fun pickDate(
        fieldId: SunriseAndSunsetField,
        currentDate: LocalDate,
        minDate: LocalDate?,
        maxDate: LocalDate?
    ) {
        val datePickerFragment = DateTimePickerFragment.create(
            requestCode = DATE_PICKER_REQUEST_CODE,
            fieldId = fieldId.toInt(),
            pickMode = DateTimePickerFragment.PickMode.DATE,
            selectedDateTime = currentDate.toLocalDateTime(12, 0, 0).toJodaDateTime(),
            minDateTime = minDate?.toLocalDateTime(0, 0, 0)?.toJodaDateTime(),
            maxDateTime = maxDate?.toLocalDateTime(23, 59, 59)?.toJodaDateTime(),
        )

        datePickerFragment.show(supportFragmentManager, "datepicker")
    }

    override fun onDateTimeSelected(fieldId: Int, dateTime: DateTime) {
        val localDate = LocalDate.fromJodaLocalDate(dateTime.toLocalDate())
        SunriseAndSunsetField.fromInt(fieldId)?.let { field ->
            controller.eventDispatchers.localDateEventDispatcher.dispatchLocalDateChanged(field, localDate)
        }
    }

    companion object {
        private const val PREFIX = "VSASTA"
        private const val PREFIX_CONTROLLER_STATE = "${PREFIX}_controller"
        private const val DATE_PICKER_REQUEST_CODE = "${PREFIX}_date_picker_request_code"
    }
}
