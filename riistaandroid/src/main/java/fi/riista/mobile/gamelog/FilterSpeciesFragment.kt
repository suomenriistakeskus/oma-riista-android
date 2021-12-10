package fi.riista.mobile.gamelog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fi.riista.mobile.R
import fi.riista.mobile.database.SpeciesInformation
import fi.riista.mobile.models.Species
import fi.riista.mobile.ui.GameLogFilterView

class FilterSpeciesFragment : DialogFragment(), FilterSpeciesAdapter.Companion.SpeciesSelectionListener {

    var listener: GameLogFilterView.GameLogFilterListener? = null

    private var categoryId: Int = -1
    private var isSrva: Boolean = false

    private lateinit var clearButton: Button
    private lateinit var allButton: Button
    private lateinit var confirmButton: Button

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FilterSpeciesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            categoryId = it.getInt(EXTRA_CATEGORY_ID)
            isSrva = it.getBoolean(EXTRA_IS_SRVA)
        }

        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_filter_species, container)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.title = if (isSrva) {
            getString(R.string.srva)
        } else {
            SpeciesInformation.getCategory(categoryId).mName
        }

        clearButton = view.findViewById(R.id.filter_clear_selections)
        clearButton.setOnClickListener { onClearClick() }
        allButton = view.findViewById(R.id.filter_select_all)
        allButton.setOnClickListener { onAllClick() }
        confirmButton = view.findViewById(R.id.filter_confirm_button)
        confirmButton.setOnClickListener { onConfirmClick() }

        val species: ArrayList<Species> = if (isSrva) {
            SpeciesInformation.srvaSupportedSpecies(true)
        } else {
            SpeciesInformation.getSpeciesForCategory(categoryId)
        }
        adapter = FilterSpeciesAdapter(context, this)
        adapter.setDataSet(species)

        recyclerView = view.findViewById(R.id.filter_species_list)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, DividerItemDecoration.HORIZONTAL))
        recyclerView.adapter = adapter

        return view
    }

    override fun onStart() {
        super.onStart()

        dialog?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT

            it.window?.setLayout(width, height)
        }
    }

    private fun onClearClick() {
        adapter.clearAll()
        adapter.notifyDataSetChanged()
    }

    private fun onAllClick() {
        adapter.selectAll()
    }

    private fun onConfirmClick() {
        listener?.onLogSpeciesSelected(adapter.getSelectedIds())
        dismiss()
    }

    override fun onSelectionsChanged(selectedCodes: List<Int>) {
        if (selectedCodes.isEmpty()) {
            confirmButton.setText(R.string.filter_confirm_no_selected)
        } else {
            confirmButton.text = String.format(getString(R.string.filter_confirm_with_amount), selectedCodes.size)
        }
    }

    companion object {
        const val TAG = "FullScreenSpeciesDialog"
        private const val EXTRA_CATEGORY_ID = "category_id"
        private const val EXTRA_IS_SRVA = "extraIsSrva"

        @JvmStatic
        fun newInstance(categoryId: Int, isSrva: Boolean) = FilterSpeciesFragment().apply {
            arguments = Bundle().apply {
                putInt(EXTRA_CATEGORY_ID, categoryId)
                putBoolean(EXTRA_IS_SRVA, isSrva)
            }
        }
    }
}
