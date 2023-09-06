package fi.riista.mobile.feature.permits.metsahallitusPermits.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.permit.metsahallitusPermit.model.CommonMetsahallitusPermit
import fi.riista.common.resources.LanguageProvider

class MetsahallitusPermitsAdapter(
    private val layoutInflater: LayoutInflater,
    private val languageProvider: LanguageProvider,
    private val permitClickListener: MetsahallitusPermitViewHolder.Listener,
) : RecyclerView.Adapter<MetsahallitusPermitViewHolder>() {

    private var permitList = listOf<CommonMetsahallitusPermit>()

    fun setPermits(permits: List<CommonMetsahallitusPermit>) {
        DiffUtil.calculateDiff(MetsahallitusPermitDiffCallback(
            oldPermits = this.permitList,
            newPermits = permits
        )).dispatchUpdatesTo(this)

        this.permitList = permits
    }

    override fun getItemCount() = permitList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetsahallitusPermitViewHolder {
        return MetsahallitusPermitViewHolder(
            inflater = layoutInflater,
            parent = parent,
            languageProvider = languageProvider,
            listener = permitClickListener,
        )
    }

    override fun onBindViewHolder(viewHolder: MetsahallitusPermitViewHolder, position: Int) {
        permitList.getOrNull(position)?.let {
            viewHolder.bind(permit = it)
        }
    }
}

private class MetsahallitusPermitDiffCallback(
    private val oldPermits: List<CommonMetsahallitusPermit>,
    private val newPermits: List<CommonMetsahallitusPermit>,
): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldPermits.size
    override fun getNewListSize(): Int = newPermits.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldPermits.getOrNull(oldItemPosition)?.permitIdentifier ==
                newPermits.getOrNull(newItemPosition)?.permitIdentifier

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        // don't compare actual harvests or observations, just the content that is probably
        // visible in the listing
        oldPermits.getOrNull(oldItemPosition) == newPermits.getOrNull(newItemPosition)
}