package fi.riista.mobile.feature.specimens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.extensions.deserializeJson
import fi.riista.common.extensions.serializeToBundleAsJson
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.domain.specimens.ui.SpecimenFieldId
import fi.riista.common.domain.specimens.ui.view.ViewSpecimensController
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.StringField
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.DataFieldPageFragment
import fi.riista.mobile.riistaSdkHelpers.AppSpeciesResolver
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.riistaSdkHelpers.determineViewHolderType
import fi.riista.mobile.riistaSdkHelpers.registerLabelFieldViewHolderFactories
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderType
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderTypeResolver
import fi.riista.mobile.ui.dataFields.viewHolder.ReadOnlySingleLineTextViewHolder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/**
 * A fragment for viewing [SpecimenFieldDataContainer]
 */
class ViewSpecimensFragment
    : DataFieldPageFragment<SpecimenFieldId>()
    , DataFieldViewHolderTypeResolver<SpecimenFieldId> {

    private lateinit var adapter: DataFieldRecyclerViewAdapter<SpecimenFieldId>
    private lateinit var controller: ViewSpecimensController
    private lateinit var specimenData: SpecimenFieldDataContainer

    private val disposeBag = DisposeBag()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_specimen_list, container, false)

        setViewTitle(R.string.specimen_details)

        controller = ViewSpecimensController(
            speciesResolver = AppSpeciesResolver(),
            stringProvider = ContextStringProviderFactory.createForContext(requireContext())
        )
        specimenData = getSpecimenDataFromArguments(requireArguments())

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_data_fields)!!
        recyclerView.adapter = createAdapter(viewHolderTypeResolver = this)
            .also { adapter ->
                this.adapter = adapter
                registerViewHolderFactories(adapter)
            }

        return view
    }

    override fun resolveViewHolderType(dataField: DataField<SpecimenFieldId>): DataFieldViewHolderType {
        return when (dataField) {
            is LabelField -> dataField.determineViewHolderType()
            is StringField -> {
                if (dataField.settings.singleLine) {
                    DataFieldViewHolderType.READONLY_TEXT_SINGLE_LINE
                } else {
                    throw IllegalStateException("Non-singleline StringField not supported: ${dataField.id}")
                }
            }
            else -> {
                throw IllegalStateException("Not supported ${dataField.id}")
            }
        }
    }

    private fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<SpecimenFieldId>) {
        adapter.apply {
            registerLabelFieldViewHolderFactories(linkActionEventDispatcher = null)
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
                }
                is ViewModelLoadStatus.Loaded -> {
                    val viewModel = viewModelLoadStatus.viewModel
                    adapter.setDataFields(viewModel.fields)
                }
            }
        }.disposeBy(disposeBag)

        loadSpecimensIfNotLoaded()
    }

    override fun onPause() {
        super.onPause()

        disposeBag.disposeAll()
    }

    private fun loadSpecimensIfNotLoaded() {
        if (controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }

        MainScope().launch {
            controller.loadSpecimenData(specimenData)
        }
    }

    companion object {
        private const val EXTRAS_PREFIX = "ViewSpecimensFragment"
        private const val KEY_SPECIMEN_DATA = "${EXTRAS_PREFIX}_specimenData"

        fun create(specimenData: SpecimenFieldDataContainer): ViewSpecimensFragment {
            return ViewSpecimensFragment().apply {
                arguments = Bundle().also { bundle ->
                    specimenData.serializeToBundleAsJson(bundle, key = KEY_SPECIMEN_DATA)
                }
            }
        }

        private fun getSpecimenDataFromArguments(args: Bundle): SpecimenFieldDataContainer {
            return requireNotNull(args.deserializeJson(key = KEY_SPECIMEN_DATA)) {
                "Specimen data required to be deserializable"
            }
        }
    }
}
