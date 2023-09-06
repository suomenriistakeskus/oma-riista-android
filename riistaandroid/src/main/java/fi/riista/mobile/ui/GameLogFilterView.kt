package fi.riista.mobile.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.button.MaterialButton
import fi.riista.common.domain.poi.ui.PoiFilter
import fi.riista.mobile.R
import fi.riista.mobile.database.SpeciesInformation
import fi.riista.mobile.feature.poi.PoiListFragment
import fi.riista.mobile.feature.poi.PoiLocationActivity
import fi.riista.mobile.gamelog.FilterCategoryFragment
import fi.riista.mobile.gamelog.FilterSpeciesFragment
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.utils.AppPreferences
import fi.riista.mobile.utils.Utils
import fi.riista.mobile.utils.toVisibility
import fi.riista.mobile.viewmodel.GameLogViewModel
import java.util.Collections.emptyList

class GameLogFilterView : ConstraintLayout {

    interface GameLogFilterListener {
        fun onLogTypeSelected(type: String)
        fun onLogSeasonSelected(season: Int)
        fun onLogSpeciesSelected(speciesIds: List<Int>)
        fun onLogSpeciesCategorySelected(categoryId: Int)
    }

    private lateinit var typeSpinner: AppCompatSpinner
    private lateinit var seasonSpinner: AppCompatSpinner
    private lateinit var speciesSpinner: View
    private lateinit var speciesText: View
    private lateinit var poiListText: View
    private lateinit var clearButton: MaterialButton

