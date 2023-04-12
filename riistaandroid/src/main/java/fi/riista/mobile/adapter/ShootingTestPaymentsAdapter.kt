package fi.riista.mobile.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageButton
import fi.riista.common.domain.model.ShootingTestType
import fi.riista.mobile.R
import fi.riista.mobile.activity.ShootingTestEditPaymentActivity
import fi.riista.mobile.models.shootingTest.ShootingTestParticipant
import fi.riista.mobile.ui.ShootingTestAttemptStateView
import fi.riista.mobile.ui.ShootingTestAttemptStateView.AttemptState
import fi.riista.mobile.viewmodel.ShootingTestMainViewModel

class ShootingTestPaymentsAdapter(
    context: Context,
    data: List<ShootingTestParticipant?>,
    val viewModel: ShootingTestMainViewModel,
    completeAllPaymentsListener: (ShootingTestParticipant) -> Unit,
) : ArrayAdapter<ShootingTestParticipant?>(context, R.layout.view_shooting_test_payment_item, data) {

    private var canEdit = false

    private val onEditClickListener = View.OnClickListener { v: View ->
        val position = v.tag as Int
        val participant = getItem(position)
        if (participant != null) {
            val intent = Intent(getContext(), ShootingTestEditPaymentActivity::class.java)
            intent.putExtra(ShootingTestEditPaymentActivity.EXTRA_PARTICIPANT_ID, participant.id)
            context.startActivity(intent)
        }
    }
    private val onDoneClickListener = View.OnClickListener { v: View ->
        val position = v.tag as Int
        val participant = getItem(position)
        if (participant != null) {
            completeAllPaymentsListener(participant)
        }
    }

    fun setEditEnabled(canEdit: Boolean) {
        this.canEdit = canEdit
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            convertView = inflater.inflate(R.layout.view_shooting_test_payment_item, parent, false)
            viewHolder = ViewHolder()
            viewHolder.participantTitle = convertView.findViewById(R.id.item_title_text)
            viewHolder.participantStatus = convertView.findViewById(R.id.participant_status)
            viewHolder.mooseAttemptsView = convertView.findViewById(R.id.attempts_moose_view)
            viewHolder.bearAttemptsView = convertView.findViewById(R.id.attempts_bear_view)
            viewHolder.roeDeerAttemptsView = convertView.findViewById(R.id.attempts_roedeer_view)
            viewHolder.bowAttemptsView = convertView.findViewById(R.id.attempts_bow_view)
            viewHolder.paymentTotal = convertView.findViewById(R.id.payment_total_amount)
            viewHolder.paymentPaid = convertView.findViewById(R.id.payment_paid_amount)
            viewHolder.paymentRemaining = convertView.findViewById(R.id.payment_remaining_amount)
            viewHolder.markDoneButton = convertView.findViewById(R.id.finish_payment_btn)
            viewHolder.markDoneButton.setOnClickListener(onDoneClickListener)
            viewHolder.editPaymentButton = convertView.findViewById(R.id.edit_payment_btn)
            viewHolder.editPaymentButton.setOnClickListener(onEditClickListener)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        val item = getItem(position)
        if (item != null) {
            viewHolder.participantTitle.text =
                String.format("%s %s, %s", item.lastName, item.firstName, item.hunterNumber)
            viewHolder.participantStatus.visibility = if (item.completed) View.GONE else View.VISIBLE
            setupAttemptsView(
                viewHolder.mooseAttemptsView,
                item,
                R.string.shooting_test_type_moose_short,
                ShootingTestType.MOOSE
            )
            setupAttemptsView(
                viewHolder.bearAttemptsView,
                item,
                R.string.shooting_test_type_bear_short,
                ShootingTestType.BEAR
            )
            setupAttemptsView(
                viewHolder.roeDeerAttemptsView,
                item,
                R.string.shooting_test_type_roe_deer_short,
                ShootingTestType.ROE_DEER
            )
            setupAttemptsView(
                viewHolder.bowAttemptsView,
                item,
                R.string.shooting_test_type_bow_short,
                ShootingTestType.BOW
            )
            viewHolder.paymentTotal.text = String.format("%S €", item.totalDueAmount)
            viewHolder.paymentPaid.text = String.format("%s €", item.paidAmount)
            viewHolder.paymentRemaining.text = String.format("%s €", item.remainingAmount)
            viewHolder.markDoneButton.tag = position
            viewHolder.markDoneButton.isEnabled = canEdit && !item.completed
            viewHolder.editPaymentButton.tag = position
            viewHolder.editPaymentButton.isEnabled = canEdit
        }
        return convertView!!
    }

    private fun setupAttemptsView(
        view: ShootingTestAttemptStateView,
        item: ShootingTestParticipant,
        @StringRes stringId: Int,
        attemptType: ShootingTestType
    ) {
        val attemptSummary = item.getAttemptSummaryFor(attemptType)
        val state: AttemptState
        val numberOfAttempts: Int
        if (attemptSummary != null) {
            state = if (attemptSummary.qualified) AttemptState.PASS else AttemptState.FAIL
            numberOfAttempts = attemptSummary.attemptCount
        } else {
            state = AttemptState.NONE
            numberOfAttempts = 0
        }
        view.setState(state, numberOfAttempts, context.getString(stringId))
    }

    private class ViewHolder {
        lateinit var participantTitle: TextView
        lateinit var participantStatus: TextView
        lateinit var mooseAttemptsView: ShootingTestAttemptStateView
        lateinit var bearAttemptsView: ShootingTestAttemptStateView
        lateinit var roeDeerAttemptsView: ShootingTestAttemptStateView
        lateinit var bowAttemptsView: ShootingTestAttemptStateView
        lateinit var paymentTotal: TextView
        lateinit var paymentPaid: TextView
        lateinit var paymentRemaining: TextView
        lateinit var markDoneButton: AppCompatImageButton
        lateinit var editPaymentButton: AppCompatImageButton
    }
}
