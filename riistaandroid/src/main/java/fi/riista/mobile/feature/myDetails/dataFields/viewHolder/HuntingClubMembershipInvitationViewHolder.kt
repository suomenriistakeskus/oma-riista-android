package fi.riista.mobile.feature.myDetails.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.huntingclub.model.HuntingClubMemberInvitationId
import fi.riista.common.domain.huntingclub.ui.HuntingClubViewModel
import fi.riista.mobile.R

class HuntingClubMembershipInvitationViewHolder(
    view: View,
    private val listener: Listener
): RecyclerView.ViewHolder(view) {

    interface Listener {
        fun onAcceptInvitation(invitationId: HuntingClubMemberInvitationId)
        fun onRejectInvitation(invitationId: HuntingClubMemberInvitationId)
    }

    private val officialCodeTextView: TextView = view.findViewById(R.id.tv_official_code)
    private val huntingClubTextView: TextView = view.findViewById(R.id.tv_hunting_club)
    private val acceptButton: Button = view.findViewById(R.id.btn_accept)
    private val rejectButton: Button = view.findViewById(R.id.btn_reject)
    private var invitation: HuntingClubViewModel.Invitation? = null

    fun bind(invitation: HuntingClubViewModel.Invitation) {
        this.invitation = invitation
        officialCodeTextView.text = itemView.context.getString(
            R.string.my_details_customer_number,
            invitation.officialCode
        )
        huntingClubTextView.text = invitation.name

        acceptButton.setOnClickListener {
            listener.onAcceptInvitation(invitation.invitationId)
        }

        rejectButton.setOnClickListener {
            listener.onRejectInvitation(invitation.invitationId)
        }
    }

    companion object {
        fun create(
            layoutInflater: LayoutInflater,
            parent: ViewGroup,
            attachToParent: Boolean,
            listener: Listener
        ): HuntingClubMembershipInvitationViewHolder {
            val view = layoutInflater.inflate(R.layout.item_hunting_club_membership_invitation, parent, attachToParent)
            return HuntingClubMembershipInvitationViewHolder(view, listener)
        }
    }
}
