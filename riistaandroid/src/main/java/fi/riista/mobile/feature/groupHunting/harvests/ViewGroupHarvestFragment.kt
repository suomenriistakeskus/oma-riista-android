package fi.riista.mobile.feature.groupHunting.harvests

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.domain.groupHunting.GroupHuntingHarvestOperationResponse
import fi.riista.common.domain.groupHunting.model.AcceptStatus
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.domain.groupHunting.ui.groupHarvest.view.ViewGroupHarvestController
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
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
import fi.riista.mobile.R
import fi.riista.mobile.activity.MapViewerActivity
import fi.riista.mobile.database.SpeciesResolver
import fi.riista.mobile.feature.groupHunting.DataFieldPageFragment
import fi.riista.mobile.pages.MapExternalIdProvider
import fi.riista.mobile.riistaSdkHelpers.determineViewHolderType
import fi.riista.mobile.riistaSdkHelpers.registerLabelFieldViewHolderFactories
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.DelegatingAlertDialogListener
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderType
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderTypeResolver
import fi.riista.mobile.ui.dataFields.viewHolder.LocationOnMapViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.MapOpener
import fi.riista.mobile.ui.dataFields.viewHolder.ReadOnlyAgeViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.ReadOnlyBooleanAsRadioToggleViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.ReadOnlyDateAndTimeViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.ReadOnlyGenderViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.ReadOnlySingleLineTextViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.SpeciesNameAndIconViewHolder
import fi.riista.mobile.utils.toVisibility
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * A fragment for viewing proposed [GroupHuntingHarvest]
 */
