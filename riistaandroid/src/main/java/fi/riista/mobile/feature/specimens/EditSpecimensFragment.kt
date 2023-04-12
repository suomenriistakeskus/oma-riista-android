package fi.riista.mobile.feature.specimens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.domain.specimens.ui.SpecimenFieldId
import fi.riista.common.domain.specimens.ui.edit.EditSpecimensController
import fi.riista.common.extensions.deserializeJson
import fi.riista.common.extensions.serializeToBundleAsJson
import fi.riista.common.model.StringId
import fi.riista.common.model.StringWithId
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.AgeField
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DoubleField
import fi.riista.common.ui.dataField.GenderField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.StringListField
import fi.riista.mobile.R
import fi.riista.mobile.activity.SelectStringWithIdActivity
import fi.riista.mobile.feature.groupHunting.DataFieldPageFragment
import fi.riista.mobile.ui.NoChangeAnimationsItemAnimator
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.ChoiceViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.ChoiceViewLauncher
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderType
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderTypeResolver
import fi.riista.mobile.ui.dataFields.viewHolder.EditableAgeViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.EditableDoubleViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.EditableGenderViewHolder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/**
 * A fragment for editing [SpecimenFieldDataContainer]
 */
class EditSpecimensFragment
    : DataFieldPageFragment<SpecimenFieldId>()
    , DataFieldViewHolderTypeResolver<SpecimenFieldId>
    , SpecimenHeaderViewHolderListener<SpecimenFieldId>
    , ChoiceViewLauncher<SpecimenFieldId> {

    interface Manager {
        val editSpecimensController: EditSpecimensController
    }

    private lateinit var manager: Manager
    private lateinit var adapter: DataFieldRecyclerViewAdapter<SpecimenFieldId>
    private lateinit var specimenData: SpecimenFieldDataContainer

    private val selectStringWithIdActivityResultLaunch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            handleSelectStringWithIdResult(data)
        }
    }

    private val controller: EditSpecimensController
        get() = manager.editSpecimensController

    private val disposeBag = DisposeBag()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        manager = requireNotNull(context as? Manager) {
            "Context required to implement Manager interface!"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_specimen_list, container, false)

        setViewTitle(R.string.specimen_details)

        specimenData = getSpecimenDataFromArguments(requireArguments())

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_data_fields)!!
        recyclerView.adapter = createAdapter(viewHolderTypeResolver = this)
            .also { adapter ->
                this.adapter = adapter
                registerViewHolderFactories(adapter)
            }
        recyclerView.itemAnimator = NoChangeAnimationsItemAnimator(
            // animate headers as otherwise removing specimens doesn't "move" remaining headers correctly
            animatedViewHolderClasses = listOf(SpecimenHeaderViewHolder::class)
        )

        setHasOptionsMenu(true)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_add -> {
                controller.addSpecimen()
                true
            }
            else -> false
        }
    }

    override fun resolveViewHolderType(dataField: DataField<SpecimenFieldId>): DataFieldViewHolderType {
        return when (dataField) {
            is LabelField -> {
                when (dataField.type) {
                    LabelField.Type.CAPTION -> DataFieldViewHolderType.LABEL_CAPTION
                    else -> throw IllegalStateException("Not supported $dataField ")
                }
            }
            is GenderField -> DataFieldViewHolderType.EDITABLE_GENDER
            is AgeField -> DataFieldViewHolderType.EDITABLE_AGE
            is DoubleField -> DataFieldViewHolderType.EDITABLE_DOUBLE
            is StringListField -> DataFieldViewHolderType.SELECTABLE_STRING
            else -> {
                throw IllegalStateException("Not supported ${dataField.id}")
            }
        }
    }

    private fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<SpecimenFieldId>) {
        adapter.apply {
            registerViewHolderFactory(SpecimenHeaderViewHolder.Factory(
                listener = this@EditSpecimensFragment
            ))
            registerViewHolderFactory(EditableGenderViewHolder.Factory(
                eventDispatcher = controller.eventDispatchers.genderEventDispatcher
            ))
            registerViewHolderFactory(EditableAgeViewHolder.Factory(
                eventDispatcher = controller.eventDispatchers.ageEventDispatcher
            ))
            registerViewHolderFactory(EditableDoubleViewHolder.Factory(
                eventDispatcher = controller.eventDispatchers.doubleEventDispatcher
            ))
            registerViewHolderFactory(
                ChoiceViewHolder.Factory(
                    eventDispatcher = controller.eventDispatchers.stringWithIdDispatcher,
                    choiceViewLauncher = this@EditSpecimensFragment
                )
            )
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

        fun create(specimenData: SpecimenFieldDataContainer): EditSpecimensFragment {
            return EditSpecimensFragment().apply {
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

    override fun onRemoveSpecimenClicked(fieldId: SpecimenFieldId) {
        controller.removeSpecimen(fieldId)
    }

    override fun displayChoicesInSeparateView(
        fieldId: SpecimenFieldId,
        mode: StringListField.Mode,
        choices: List<StringWithId>,
        selectedChoices: List<StringId>?,
        viewConfiguration: StringListField.ExternalViewConfiguration
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

    private fun handleSelectStringWithIdResult(data: Intent) {
        val fieldId = SpecimenFieldId.fromInt(
            SelectStringWithIdActivity.getFieldIdFromIntent(data)
        )
        val selectedValue = SelectStringWithIdActivity.getStringWithIdResulListFromIntent(data)

        if (fieldId != null && !selectedValue.isNullOrEmpty()) {
            controller.eventDispatchers.stringWithIdDispatcher
                .dispatchStringWithIdChanged(fieldId, selectedValue)
        }
    }
}
