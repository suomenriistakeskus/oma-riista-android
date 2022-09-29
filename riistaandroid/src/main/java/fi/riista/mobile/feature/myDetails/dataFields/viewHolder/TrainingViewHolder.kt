package fi.riista.mobile.feature.myDetails.dataFields.viewHolder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.training.ui.TrainingViewModel
import fi.riista.mobile.R
import fi.riista.mobile.riistaSdkHelpers.toJodaLocalDate
import fi.riista.mobile.utils.DATE_FORMAT_FINNISH_SHORT

class TrainingViewHolder(
    view: View,
): RecyclerView.ViewHolder(view) {

    private val trainingTypeTextView: TextView = view.findViewById(R.id.tv_training_type)
    private val trainingDateTextView: TextView = view.findViewById(R.id.tv_training_date)
    private val trainingPlaceTitleTextView: TextView = view.findViewById(R.id.tv_training_place_title)
    private val trainingPlaceTextView: TextView = view.findViewById(R.id.tv_training_place)

    @SuppressLint("SetTextI18n")
    fun bind(training: TrainingViewModel.JhtTraining) {
        trainingTypeTextView.text = "${training.occupationType} (${training.trainingType})"
        trainingDateTextView.text = dateFormat.print(training.date.toJodaLocalDate())
        trainingPlaceTextView.text = training.location
        trainingPlaceTextView.visibility = View.VISIBLE
        trainingPlaceTitleTextView.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    fun bind(training: TrainingViewModel.OccupationTraining) {
        trainingTypeTextView.text = "${training.occupationType} (${training.trainingType})"
        trainingDateTextView.text = dateFormat.print(training.date.toJodaLocalDate())
        trainingPlaceTextView.visibility = View.GONE
        trainingPlaceTitleTextView.visibility = View.GONE
    }

    companion object {
        fun create(
            layoutInflater: LayoutInflater,
            parent: ViewGroup,
            attachToParent: Boolean,
        ): TrainingViewHolder {
            val view = layoutInflater.inflate(R.layout.item_training, parent, attachToParent)
            return TrainingViewHolder(view)
        }

        private val dateFormat by lazy {
            org.joda.time.format.DateTimeFormat.forPattern(DATE_FORMAT_FINNISH_SHORT)
        }
    }
}
