package fi.riista.mobile.feature.harvest

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
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.modify.EditableHarvest
import fi.riista.common.domain.harvest.ui.view.ViewHarvestController
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
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
import fi.riista.mobile.database.PermitManager
import fi.riista.mobile.database.SpeciesResolver
import fi.riista.mobile.feature.groupHunting.DataFieldPageFragment
import fi.riista.mobile.feature.specimens.SpecimensActivity
import fi.riista.mobile.riistaSdkHelpers.AppHarvestPermitProvider
import fi.riista.mobile.riistaSdkHelpers.AppLanguageProvider
import fi.riista.mobile.riistaSdkHelpers.AppSpeciesResolver
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.riistaSdkHelpers.determineViewHolderType
import fi.riista.mobile.riistaSdkHelpers.registerLabelFieldViewHolderFactories
import fi.riista.mobile.sync.SyncConfig
import fi.riista.mobile.ui.CanIndicateBusy
import fi.riista.mobile.ui.FullScreenEntityImageDialog
import fi.riista.mobile.ui.FullScreenEntityImageDialogLauncher
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
import fi.riista.mobile.ui.dataFields.viewHolder.SpecimensActivityLauncher
import fi.riista.mobile.ui.dataFields.viewHolder.SpecimensViewHolder
import fi.riista.mobile.utils.EditUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * A fragment for viewing [CommonHarvest]
 */
class ViewHarvestFragment
    : DataFieldPageFragment<CommonHarvestField>()
    , DataFieldViewHolderTypeResolver<CommonHarvestField>
    , MapOpener
    , FullScreenEntityImageDialogLauncher
    , SpecimensActivityLauncher<CommonHarvestField> {

    interface Manager : CanIndicateBusy {
        fun getHarvestForViewing(): CommonHarvest
        fun startEditHarvest(editableHarvest: EditableHarvest)
        fun onHarvestDeleted()
    }

    @Inject
    lateinit var speciesResolver: SpeciesResolver

    @Inject
    lateinit var permitManager: PermitManager

    @Inject
    lateinit var syncConfig: SyncConfig

    private lateinit var adapter: DataFieldRecyclerViewAdapter<CommonHarvestField>

    private lateinit var manager: Manager

    private val controller: ViewHarvestController by lazy {
        val harvestId = requireNotNull(manager.getHarvestForViewing().localId) {
            "Manager is required to provide harvest"
        }

        ViewHarvestController(
            harvestId = harvestId,
            harvestContext = RiistaSDK.harvestContext,
            harvestSeasons = RiistaSDK.harvestSeasons,
            speciesResolver = AppSpeciesResolver(),
            harvestPermitProvider = AppHarvestPermitProvider(permitManager),
            preferences = RiistaSDK.preferences,
            stringProvider = ContextStringProviderFactory.createForContext(requireContext()),
            languageProvider = AppLanguageProvider(requireContext()),
        )
    }

    private val disposeBag = DisposeBag()

    private var canEditHarvest = false
        set(value) {
            val shouldInvalidateMenu = value != canEditHarvest
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
        val view = inflater.inflate(R.layout.fragment_view_harvest, container, false)

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
            isVisible = canEditHarvest
        }
        menu.findItem(R.id.item_delete).apply {
            isVisible = canEditHarvest
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_edit -> {
                startEditHarvest()
                true
            }
            R.id.item_delete -> {
                askDeleteHarvestConfirmation()
                true
            }
            else -> false
        }
    }

    override fun resolveViewHolderType(dataField: DataField<CommonHarvestField>): DataFieldViewHolderType {
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

    private fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<CommonHarvestField>) {
        adapter.apply {
            registerLabelFieldViewHolderFactories(linkActionEventDispatcher = null)
            registerViewHolderFactory(
                LocationOnMapViewHolder.Factory(
                    mapOpener = this@ViewHarvestFragment,
                    mapExternalIdProvider = null,
                ),
            )
            registerViewHolderFactory(SpeciesNameAndIconViewHolder.Factory(
                speciesResolver = speciesResolver,
                fullscreenDialogLauncher = this@ViewHarvestFragment
            ))
            registerViewHolderFactory(ReadOnlyDateAndTimeViewHolder.Factory())
            registerViewHolderFactory(SpecimensViewHolder.Factory(activityLauncher = this@ViewHarvestFragment))
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
                    canEditHarvest = viewModel.canEdit
                }
            }
        }.disposeBy(disposeBag)

        loadHarvest()
    }

    override fun onPause() {
        super.onPause()

        disposeBag.disposeAll()
    }

    private fun loadHarvest() {
        MainScope().launch {
            controller.loadViewModel(refresh = false)
        }
    }

    private fun startEditHarvest() {
        val editableHarvest = controller.getLoadedViewModelOrNull()?.editableHarvest

        if (editableHarvest != null) {
            manager.startEditHarvest(editableHarvest)
        }
    }

    private fun askDeleteHarvestConfirmation() {
        EditUtils.showDeleteDialog(requireContext(), object : EditUtils.OnDeleteListener {
            override fun onDelete() {
                deleteHarvest()
            }
        })
    }

    private fun deleteHarvest() {
        manager.indicateBusy()

        MainScope().launch {
            val deleted = controller.deleteHarvest(updateToBackend = syncConfig.isAutomatic())

            manager.hideBusyIndicators {
                if (deleted) {
                    manager.onHarvestDeleted()
                }
            }
        }
    }

    override fun openMap(location: Location) {
        val intent = Intent(context, MapViewerActivity::class.java)
        intent.putExtra(MapViewerActivity.EXTRA_EDIT_MODE, false)
        intent.putExtra(MapViewerActivity.EXTRA_START_LOCATION, location)
        intent.putExtra(MapViewerActivity.EXTRA_NEW, false)
        intent.putExtra(MapViewerActivity.EXTRA_SHOW_ITEMS, false)
        startActivity(intent)
    }

    override fun showEntityImageInFullscreen(entityImage: EntityImage) {
        FullScreenEntityImageDialog.newInstance(entityImage)
            .show(parentFragmentManager, FullScreenEntityImageDialog.TAG)
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

        activity?.startActivity(launchIntent)
    }

    companion object {
        fun create(): ViewHarvestFragment {
            return ViewHarvestFragment()
        }
    }
}
