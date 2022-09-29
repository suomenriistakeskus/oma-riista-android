package fi.riista.mobile.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.model.StringId
import fi.riista.common.model.StringWithId
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.common.ui.controller.selectString.SelectStringWithIdController
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.StringListField
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import fi.riista.mobile.R
import fi.riista.mobile.ui.dataFields.viewHolder.SelectStringWithIdAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.SelectStringWithIdViewHolder
import fi.riista.mobile.utils.toVisibility
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SelectStringWithIdActivity : BaseActivity(), SelectStringWithIdViewHolder.SelectionListener {

    private lateinit var adapter: SelectStringWithIdAdapter
    private lateinit var controller: SelectStringWithIdController

    private lateinit var searchLabel: TextView
    private lateinit var selectButton: Button
    private lateinit var filterText: EditText
    private lateinit var separator: View
    private val disposeBag = DisposeBag()

    private var shouldRefreshEntries = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_string_with_id)

        val viewConfiguration = getConfigurationFromIntent(intent)

        setCustomTitle(viewConfiguration.title)

        val possibleValues = getPossibleValuesFromIntent(intent)
        val initialSelection = getSelectedStringIdsFromIntent(intent)
        val mode = getModeFromIntent(intent)
        controller = SelectStringWithIdController(
            mode = mode,
            possibleValues = possibleValues,
            initiallySelectedValues = initialSelection,
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

        separator = findViewById(R.id.separator)
        separator.visibility = viewConfiguration.filterEnabled.toVisibility()
        searchLabel = findViewById(R.id.tv_search_label)
        searchLabel.visibility = viewConfiguration.filterEnabled.toVisibility()

        filterText = findViewById(R.id.et_filter)
        filterText.visibility = viewConfiguration.filterEnabled.toVisibility()
        if (viewConfiguration.filterEnabled) {
            filterText.hint = viewConfiguration.filterTextHint
            searchLabel.text = viewConfiguration.filterLabelText

            filterText.addTextChangedListener(object : TextWatcher {
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
                controller.selectedValues.let { selectedValues ->
                    setResult(
                        Activity.RESULT_OK,
                        createResultData(
                            fieldId = getFieldIdFromIntent(intent),
                            selected = selectedValues,
                        )
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
                    selectButton.isEnabled = viewModel.selectedValues != null
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
        private const val KEY_MODE = "${EXTRAS_PREFIX}_mode"
        private const val KEY_VALUES = "${EXTRAS_PREFIX}_values"
        private const val KEY_SELECTED_IDS = "${EXTRAS_PREFIX}_selected_id"
        private const val KEY_RESULT = "${EXTRAS_PREFIX}_result"
        private const val KEY_CONFIGURATION = "${EXTRAS_PREFIX}_configuration"

        fun getLaunchIntent(
            packageContext: Context,
            fieldId: DataFieldId,
            mode: StringListField.Mode,
            possibleValues: List<StringWithId>,
            selectedValueIds: List<StringId>?,
            configuration: StringListField.ExternalViewConfiguration,
        ): Intent {
            return Intent(packageContext, SelectStringWithIdActivity::class.java).apply {
                putExtras(
                    Bundle().also { bundle ->
                        bundle.putInt(KEY_FIELD_ID, fieldId.toInt())
                        bundle.putString(KEY_MODE, mode.name)
                        bundle.putString(KEY_VALUES, possibleValues.serializeToJson())
                        selectedValueIds?.let {
                            bundle.putLongArray(KEY_SELECTED_IDS, it.toLongArray())
                        }
                        bundle.putString(KEY_CONFIGURATION, configuration.serializeToJson())
                    }
                )
            }
        }

        private fun createResultData(fieldId: Int, selected: List<StringWithId>): Intent {
            return Intent().apply {
                putExtras(Bundle().also {
                    it.putInt(KEY_FIELD_ID, fieldId)
                    it.putString(KEY_RESULT, selected.serializeToJson())
                })
            }
        }

        fun getStringWithIdResulListFromIntent(intent: Intent): List<StringWithId>? {
            return intent.getStringExtra(KEY_RESULT)
                ?.let { json ->
                    json.deserializeFromJson()
                }
        }

        fun getFieldIdFromIntent(intent: Intent): Int {
            val fieldId = intent.getIntExtra(KEY_FIELD_ID, -1).takeIf { it >= 0 }
            requireNotNull(fieldId) {
                "FieldId is required to exist in the intent"
            }
            return fieldId
        }

        fun getModeFromIntent(intent: Intent): StringListField.Mode {
            val mode = intent.getStringExtra(KEY_MODE)
                ?.let {
                    StringListField.Mode.valueOf(it)
                }

            return requireNotNull(mode) { "Mode must exist in intent!" }
        }

        private fun getPossibleValuesFromIntent(intent: Intent): List<StringWithId> {
            return intent.getStringExtra(KEY_VALUES)
                ?.let { json ->
                    json.deserializeFromJson()
                } ?: listOf()
        }

        private fun getSelectedStringIdsFromIntent(intent: Intent): List<StringId>? {
            return if (intent.hasExtra(KEY_SELECTED_IDS)) {
                val ids = intent.getLongArrayExtra(KEY_SELECTED_IDS)
                return ids?.toList() ?: listOf()
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
