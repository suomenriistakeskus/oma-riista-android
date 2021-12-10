package fi.riista.mobile.feature.poi

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.RadioButton
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import fi.riista.common.poi.ui.PoiFilter
import fi.riista.mobile.R

class PoiFilterFragment : DialogFragment() {

    private lateinit var filterAllButton: RadioButton
    private lateinit var filterSightingPlaceButton: RadioButton
    private lateinit var filterFeedingPlaceButton: RadioButton
    private lateinit var filterMineralLickButton: RadioButton
    private lateinit var filterOtherButton: RadioButton


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val filter = PoiFilter(PoiFilter.PoiFilterType.valueOf(args.getString(KEY_FILTER, "ALL")))
        return createDialog(filter)
    }

    private fun createDialog(filter: PoiFilter): Dialog {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.poi_filter)
        initButtons(dialog)

        when (filter.poiFilterType) {
            PoiFilter.PoiFilterType.ALL -> filterAllButton.isChecked = true
            PoiFilter.PoiFilterType.SIGHTING_PLACE -> filterSightingPlaceButton.isChecked = true
            PoiFilter.PoiFilterType.FEEDING_PLACE -> filterFeedingPlaceButton.isChecked = true
            PoiFilter.PoiFilterType.MINERAL_LICK -> filterMineralLickButton.isChecked = true
            PoiFilter.PoiFilterType.OTHER -> filterOtherButton.isChecked = true
        }

        dialog.findViewById<Button>(R.id.ok_button).setOnClickListener {
            setFragmentResult(REQUEST_KEY, bundleOf(KEY_FILTER to selectedFilter()))
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }
        return dialog
    }

    private fun initButtons(dialog: Dialog) {
        filterAllButton = dialog.findViewById(R.id.poiFilterAll)
        filterSightingPlaceButton = dialog.findViewById(R.id.poiFilterSightingPlace)
        filterFeedingPlaceButton = dialog.findViewById(R.id.poiFilterFeedingPlace)
        filterMineralLickButton = dialog.findViewById(R.id.poiFilterMineralLick)
        filterOtherButton = dialog.findViewById(R.id.poiFilterOther)
    }

    private fun selectedFilter(): String {
        return when {
            filterSightingPlaceButton.isChecked -> PoiFilter.PoiFilterType.SIGHTING_PLACE.name
            filterFeedingPlaceButton.isChecked -> PoiFilter.PoiFilterType.FEEDING_PLACE.name
            filterMineralLickButton.isChecked -> PoiFilter.PoiFilterType.MINERAL_LICK.name
            filterOtherButton.isChecked -> PoiFilter.PoiFilterType.OTHER.name
            else -> PoiFilter.PoiFilterType.ALL.name
        }
    }

    companion object {
        const val REQUEST_KEY = "PFD_request"
        const val KEY_FILTER = "PFD_filter"

        fun create(filter: PoiFilter): PoiFilterFragment {
            return PoiFilterFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_FILTER, filter.poiFilterType.name)
                }
            }
        }
    }
}
