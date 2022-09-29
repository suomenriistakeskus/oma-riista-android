package fi.riista.mobile.feature.groupHunting

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.widget.Button
import android.widget.RadioButton
import fi.riista.common.domain.groupHunting.ui.diary.DiaryFilter
import fi.riista.mobile.R

class DiaryFilterDialog private constructor(
    activity: Activity,
    listener: DiaryFilterDialogListener,
    eventType: DiaryFilter.EventType,
    acceptStatus: DiaryFilter.AcceptStatus
) {
    interface DiaryFilterDialogListener {
        fun filterSelected(eventType: DiaryFilter.EventType, acceptStatus: DiaryFilter.AcceptStatus)
    }

    private lateinit var eventTypeAllBtn: RadioButton
    private lateinit var eventTypeHarvestsBtn: RadioButton
    private lateinit var evenTypeObservationsBtn: RadioButton
    private lateinit var acceptTypeAllBtn: RadioButton
    private lateinit var acceptTypeAcceptedBtn: RadioButton
    private lateinit var acceptTypeRejectedBtn: RadioButton
    private lateinit var acceptTypeProposedBtn: RadioButton

    init {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.group_hunting_diary_filter)
        initButtons(dialog)
        when (eventType) {
            DiaryFilter.EventType.ALL -> eventTypeAllBtn.isChecked = true
            DiaryFilter.EventType.HARVESTS -> eventTypeHarvestsBtn.isChecked = true
            DiaryFilter.EventType.OBSERVATIONS -> evenTypeObservationsBtn.isChecked = true
        }
        when (acceptStatus) {
            DiaryFilter.AcceptStatus.ALL -> acceptTypeAllBtn.isChecked = true
            DiaryFilter.AcceptStatus.ACCEPTED -> acceptTypeAcceptedBtn.isChecked = true
            DiaryFilter.AcceptStatus.REJECTED -> acceptTypeRejectedBtn.isChecked = true
            DiaryFilter.AcceptStatus.PROPOSED -> acceptTypeProposedBtn.isChecked = true
        }
        dialog.findViewById<Button>(R.id.ok_button).setOnClickListener {
            listener.filterSelected(selectedEventType(), selectedAcceptType())
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun initButtons(dialog: Dialog) {
        eventTypeAllBtn = dialog.findViewById(R.id.eventTypeAll)
        eventTypeHarvestsBtn = dialog.findViewById(R.id.eventTypeHarvests)
        evenTypeObservationsBtn = dialog.findViewById(R.id.eventTypeObservations)
        acceptTypeAllBtn = dialog.findViewById(R.id.acceptTypeAll)
        acceptTypeAcceptedBtn = dialog.findViewById(R.id.acceptTypeAccepted)
        acceptTypeRejectedBtn = dialog.findViewById(R.id.acceptTypeRejected)
        acceptTypeProposedBtn = dialog.findViewById(R.id.acceptTypeProposed)

    }

    private fun selectedEventType(): DiaryFilter.EventType {
        return if (eventTypeAllBtn.isChecked) {
            DiaryFilter.EventType.ALL
        } else if (eventTypeHarvestsBtn.isChecked) {
            DiaryFilter.EventType.HARVESTS
        } else {
            DiaryFilter.EventType.OBSERVATIONS
        }
    }

    private fun selectedAcceptType(): DiaryFilter.AcceptStatus {
        return if (acceptTypeAllBtn.isChecked) {
            DiaryFilter.AcceptStatus.ALL
        } else if (acceptTypeAcceptedBtn.isChecked) {
            DiaryFilter.AcceptStatus.ACCEPTED
        } else if (acceptTypeRejectedBtn.isChecked) {
            DiaryFilter.AcceptStatus.REJECTED
        } else {
            DiaryFilter.AcceptStatus.PROPOSED
        }
    }

    companion object {
        fun showDialog(
            activity: Activity,
            listener: DiaryFilterDialogListener,
            eventType: DiaryFilter.EventType,
            acceptStatus: DiaryFilter.AcceptStatus
        ) {
            DiaryFilterDialog(activity, listener, eventType, acceptStatus)
        }
    }
}
