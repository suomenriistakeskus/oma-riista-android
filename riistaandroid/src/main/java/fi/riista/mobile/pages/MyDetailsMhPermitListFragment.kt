package fi.riista.mobile.pages

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.android.support.AndroidSupportInjection
import fi.riista.mobile.R
import fi.riista.mobile.adapter.MetsahallitusPermitListItemAdapter
import fi.riista.mobile.models.MetsahallitusPermit
import fi.riista.mobile.utils.AppPreferences
import fi.riista.mobile.viewmodel.MetsahallitusPermitListViewModel
import javax.inject.Inject

class MyDetailsMhPermitListFragment : DialogFragment(), View.OnClickListener {

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var username: String
    private lateinit var languageCode: String

    private lateinit var listViewModel: MetsahallitusPermitListViewModel
    private lateinit var adapter: MetsahallitusPermitListItemAdapter

    private lateinit var toolbar: Toolbar
    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView

    private val permitList = ArrayList<MetsahallitusPermit>()

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { args ->
            args.getString(EXTRA_USERNAME)?.let { username = it }
        }
        languageCode = AppPreferences.getLanguageCodeSetting(context)

        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_my_details_mh_permit_list, container, false)

        toolbar = view.findViewById(R.id.toolbar)
        swipeContainer = view.findViewById(R.id.my_details_mh_permits)
        swipeContainer.setColorSchemeResources(R.color.colorPrimary)

        recyclerView = view.findViewById(R.id.my_details_mh_permit_list)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        adapter = MetsahallitusPermitListItemAdapter(permitList, languageCode)
        adapter.setClickListener(this)
        recyclerView.adapter = adapter

        listViewModel = ViewModelProvider(this, viewModelFactory)
            .get(MetsahallitusPermitListViewModel::class.java)
        listViewModel.setUsername(username)
        listViewModel.metsahallitusPermits.observe(viewLifecycleOwner) { permits ->
            permitList.clear()
            permitList.addAll(permits)

            adapter.notifyDataSetChanged()
        }

        return view
    }

    override fun onClick(view: View) {
        val viewHolder = view.tag as RecyclerView.ViewHolder
        val position = viewHolder.adapterPosition
        val permit = permitList[position]
        val detailsDialog = MyDetailsMhPermitDetailsFragment.newInstance(permit.permitIdentifier)
        val tx = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
        detailsDialog.show(tx, TAG)
    }

    override fun onStart() {
        super.onStart()

        toolbar.setNavigationOnClickListener { dismiss() }

        swipeContainer.setOnRefreshListener {
            listViewModel.triggerUpdate()
            swipeContainer.isRefreshing = false
        }
    }

    override fun onStop() {
        super.onStop()

        // Unregister callbacks that hold references to this Fragment instance.
        toolbar.setNavigationOnClickListener(null)
        swipeContainer.setOnRefreshListener(null)
    }

    override fun onDestroyView() {
        adapter.setClickListener(null)
        recyclerView.adapter = null

        super.onDestroyView()
    }

    companion object {

        const val TAG = "MhPermitListFragment"
        private const val EXTRA_USERNAME = "username"

        fun newInstance(username: String) = MyDetailsMhPermitListFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_USERNAME, username)
            }
        }
    }
}
