package fi.riista.mobile.activity

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.switchmaterial.SwitchMaterial
import dagger.android.AndroidInjection
import fi.riista.mobile.R
import fi.riista.mobile.activity.MapAreaListActivity.Companion.REQUEST_SELECT_AREA_CLUB
import fi.riista.mobile.activity.MapAreaListActivity.Companion.REQUEST_SELECT_AREA_MOOSE
import fi.riista.mobile.activity.MapAreaListActivity.Companion.REQUEST_SELECT_AREA_PIENRIISTA
import fi.riista.mobile.activity.MapAreaListActivity.Companion.RESULT_EXTRA_AREA_CODE
import fi.riista.mobile.activity.MapAreaListActivity.Companion.RESULT_EXTRA_AREA_NAME
import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME
import fi.riista.mobile.models.AreaMap
import fi.riista.mobile.models.ClubAreaMap
import fi.riista.mobile.network.FetchUserMapAreasTask
import fi.riista.mobile.ui.MapOverlayView
import fi.riista.mobile.utils.AppPreferences
import fi.riista.mobile.utils.ClubAreaHelper
import fi.riista.mobile.utils.ClubAreaUtils
import fi.riista.mobile.utils.LocaleUtil
import fi.riista.mobile.vectormap.VectorTileProvider
import fi.vincit.androidutilslib.context.WorkContext
import fi.vincit.androidutilslib.task.NetworkTask
import javax.inject.Inject
import javax.inject.Named

class MapSettingsActivity : BaseActivity() {

    @Inject
    @Named(APPLICATION_WORK_CONTEXT_NAME)
    internal lateinit var mAppWorkContext: WorkContext

    @Inject
    internal lateinit var mClubAreaHelper: ClubAreaHelper

