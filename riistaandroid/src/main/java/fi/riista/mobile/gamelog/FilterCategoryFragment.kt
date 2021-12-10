package fi.riista.mobile.gamelog

import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import fi.riista.mobile.R
import fi.riista.mobile.database.SpeciesInformation
import fi.riista.mobile.models.SpeciesCategory
import fi.riista.mobile.ui.GameLogFilterView

class FilterCategoryFragment : DialogFragment() {

    var listener: GameLogFilterView.GameLogFilterListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_filter_categories, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { dismiss() }

        val categories = SpeciesInformation.getSpeciesCategories()

        val cat1view = view.findViewById<View>(R.id.filter_category_item1)
        setupCategoryView(cat1view, categories, 1)
        val cat2view = view.findViewById<View>(R.id.filter_category_item2)
        setupCategoryView(cat2view, categories, 2)
        val cat3view = view.findViewById<View>(R.id.filter_category_item3)
        setupCategoryView(cat3view, categories, 3)

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

    private fun setupCategoryView(view: View, categories: SparseArray<SpeciesCategory>, categoryId: Int) {
        view.findViewById<TextView>(R.id.filter_category_name).text = categories[categoryId].mName
        view.findViewById<CardView>(R.id.filter_select_category_button).setOnClickListener { onSelectCategoryClicked(categoryId) }
        view.findViewById<CardView>(R.id.filter_select_species_button).setOnClickListener { onOpenSpeciesListClicked(categoryId) }
    }

    private fun onSelectCategoryClicked(categoryId: Int) {
        listener?.onLogSpeciesCategorySelected(categoryId)

        dismiss()
    }

    private fun onOpenSpeciesListClicked(categoryId: Int) {
        val dialog = FilterSpeciesFragment.newInstance(categoryId, false)
        dialog.listener = listener
        val ft = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
        dialog.show(ft, TAG)

        dismiss()
    }

    companion object {
        const val TAG = "FullScreenCategoryDialog"

        @JvmStatic
        fun newInstance() = FilterCategoryFragment().apply {
            arguments = Bundle().apply { }
        }
    }
}
