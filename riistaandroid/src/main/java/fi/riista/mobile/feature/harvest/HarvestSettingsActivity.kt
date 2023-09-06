package fi.riista.mobile.feature.harvest

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.harvest.ui.settings.HarvestSettingsController
import fi.riista.common.domain.harvest.ui.settings.HarvestSettingsField
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.BooleanField
import fi.riista.common.ui.dataField.DataField
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.BooleanAsSwitchViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderType
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderTypeResolver
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class HarvestSettingsActivity
    : BaseActivity()
    , DataFieldViewHolderTypeResolver<HarvestSettingsField>
{
    private lateinit var adapter: DataFieldRecyclerViewAdapter<HarvestSettingsField>
    private val controller by lazy {
        HarvestSettingsController(
            stringProvider = ContextStringProviderFactory.createForContext(this),
            preferences = RiistaSDK.preferences,
        )
    }
    private val disposeBag = DisposeBag()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_harvest_settings)
        setCustomTitle(getString(R.string.harvest_settings))

        val recyclerView = findViewById<RecyclerView>(R.id.rv_data_fields)!!
        recyclerView.adapter = DataFieldRecyclerViewAdapter(viewHolderTypeResolver = this)
            .also { adapter ->
                this.adapter = adapter
                registerViewHolderFactories(adapter)
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
                }
            }
        }.disposeBy(disposeBag)

        MainScope().launch {
            controller.loadViewModel(refresh = false)
        }
    }

    override fun onPause() {
        super.onPause()

        disposeBag.disposeAll()
    }

    override fun resolveViewHolderType(dataField: DataField<HarvestSettingsField>): DataFieldViewHolderType {
        return when (dataField) {
            is BooleanField -> DataFieldViewHolderType.BOOLEAN_AS_SWITCH
            else -> throw IllegalStateException("Not supported ${dataField.id}")
        }
    }

    private fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<HarvestSettingsField>) {
        adapter.registerViewHolderFactory(
            BooleanAsSwitchViewHolder.Factory(
                eventDispatcher = controller.eventDispatchers.booleanEventDispatcher,
            )
        )
    }
}
