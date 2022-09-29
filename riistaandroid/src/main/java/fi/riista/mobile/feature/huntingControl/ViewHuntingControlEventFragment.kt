package fi.riista.mobile.feature.huntingControl

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.domain.huntingControl.model.HuntingControlAttachment
import fi.riista.common.domain.huntingControl.model.HuntingControlEventTarget
import fi.riista.common.domain.huntingControl.ui.HuntingControlEventField
import fi.riista.common.domain.huntingControl.ui.view.ViewHuntingControlEventController
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.*
import fi.riista.mobile.AppConfig
import fi.riista.mobile.R
import fi.riista.mobile.activity.MapViewerActivity
import fi.riista.mobile.feature.groupHunting.DataFieldPageFragment
import fi.riista.mobile.network.AppDownloadManager
import fi.riista.mobile.riistaSdkHelpers.determineViewHolderType
import fi.riista.mobile.riistaSdkHelpers.registerLabelFieldViewHolderFactories
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject

class ViewHuntingControlEventFragment
    : DataFieldPageFragment<HuntingControlEventField>()
    , DataFieldViewHolderTypeResolver<HuntingControlEventField>
    , MapOpener {

    interface InteractionManager {
        val viewHuntingControlEventController: ViewHuntingControlEventController
        val huntingControlEventTarget: HuntingControlEventTarget

        fun startEditingHuntingControlEvent()
        fun showAttachment(attachment: HuntingControlAttachment)
    }

    @Inject
    lateinit var appDownloadManager: AppDownloadManager

    private lateinit var adapter: DataFieldRecyclerViewAdapter<HuntingControlEventField>
    private lateinit var interactionManager: InteractionManager
    private lateinit var controller: ViewHuntingControlEventController

    private val disposeBag = DisposeBag()
    private var canEditHuntingControlEvent = false
        set(value) {
            val shouldInvalidateMenu = value != canEditHuntingControlEvent
            field = value

            if (shouldInvalidateMenu) {
                activity?.invalidateOptionsMenu()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_hunting_control_event, container, false)
        setViewTitle(getString(R.string.hunting_control_view_page_title))

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_data_fields)!!
        recyclerView.adapter = createAdapter(viewHolderTypeResolver = this)
            .also { adapter ->
                this.adapter = adapter
                registerViewHolderFactories(adapter)
            }

        controller = interactionManager.viewHuntingControlEventController

        setHasOptionsMenu(true)

        return view
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        interactionManager = context as InteractionManager
    }

    private fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<HuntingControlEventField>) {
        adapter.apply {
            registerViewHolderFactory(
                LocationOnMapViewHolder.Factory(
                    mapOpener = this@ViewHuntingControlEventFragment,
                ),
            )
            registerViewHolderFactory(DateViewHolder.Factory(null))
            registerViewHolderFactory(TimespanViewHolder.Factory(null))
            registerViewHolderFactory(ReadOnlySingleLineTextViewHolder.Factory())
            registerViewHolderFactory(ReadOnlyTextViewHolder.Factory())
            registerViewHolderFactory(AttachmentViewHolder.Factory(::attachmentClicked))
            registerViewHolderFactory(ChipsViewHolder.Factory())
            registerLabelFieldViewHolderFactories()
        }
    }

    private fun attachmentClicked(field: HuntingControlEventField, fileName: String) {
        val attachment = controller.getAttachment(field)
        if (attachment != null) {
            if (attachment.uuid != null) {
                interactionManager.showAttachment(attachment)
            } else {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.hunting_control_download_attachment_question)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        val remoteId = attachment.remoteId
                        if (remoteId != null) {
                            val url = URL(AppConfig.getBaseUrl() + "/huntingcontrol/attachment/$remoteId/download")
                            appDownloadManager.startDownload(url, fileName)
                        }
                    }
                    .setNegativeButton(R.string.no) { _, _ ->
                        // No op
                    }
                    .create()
                    .show()
            }
        }
    }

    override fun resolveViewHolderType(dataField: DataField<HuntingControlEventField>): DataFieldViewHolderType {
        return when (dataField) {
            is LocationField -> DataFieldViewHolderType.LOCATION_ON_MAP
            is DateField -> DataFieldViewHolderType.DATE
            is TimespanField -> DataFieldViewHolderType.TIMESPAN
            is StringField -> {
                if (dataField.settings.singleLine) {
                    DataFieldViewHolderType.READONLY_TEXT_SINGLE_LINE
                } else {
                    DataFieldViewHolderType.READONLY_TEXT
                }
            }
            is LabelField -> dataField.determineViewHolderType()
            is AttachmentField -> DataFieldViewHolderType.ATTACHMENT
            is ChipField -> DataFieldViewHolderType.CHIPS
            else -> throw IllegalStateException("Not supported ${dataField.id}")
        }
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { viewModelLoadStatus ->
            when (viewModelLoadStatus) {
                ViewModelLoadStatus.NotLoaded,
                ViewModelLoadStatus.Loading,
                ViewModelLoadStatus.LoadFailed -> {
                    adapter.setDataFields(listOf())
                }
                is ViewModelLoadStatus.Loaded -> {
                    val viewModel = viewModelLoadStatus.viewModel
                    adapter.setDataFields(viewModel.fields)
                    canEditHuntingControlEvent = viewModel.canEditHuntingControlEvent
                }
            }
        }.disposeBy(disposeBag)

        loadHuntingControlEventIfNotLoaded()
    }

    override fun onPause() {
        super.onPause()
        disposeBag.disposeAll()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit, menu)
        menu.findItem(R.id.item_edit).apply {
            isVisible = canEditHuntingControlEvent
        }
        menu.findItem(R.id.item_delete).apply {
            isVisible = false
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_edit -> {
                interactionManager.startEditingHuntingControlEvent()
                true
            }
            else -> {
                false
            }
        }
    }

    private fun loadHuntingControlEventIfNotLoaded() {
        if (controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }

        MainScope().launch {
             controller.loadHuntingControlEvent()
        }
    }

    override fun openMap(location: Location) {
        val intent = Intent(context, MapViewerActivity::class.java)
        intent.putExtra(MapViewerActivity.EXTRA_EDIT_MODE, false)
        intent.putExtra(MapViewerActivity.EXTRA_START_LOCATION, location)
        intent.putExtra(MapViewerActivity.EXTRA_NEW, false)
        startActivity(intent)
    }
}
