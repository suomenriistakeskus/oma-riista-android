package fi.riista.mobile.feature.groupHunting.huntingDays

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.updatePadding
import androidx.core.widget.ImageViewCompat
import com.google.android.material.button.MaterialButton
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.groupHunting.model.*
import fi.riista.common.groupHunting.ui.huntingDays.HuntingDayDiaryEntryViewModel
import fi.riista.common.groupHunting.ui.huntingDays.HuntingDayViewModel
import fi.riista.common.groupHunting.ui.huntingDays.ViewGroupHuntingDayController
import fi.riista.common.model.DeerHuntingType
import fi.riista.common.model.HoursAndMinutes
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.resources.ContextStringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.mobile.R
import fi.riista.mobile.database.SpeciesResolver
import fi.riista.mobile.feature.groupHunting.observations.GroupObservationActivity
import fi.riista.mobile.feature.groupHunting.harvests.GroupHarvestActivity
import fi.riista.mobile.pages.PageFragment
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.riistaSdkHelpers.formatToHoursAndMinutesString
import fi.riista.mobile.riistaSdkHelpers.localized
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime
import fi.riista.mobile.utils.DateTimeUtils
import fi.riista.mobile.utils.UiUtils
import fi.riista.mobile.utils.toVisibility
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * A fragment for viewing single [GroupHuntingDay] data.
 */
