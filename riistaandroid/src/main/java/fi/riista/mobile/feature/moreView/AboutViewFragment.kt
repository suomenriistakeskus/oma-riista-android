package fi.riista.mobile.feature.moreView

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import fi.riista.mobile.R
import fi.riista.mobile.pages.PageFragment

class AboutViewFragment : PageFragment() {

    private lateinit var adapter: MoreRecyclerViewAdapter
    private lateinit var interactionManager: MoreViewFragment.InteractionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about_view, container, false)
        setupActionBar(R.layout.actionbar_about, false)

        adapter = MoreRecyclerViewAdapter(
            layoutInflater = layoutInflater,
            moreItemClickListener = { type -> interactionManager.moreItemClicked(type) },
        )
        view.findViewById<RecyclerView>(R.id.rv_items).also {
            it.adapter = adapter
            it.addItemDecoration(DividerItemDecoration(it.context, DividerItemDecoration.VERTICAL))
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interactionManager = context as MoreViewFragment.InteractionManager
    }

    override fun onResume() {
        super.onResume()
        adapter.setItems(createItems())
    }

    private fun createItems(): List<MoreItem> = listOfNotNull(
        MoreItem(MoreItemType.PRIVACY_STATEMENT, R.drawable.ic_privacy, getString(R.string.settings_privacy_statement), true),
        MoreItem(MoreItemType.TERMS_OF_SERVICE, R.drawable.ic_terms_of_service, getString(R.string.settings_terms_of_service), true),
        MoreItem(MoreItemType.ACCESSIBILITY, R.drawable.ic_accessibility, getString(R.string.settings_accessibility), true),
        MoreItem(MoreItemType.LICENSES, R.drawable.ic_licenses, getString(R.string.settings_third_party_libraries)),
    )
}
