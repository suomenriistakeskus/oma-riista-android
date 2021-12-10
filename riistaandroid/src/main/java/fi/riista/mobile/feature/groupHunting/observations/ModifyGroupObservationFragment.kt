package fi.riista.mobile.feature.groupHunting.observations

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.activity.result.contract.ActivityResultContracts
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.groupHunting.model.GroupHuntingDayId
import fi.riista.common.groupHunting.model.HuntingGroupTarget
import fi.riista.common.groupHunting.ui.GroupHarvestField
import fi.riista.common.groupHunting.ui.GroupObservationField
import fi.riista.common.groupHunting.ui.groupObservation.modify.ModifyGroupObservationController
import fi.riista.common.model.*
import fi.riista.common.ui.dataField.*
import fi.riista.mobile.activity.MapViewerActivity
import fi.riista.mobile.database.SpeciesResolver
import fi.riista.mobile.feature.groupHunting.DataFieldPageFragment
import fi.riista.mobile.feature.groupHunting.SelectStringWithIdActivity
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.feature.groupHunting.dataFields.viewHolder.*
import fi.riista.mobile.feature.groupHunting.huntingDays.select.SelectGroupHuntingDayActivity
import fi.riista.mobile.riistaSdkHelpers.determineViewHolderType
import fi.riista.mobile.riistaSdkHelpers.registerLabelFieldViewHolderFactories
import fi.riista.mobile.utils.MapUtils
import javax.inject.Inject

abstract class ModifyGroupObservationFragment<
        Controller : ModifyGroupObservationController
    >
    : DataFieldPageFragment<GroupObservationField>()
    , DataFieldViewHolderTypeResolver<GroupObservationField>
    , SelectHuntingDayLauncher<GroupObservationField>
    , ChoiceViewLauncher<GroupObservationField>
    , MapOpener
{
    @Inject
    lateinit var speciesResolver: SpeciesResolver

    private val locationRequestActivityResultLaunch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val location = result.data?.getParcelableExtra<Location>(MapViewerActivity.RESULT_LOCATION)
            val source = result.data?.getStringExtra(MapViewerActivity.RESULT_LOCATION_SOURCE)

            if (location != null && source != null) {
                setLocation(location, source)
            }
        }
    }

    private val selectHuntingDayActivityResultLaunch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            handleHuntingDaySelectionResult(data)
        }
    }

    private val selectStringWithIdActivityResultLaunch = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            handleSelectStringWithIdResult(data)
        }
    }

    protected abstract fun getController(): Controller
    protected abstract fun getHuntingGroupTarget(): HuntingGroupTarget

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun openMap(location: Location) {
        val intent = Intent(context, MapViewerActivity::class.java)
        intent.putExtra(MapViewerActivity.EXTRA_EDIT_MODE, true)
        intent.putExtra(MapViewerActivity.EXTRA_START_LOCATION, location)
        intent.putExtra(MapViewerActivity.EXTRA_NEW, false)
        locationRequestActivityResultLaunch.launch(intent)
    }

    override fun resolveViewHolderType(dataField: DataField<GroupObservationField>): DataFieldViewHolderType {
        return when (dataField) {
            is LabelField -> dataField.determineViewHolderType()
            is StringField -> {
                if (dataField.settings.singleLine) {
                    DataFieldViewHolderType.READONLY_TEXT_SINGLE_LINE
                } else {
                    throw IllegalStateException("Non-singleline StringField not supported: ${dataField.id}")
                }
            }
            is SpeciesCodeField -> DataFieldViewHolderType.SPECIES_NAME_AND_ICON
            is HuntingDayAndTimeField -> DataFieldViewHolderType.SELECT_HUNTING_DAY_AND_TIME
            is LocationField -> DataFieldViewHolderType.LOCATION_ON_MAP
            is StringListField -> DataFieldViewHolderType.SELECTABLE_STRING
            is IntField -> DataFieldViewHolderType.INT
            else -> {
                throw IllegalArgumentException("Unexpected DataField type: ${dataField::class.simpleName}")
            }
        }
    }

    protected fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<GroupObservationField>) {
        adapter.apply {
            registerLabelFieldViewHolderFactories()
            registerViewHolderFactory(SpeciesNameAndIconViewHolder.Factory(speciesResolver))
            registerViewHolderFactory(LocationOnMapViewHolder.Factory(this@ModifyGroupObservationFragment))
            registerViewHolderFactory(
                SelectHuntingDayAndTimeViewHolder.Factory(
                    eventDispatcher = getController().timeEventDispatcher,
                    selectHuntingDayLauncher = this@ModifyGroupObservationFragment
                ))
            registerViewHolderFactory(ReadOnlySingleLineTextViewHolder.Factory())
            registerViewHolderFactory(ChoiceViewHolder.Factory(
                eventDispatcher = getController().stringWithIdEventDispatcher,
                choiceViewLauncher = this@ModifyGroupObservationFragment
            ))
            registerViewHolderFactory(IntFieldViewHolder.Factory(getController().intEventDispatcher))
        }
    }

    override fun launchHuntingDaySelection(
        fieldId: GroupObservationField,
        selectedHuntingDayId: GroupHuntingDayId?,
        preferredHuntingDayDate: LocalDate?,
    ) {
        val intent = SelectGroupHuntingDayActivity.getLaunchIntent(
            packageContext = requireContext(),
            huntingGroupTarget = getHuntingGroupTarget(),
            fieldId = fieldId.toInt(),
            selectedHuntingDayId = selectedHuntingDayId,
            preferredHuntingDayDate = preferredHuntingDayDate
        )
        selectHuntingDayActivityResultLaunch.launch(intent)
    }

    private fun handleHuntingDaySelectionResult(data: Intent) {
        val fieldId = GroupObservationField.fromInt(
            SelectGroupHuntingDayActivity.getFieldIdFromIntent(data)
        )
        val huntingDayId = SelectGroupHuntingDayActivity.getHuntingDayIdFromIntent(data)

        if (fieldId != null && huntingDayId != null) {
            getController().huntingDayEventDispatcher.dispatchHuntingDayChanged(fieldId, huntingDayId)
        }
    }

    override fun displayChoicesInSeparateView(
        fieldId: GroupObservationField,
        choices: List<StringWithId>,
        selectedChoice: StringId?,
        viewConfiguration: StringListField.ExternalViewConfiguration,
    ) {
        val intent = SelectStringWithIdActivity.getLaunchIntent(
            packageContext = requireContext(),
            fieldId = fieldId,
            possibleValues = choices,
            selectedValueId = selectedChoice,
            configuration = viewConfiguration
        )

        selectStringWithIdActivityResultLaunch.launch(intent)
    }

    private fun handleSelectStringWithIdResult(data: Intent) {
        val fieldId = GroupObservationField.fromInt(
            SelectStringWithIdActivity.getFieldIdFromIntent(data)
        )
        val selectedValue = SelectStringWithIdActivity.getStringWithIdResultFromIntent(data)

        if (fieldId != null && selectedValue != null) {
            getController().stringWithIdEventDispatcher.dispatchStringWithIdChanged(fieldId, selectedValue)
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
            getController().locationEventDispatcher.dispatchLocationChanged(GroupObservationField.LOCATION, geoLocation)
        }
    }
}
