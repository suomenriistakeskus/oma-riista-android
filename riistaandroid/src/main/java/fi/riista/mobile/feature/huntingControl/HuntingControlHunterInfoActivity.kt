package fi.riista.mobile.feature.huntingControl

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.integration.android.IntentIntegrator
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.huntingControl.ui.hunterInfo.HunterInfoController
import fi.riista.common.domain.huntingControl.ui.hunterInfo.HunterInfoField
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.common.ui.dataField.ButtonField
import fi.riista.common.ui.dataField.CustomUserInterfaceField
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.IntField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.StringField
import fi.riista.mobile.R
import fi.riista.mobile.RemoteConfig
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.riistaSdkHelpers.AppLanguageProvider
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.riistaSdkHelpers.determineViewHolderType
import fi.riista.mobile.riistaSdkHelpers.registerLabelFieldViewHolderFactories
import fi.riista.mobile.ui.NoChangeAnimationsItemAnimator
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.ButtonViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderType
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderTypeResolver
import fi.riista.mobile.ui.dataFields.viewHolder.IntFieldViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.ReadOnlySingleLineTextViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.ReadOnlyTextViewHolder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class HuntingControlHunterInfoActivity
    : BaseActivity()
    , DataFieldViewHolderTypeResolver<HunterInfoField> {

    private lateinit var fieldsView: RecyclerView
    private lateinit var adapter: DataFieldRecyclerViewAdapter<HunterInfoField>

    private val controller = HunterInfoController(
        huntingControlContext = RiistaSDK.huntingControlContext,
        languageProvider = AppLanguageProvider(context = this),
        stringProvider = ContextStringProviderFactory.createForContext(context = this),
    )
    private val disposeBag = DisposeBag()

    private val zxingActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onIntegratorActivityResult(result.resultCode, result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hunting_control_hunter_info)

        setCustomTitle(getString(R.string.hunting_control_main_page_title))

        if (savedInstanceState != null) {
            controller.restoreFromBundle(savedInstanceState, CONTROLLER_STATE_PREFIX)
        }

        fieldsView = findViewById(R.id.rv_data_fields)
        fieldsView.adapter = DataFieldRecyclerViewAdapter(viewHolderTypeResolver = this)
            .also { adapter ->
                this.adapter = adapter
                registerViewHolderFactories(adapter)
            }
        fieldsView.itemAnimator = NoChangeAnimationsItemAnimator()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller.saveToBundle(outState, CONTROLLER_STATE_PREFIX)
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

        loadEntries()
    }

    override fun onPause() {
        super.onPause()

        disposeBag.disposeAll()
    }

    override fun resolveViewHolderType(dataField: DataField<HunterInfoField>): DataFieldViewHolderType {
        return when (dataField) {
            is LabelField -> dataField.determineViewHolderType()
            is StringField -> {
                if (dataField.settings.singleLine) {
                    DataFieldViewHolderType.READONLY_TEXT_SINGLE_LINE
                } else {
                    DataFieldViewHolderType.READONLY_TEXT
                }
            }
            is IntField -> DataFieldViewHolderType.INT
            is CustomUserInterfaceField -> DataFieldViewHolderType.CUSTOM
            is ButtonField -> DataFieldViewHolderType.BUTTON
            else -> {
                throw IllegalArgumentException("Unexpected DataField type: ${dataField::class.simpleName}")
            }
        }
    }

    private fun loadEntries() {
        MainScope().launch {
            controller.loadViewModel(refresh = false)
        }
    }

    private fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<HunterInfoField>) {
        adapter.apply {
            registerLabelFieldViewHolderFactories(linkActionEventDispatcher = null)
            registerViewHolderFactory(ReadOnlySingleLineTextViewHolder.Factory())
            registerViewHolderFactory(ReadOnlyTextViewHolder.Factory())
            registerViewHolderFactory(IntFieldViewHolder.Factory(controller.intEventDispatcher))
            registerViewHolderFactory(StartScanButtonViewHolder.Factory(::onStartScanClicked))
            registerViewHolderFactory(ButtonViewHolder.Factory(::buttonClicked))
        }
    }

    private fun onStartScanClicked() {
        val intent = IntentIntegrator(this)
            .setBarcodeImageEnabled(true)
            .setOrientationLocked(false)
            .createScanIntent()
        zxingActivityResultLauncher.launch(intent)
    }

    private fun buttonClicked(field: HunterInfoField) {
        controller.actionEventDispatcher.dispatchEvent(field)
    }

    private fun onIntegratorActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val intentResult = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data)
            val content = intentResult?.contents
            if (content != null) {
                val huntingLicenseMatcher = HUNTING_LICENSE_PATTERN.matcher(content)
                if (huntingLicenseMatcher.find()) {
                    val hunterNumber = huntingLicenseMatcher.group(1)
                    if (hunterNumber != null) {
                        controller.eventDispatcher.dispatchHunterNumber(hunterNumber)
                    }
                } else {
                    val ssnMatcher = SSN_PATTERN.matcher(content)
                    if (ssnMatcher.find()) {
                        val ssn = ssnMatcher.group(0)
                        if (ssn != null) {
                            controller.eventDispatcher.dispatchSsn(ssn)
                        }
                    } else {
                        val errorMsg = getString(R.string.hunting_licence_qr_code_invalid_format)
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    companion object {
        private const val CONTROLLER_STATE_PREFIX = "HCHIA_controller"
        private val HUNTING_LICENSE_PATTERN = Pattern.compile("^.*;.*;.*;\\d*;(\\d{8});\\d*;\\d*;.*$")
        private val SSN_PATTERN = Pattern.compile(RemoteConfig.ssnPattern)
    }
}