class ViewGroupHarvestFragment
    : DataFieldPageFragment<CommonHarvestField>()
    , DataFieldViewHolderTypeResolver<CommonHarvestField>
    , MapOpener
    , MapExternalIdProvider
{

    interface Manager {
        val viewGroupHarvestController: ViewGroupHarvestController

        fun startEditGroupHarvest()
        fun startApproveProposedGroupHarvest()
        fun proposedGroupHarvestRejected()
    }

    @Inject
    lateinit var speciesResolver: SpeciesResolver

    private lateinit var adapter: DataFieldRecyclerViewAdapter<CommonHarvestField>
    private lateinit var dialogListener: AlertDialogFragment.Listener
    private lateinit var manager: Manager
    private lateinit var controller: ViewGroupHarvestController
    private lateinit var approveButton: MaterialButton
    private val disposeBag = DisposeBag()

    private var canEditHarvest = false
        set(value) {
            val shouldInvalidateMenu = value != canEditHarvest
            field = value

            if (shouldInvalidateMenu) {
                activity?.invalidateOptionsMenu()
            }
        }
    private var canRejectHarvest = false
        set(value) {
            val shouldInvalidateMenu = value != canRejectHarvest
            field = value

            if (shouldInvalidateMenu) {
                activity?.invalidateOptionsMenu()
            }
        }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)

        super.onAttach(context)
        manager = context as Manager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_group_harvest, container, false)

        val viewTitle = when(getHarvestAcceptStatusFromArgs(requireArguments())) {
            AcceptStatus.ACCEPTED -> R.string.harvest
            AcceptStatus.PROPOSED -> R.string.group_hunting_proposed_harvest
            AcceptStatus.REJECTED -> R.string.group_hunting_rejected_harvest
        }
        setViewTitle(viewTitle)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_data_fields)!!
        recyclerView.adapter = createAdapter(viewHolderTypeResolver = this)
            .also { adapter ->
                this.adapter = adapter
                registerViewHolderFactories(adapter)
            }

        controller = manager.viewGroupHarvestController

        approveButton = view.findViewById(R.id.btn_start_proposed_harvest_approve_flow)!!
        approveButton.setOnClickListener {
                manager.startApproveProposedGroupHarvest()
        }

        setHasOptionsMenu(true)

        dialogListener = DelegatingAlertDialogListener(requireActivity()).apply {
            registerPositiveCallback(AlertDialogId.VIEW_GROUP_HARVEST_FRAGMENT_REJECT_HARVEST_QUESTION) {
                rejectHarvest()
            }
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit, menu)
        menu.findItem(R.id.item_edit).apply {
            isVisible = canEditHarvest
        }
        menu.findItem(R.id.item_delete).apply {
            isVisible = false
        }
        inflater.inflate(R.menu.menu_reject, menu)
        menu.findItem(R.id.item_reject_harvest).apply {
            isVisible = canRejectHarvest
        }
        menu.findItem(R.id.item_reject_observation).apply {
            isVisible = false
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_edit -> {
                manager.startEditGroupHarvest()
                true
            }
            R.id.item_reject_harvest -> {
                AlertDialogFragment.Builder(
                    requireContext(),
                    AlertDialogId.VIEW_GROUP_HARVEST_FRAGMENT_REJECT_HARVEST_QUESTION
                )
                    .setTitle(getString(R.string.group_hunting_are_you_sure))
                    .setMessage(getString(R.string.group_hunting_reject_proposed_harvest_question))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(R.string.yes)
                    .setNegativeButton(R.string.no)
                    .build()
                    .show(requireActivity().supportFragmentManager)
                true
            }
            else -> {
                false
            }
        }
    }

    override fun resolveViewHolderType(dataField: DataField<CommonHarvestField>): DataFieldViewHolderType {
        return when (dataField) {
            is LabelField -> dataField.determineViewHolderType()
            is SpeciesField -> DataFieldViewHolderType.SPECIES_NAME_AND_ICON
            is DateAndTimeField -> DataFieldViewHolderType.READONLY_DATE_AND_TIME
            is LocationField -> DataFieldViewHolderType.LOCATION_ON_MAP
            is GenderField -> DataFieldViewHolderType.GENDER
            is AgeField -> DataFieldViewHolderType.AGE
            is BooleanField -> DataFieldViewHolderType.READONLY_BOOLEAN_AS_RADIO_TOGGLE
            is StringField -> {
                if (dataField.settings.singleLine) {
                    DataFieldViewHolderType.READONLY_TEXT_SINGLE_LINE
                } else {
                    throw IllegalStateException("Non-singleline StringField not supported: ${dataField.id}")
                }
            }
            is SpecimenField,
            is InstructionsField,
            is HuntingDayAndTimeField,
            is DoubleField,
            is StringListField,
            is SelectDurationField,
            is IntField,
            is HarvestField,
            is ObservationField,
            is DateField,
            is TimespanField,
            is AttachmentField,
            is ButtonField,
            is ChipField,
            is CustomUserInterfaceField -> {
                throw IllegalStateException("Not supported ${dataField.id}")
            }
        }
    }

    private fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<CommonHarvestField>) {
        adapter.apply {
            registerLabelFieldViewHolderFactories(linkActionEventDispatcher = null)
            registerViewHolderFactory(
                LocationOnMapViewHolder.Factory(
                    mapOpener = this@ViewGroupHarvestFragment,
                    mapExternalIdProvider = this@ViewGroupHarvestFragment,
                ),
            )
            registerViewHolderFactory(SpeciesNameAndIconViewHolder.Factory(speciesResolver))
            registerViewHolderFactory(ReadOnlyDateAndTimeViewHolder.Factory())
            registerViewHolderFactory(ReadOnlySingleLineTextViewHolder.Factory())
            registerViewHolderFactory(ReadOnlyGenderViewHolder.Factory())
            registerViewHolderFactory(ReadOnlyAgeViewHolder.Factory())
            registerViewHolderFactory(ReadOnlyBooleanAsRadioToggleViewHolder.Factory())
        }
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { viewModelLoadStatus ->
            when (viewModelLoadStatus) {
                ViewModelLoadStatus.NotLoaded,
                ViewModelLoadStatus.Loading,
                ViewModelLoadStatus.LoadFailed -> {
                    // todo: indicating loading state + load failure state
                    adapter.setDataFields(listOf())
                    approveButton.visibility = View.GONE
                }
                is ViewModelLoadStatus.Loaded -> {
                    val viewModel = viewModelLoadStatus.viewModel
                    canEditHarvest = viewModel.canEditHarvest
                    canRejectHarvest = viewModel.canRejectHarvest
                    adapter.setDataFields(viewModel.fields)
                    approveButton.visibility = viewModel.canApproveHarvest.toVisibility()
                }
            }
        }.disposeBy(disposeBag)

        loadHarvestIfNotLoaded()
    }

    override fun onPause() {
        super.onPause()

        disposeBag.disposeAll()
    }

    private fun loadHarvestIfNotLoaded() {
        if (controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }

        MainScope().launch {
            controller.loadHarvest()
        }
    }

    override fun openMap(location: Location) {
        val intent = Intent(context, MapViewerActivity::class.java)
        intent.putExtra(MapViewerActivity.EXTRA_EDIT_MODE, false)
        intent.putExtra(MapViewerActivity.EXTRA_START_LOCATION, location)
        intent.putExtra(MapViewerActivity.EXTRA_NEW, false)
        intent.putExtra(MapViewerActivity.EXTRA_EXTERNAL_ID, getMapExternalId())
        startActivity(intent)
    }

    override fun getMapExternalId(): String? {
        return controller.getLoadedViewModelOrNull()?.huntingGroupArea?.externalId
    }

    private fun rejectHarvest() {
        MainScope().launch {
            val response = controller.rejectHarvest()
            if (!isResumed) {
                return@launch
            }
            if (response is GroupHuntingHarvestOperationResponse.Success) {
                manager.proposedGroupHarvestRejected()
            } else {
                AlertDialogFragment.Builder(requireContext(), AlertDialogId.VIEW_GROUP_HARVEST_FRAGMENT_OPERATION_FAILED)
                    .setMessage(R.string.group_hunting_operation_failed)
                    .setPositiveButton(R.string.ok)
                    .build()
                    .show(requireActivity().supportFragmentManager)
            }
        }
    }

    companion object {
        private const val KEY_HARVEST_ACCEPT_STATUS = "VGHF_harvestAcceptStatus"

        fun create(harvestAcceptStatus: AcceptStatus): ViewGroupHarvestFragment {
            return ViewGroupHarvestFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_HARVEST_ACCEPT_STATUS, harvestAcceptStatus.name)
                }
            }
        }

        fun getHarvestAcceptStatusFromArgs(args: Bundle): AcceptStatus {
            val stringValue = args.getString(KEY_HARVEST_ACCEPT_STATUS, AcceptStatus.PROPOSED.name)
            return AcceptStatus.valueOf(stringValue)
        }
    }
}
