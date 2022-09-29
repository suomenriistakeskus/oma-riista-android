package fi.riista.mobile.feature.myDetails.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.training.ui.TrainingViewModel

class TrainingsRecyclerViewAdapter(
    private val layoutInflater: LayoutInflater,
) : ListAdapter<TrainingViewModel, RecyclerView.ViewHolder>(TrainingDiffCallback()) {

    fun setItems(model: List<TrainingViewModel>) {
        submitList(model)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TrainingViewHolder.create(
            layoutInflater = layoutInflater,
            parent = parent,
            attachToParent = false,
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TrainingViewHolder) {
            val training = getItem(position) as TrainingViewModel
            if (training is TrainingViewModel.JhtTraining) {
                holder.bind(training)
            } else if (training is TrainingViewModel.OccupationTraining) {
                holder.bind(training)
            }
        }
    }
}

private class TrainingDiffCallback : DiffUtil.ItemCallback<TrainingViewModel>() {
    override fun areItemsTheSame(oldItem: TrainingViewModel, newItem: TrainingViewModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TrainingViewModel, newItem: TrainingViewModel): Boolean {
        return oldItem == newItem
    }
}
