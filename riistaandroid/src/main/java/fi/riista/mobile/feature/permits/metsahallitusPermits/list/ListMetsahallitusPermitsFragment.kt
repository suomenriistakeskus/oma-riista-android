package fi.riista.mobile.feature.permits.metsahallitusPermits.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.permit.metsahallitusPermit.model.CommonMetsahallitusPermit
import fi.riista.common.domain.permit.metsahallitusPermit.ui.list.ListMetsahallitusPermitsController
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.mobile.R
import fi.riista.mobile.feature.permits.ViewPermitActivity
import fi.riista.mobile.pages.PageFragment
import fi.riista.mobile.riistaSdkHelpers.AppLanguageProvider
import fi.riista.mobile.riistaSdkHelpers.loadViewModelIfNotLoaded
import fi.riista.mobile.ui.RefreshMenuProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ListMetsahallitusPermitsFragment : PageFragment() {

    private lateinit var controller: ListMetsahallitusPermitsController
    private val disposeBag = DisposeBag()

    private lateinit var languageProvider: LanguageProvider

    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MetsahallitusPermitsAdapter

    private val refreshMenuProvider by lazy {
        RefreshMenuProvider {
            reloadPermits(launchedUsingButton = true)
            true
        }
    }

    override fun onAttach(context: Context) {
        languageProvider = AppLanguageProvider(context)
        controller = ListMetsahallitusPermitsController(
            usernameProvider = RiistaSDK.currentUserContext,
            permitProvider = RiistaSDK.metsahallitusPermits,
        )

        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_list_metsahallitus_permits, container, false)

        swipeRefreshLayout = view.findViewById(R.id.srl_permits_layout)
        swipeRefreshLayout?.setColorSchemeResources(R.color.colorPrimary)

        recyclerView = view.findViewById(R.id.rv_permits)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        adapter = MetsahallitusPermitsAdapter(
            layoutInflater = layoutInflater,
            languageProvider = languageProvider,
            permitClickListener = object : MetsahallitusPermitViewHolder.Listener {
                override fun onMetsahallitusPermitClicked(permit: CommonMetsahallitusPermit) {
                    viewPermit(permit)
                }
            }
        )
        recyclerView.adapter = adapter

        requireActivity().addMenuProvider(refreshMenuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)

        return view
    }

    private fun viewPermit(permit: CommonMetsahallitusPermit) {
        val intent = ViewPermitActivity.getIntentForViewing(
            context = requireContext(),
            permit = permit
        )
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()

        swipeRefreshLayout?.setOnRefreshListener {
            reloadPermits(launchedUsingButton = false)
        }
    }

    override fun onStop() {
        super.onStop()
        swipeRefreshLayout?.setOnRefreshListener(null)
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { viewModelLoadStatus ->
            when (viewModelLoadStatus) {
                ViewModelLoadStatus.NotLoaded,
                ViewModelLoadStatus.Loading -> {
                    // nop
                }
                ViewModelLoadStatus.LoadFailed -> {
                    onViewModelLoadCompleted()
                }
                is ViewModelLoadStatus.Loaded -> {
                    onViewModelLoadCompleted()

                    val viewModel = viewModelLoadStatus.viewModel
                    adapter.setPermits(permits = viewModel.permits)
                }
            }
        }.disposeBy(disposeBag)

        controller.loadViewModelIfNotLoaded {
            swipeRefreshLayout?.isEnabled = false
            refreshMenuProvider.setCanRefresh(canRefresh = false)
        }
    }

    private fun reloadPermits(launchedUsingButton: Boolean) {
        refreshMenuProvider.setCanRefresh(canRefresh = false)
        if (launchedUsingButton) {
            swipeRefreshLayout?.post { swipeRefreshLayout?.isRefreshing = true }
            swipeRefreshLayout?.isEnabled = false
        }

        MainScope().launch {
            // delay the actual loading by half a second. Typically loading occurs within few hundred milliseconds
            // and thus the UI just flashes the loading indicator. By delaying the loading the user gets a chance
            // to actually see the indicator and experience that "app is actually now doing something"
            delay(500)
            controller.loadViewModel(refresh = true)
        }
    }

    private fun onViewModelLoadCompleted() {
        swipeRefreshLayout?.isRefreshing = false
        swipeRefreshLayout?.isEnabled = true
        refreshMenuProvider.setCanRefresh(canRefresh = true)
    }

    override fun onPause() {
        disposeBag.disposeAll()
        super.onPause()
    }

    override fun onDestroyView() {
        recyclerView.adapter = null
        super.onDestroyView()
    }
}