    private var mClubAreas: MutableList<ClubAreaMap> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_settings)
        setCustomTitle(getString(R.string.more_settings))

        val mapTileSource = AppPreferences.getMapTileSource(this)
        val mapTypeGroup = findViewById<RadioGroup>(R.id.map_type_select)
        mapTypeGroup.clearCheck()

        when (mapTileSource) {
            AppPreferences.MapTileSource.GOOGLE -> mapTypeGroup.check(findViewById<View>(R.id.map_type_google).id)
            AppPreferences.MapTileSource.MML_BACKGROUND -> mapTypeGroup.check(findViewById<View>(R.id.map_type_mml_background).id)
            AppPreferences.MapTileSource.MML_AERIAL -> mapTypeGroup.check(findViewById<View>(R.id.map_type_mml_aerial).id)
            AppPreferences.MapTileSource.MML_TOPOGRAPHIC -> mapTypeGroup.check(findViewById<View>(R.id.map_type_mml_topographic).id)
            else -> mapTypeGroup.check(findViewById<View>(R.id.map_type_mml_topographic).id)
        }

        findViewById<View>(R.id.map_type_google).setOnClickListener { onMapTypeSelected(AppPreferences.MapTileSource.GOOGLE) }
        findViewById<View>(R.id.map_type_mml_topographic).setOnClickListener { onMapTypeSelected(AppPreferences.MapTileSource.MML_TOPOGRAPHIC) }
        findViewById<View>(R.id.map_type_mml_background).setOnClickListener { onMapTypeSelected(AppPreferences.MapTileSource.MML_BACKGROUND) }
        findViewById<View>(R.id.map_type_mml_aerial).setOnClickListener { onMapTypeSelected(AppPreferences.MapTileSource.MML_AERIAL) }

        findViewById<AppCompatButton>(R.id.btn_offline_map_settings).setOnClickListener {
            startActivity(Intent(this, OfflineMapSettingsActivity::class.java))
        }

        val locationCheck = findViewById<SwitchMaterial>(R.id.check_show_location)
        locationCheck.isChecked = AppPreferences.getShowUserMapLocation(this)
        locationCheck.setOnCheckedChangeListener { _, b ->
            AppPreferences.setShowUserMapLocation(this, b)
        }

        val invertCheck = findViewById<SwitchMaterial>(R.id.check_invert_map_colors)
        invertCheck.isChecked = AppPreferences.getInvertMapColors(this)
        invertCheck.setOnCheckedChangeListener { _, b ->
            AppPreferences.setInvertMapColors(this, b)
        }

        val valtionmaatCheck = findViewById<SwitchMaterial>(R.id.enable_layer_valtionmaat)
        valtionmaatCheck.isChecked = AppPreferences.getShowValtionmaat(this)
        valtionmaatCheck.setOnCheckedChangeListener { _, b ->
            AppPreferences.setShowValtionmaat(this, b)
        }

        val rhyCheck = findViewById<SwitchMaterial>(R.id.enable_layer_rhy_borders)
        rhyCheck.isChecked = AppPreferences.getShowRhyBorders(this)
        rhyCheck.setOnCheckedChangeListener { _, b ->
            AppPreferences.setShowRhyBorders(this, b)
        }

        val gameTrianglesCheck = findViewById<SwitchMaterial>(R.id.enable_layer_game_triangles)
        gameTrianglesCheck.isChecked = AppPreferences.getShowGameTriangles(this)
        gameTrianglesCheck.setOnCheckedChangeListener { _, b ->
            AppPreferences.setShowGameTriangles(this, b)
        }

        val mooseRestrictionsCheck = findViewById<SwitchMaterial>(R.id.enable_layer_moose_restrictions)
        mooseRestrictionsCheck.isChecked = AppPreferences.getShowMooseRestrictions(this)
        mooseRestrictionsCheck.setOnCheckedChangeListener { _, b ->
            AppPreferences.setShowMooseRestrictions(this, b)
        }

        val smallGameRestrictionsCheck = findViewById<SwitchMaterial>(R.id.enable_layer_small_game_restrictions)
        smallGameRestrictionsCheck.isChecked = AppPreferences.getShowSmallGameRestrictions(this)
        smallGameRestrictionsCheck.setOnCheckedChangeListener { _, b ->
            AppPreferences.setShowSmallGameRestrictions(this, b)
        }

        val aviHuntingBanCheck = findViewById<SwitchMaterial>(R.id.enable_layer_avi_hunting_ban)
        aviHuntingBanCheck.isChecked = AppPreferences.getShowAviHuntingBan(this)
        aviHuntingBanCheck.setOnCheckedChangeListener { _, b ->
            AppPreferences.setShowAviHuntingBan(this, b)
        }

        val areasContainer = findViewById<LinearLayout>(R.id.container_map_areas)
        refreshSelectedAreasView(areasContainer)

        findViewById<View>(R.id.btn_add_area_map_club).setOnClickListener {
            val intent = Intent(this, MapAreaListActivity::class.java)
            intent.putExtra(MapAreaListActivity.EXTRA_MAP_AREA_TYPE, VectorTileProvider.AreaType.SEURA)
            startActivityForResult(intent, REQUEST_SELECT_AREA_CLUB)
        }

        findViewById<View>(R.id.btn_add_area_map_moose).setOnClickListener {
            val intent = Intent(this, MapAreaListActivity::class.java)
            intent.putExtra(MapAreaListActivity.EXTRA_MAP_AREA_TYPE, VectorTileProvider.AreaType.MOOSE)
            startActivityForResult(intent, REQUEST_SELECT_AREA_MOOSE)
        }

        findViewById<View>(R.id.btn_add_area_map_pienriista).setOnClickListener {
            val intent = Intent(this, MapAreaListActivity::class.java)
            intent.putExtra(MapAreaListActivity.EXTRA_MAP_AREA_TYPE, VectorTileProvider.AreaType.PIENRIISTA)
            startActivityForResult(intent, REQUEST_SELECT_AREA_PIENRIISTA)
        }
    }

    override fun onResume() {
        super.onResume()

        // List may be different after back navigation
        fetchCachedClubAreas()
        refreshSelectedAreaTypes()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SELECT_AREA_CLUB -> {
                if (Activity.RESULT_OK == resultCode) {
                    val result = data?.getStringExtra(RESULT_EXTRA_AREA_CODE)

                    AppPreferences.setSelectedClubAreaMapId(this, result)
                    refreshSelectedAreaTypes()
                    refreshSelectedAreasView(findViewById(R.id.container_map_areas))
                }
            }
            REQUEST_SELECT_AREA_PIENRIISTA -> {
                if (Activity.RESULT_OK == resultCode) {
                    val area = AreaMap()
                    area.number = data?.getStringExtra(RESULT_EXTRA_AREA_CODE)
                    area.name = data?.getStringExtra(RESULT_EXTRA_AREA_NAME)

                    AppPreferences.setSelectedMhPienriistaAreasMapIds(this, setOf(area))
                    refreshSelectedAreaTypes()
                    refreshSelectedAreasView(findViewById(R.id.container_map_areas))
                }
            }
            REQUEST_SELECT_AREA_MOOSE -> {
                if (Activity.RESULT_OK == resultCode) {
                    val area = AreaMap()
                    area.number = data?.getStringExtra(RESULT_EXTRA_AREA_CODE)
                    area.name = data?.getStringExtra(RESULT_EXTRA_AREA_NAME)

                    AppPreferences.setSelectedMhMooseAreaMapIds(this, setOf(area))
                    refreshSelectedAreaTypes()
                    refreshSelectedAreasView(findViewById(R.id.container_map_areas))
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun onMapTypeSelected(selection: AppPreferences.MapTileSource) {
        AppPreferences.setMapTileSource(this, selection)
    }

    private fun refreshSelectedAreaTypes() {
        val selectedClubArea = AppPreferences.getSelectedClubAreaMapId(this)
        val selectedMooseAreas = AppPreferences.getSelectedMhMooseAreaMapIds(this)
        val selectedPienriistaAreas = AppPreferences.getSelectedMhPienriistaAreasMapIds(this)

        findViewById<AppCompatButton>(R.id.btn_add_area_map_club)
                .setCompoundDrawablesWithIntrinsicBounds(null,
                        null,
                        if (selectedClubArea != null) AppCompatResources.getDrawable(this, R.drawable.ic_check_circle_green_24dp) else null,
                        null)
        findViewById<AppCompatButton>(R.id.btn_add_area_map_moose)
                .setCompoundDrawablesWithIntrinsicBounds(null,
                        null,
                        if (selectedMooseAreas != null && selectedMooseAreas.isNotEmpty()) AppCompatResources.getDrawable(this, R.drawable.ic_check_circle_green_24dp) else null,
                        null)
        findViewById<AppCompatButton>(R.id.btn_add_area_map_pienriista)
                .setCompoundDrawablesWithIntrinsicBounds(null,
                        null,
                        if (selectedPienriistaAreas != null && selectedPienriistaAreas.isNotEmpty()) AppCompatResources.getDrawable(this, R.drawable.ic_check_circle_green_24dp) else null,
                        null)
    }

    private fun refreshSelectedAreasView(container: LinearLayout) {
        container.removeAllViews()

        AppPreferences.getSelectedClubAreaMapId(this)?.let { selectedClubArea ->
            ClubAreaUtils.findAreaById(selectedClubArea, mClubAreas)?.let { clubArea ->
                addSelectedItemViewFrom(clubArea, container)
            }
        }

        AppPreferences.getSelectedMhMooseAreaMapIds(this)?.let { mooseAreas ->
            for (area in mooseAreas) {
                addSelectedItemViewFrom(area, VectorTileProvider.AreaType.MOOSE, container)
            }
        }

        AppPreferences.getSelectedMhPienriistaAreasMapIds(this)?.let { pienriistaAreas ->
            for (area in pienriistaAreas) {
                addSelectedItemViewFrom(area, VectorTileProvider.AreaType.PIENRIISTA, container)
            }
        }
    }

    private fun addSelectedItemViewFrom(area: ClubAreaMap, container: LinearLayout) {
        val item = LayoutInflater.from(this).inflate(R.layout.view_club_area_item, container, false)

        val titleText = item.findViewById<TextView>(R.id.club_area_title)
        titleText.text = LocaleUtil.getLocalizedValue(area.clubName)
        val areaId = area.externalId ?: ""
        if (areaId == MapOverlayView.NO_AREA_ID) {
            titleText.visibility = View.GONE
        }

        val nameText = item.findViewById<TextView>(R.id.club_area_name)
        val localizedAreaName = LocaleUtil.getLocalizedValue(area.name)
        nameText.text = localizedAreaName

        val idText = item.findViewById<TextView>(R.id.club_area_id)
        if (areaId.isNotEmpty() && localizedAreaName != areaId &&
            areaId != MapOverlayView.NO_AREA_ID) {
            idText.text = getString(R.string.map_area_code_format, areaId)
            idText.visibility = View.VISIBLE
        } else {
            idText.visibility = View.GONE
        }

        val copyToClipboardButton = item.findViewById<AppCompatImageView>(R.id.btn_copy_to_clipboard)
        copyToClipboardButton.setOnClickListener {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(getString(R.string.map_area_code), areaId)
            clipboardManager.setPrimaryClip(clipData)
            val androidSv2 = Build.VERSION_CODES.S + 1 // TODO: Use build code S_V2 when updated to target API 32
            if (Build.VERSION.SDK_INT <= androidSv2) {
                // Android 13+ automatically shows something similar
                Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show()
            }
        }

        val removeButton = item.findViewById<TextView>(R.id.btn_remove_area)
        removeButton.setOnClickListener {
            AppPreferences.setSelectedClubAreaMapId(this, null)

            refreshSelectedAreaTypes()
            refreshSelectedAreasView(container)
        }

        container.addView(item)
    }

    private fun addSelectedItemViewFrom(area: AreaMap, type: VectorTileProvider.AreaType, container: LinearLayout) {
        val item = LayoutInflater.from(this).inflate(R.layout.view_club_area_item, container, false)

        val titleText = item.findViewById<TextView>(R.id.club_area_title)
        when (type) {
            VectorTileProvider.AreaType.MOOSE -> titleText.text = String.format("%s - %s", area.number, getString(R.string.map_settings_add_hirvialue))
            VectorTileProvider.AreaType.PIENRIISTA -> titleText.text = String.format("%s - %s", area.number, getString(R.string.map_settings_add_pienriista))
            else -> titleText.text = area.number
        }

        val nameText = item.findViewById<TextView>(R.id.club_area_name)
        nameText.text = area.name

        val idText = item.findViewById<TextView>(R.id.club_area_id)
        idText.visibility = View.GONE

        val removeButton = item.findViewById<TextView>(R.id.btn_remove_area)
        removeButton.setOnClickListener {
            if (type == VectorTileProvider.AreaType.MOOSE) {
                AppPreferences.setSelectedMhMooseAreaMapIds(this, null)
            } else if (type == VectorTileProvider.AreaType.PIENRIISTA) {
                AppPreferences.setSelectedMhPienriistaAreasMapIds(this, null)
            }

            refreshSelectedAreaTypes()
            refreshSelectedAreasView(container)
        }

        container.addView(item)
    }

    private fun fetchCachedClubAreas() {
        val task = object : FetchUserMapAreasTask(mAppWorkContext) {
            override fun onFinishObjects(results: List<ClubAreaMap>) {
                mClubAreas = results.toMutableList()

                refreshSelectedAreasView(findViewById(R.id.container_map_areas))
            }
        }
        task.baseUrl = NetworkTask.SCHEME_INTERNAL + mClubAreaHelper.getMapCacheFileName()
        task.start()
    }
}
