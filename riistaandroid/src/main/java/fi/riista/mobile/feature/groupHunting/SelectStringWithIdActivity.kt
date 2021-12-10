package fi.riista.mobile.feature.groupHunting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.extensions.loadStringWithId
import fi.riista.common.extensions.saveToBundle
import fi.riista.common.ui.controller.selectString.SelectStringWithIdController
import fi.riista.common.model.StringId
import fi.riista.common.model.StringWithId
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.StringListField
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.feature.groupHunting.dataFields.viewHolder.SelectStringWithIdAdapter
import fi.riista.mobile.feature.groupHunting.dataFields.viewHolder.SelectStringWithIdViewHolder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SelectStringWithIdActivity : BaseActivity(), SelectStringWithIdViewHolder.SelectionListener {

    private lateinit var adapter: SelectStringWithIdAdapter
    private lateinit var controller: SelectStringWithIdController

    private lateinit var searchLabel: TextView
    private lateinit var selectButton: Button
    private lateinit var filterText: EditText
    private val disposeBag = DisposeBag()

    private var shouldRefreshEntries = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_string_with_id)

        val viewConfiguration = getConfigurationFromIntent(intent)

        setCustomTitle(viewConfiguration.title)
        searchLabel = findViewById(R.id.tv_search_label)
        searchLabel.text = viewConfiguration.filterLabelText


        val possibleValues = getPossibleValuesFromIntent(intent)
        val initialSelection = getSelectedStringIdFromIntent(intent)
        controller = SelectStringWithIdController(
            possibleValues = possibleValues,
            initiallySelectedValue = initialSelection,
        )
        savedInstanceState?.let {
            controller.restoreFromBundle(it, CONTROLLER_STATE_PREFIX)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rv_values)!!
        recyclerView.adapter = SelectStringWithIdAdapter(
            layoutInflater = layoutInflater,
            selectionListener = this,
        ).also { adapter ->
            this.adapter = adapter
        }

        filterText = findViewById<EditText>(R.id.et_filter).also { editText ->
            editText.hint = viewConfiguration.filterTextHint

            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    controller.eventDispatcher.dispatchFilterChanged(s.toString())
                }
            })
        }
        findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            finish()
        }
        selectButton = findViewById<Button>(R.id.btn_select).also { button ->
            button.setOnClickListener {
                controller.selectedValue?.let { person ->
                    setResult(
                        Activity.RESULT_OK,
                        createResultData(fieldId = getFieldIdFromIntent(intent), selected = person)
                    )
                    finish()
                }
            }
            button.isEnabled = false
        }
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { viewModelLoadStatus ->
            when (viewModelLoadStatus) {
                ViewModelLoadStatus.NotLoaded,
                ViewModelLoadStatus.Loading,
                ViewModelLoadStatus.LoadFailed -> {
                    adapter.values = listOf()
                }
                is ViewModelLoadStatus.Loaded -> {
                    val viewModel = viewModelLoadStatus.viewModel
                    adapter.values = viewModel.filteredValues
                    selectButton.isEnabled = viewModel.selectedValue != null
                }
            }
        }.disposeBy(disposeBag)

        loadEntries(refresh = shouldRefreshEntries)
    }

    override fun onPause() {
        super.onPause()

        disposeBag.disposeAll()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller.saveToBundle(outState, CONTROLLER_STATE_PREFIX)
    }

    private fun loadEntries(refresh: Boolean) {
        MainScope().launch {
            controller.loadViewModel(refresh = refresh)
            shouldRefreshEntries = false
        }
    }

    override fun onValueClicked(value: StringWithId) {
        controller.eventDispatcher.dispatchSelectedValueChanged(value)
    }

    companion object {
        private const val EXTRAS_PREFIX = "SSWIA_args"
        private const val CONTROLLER_STATE_PREFIX = "SSWIA_controller"
        private const val KEY_FIELD_ID = "${EXTRAS_PREFIX}_field_id"
        private const val KEY_VALUES = "${EXTRAS_PREFIX}_values"
        private const val KEY_SELECTED_ID = "${EXTRAS_PREFIX}_selected_id"
        private const val KEY_CONFIGURATION = "${EXTRAS_PREFIX}_configuration"

        fun getLaunchIntent(
            packageContext: Context,
            fieldId: DataFieldId,
            possibleValues: List<StringWithId>,
            selectedValueId: StringId?,
            configuration: StringListField.ExternalViewConfiguration
        ): Intent {
            return Intent(packageContext, SelectStringWithIdActivity::class.java).apply {
                putExtras(
                    Bundle().also { bundle ->
                        bundle.putInt(KEY_FIELD_ID, fieldId.toInt())
                        bundle.putString(KEY_VALUES, possibleValues.serializeToJson())
                        selectedValueId?.let {
                            bundle.putLong(KEY_SELECTED_ID, it)
                        }
                        bundle.putString(KEY_CONFIGURATION, configuration.serializeToJson())
                    }
                )
            }
        }

        private fun createResultData(fieldId: Int, selected: StringWithId): Intent {
            return Intent().apply {
                putExtras(Bundle().also {
                    it.putInt(KEY_FIELD_ID, fieldId)
                    selected.saveToBundle(it, EXTRAS_PREFIX)
                })
            }
        }

        fun getStringWithIdResultFromIntent(intent: Intent): StringWithId? {
            return intent.extras?.loadStringWithId(EXTRAS_PREFIX)
        }

        fun getFieldIdFromIntent(intent: Intent): Int {
            val fieldId = intent.getIntExtra(KEY_FIELD_ID, -1).takeIf { it >= 0 }
            requireNotNull(fieldId) {
                "FieldId is required to exist in the intent"
            }
            return fieldId
        }

        private fun getPossibleValuesFromIntent(intent: Intent): List<StringWithId> {
            return intent.getStringExtra(KEY_VALUES)
                ?.let { json ->
                    json.deserializeFromJson()
                } ?: listOf()
        }

        private fun getSelectedStringIdFromIntent(intent: Intent): StringId? {
            return if (intent.hasExtra(KEY_SELECTED_ID)) {
                intent.getLongExtra(KEY_SELECTED_ID, -1)
            } else {
                null
            }
        }

        private fun getConfigurationFromIntent(intent: Intent): StringListField.ExternalViewConfiguration {
            return requireNotNull(
                intent.getStringExtra(KEY_CONFIGURATION)
                    ?.let { json ->
                        json.deserializeFromJson()
                    }
            ) {
                "View configuration is required!"
            }
        }
    }
}
