package fi.riista.mobile.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import dagger.android.AndroidInjection
import fi.riista.common.util.letWith
import fi.riista.mobile.R
import fi.riista.mobile.adapter.MapAreaAdapter
import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME
import fi.riista.mobile.models.AreaMap
import fi.riista.mobile.models.ClubAreaMap
import fi.riista.mobile.network.FetchClubAreaMapTask
import fi.riista.mobile.network.FetchUserMapAreasTask
import fi.riista.mobile.network.ListAreasTask
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.DelegatingAlertDialogListener
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.utils.*
import fi.riista.mobile.vectormap.VectorTileProvider
import fi.vincit.androidutilslib.context.WorkContext
import fi.vincit.androidutilslib.task.NetworkTask
import javax.inject.Inject
import javax.inject.Named


class MapAreaListActivity : BaseActivity() {

    @Inject
    @Named(APPLICATION_WORK_CONTEXT_NAME)
    internal lateinit var mAppWorkContext: WorkContext

    @Inject
    internal lateinit var mClubAreaHelper: ClubAreaHelper

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: MapAreaAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var areaType: VectorTileProvider.AreaType
    private lateinit var dataSet: Array<MapAreaAdapter.AreaListItem>
    private lateinit var searchField: EditText
    private lateinit var addWithAreaCodeButton: Button

    private var mClubAreas: MutableList<ClubAreaMap> = ArrayList()

