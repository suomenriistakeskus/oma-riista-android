package fi.riista.mobile.feature.myDetails.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.huntingclub.ui.HuntingClubViewModel

class HuntingClubMembershipsRecyclerViewAdapter(
    private val layoutInflater: LayoutInflater,
    private val itemListener: HuntingClubMembershipInvitationViewHolder.Listener,
) : ListAdapter<HuntingClubViewModel, RecyclerView.ViewHolder>(HuntingClubDiffCallback()) {

    fun setItems(model: List<HuntingClubViewModel>) {
        submitList(model)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HuntingClubViewModel.Header -> ITEM_VIEW_TYPE_HEADER
            is HuntingClubViewModel.HuntingClub -> ITEM_VIEW_TYPE_HUNTING_CLUB
            is HuntingClubViewModel.Invitation -> ITEM_VIEW_TYPE_INVITATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.create(
                layoutInflater = layoutInflater,
                parent = parent,
                attachToParent = false)
            ITEM_VIEW_TYPE_HUNTING_CLUB -> HuntingClubViewHolder.create(
                layoutInflater = layoutInflater,
                parent = parent,
                attachToParent = false,
            )
            ITEM_VIEW_TYPE_INVITATION -> HuntingClubMembershipInvitationViewHolder.create(
                layoutInflater = layoutInflater,
                parent = parent,
                attachToParent = false,
                listener = itemListener,
            )
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = getItem(position) as HuntingClubViewModel.Header
                holder.bind(header)
            }
            is HuntingClubViewHolder -> {
                val huntingClub = getItem(position) as HuntingClubViewModel.HuntingClub
                holder.bind(huntingClub)
            }
            is HuntingClubMembershipInvitationViewHolder -> {
                val invitation = getItem(position) as HuntingClubViewModel.Invitation
                holder.bind(invitation)
            }
        }
    }

    companion object {
        private const val ITEM_VIEW_TYPE_HEADER = 0
        private const val ITEM_VIEW_TYPE_HUNTING_CLUB = 1
        private const val ITEM_VIEW_TYPE_INVITATION = 2
    }
}

private class HuntingClubDiffCallback : DiffUtil.ItemCallback<HuntingClubViewModel>() {
    override fun areItemsTheSame(oldItem: HuntingClubViewModel, newItem: HuntingClubViewModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: HuntingClubViewModel, newItem: HuntingClubViewModel): Boolean {
        return oldItem == newItem
    }
}
