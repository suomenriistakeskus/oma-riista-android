package fi.riista.mobile.feature.groupHunting.observations

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.groupHunting.GroupHuntingObservationOperationResponse
import fi.riista.common.groupHunting.model.AcceptStatus
import fi.riista.common.groupHunting.ui.GroupObservationField
import fi.riista.common.groupHunting.ui.groupObservation.view.ViewGroupObservationController
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
import fi.riista.mobile.riistaSdkHelpers.determineViewHolderType
import fi.riista.mobile.riistaSdkHelpers.registerLabelFieldViewHolderFactories
import fi.riista.mobile.utils.toVisibility
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ViewGroupObservationFragment
    : DataFieldPageFragment<GroupObservationField>()
    , DataFieldViewHolderTypeResolver<GroupObservationField>
    , MapOpener {

    interface InteractionManager {
        val viewGroupObservationController: ViewGroupObservationController

        fun startApproveGroupObservation()
        fun startEditingGroupObservation()
        fun groupObservationRejected()
    }

    @Inject
    lateinit var speciesResolver: SpeciesResolver

    private lateinit var adapter: DataFieldRecyclerViewAdapter<GroupObservationField>
    private lateinit var interactionManager: InteractionManager
    private lateinit var controller: ViewGroupObservationController
    private lateinit var approveButton: MaterialButton

    private val disposeBag = DisposeBag()
    private var canEditObservation = false
        set(value) {
            val shouldInvalidateMenu = value != canEditObservation
            field = value

            if (shouldInvalidateMenu) {
                activity?.invalidateOptionsMenu()
            }
        }
    private var canRejectObservation = false
        set(value) {
            val shouldInvalidateMenu = value != canRejectObservation
            field = value

            if (shouldInvalidateMenu) {
                activity?.invalidateOptionsMenu()
            }
        }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)

        super.onAttach(context)
        interactionManager = context as InteractionManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_group_observation, container, false)

        val viewTitle = when (getObservationAcceptStatusFromArgs(requireArguments())) {
            AcceptStatus.ACCEPTED -> R.string.observation
            AcceptStatus.PROPOSED -> R.string.group_hunting_proposed_observation
            AcceptStatus.REJECTED -> R.string.group_hunting_rejected_observation
        }
        setViewTitle(viewTitle)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_data_fields)!!
        recyclerView.adapter = createAdapter(viewHolderTypeResolver = this)
            .also { adapter ->
                this.adapter = adapter
                registerViewHolderFactories(adapter)
            }

        controller = interactionManager.viewGroupObservationController

        approveButton = view.findViewById(R.id.btn_start_observation_approve_flow)!!
        approveButton.setOnClickListener {
            interactionManager.startApproveGroupObservation()
        }

        setHasOptionsMenu(true)
        return view
    }

    override fun resolveViewHolderType(dataField: DataField<GroupObservationField>): DataFieldViewHolderType {
        return when (dataField) {
            is LabelField -> dataField.determineViewHolderType()
            is SpeciesCodeField -> DataFieldViewHolderType.SPECIES_NAME_AND_ICON
            is DateAndTimeField -> DataFieldViewHolderType.READONLY_DATE_AND_TIME
            is LocationField -> DataFieldViewHolderType.LOCATION_ON_MAP
            is StringField -> {
                if (dataField.settings.singleLine) {
                    DataFieldViewHolderType.READONLY_TEXT_SINGLE_LINE
                } else {
                    throw IllegalStateException("Non-singleline StringField not supported: ${dataField.id}")
                }
            }
            is InstructionsField,
            is HuntingDayAndTimeField,
            is GenderField,
            is AgeField,
            is BooleanField,
            is DoubleField,
            is StringListField,
            is SelectDurationField,
            is IntField -> {
                throw IllegalStateException("Not supported ${dataField.id}")
            }
        }
    }

    private fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<GroupObservationField>) {
        adapter.apply {
            registerViewHolderFactory(
                LocationOnMapViewHolder.Factory(
                mapOpener = this@ViewGroupObservationFragment)
            )
            registerLabelFieldViewHolderFactories()
            registerViewHolderFactory(SpeciesNameAndIconViewHolder.Factory(speciesResolver))
            registerViewHolderFactory(ReadOnlyDateAndTimeViewHolder.Factory())
            registerViewHolderFactory(ReadOnlySingleLineTextViewHolder.Factory())
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
                    adapter.setDataFields(viewModel.fields)
                    approveButton.visibility = viewModel.canApproveObservation.toVisibility()
                    canRejectObservation = viewModel.canRejectObservation
                    canEditObservation = viewModel.canEditObservation
                }
            }
        }.disposeBy(disposeBag)

        loadObservationIfNotLoaded()
    }

    override fun onPause() {
        super.onPause()

        disposeBag.disposeAll()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit, menu)
        menu.findItem(R.id.item_edit).apply {
            isVisible = canEditObservation
        }
        menu.findItem(R.id.item_delete).apply {
            isVisible = false
        }
        inflater.inflate(R.menu.menu_reject, menu)
        menu.findItem(R.id.item_reject_observation).apply {
            isVisible = canRejectObservation
        }
        menu.findItem(R.id.item_reject_harvest).apply {
            isVisible = false
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_edit -> {
                interactionManager.startEditingGroupObservation()
                true
            }
            R.id.item_reject_observation -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.group_hunting_are_you_sure))
                    .setMessage(getString(R.string.group_hunting_reject_proposed_observation_question))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        MainScope().launch {
                            val response = controller.rejectObservation()
                            if (!isResumed) {
                                return@launch
                            }
                            if (response is GroupHuntingObservationOperationResponse.Success) {
                                interactionManager.groupObservationRejected()
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

    private fun loadObservationIfNotLoaded() {
        if (controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }

        MainScope().launch {
            controller.loadObservation()
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
        private const val KEY_OBSERVATION_ACCEPT_STATUS = "VGOF_observationAcceptStatus"

        fun create(observationAcceptStatus: AcceptStatus): ViewGroupObservationFragment {
            return ViewGroupObservationFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_OBSERVATION_ACCEPT_STATUS, observationAcceptStatus.name)
                }
            }
        }

        private fun getObservationAcceptStatusFromArgs(args: Bundle): AcceptStatus {
            val stringValue = args.getString(KEY_OBSERVATION_ACCEPT_STATUS, AcceptStatus.PROPOSED.name)
            return AcceptStatus.valueOf(stringValue)
        }
    }
}