    val dialogListener: AlertDialogFragment.Listener = DelegatingAlertDialogListener(this).apply {
        registerPositiveCallback(AlertDialogId.MAP_AREA_LIST_ACTIVITY_REMOVE_AREA) { value ->
            value?.let { areaId ->
                doRemoveArea(areaId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_area_list)
        setCustomTitle(getString(R.string.map_settings_select_area))

        areaType = intent.getSerializableExtra(EXTRA_MAP_AREA_TYPE) as VectorTileProvider.AreaType

        viewManager = LinearLayoutManager(this)
        dataSet = arrayOf()
        viewAdapter = MapAreaAdapter(
            dataSet = this.dataSet,
            clickListener = ::itemSelected,
            areaRemoveListener = ::removeArea,
        )

        this.recyclerView = findViewById<RecyclerView>(R.id.area_map_list).apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = viewAdapter
        }

        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(this, R.drawable.line_divider_horizontal)?.let { divider.setDrawable(it) }
        recyclerView.addItemDecoration(divider)

        searchField = findViewById(R.id.search_input)
        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                filterList(s.toString())
            }
        })

        addWithAreaCodeButton = findViewById(R.id.btn_add_area_map_code)
        addWithAreaCodeButton.visibility = if (VectorTileProvider.AreaType.SEURA == areaType) View.VISIBLE else View.GONE
        addWithAreaCodeButton.setOnClickListener { showAddWithAreaCodeDialog() }

        supportFragmentManager.setFragmentResultListener(
            AlertDialogId.MAP_AREA_LIST_ACTIVITY_FETCH_AREA.name,
            this,
        ) { _, result ->
            val areaId = result.getString(AreaCodeDialogFragment.KEY_AREA_ID)
            areaId?.let {
                fetchClubAreaMap(areaId)
            }
        }

        refreshList()
    }

    override fun onBackPressed() {
        KeyboardUtils.hideSoftKeyboard(this)
        super.onBackPressed()
    }

    private fun itemSelected(externalAreaId: String) {
        val data = Intent()
        data.putExtra(RESULT_EXTRA_AREA_CODE, externalAreaId)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun itemSelected(area: MapAreaAdapter.AreaListItem) {
        val data = Intent()
        data.putExtra(RESULT_EXTRA_AREA_CODE, area.areaId)
        data.putExtra(RESULT_EXTRA_AREA_NAME, area.nameText)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun removeArea(areaId: String) {
        AlertDialogFragment.Builder(this, AlertDialogId.MAP_AREA_LIST_ACTIVITY_REMOVE_AREA)
            .setMessage(getString(R.string.map_settings_remove_area_confirm))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(R.string.yes, areaId)
            .setNegativeButton(R.string.no)
            .build()
            .show(supportFragmentManager)
    }

    private fun doRemoveArea(areaId: String) {
        val selectedAreaId = AppPreferences.getSelectedClubAreaMapId(this@MapAreaListActivity)
        if (selectedAreaId == areaId) {
            AppPreferences.setSelectedClubAreaMapId(this@MapAreaListActivity, null)
        }
        ClubAreaUtils.removeRemoteAreaMapFromList(areaId, mClubAreas)
        mClubAreaHelper.saveAreasToFile(mClubAreas)
        refreshClubAreaList()
    }

    private fun refreshList() {
        if (areaType == VectorTileProvider.AreaType.MOOSE || areaType == VectorTileProvider.AreaType.PIENRIISTA) {
            val task = object : ListAreasTask(mAppWorkContext, areaType) {
                override fun onFinishObjects(results: MutableList<AreaMap>?) {

                    dataSet = results?.let { listItemsFromAreaMaps(it).toTypedArray() }!!
                    viewAdapter.setDataSet(dataSet)
                }
            }
            task.start()
        } else if (areaType == VectorTileProvider.AreaType.SEURA) {
            fetchCachedClubAreas()
        }
    }

    private fun fetchCachedClubAreas() {
        val task = object : FetchUserMapAreasTask(mAppWorkContext) {
            override fun onFinishObjects(results: List<ClubAreaMap>) {
                mClubAreas = results.toMutableList()

                refreshClubAreaList()
            }

            override fun onEnd() {
                fetchServerAreas()
            }
        }
        task.baseUrl = NetworkTask.SCHEME_INTERNAL + mClubAreaHelper.getMapCacheFileName()
        task.start()
    }

    private fun fetchServerAreas() {
        val task = object : FetchUserMapAreasTask(mAppWorkContext) {
            override fun onFinishObjects(results: List<ClubAreaMap>) {
                mClubAreaHelper.mergeServerResultToLocal(results, mClubAreas)

                refreshClubAreaList()
            }
        }
        task.start()
    }

    private fun refreshClubAreaList() {
        val areas = listItemsFromClubAreas()

        dataSet = areas.toTypedArray()
        viewAdapter.setDataSet(dataSet)
    }

    private fun listItemsFromAreaMaps(areas: MutableList<AreaMap>): List<MapAreaAdapter.AreaListItem> {
        val items = ArrayList<MapAreaAdapter.AreaListItem>()

        for (area in areas) {
            val item = MapAreaAdapter.AreaListItem(
                areaId = area.number.toString(),
                titleText = area.number.toString(),
                nameText = area.name.toString(),
                idText = null,
                manuallyAdded = false
            )
            items.add(item)
        }

        val locale = LocaleUtil.localeFromLanguageSetting(this)
        items.sortWith { a, b ->
            a.areaId.lowercase(locale).compareTo(b.areaId.lowercase(locale))
        }

        return items
    }

    private fun listItemsFromClubAreas(): List<MapAreaAdapter.AreaListItem> {
        val items = ArrayList<MapAreaAdapter.AreaListItem>()

        for (clubArea in mClubAreas) {
            val localizedAreaName = LocaleUtil.getLocalizedValue(clubArea.name)
            val displayedAreaId = getDisplayedAreaId(
                    localizedAreaName,
                    clubArea.externalId
            )
            val item = MapAreaAdapter.AreaListItem(
                areaId = clubArea.externalId,
                titleText = LocaleUtil.getLocalizedValue(clubArea.clubName),
                nameText = localizedAreaName,
                idText = displayedAreaId,
                manuallyAdded = clubArea.manuallyAdded,
            )
            items.add(item)
        }

        val locale = LocaleUtil.localeFromLanguageSetting(this)
        items.sortWith { a, b ->
            a.titleText.lowercase(locale).compareTo(b.titleText.lowercase(locale))
        }

        return items
    }

    private fun getDisplayedAreaId(clubAreaName: String, externalId: String?): String? {
        if (!externalId.isNullOrEmpty() && clubAreaName != externalId) {
            return getString(R.string.map_area_code_format, externalId)
        }

        return null
    }

    private fun filterList(text: String) {
        val filteredAreas = ArrayList<MapAreaAdapter.AreaListItem>()

        if (text.isEmpty()) {
            filteredAreas.addAll(dataSet)
        } else {
            val locale = LocaleUtil.localeFromLanguageSetting(this)
            val searchText = text.lowercase(locale)

            for (s in dataSet) {
                if (s.titleText.lowercase(locale).contains(searchText) ||
                    s.nameText.lowercase(locale).contains(searchText) ||
                    s.idText.orEmpty().lowercase(locale).contains(searchText)) {
                    filteredAreas.add(s)
                }
            }
        }
        viewAdapter.setDataSet(filteredAreas.toTypedArray())
    }

    private fun showAddWithAreaCodeDialog() {
        val dialog = AreaCodeDialogFragment()
        dialog.show(supportFragmentManager, "AreaCodeDialogFragment")
    }

    private fun fetchClubAreaMap(externalId: String) {
        val task = object : FetchClubAreaMapTask(mAppWorkContext, externalId) {
            override fun onFinishObject(result: ClubAreaMap) {
                if (result.externalId != null) {
                    result.manuallyAdded = true

                    ClubAreaUtils.addRemoteAreaMapToList(result, mClubAreas)
                    mClubAreaHelper.saveAreasToFile(mClubAreas)

                    AppPreferences.setSelectedClubAreaMapId(this@MapAreaListActivity, externalId)

                    itemSelected(result.externalId)
                } else {
                    showAddAreaErrorDialog()
                }
            }

            override fun onError() {
                showAddAreaErrorDialog()
            }
        }
        task.setProgressDialog(null, true)
        task.start()
    }

    private fun showAddAreaErrorDialog() {
        if (!this.isFinishing) {
            AlertDialogFragment.Builder(this, AlertDialogId.MAP_AREA_LIST_ACTIVITY_AREA_ERROR_DIALOG)
                .setMessage(getString(R.string.map_add_area_error))
                .setPositiveButton(R.string.ok)
                .build()
                .show(supportFragmentManager)
        }
    }

    companion object {
        const val EXTRA_MAP_AREA_TYPE = "extra_map_area_type"
        const val RESULT_EXTRA_AREA_CODE: String = "result_extra_area_code"
        const val RESULT_EXTRA_AREA_NAME: String = "result_extra_area_name"

        const val REQUEST_SELECT_AREA_CLUB: Int = 201
        const val REQUEST_SELECT_AREA_PIENRIISTA: Int = 202
        const val REQUEST_SELECT_AREA_MOOSE: Int = 203
    }
}

class AreaCodeDialogFragment : DialogFragment() {
    private var areaCodeDialog: AlertDialog? = null
    private var input: TextInputEditText? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.map_add_area))

        val view = layoutInflater.inflate(R.layout.view_dialog_text_input, null)
        input = view.findViewById(R.id.dialog_text_input)
        input?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                input?.let { input ->
                    refreshDialogButtonState(input)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
        builder.setView(view)

        builder.setPositiveButton(R.string.ok) { _, _ ->
            hideKeyboard()

            val areaId = input?.text.toString()
                .trim()
                .uppercase(LocaleUtil.localeFromLanguageSetting(requireContext()))
            val bundle = Bundle().also {
                it.putString(KEY_AREA_ID, areaId)
            }
            requireActivity().supportFragmentManager.setFragmentResult(AlertDialogId.MAP_AREA_LIST_ACTIVITY_FETCH_AREA.name, bundle)
        }
        builder.setNegativeButton(R.string.cancel) { _, _ ->
            hideKeyboard()
        }

        val dialog = builder.create()
        areaCodeDialog = dialog
        dialog.setOnShowListener {
            refreshDialogButtonState(input)
            SoftInputService(context, input).show()
        }

        return dialog
    }

    private fun refreshDialogButtonState(input: EditText?) {
        val button = areaCodeDialog?.getButton(AlertDialog.BUTTON_POSITIVE)
        val text = input?.text
        button?.isEnabled = (text?.length ?: 0) >= 10
    }

    private fun hideKeyboard() {
        context?.letWith(input) { context, input ->
            SoftInputService.hide(context, input.windowToken)
        }
    }

    companion object {
        const val KEY_AREA_ID = "AreaCodeDialogFragment_AREA_ID"
    }
}
