package fi.riista.mobile.feature.poi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.poi.model.PoiLocation
import fi.riista.common.domain.poi.model.PoiLocationGroup
import fi.riista.common.domain.poi.ui.PoiLocationController
import fi.riista.common.domain.poi.ui.PoiLocationsViewModel
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.toBackendEnum
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.controller.restoreFromBundle
import fi.riista.common.ui.controller.saveToBundle
import fi.riista.common.util.toLocation
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.riistaSdkHelpers.localized
import fi.riista.mobile.utils.UiUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class PoiLocationActivity : BaseActivity(), PoiLocationFragment.Manager {

    interface CenterMapListener {
        fun centerMapTo(location: Location)
    }

    private lateinit var pagerLayout: ViewGroup
    private lateinit var noContent: TextView
    private lateinit var viewPager: ViewPager2
    private lateinit var previousButton: AppCompatButton
    private lateinit var nextButton: AppCompatButton
    private lateinit var pagerAdapter: ScreenSlidePagerAdapter
    private lateinit var controller: PoiLocationController

    private val disposeBag = DisposeBag()

    private val pageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            controller.eventDispatcher.dispatchPoiLocationSelected(position)

            val previousEnabled = (position > 0)
            if (previousButton.isEnabled != previousEnabled) {
                previousButton.isEnabled = previousEnabled
                setIconToPreviousButton()
            }

            val nextEnabled = (position < (pagerAdapter.model?.poiLocations?.size ?: 0) - 1)
            if (nextButton.isEnabled != nextEnabled) {
                nextButton.isEnabled = nextEnabled
                setIconToNextButton()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poi_location)

        pagerLayout = findViewById(R.id.layout_pager)
        noContent = findViewById(R.id.tv_no_content)
        viewPager = findViewById(R.id.pager)
        previousButton = findViewById<AppCompatButton>(R.id.btn_previous).also { button ->
            button.setOnClickListener {
                viewPager.setCurrentItem(viewPager.currentItem - 1, true)
            }
        }
        nextButton = findViewById<AppCompatButton>(R.id.btn_next).also{  button ->
            button.setOnClickListener {
                viewPager.setCurrentItem(viewPager.currentItem + 1, true)
            }
        }
        setIconToPreviousButton()
        setIconToNextButton()
        pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(pageChangeListener)

        val externalId = intent.getStringExtra(EXTRA_EXTERNAL_ID)
        val locationGroupId = intent.getLongExtra(EXTRA_LOCATION_GROUP_ID, 0)
        val initialPoiLocationId = intent.getLongExtra(EXTRA_SELECTED_LOCATION_ID, 0)

        if (externalId == null) {
            Log.e("PoiLocationActivity", "External ID is null")
            finish()
            return
        }
        controller = PoiLocationController(
            locationGroupContext = RiistaSDK.poiContext.getPoiLocationGroupContext(externalId),
            poiLocationGroupId = locationGroupId,
            initiallySelectedPoiLocationId = initialPoiLocationId,
        )
        savedInstanceState?.let {
            controller.restoreFromBundle(it, CONTROLLER_STATE_PREFIX)
        }

        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        setCustomTitle(title)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller.saveToBundle(outState, CONTROLLER_STATE_PREFIX)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewPager.unregisterOnPageChangeCallback(pageChangeListener)
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { loadStatus ->
            when (loadStatus) {
                ViewModelLoadStatus.NotLoaded -> onNotLoaded()
                ViewModelLoadStatus.Loading -> onLoading()
                ViewModelLoadStatus.LoadFailed -> onLoadFailed()
                is ViewModelLoadStatus.Loaded -> onLoaded(loadStatus.viewModel)
            }
        }.disposeBy(disposeBag)

        loadViewModelIfNotLoaded()
    }

    private fun onNotLoaded() {
        pagerLayout.visibility = View.GONE
        noContent.visibility = View.GONE
    }

    private fun onLoading() {
        pagerLayout.visibility = View.GONE
        noContent.visibility = View.VISIBLE
        noContent.setText(R.string.loading_content)

    }

    private fun onLoadFailed() {
        pagerLayout.visibility = View.GONE
        noContent.visibility = View.VISIBLE
        noContent.setText(R.string.content_loading_failed)
    }

    private fun onLoaded(viewModel: PoiLocationsViewModel) {
        noContent.visibility = View.GONE
        pagerLayout.visibility = View.VISIBLE

        // Don't update adapter model when only index has changed. Adapter knows it already.
        val adapterModel = pagerAdapter.model
        if (adapterModel == null || !viewModel.hasSamePoiLocations(adapterModel)) {
            pagerAdapter.model = viewModel
            viewPager.setCurrentItem(viewModel.selectedIndex, false)
        }
    }

    override fun onPause() {
        super.onPause()
        disposeBag.disposeAll()
    }

    override fun showPoiOnMap(latitude: Int, longitude: Int) {
        val data = Intent()
        data.putExtra(RESULT_LATITUDE, latitude)
        data.putExtra(RESULT_LONGITUDE, longitude)
        setResult(RESULT_OK, data)
        finish()
    }

    private fun loadViewModelIfNotLoaded() {
        if (controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }

        MainScope().launch {
            controller.loadViewModel(refresh = false)
        }
    }

    private fun setIconToPreviousButton() {
        UiUtils.addIconWithTint(
            button = previousButton,
            icon = R.drawable.ic_arrow_back_white,
            color = getTintColor(previousButton.isEnabled),
            position = UiUtils.IconPosition.LEFT,
        )
    }

    private fun setIconToNextButton() {
        UiUtils.addIconWithTint(
            button = nextButton,
            icon = R.drawable.ic_arrow_forward,
            color = getTintColor(nextButton.isEnabled),
            position = UiUtils.IconPosition.RIGHT,
        )
    }

    private fun getTintColor(enabled: Boolean): Int {
        @ColorRes
        val color = if (enabled) {
            R.color.colorPrimary
        } else {
            R.color.colorLightGrey
        }
        return color
    }

    companion object {
        private const val CONTROLLER_STATE_PREFIX = "PLA_controller"
        private const val EXTRA_EXTERNAL_ID = "poi_external_id"
        private const val EXTRA_LOCATION_GROUP_ID = "poi_location_group_id"
        private const val EXTRA_SELECTED_LOCATION_ID = "poi_selected_location_id"
        private const val EXTRA_TITLE = "poi_title"
        private const val RESULT_LATITUDE = "latitude"
        private const val RESULT_LONGITUDE = "longitude"

        fun getIntent(
            context: Context,
            externalId: String,
            poiLocationGroup: PoiLocationGroup,
            poiLocation: PoiLocation
        ): Intent {
            val intent = Intent(context, PoiLocationActivity::class.java)
            val title = "${poiLocationGroup.visibleId}: ${poiLocationGroup.type.localized(context)}"
            intent.putExtra(EXTRA_TITLE, title)
            intent.putExtra(EXTRA_EXTERNAL_ID, externalId)
            intent.putExtra(EXTRA_LOCATION_GROUP_ID, poiLocationGroup.id)
            intent.putExtra(EXTRA_SELECTED_LOCATION_ID, poiLocation.id)

            return intent
        }


        fun registerForActivityResult(
            fragment: Fragment,
            callback: (Location) -> Unit
        ): ActivityResultLauncher<Intent> {
            return fragment.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val data = result.data
                if (result.resultCode == Activity.RESULT_OK && data != null) {
                    val latitude = data.getIntExtra(RESULT_LATITUDE, -1)
                    val longitude = data.getIntExtra(RESULT_LONGITUDE, -1)
                    if (latitude != -1 && longitude != -1) {
                        val geoLocation = ETRMSGeoLocation(
                            latitude = latitude,
                            longitude = longitude,
                            source = GeoLocationSource.MANUAL.toBackendEnum()
                        )
                        callback(geoLocation.toLocation())
                    }
                }
            }
        }
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        var model: PoiLocationsViewModel? = null
            @SuppressLint("NotifyDataSetChanged")
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getItemCount(): Int = model?.poiLocations?.size ?: 0

        override fun createFragment(position: Int): Fragment {
            val poi = model?.poiLocations?.get(position) ?: throw RuntimeException("Poi missing")
            return PoiLocationFragment.create(
                poiLocationGroupDescription = poi.groupDescription,
                poiLocationDescription = poi.description,
                poiLocationVisibleId = poi.visibleId,
                location = poi.location,
            )
        }
    }
}
