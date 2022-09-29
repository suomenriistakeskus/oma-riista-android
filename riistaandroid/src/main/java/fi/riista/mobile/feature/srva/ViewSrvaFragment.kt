package fi.riista.mobile.feature.srva

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.ui.SrvaEventField
import fi.riista.common.domain.srva.ui.modify.EditableSrvaEvent
import fi.riista.common.domain.srva.ui.view.ViewSrvaEventController
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.*
import fi.riista.mobile.R
import fi.riista.mobile.activity.MapViewerActivity
import fi.riista.mobile.database.SpeciesResolver
import fi.riista.mobile.feature.groupHunting.DataFieldPageFragment
import fi.riista.mobile.feature.specimens.SpecimensActivity
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.riistaSdkHelpers.determineViewHolderType
import fi.riista.mobile.riistaSdkHelpers.registerLabelFieldViewHolderFactories
import fi.riista.mobile.ui.FullScreenEntityImageDialog
import fi.riista.mobile.ui.FullScreenEntityImageDialogLauncher
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.*
import fi.riista.mobile.utils.EditUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * A fragment for viewing [CommonSrvaEvent]
 */
class ViewSrvaFragment
    : DataFieldPageFragment<SrvaEventField>()
    , DataFieldViewHolderTypeResolver<SrvaEventField>
    , MapOpener
    , FullScreenEntityImageDialogLauncher
    , SpecimensActivityLauncher<SrvaEventField> {

    interface Manager {
        fun getSrvaEventForViewing(): CommonSrvaEvent
        fun startEditSrvaEvent(editableSrvaEvent: EditableSrvaEvent)
        fun deleteSrvaEvent()
    }

    @Inject
    lateinit var speciesResolver: SpeciesResolver

    private lateinit var adapter: DataFieldRecyclerViewAdapter<SrvaEventField>

    private lateinit var manager: Manager

    private val controller: ViewSrvaEventController by lazy {
        ViewSrvaEventController(
            metadataProvider = RiistaSDK.metadataProvider,
            stringProvider = ContextStringProviderFactory.createForContext(requireContext())
        )
    }

    private val disposeBag = DisposeBag()

    private var canEditSrvaEvent = false
        set(value) {
            val shouldInvalidateMenu = value != canEditSrvaEvent
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
        val view = inflater.inflate(R.layout.fragment_view_srva_event, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_data_fields)!!
        recyclerView.adapter = createAdapter(viewHolderTypeResolver = this)
            .also { adapter ->
                this.adapter = adapter
                registerViewHolderFactories(adapter)
            }

        setHasOptionsMenu(true)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit, menu)
        menu.findItem(R.id.item_edit).apply {
            isVisible = canEditSrvaEvent
        }
        menu.findItem(R.id.item_delete).apply {
            isVisible = canEditSrvaEvent
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_edit -> {
                startEditSrvaEvent()
                true
            }
            R.id.item_delete -> {
                deleteSrvaEvent()
                true
            }
            else -> false
        }
    }

    override fun resolveViewHolderType(dataField: DataField<SrvaEventField>): DataFieldViewHolderType {
        return when (dataField) {
            is LabelField -> dataField.determineViewHolderType()
            is SpeciesField -> DataFieldViewHolderType.SPECIES_NAME_AND_ICON
            is SpecimenField -> DataFieldViewHolderType.SPECIMEN
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

    private fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<SrvaEventField>) {
        adapter.apply {
            registerLabelFieldViewHolderFactories()
            registerViewHolderFactory(
                LocationOnMapViewHolder.Factory(
                    mapOpener = this@ViewSrvaFragment,
                    mapExternalIdProvider = null,
                ),
            )
            registerViewHolderFactory(SpeciesNameAndIconViewHolder.Factory(
                speciesResolver = speciesResolver,
                fullscreenDialogLauncher = this@ViewSrvaFragment
            ))
            registerViewHolderFactory(ReadOnlyDateAndTimeViewHolder.Factory())
            registerViewHolderFactory(SpecimensViewHolder.Factory(activityLauncher = this@ViewSrvaFragment))
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
                }
                is ViewModelLoadStatus.Loaded -> {
                    val viewModel = viewModelLoadStatus.viewModel
                    adapter.setDataFields(viewModel.fields)
                    canEditSrvaEvent = viewModel.canEdit
                }
            }
        }.disposeBy(disposeBag)

        loadSrvaEvent()
    }

    override fun onPause() {
        super.onPause()

        disposeBag.disposeAll()
    }

    private fun loadSrvaEvent() {
        MainScope().launch {
            controller.srvaEvent = manager.getSrvaEventForViewing()
            controller.loadViewModel(refresh = false)
        }
    }

    private fun startEditSrvaEvent() {
        val editableSrvaEvent = controller.getLoadedViewModelOrNull()?.editableSrvaEvent

        if (editableSrvaEvent != null) {
            manager.startEditSrvaEvent(editableSrvaEvent)
        }
    }

    private fun deleteSrvaEvent() {
        EditUtils.showDeleteDialog(requireContext(), object : EditUtils.OnDeleteListener {
            override fun onDelete() {
                manager.deleteSrvaEvent()
            }
        })
    }

    override fun openMap(location: Location) {
        val intent = Intent(context, MapViewerActivity::class.java)
        intent.putExtra(MapViewerActivity.EXTRA_EDIT_MODE, false)
        intent.putExtra(MapViewerActivity.EXTRA_START_LOCATION, location)
        intent.putExtra(MapViewerActivity.EXTRA_NEW, false)
        startActivity(intent)
    }

    override fun showEntityImageInFullscreen(entityImage: EntityImage) {
        FullScreenEntityImageDialog.newInstance(entityImage)
            .show(parentFragmentManager, FullScreenEntityImageDialog.TAG)
    }

    override fun viewSpecimens(
        fieldId: SrvaEventField,
        mode: SpecimensActivity.Mode,
        specimenData: SpecimenFieldDataContainer,
    ) {
        val launchIntent = SpecimensActivity.getLaunchIntentForMode(
            packageContext = requireContext(),
            mode = mode,
            fieldId = fieldId.toInt(),
            specimenData = specimenData
        )

        activity?.startActivity(launchIntent)
    }

    companion object {
        fun create(): ViewSrvaFragment {
            return ViewSrvaFragment()
        }
    }
}
