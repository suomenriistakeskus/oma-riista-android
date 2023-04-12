package fi.riista.mobile.adapter

import android.content.Context
import fi.riista.mobile.models.shootingTest.ShootingTestParticipant
import android.widget.ArrayAdapter
import fi.riista.mobile.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import fi.riista.common.domain.model.ShootingTestType
import fi.riista.mobile.ui.ShootingTestAttemptStateView
import fi.riista.mobile.ui.ShootingTestAttemptStateView.AttemptState

class ShootingTestQueueAdapter(data: List<ShootingTestParticipant?>?, context: Context) :
    ArrayAdapter<ShootingTestParticipant?>(context, R.layout.view_shooting_test_participant, data!!) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            convertView = inflater.inflate(R.layout.view_shooting_test_participant, parent, false)
            viewHolder = ViewHolder()
            viewHolder.participantTitle = convertView.findViewById(R.id.participant_title)
            viewHolder.mooseTestState = convertView.findViewById(R.id.participant_test_type_moose_short)
            viewHolder.bearTestState = convertView.findViewById(R.id.participant_test_type_bear_short)
            viewHolder.roeDeerTestState = convertView.findViewById(R.id.participant_test_type_roedeer_short)
            viewHolder.bowTestState = convertView.findViewById(R.id.participant_test_type_bow_short)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        val item = getItem(position)
        if (item != null) {
            viewHolder.participantTitle.text =
                String.format("%s %s, %s", item.lastName, item.firstName, item.hunterNumber)
            setAttemptsView(
                viewHolder.mooseTestState,
                item,
                R.string.shooting_test_type_moose_short,
                ShootingTestType.MOOSE
            )
            setAttemptsView(
                viewHolder.bearTestState,
                item,
                R.string.shooting_test_type_bear_short,
                ShootingTestType.BEAR
            )
            setAttemptsView(
                viewHolder.roeDeerTestState,
                item,
                R.string.shooting_test_type_roe_deer_short,
                ShootingTestType.ROE_DEER
            )
            setAttemptsView(
                viewHolder.bowTestState,
                item,
                R.string.shooting_test_type_bow_short,
                ShootingTestType.BOW
            )
        }
        return convertView!!
    }

    private fun setAttemptsView(
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
            val intended =
                ShootingTestType.MOOSE === attemptType && item.mooseTestIntended || ShootingTestType.BEAR === attemptType && item.bearTestIntended || ShootingTestType.ROE_DEER === attemptType && item.deerTestIntended || ShootingTestType.BOW === attemptType && item.bowTestIntended
            state = if (intended) AttemptState.INTENDED else AttemptState.NONE
            numberOfAttempts = 0
        }
        view.setState(state, numberOfAttempts, context.getString(stringId))
    }

    private class ViewHolder {
        lateinit var participantTitle: TextView
        lateinit var mooseTestState: ShootingTestAttemptStateView
        lateinit var bearTestState: ShootingTestAttemptStateView
        lateinit var roeDeerTestState: ShootingTestAttemptStateView
        lateinit var bowTestState: ShootingTestAttemptStateView
    }
}