class ViewGroupHuntingDayFragment
    : PageFragment() {

    interface Manager: ViewOrEditGroupHuntingDayFragmentManager {
        val viewGroupHuntingDayController: ViewGroupHuntingDayController

        fun getTargetForHarvest(harvestId: GroupHuntingHarvestId): GroupHuntingHarvestTarget?
        fun getTargetForObservation(observationId: GroupHuntingObservationId): GroupHuntingObservationTarget?

        fun createHuntingDay()
        fun editHuntingDay()
    }

    @Inject
    lateinit var speciesResolver: SpeciesResolver

    private lateinit var manager: Manager
    private lateinit var controller: ViewGroupHuntingDayController
    private lateinit var stringProvider: ContextStringProvider
    private val disposeBag = DisposeBag()

    private lateinit var layoutStartDateAndTime: View
    private lateinit var layoutEndDateAndTime: View
    private lateinit var layoutNumberOfHunters: View
    private lateinit var layoutHuntingMethod: View
    private lateinit var layoutNumberOfHounds: View
    private lateinit var layoutSnowDepth: View
    private lateinit var layoutBreakDuration: View
    private lateinit var layoutSuggestedHuntingDay: View
    private lateinit var layoutHarvestsAndObservations: LinearLayout

    private var canCreateHuntingDay = false
        set(value) {
            val shouldInvalidateMenu = value != canCreateHuntingDay
            field = value

            if (shouldInvalidateMenu) {
                activity?.invalidateOptionsMenu()
            }
        }

    private var canEditHuntingDay = false
        set(value) {
            val shouldInvalidateMenu = value != canEditHuntingDay
            field = value

            if (shouldInvalidateMenu) {
                activity?.invalidateOptionsMenu()
            }
        }

    private var refreshHuntingDayData: Boolean = false

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)

        super.onAttach(context)
        manager = context as Manager
        stringProvider = ContextStringProviderFactory.createForContext(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_group_hunting_day, container, false)
        setViewTitle(R.string.group_hunting_hunting_day)

        controller = manager.viewGroupHuntingDayController

        layoutStartDateAndTime = view.findViewById(R.id.layout_start_datetime)
        layoutEndDateAndTime = view.findViewById(R.id.layout_end_datetime)
        layoutNumberOfHunters = view.findViewById<View>(R.id.layout_number_of_hunters).also {
            it.updateLabel(R.string.group_hunting_day_label_number_of_hunters)
        }
        layoutHuntingMethod = view.findViewById<View>(R.id.layout_hunting_method).also {
            it.updateLabel(R.string.group_hunting_day_label_hunting_method)
        }
        layoutNumberOfHounds = view.findViewById<View>(R.id.layout_number_of_hounds).also {
            it.updateLabel(R.string.group_hunting_day_label_number_of_hounds)
        }
        layoutSnowDepth = view.findViewById<View>(R.id.layout_snow_depth).also {
            it.updateLabel(R.string.group_hunting_day_label_snow_depth)
        }
        layoutBreakDuration = view.findViewById<View>(R.id.layout_break_duration).also {
            it.updateLabel(R.string.group_hunting_day_label_break_duration)
        }
        layoutSuggestedHuntingDay = view.findViewById<View>(R.id.layout_suggested_day_notification)
            .also {
                it.findViewById<TextView>(R.id.tv_suggested_day_text)
                    .setText(R.string.group_hunting_suggested_hunting_day)

                it.findViewById<MaterialButton>(R.id.btn_add_hunting_day).setOnClickListener {
                    startCreateHuntingDay()
                }
            }
        layoutHarvestsAndObservations = view.findViewById(R.id.layout_harvests_and_observations)

        setHasOptionsMenu(true)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add, menu)
        inflater.inflate(R.menu.menu_edit, menu)
        menu.findItem(R.id.item_add).apply {
            isVisible = canCreateHuntingDay
        }
        menu.findItem(R.id.item_edit).apply {
            isVisible = canEditHuntingDay
        }
        menu.findItem(R.id.item_delete).apply {
            // todo: change when it is possible to delete hunting days
            isVisible = false
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_add -> {
                startCreateHuntingDay()
                true
            }
            R.id.item_edit -> {
                manager.editHuntingDay()
                true
            }
            else -> {
                false
            }
        }
    }

    private fun startCreateHuntingDay() {
        refreshHuntingDayData = true
        manager.createHuntingDay()
    }

    private fun View.updateLabel(@StringRes stringId: Int) {
        findViewById<TextView>(R.id.tv_label).setText(stringId)
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { viewModelLoadStatus ->
            when (viewModelLoadStatus) {
                ViewModelLoadStatus.NotLoaded -> {}
                ViewModelLoadStatus.Loading -> manager.onViewModelLoading()
                ViewModelLoadStatus.LoadFailed -> manager.onViewModelLoadFailed()
                is ViewModelLoadStatus.Loaded -> {
                    manager.onViewModelLoaded()
                    displayViewModelData(viewModelLoadStatus.viewModel)
                }
            }
        }.disposeBy(disposeBag)

        loadHuntingDayIfNotLoaded()
    }

    private fun displayViewModelData(viewModel: HuntingDayViewModel) {
        val huntingDay = viewModel.huntingDay

        canEditHuntingDay = viewModel.canEditHuntingDay
        canCreateHuntingDay = viewModel.canCreateHuntingDay

        with (layoutStartDateAndTime) {
            huntingDay.startDateTime.toJodaDateTime().let { dateAndTime ->
                findViewById<TextView>(R.id.tv_date).text =
                    DateTimeUtils.formatLocalDateUsingShortFinnishFormat(dateAndTime.toLocalDate())
                findViewById<TextView>(R.id.tv_time).text = DateTimeUtils.formatTime(dateAndTime)
            }
        }

        with (layoutEndDateAndTime) {
            huntingDay.endDateTime.toJodaDateTime().let { dateAndTime ->
                findViewById<TextView>(R.id.tv_date).text =
                    DateTimeUtils.formatLocalDateUsingShortFinnishFormat(dateAndTime.toLocalDate())
                findViewById<TextView>(R.id.tv_time).text = DateTimeUtils.formatTime(dateAndTime)
            }
        }

        val huntingDayDetails = listOf(
                layoutNumberOfHunters,
                layoutHuntingMethod,
                layoutNumberOfHounds,
                layoutSnowDepth,
                layoutBreakDuration,
        )

        if (viewModel.showHuntingDayDetails) {
            huntingDayDetails.forEach { it.visibility = View.VISIBLE }

            layoutNumberOfHunters.updateValueTextOrDisplayNoData(huntingDay.numberOfHunters)
            layoutHuntingMethod.updateValueTextOrDisplayNoData(
                    huntingDay.huntingMethod.localized(requireContext())
            )
            layoutNumberOfHounds.updateValueTextOrDisplayNoData(huntingDay.numberOfHounds)
            layoutSnowDepth.updateValueTextOrDisplayNoData(
                    huntingDay.snowDepth?.let { snowDepth ->
                        getString(R.string.centimeters_format, snowDepth)
                    }
            )

            huntingDay.breakDurationInMinutes
                ?.let { totalMinutes ->
                    HoursAndMinutes(totalMinutes).formatToHoursAndMinutesString(
                            context = requireContext(),
                            zeroMinutesStringRes = R.string.group_hunting_day_no_breaks
                    )
                }.let { breakDurationString ->
                    layoutBreakDuration.updateValueTextOrDisplayNoData(breakDurationString)
                }
        } else {
            huntingDayDetails.forEach { it.visibility = View.GONE }
        }

        layoutSuggestedHuntingDay.visibility = (viewModel.canCreateHuntingDay &&
                viewModel.huntingDayType == HuntingDayViewModel.HuntingDayType.SUGGESTED).toVisibility()

        replaceDisplayedHarvestAndObservations(viewModel)
    }

    private fun replaceDisplayedHarvestAndObservations(viewModel: HuntingDayViewModel) {
        val entriesByDeerHuntingType: Map<DeerHuntingType?, List<HuntingDayDiaryEntryViewModel>> =
            (viewModel.harvests + viewModel.observations)
                .groupBy { it.deerHuntingType }

        layoutHarvestsAndObservations.removeAllViews()

        // labels should only be shown if there are known deer hunting types
        val showDeerHuntingTypeLabels = DeerHuntingType.values().find {
            entriesByDeerHuntingType.contains(it)
        } != null

        var addPaddingBeforeNextLabel = false

        DeerHuntingType.values().forEach { deerHuntingType ->
            val entries = when (deerHuntingType) {
                DeerHuntingType.OTHER -> {
                    // include unknown (== null) deer hunting types under "OTHER" category
                    entriesByDeerHuntingType.getOrDefault(deerHuntingType, listOf()) +
                            entriesByDeerHuntingType.getOrDefault(null, listOf())
                }
                else -> entriesByDeerHuntingType.getOrDefault(deerHuntingType, listOf())
            }.sortedBy { it.pointOfTime }

            if (showDeerHuntingTypeLabels && entries.isNotEmpty()) {
                val labelLayout = layoutInflater.inflate(
                        R.layout.layout_deer_hunting_type_label,
                        layoutHarvestsAndObservations,
                        false
                ).also {
                    it.findViewById<TextView>(R.id.tv_text).text =
                        stringProvider.getString(deerHuntingType.resourcesStringId)

                    it.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    if (addPaddingBeforeNextLabel) {
                        it.updatePadding(top = UiUtils.dipToPixels(requireContext(), 16))
                    }
                }

                layoutHarvestsAndObservations.addView(labelLayout)

                addPaddingBeforeNextLabel = true
            }

            entries.forEach { entry ->
                val entryLayout = layoutInflater.inflate(
                        R.layout.layout_hunting_day_diary_entry,
                        layoutHarvestsAndObservations,
                        false
                )

                speciesResolver.findSpecies(entry.speciesCode)?.let {
                    entryLayout.findViewById<TextView>(R.id.tv_species_name).text = it.mName
                }

                entryLayout.findViewById<TextView>(R.id.tv_species_amount).text =
                    if (entry.amount != 0) {
                        getString(R.string.amount_format, entry.amount)
                    } else {
                        // indicates that we don't know the amount. This is the case e.g. with
                        // observations where observation type doesn't allow figuring out the amount
                        "-"
                    }
                entryLayout.findViewById<TextView>(R.id.tv_time).text =
                    DateTimeUtils.formatTime(entry.pointOfTime.toJodaDateTime())

                @DrawableRes
                val icon: Int = when (entry.type) {
                    HuntingDayDiaryEntryViewModel.Type.HARVEST -> R.drawable.ic_harvest
                    HuntingDayDiaryEntryViewModel.Type.OBSERVATION -> R.drawable.ic_observation
                }

                @ColorRes
                val tintColor: Int = when (entry.acceptStatus) {
                    AcceptStatus.PROPOSED -> R.color.colorProposedEntry
                    AcceptStatus.ACCEPTED -> R.color.colorPrimary
                    AcceptStatus.REJECTED -> throw IllegalStateException("Rejected entries are not supported.")
                }

                entryLayout.findViewById<AppCompatImageView>(R.id.iv_icon)?.let { imageView ->
                    imageView.setImageResource(icon)
                    ImageViewCompat.setImageTintList(
                            imageView,
                            AppCompatResources.getColorStateList(requireContext(), tintColor)
                    )
                }

                entryLayout.setOnClickListener {
                    val intent = when (entry.type) {
                        HuntingDayDiaryEntryViewModel.Type.HARVEST -> {
                            manager.getTargetForHarvest(entry.id)?.let { target ->
                                GroupHarvestActivity.getLaunchIntentForViewing(
                                        packageContext = requireContext(),
                                        groupHuntingHarvestTarget = target,
                                        harvestAcceptStatus = entry.acceptStatus,
                                )
                            }
                        }
                        HuntingDayDiaryEntryViewModel.Type.OBSERVATION -> {
                            manager.getTargetForObservation(entry.id)?.let { target ->
                                GroupObservationActivity.getLaunchIntentForViewing(
                                        packageContext = requireContext(),
                                        groupHuntingObservationTarget = target,
                                        observationAcceptStatus = entry.acceptStatus,
                                )
                            }
                        }
                    }

                    if (intent != null) {
                        // ensure data is refreshed when returning. This way the UI gets updated
                        // if user updates the harvest/observation data
                        refreshHuntingDayData = true
                        startActivity(intent)
                    }
                }

                entryLayout.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutHarvestsAndObservations.addView(entryLayout)
            }
        }

    }

    override fun onPause() {
        super.onPause()

        disposeBag.disposeAll()
    }

    private fun loadHuntingDayIfNotLoaded() {
        val loadStatus = controller.viewModelLoadStatus.value
        if (loadStatus is ViewModelLoadStatus.Loaded && !refreshHuntingDayData) {
            return
        }

        MainScope().launch {
            controller.loadViewModel()
            refreshHuntingDayData = false
        }
    }
}

private fun View.updateValueTextOrDisplayNoData(value: Any?) {
    findViewById<TextView>(R.id.tv_value).text = value?.toString() ?: "-"
}