    var listener: GameLogFilterListener? = null
    var centerMapListener: PoiLocationActivity.CenterMapListener? = null
    var poiFilter: PoiFilter? = null
    private var srvaPosition = -1
    private var poiPosition = -1

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.view_log_filter, this)

        typeSpinner = findViewById(R.id.log_filter_type)
        seasonSpinner = findViewById(R.id.log_filter_season)
        speciesSpinner = findViewById(R.id.log_filter_species)
        speciesText = findViewById(R.id.log_filter_species_text)
        poiListText = findViewById(R.id.log_filter_poi_list_text)
        clearButton = findViewById(R.id.log_filter_clear_button)

        speciesSpinner.setOnClickListener {
            val dialog: DialogFragment

            if (isSrvaSelected()) {
                dialog = FilterSpeciesFragment.newInstance(0, true)
                dialog.listener = listener
            } else if (isPoiSelected()) {
                val externalId = AppPreferences.getSelectedClubAreaMapId(context)
                dialog = PoiListFragment.create(
                    externalId = externalId,
                    filter = poiFilter ?: PoiFilter(PoiFilter.PoiFilterType.ALL)
                )
                dialog.listener = centerMapListener
            } else {
                dialog = FilterCategoryFragment.newInstance()
                dialog.listener = listener
            }

            val ft = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
            dialog.show(ft, FilterCategoryFragment.TAG)
        }

        clearButton.setOnClickListener {
            listener?.onLogSpeciesSelected(emptyList())
        }
    }

    fun setupTypes(showSrva: Boolean, showPoi: Boolean, selected: String?) {
        srvaPosition = if (showSrva) { 2 } else { -1 }
        poiPosition = if (!showPoi) { -1 } else if (showSrva) { 3 } else { 2 }

        val items = listOfNotNull(
            resources.getString(R.string.harvest),
            resources.getString(R.string.observation),
            resources.getString(R.string.srva).takeIf { showSrva },
            resources.getString(R.string.poi).takeIf { showPoi },
        )

        val adapter = ArrayAdapter(context, R.layout.view_log_filter_spinner_item, items)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        typeSpinner.adapter = adapter

        val selectedIndex = when (selected) {
            GameLog.TYPE_HARVEST -> 0
            GameLog.TYPE_OBSERVATION -> 1
            GameLog.TYPE_SRVA -> 2
            GameLog.TYPE_POI -> poiPosition
            else -> throw RuntimeException(String.format("Illegal type selection: %s", selected))
        }
        typeSpinner.setSelection(selectedIndex)

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        showPoiListButton(false)
                        listener?.onLogTypeSelected(GameLog.TYPE_HARVEST)
                    }
                    1 -> {
                        showPoiListButton(false)
                        listener?.onLogTypeSelected(GameLog.TYPE_OBSERVATION)
                    }
                    2 -> {
                        if (showSrva) {
                            selectSrva(showSrva)
                        } else {
                            selectPoi(showPoi)
                        }
                    }
                    3 -> {
                        selectPoi(showPoi)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
    }

    private fun selectSrva(showSrva: Boolean) {
        showPoiListButton(false)
        if (View.VISIBLE == showSrva.toVisibility()) {
            listener?.onLogTypeSelected(GameLog.TYPE_SRVA)
        } else {
            typeSpinner.setSelection(0)
        }
    }

    private fun selectPoi(showPoi: Boolean) {
        if (View.VISIBLE == showPoi.toVisibility()) {
            showPoiListButton(true)
            listener?.onLogTypeSelected(GameLog.TYPE_POI)
        } else {
            typeSpinner.setSelection(0)
        }
    }

    private fun showPoiListButton(show: Boolean) {
        poiListText.visibility = show.toVisibility()
        speciesText.visibility = (!show).toVisibility()
        seasonSpinner.visibility = (!show).toVisibility()
    }

    fun setupSeasons(seasons: List<Int>?, selected: Int?) {
        if (seasons == null) {
            Utils.LogMessage("Trying to init season filter with null")
            return
        }

        val items = ArrayList<String>()

        if (isSrvaSelected()) {
            for (season: Int in seasons) {
                items.add(season.toString())
            }
        } else {
            for (season: Int in seasons) {
                items.add("$season - ${season + 1}")
            }
        }

        val adapter = ArrayAdapter(context, R.layout.view_log_filter_spinner_item, items)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        seasonSpinner.adapter = adapter

        // Default to latest season which should be currently ongoing
        var itemIndex = 0
        seasons.forEachIndexed { i, item ->
            if (item == selected) {
                itemIndex = i
            }
        }

        seasonSpinner.setSelection(itemIndex)

        seasonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                listener?.onLogSeasonSelected(seasons[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
    }

    fun setupSpecies(selected: List<Int?>, category: Int?) {
        val textView = findViewById<TextView>(R.id.log_filter_species_text)

        if (category != null) {
            textView.text = SpeciesInformation.getCategory(category).mName
            clearButton.visibility = View.VISIBLE
        } else {
            when {
                selected.isEmpty() -> {
                    textView.text = resources.getString(R.string.species_prompt)
                    clearButton.visibility = View.GONE
                }
                selected.count() == 1 -> {
                    if (selected[0] == null) {
                        textView.text = resources.getString(R.string.srva_other)
                    } else {
                        val species = SpeciesInformation.getSpecies(selected[0])
                        textView.text = species.mName
                    }
                    clearButton.visibility = View.VISIBLE
                }
                else -> {
                    textView.text = String.format("%d %s", selected.count(), resources.getString(R.string.species))
                    clearButton.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun isSrvaSelected(): Boolean = typeSpinner.selectedItemPosition == srvaPosition
    private fun isPoiSelected(): Boolean = typeSpinner.selectedItemPosition == poiPosition
}


fun GameLogFilterView.updateBasedOnViewModel(model: GameLogViewModel, viewLifecycleOwner: LifecycleOwner) {
    model.getSeasonSelected().observe(viewLifecycleOwner) { seasonSelected ->
        setupSeasons(
            seasons = model.getSeasons().value,
            selected = seasonSelected
        )
    }
    model.getSeasons().observe(viewLifecycleOwner) { seasons ->
        setupSeasons(
            seasons = seasons,
            selected = model.getSeasonSelected().value
        )
    }
    model.getSpeciesSelected().observe(viewLifecycleOwner) { selectedSpecies ->
        setupSpecies(
            selected = selectedSpecies ?: listOf(),
            category = model.getCategorySelected().value
        )
    }
    model.getCategorySelected().observe(viewLifecycleOwner) { selectedSpeciesCategory ->
        setupSpecies(
            selected = model.getSpeciesSelected().value ?: listOf(),
            category = selectedSpeciesCategory
        )
    }
}
