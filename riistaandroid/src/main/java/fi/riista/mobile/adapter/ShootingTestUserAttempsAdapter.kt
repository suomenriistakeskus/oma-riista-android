package fi.riista.mobile.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import fi.riista.mobile.R
import fi.riista.mobile.activity.ShootingTestEditAttemptActivity
import fi.riista.mobile.activity.ShootingTestUserAttemptsActivity
import fi.riista.mobile.models.shootingTest.ShootingTestAttemptDetailed
import fi.riista.mobile.models.shootingTest.ShootingTestAttemptDetailed.Companion.localisedResultText
import fi.riista.mobile.models.shootingTest.ShootingTestAttemptDetailed.Companion.localizedTypeText

class ShootingTestUserAttempsAdapter(
    data: List<ShootingTestAttemptDetailed?>?,
    private var participantId: Long,
    completed: Boolean,
    private val context: ShootingTestUserAttemptsActivity,
    private val deleteClickedListener: (attemptId: Long) -> Unit,
) : ArrayAdapter<ShootingTestAttemptDetailed?>(
    context, R.layout.view_shooting_test_attempt_item, data!!
) {
    private var participantRev = 0
    private var participantName: String? = null
    private var canEdit: Boolean
    private val onEditClickListener = View.OnClickListener { v ->
        val position = v.tag as Int
        val clickedItem = getItem(position)
        if (clickedItem != null) {
            val intent = Intent(context, ShootingTestEditAttemptActivity::class.java)
            intent.putExtra(ShootingTestEditAttemptActivity.EXTRA_ATTEMPT, clickedItem)
            intent.putExtra(ShootingTestEditAttemptActivity.EXTRA_ATTEMPT_ID, clickedItem.id)
            intent.putExtra(ShootingTestEditAttemptActivity.EXTRA_PARTICIPANT_ID, participantId)
            intent.putExtra(ShootingTestEditAttemptActivity.EXTRA_PARTICIPANT_REV, participantRev)
            intent.putExtra(ShootingTestEditAttemptActivity.EXTRA_PARTICIPANT_NAME, participantName)
            context.setEditTypeLimitsTo(intent, clickedItem.type)
            context.startActivity(intent)
        }
    }
    private val onDeleteClickListener = View.OnClickListener { v ->
        val position = v.tag as Int
        val clickedItem = getItem(position)
        if (clickedItem != null) {
            deleteClickedListener(clickedItem.id)
        }
    }

    init {
        canEdit = !completed
    }

    fun setParticipant(
        participantId: Long,
        participantRev: Int,
        participantName: String?,
        completed: Boolean
    ) {
        this.participantId = participantId
        this.participantRev = participantRev
        this.participantName = participantName
        canEdit = !completed
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        val context = context
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            convertView = inflater.inflate(R.layout.view_shooting_test_attempt_item, parent, false)
            viewHolder = ViewHolder()
            viewHolder.attemptType = convertView.findViewById(R.id.attempt_type)
            viewHolder.result = convertView.findViewById(R.id.attempt_result)
            viewHolder.hits = convertView.findViewById(R.id.attempt_hits)
            viewHolder.editButton = convertView.findViewById(R.id.edit_button)
            viewHolder.deleteButton = convertView.findViewById(R.id.delete_button)
            viewHolder.editButton.setOnClickListener(onEditClickListener)
            viewHolder.deleteButton.setOnClickListener(onDeleteClickListener)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        val item = getItem(position)
        if (item != null) {
            viewHolder.attemptType.text = localizedTypeText(context, item.type)
            viewHolder.result.text = localisedResultText(context, item.result)
            viewHolder.hits.text = item.hits.toString()
            viewHolder.editButton.isEnabled = canEdit
            viewHolder.editButton.tag = position
            viewHolder.deleteButton.isEnabled = canEdit
            viewHolder.deleteButton.tag = position
        }
        return convertView!!
    }

    private class ViewHolder {
        lateinit var attemptType: TextView
        lateinit var result: TextView
        lateinit var hits: TextView
        lateinit var editButton: AppCompatImageButton
        lateinit var deleteButton: AppCompatImageButton
    }
}
