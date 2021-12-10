package fi.riista.mobile.feature.groupHunting.harvests

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.groupHunting.GroupHuntingHarvestOperationResponse
import fi.riista.common.groupHunting.model.AcceptStatus
import fi.riista.common.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.groupHunting.ui.GroupHarvestField
import fi.riista.common.groupHunting.ui.groupHarvest.view.ViewGroupHarvestController
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.*
import fi.riista.mobile.R
import fi.riista.mobile.activity.MapViewerActivity
import fi.riista.mobile.database.SpeciesResolver
import fi.riista.mobile.feature.groupHunting.DataFieldPageFragment
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.feature.groupHunting.dataFields.viewHolder.*
import fi.riista.mobile.feature.groupHunting.dataFields.viewHolder.DataFieldViewHolderType
import fi.riista.mobile.riistaSdkHelpers.determineViewHolderType
import fi.riista.mobile.riistaSdkHelpers.registerLabelFieldViewHolderFactories
import fi.riista.mobile.utils.toVisibility
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * A fragment for viewing proposed [GroupHuntingHarvest]
 */
class ViewGroupHarvestFragment
    : DataFieldPageFragment<GroupHarvestField>()
    , DataFieldViewHolderTypeResolver<GroupHarvestField>, MapOpener {

    interface Manager {
        val viewGroupHarvestController: ViewGroupHarvestController

        fun startEditGroupHarvest()
        fun startApproveProposedGroupHarvest()
        fun proposedGroupHarvestRejected()
    }

    @Inject
    lateinit var speciesResolver: SpeciesResolver

    private lateinit var adapter: DataFieldRecyclerViewAdapter<GroupHarvestField>

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
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.group_hunting_are_you_sure))
                    .setMessage(getString(R.string.group_hunting_reject_proposed_harvest_question))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        MainScope().launch {
                            val response = controller.rejectHarvest()
                            if (!isResumed) {
                                return@launch
                            }
                            if (response is GroupHuntingHarvestOperationResponse.Success) {
                                manager.proposedGroupHarvestRejected()
                            } else {
                                AlertDialog.Builder(requireContext())
                                    .setMessage(R.string.group_hunting_operation_failed)
                                    .setPositiveButton(R.string.ok, null)
                                    .create()
                                    .show()
                            }
                        }
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
                true
            }
            else -> {
                false
            }
        }
    }

    override fun resolveViewHolderType(dataField: DataField<GroupHarvestField>): DataFieldViewHolderType {
        return when (dataField) {
            is LabelField -> dataField.determineViewHolderType()
            is SpeciesCodeField -> DataFieldViewHolderType.SPECIES_NAME_AND_ICON
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
            is InstructionsField,
            is HuntingDayAndTimeField,
            is DoubleField,
            is StringListField,
            is SelectDurationField,
            is IntField -> {
                throw IllegalStateException("Not supported ${dataField.id}")
            }
        }
    }

    private fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<GroupHarvestField>) {
        adapter.apply {
            registerLabelFieldViewHolderFactories()
            registerViewHolderFactory(LocationOnMapViewHolder.Factory(
                    mapOpener = this@ViewGroupHarvestFragment)
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
        startActivity(intent)
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
