package fi.riista.mobile.feature.myDetails

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.RiistaSDK
import fi.riista.common.huntingclub.HuntingClubMemberInvitationOperationResponse
import fi.riista.common.huntingclub.model.HuntingClubMemberInvitationId
import fi.riista.common.huntingclub.ui.HuntingClubController
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.mobile.R
import fi.riista.mobile.feature.myDetails.dataFields.viewHolder.HuntingClubMembershipInvitationViewHolder
import fi.riista.mobile.feature.myDetails.dataFields.viewHolder.HuntingClubMembershipsRecyclerViewAdapter
import fi.riista.mobile.riistaSdkHelpers.AppLanguageProvider
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MyDetailsHuntingClubMembershipsFragment : DialogFragment(), HuntingClubMembershipInvitationViewHolder.Listener {

    private lateinit var controller: HuntingClubController
    private lateinit var adapter: HuntingClubMembershipsRecyclerViewAdapter
    private lateinit var noContent: TextView
    private lateinit var contentLoaded: RecyclerView
    private val disposeBag = DisposeBag()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_my_details_hunting_club_memberships, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.inflateMenu(R.menu.menu_refresh)
        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.item_refresh -> {
                    loadViewModel(refresh = true)
                    true
                }
                else -> false
            }
        }
        noContent = view.findViewById(R.id.tv_content_not_loaded)
        contentLoaded = view.findViewById(R.id.rv_data_fields)

        controller = HuntingClubController(
            huntingClubsContext = RiistaSDK.currentUserContext.huntingClubsContext,
            languageProvider = AppLanguageProvider(requireContext()),
            stringProvider = ContextStringProviderFactory.createForContext(requireContext()),
        )

        adapter = HuntingClubMembershipsRecyclerViewAdapter(layoutInflater, itemListener = this)
        view.findViewById<RecyclerView>(R.id.rv_data_fields).also {
            it.adapter = adapter
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { viewModelLoadStatus ->
            when (viewModelLoadStatus) {
                ViewModelLoadStatus.NotLoaded -> {
                    noContent.visibility = View.GONE
                    contentLoaded.visibility = View.GONE
                }
                ViewModelLoadStatus.Loading -> {
                    noContent.visibility = View.VISIBLE
                    noContent.text = getString(R.string.loading_content)
                    contentLoaded.visibility = View.GONE
                }
                ViewModelLoadStatus.LoadFailed -> {
                    noContent.visibility = View.VISIBLE
                    noContent.text = getString(R.string.content_loading_failed)
                    contentLoaded.visibility = View.GONE
                }
                is ViewModelLoadStatus.Loaded -> {
                    if (viewModelLoadStatus.viewModel.items.isEmpty()) {
                        noContent.visibility = View.VISIBLE
                        noContent.text = getString(R.string.my_details_no_club_memberships)
                        contentLoaded.visibility = View.GONE

                    } else {
                        noContent.visibility = View.GONE
                        contentLoaded.visibility = View.VISIBLE
                        adapter.setItems(viewModelLoadStatus.viewModel.items)
                    }
                }
            }
        }.disposeBy(disposeBag)

        loadViewModel(refresh = true)
    }

    private fun loadViewModel(refresh: Boolean = false) {
        MainScope().launch {
            controller.loadViewModel(refresh)
        }
    }

    override fun onPause() {
        super.onPause()
        disposeBag.disposeAll()
    }

    override fun onAcceptInvitation(invitationId: HuntingClubMemberInvitationId) {
        MainScope().launch {
            val response = controller.acceptInvitation(invitationId)

            if (!isResumed) {
                return@launch
            }

            if (response is HuntingClubMemberInvitationOperationResponse.Failure) {
                showErrorDialog()
            }
            controller.loadViewModel()
        }
    }

    override fun onRejectInvitation(invitationId: HuntingClubMemberInvitationId) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.group_hunting_are_you_sure))
            .setMessage(R.string.my_details_reject_invitation_question)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(R.string.yes) { _, _ ->
                MainScope().launch {
                    val response = controller.rejectInvitation(invitationId)

                    if (!isResumed) {
                        return@launch
                    }

                    if (response is HuntingClubMemberInvitationOperationResponse.Failure) {
                        showErrorDialog()
                    }
                    controller.loadViewModel()
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.group_hunting_operation_failed)
            .setPositiveButton(R.string.ok, null)
            .create()
            .show()
    }

    companion object {
        const val TAG = "MyDetailsHuntingGroupMembershipsFragment"
    }
}
