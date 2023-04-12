package fi.riista.mobile.feature.moreView.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.riista.mobile.R
import fi.riista.mobile.feature.moreView.MoreItem
import fi.riista.mobile.feature.moreView.MoreItemType
import fi.riista.mobile.utils.toVisibility

typealias MoreItemClickedListener = (MoreItemType) -> Unit

class MoreViewHolder(
    view: View,
    private val listener: MoreItemClickedListener
): RecyclerView.ViewHolder(view) {

    private var type: MoreItemType? = null
    private val iconImageView: ImageView = view.findViewById(R.id.iv_icon)
    private val titleTextView: TextView = view.findViewById(R.id.tv_title)
    private val opensInBrowserImageView: ImageView = view.findViewById(R.id.iv_opensInBrowser)

    init {
        view.setOnClickListener {
            type?.let {
                listener(it)
            }
        }
    }

    fun bind(item: MoreItem) {
        type = item.type
        iconImageView.setImageResource(item.iconResource)
        titleTextView.text = item.title
        opensInBrowserImageView.visibility = item.opensInBrowser.toVisibility()
    }

    companion object {
        fun create(
            layoutInflater: LayoutInflater,
            parent: ViewGroup,
            attachToParent: Boolean,
            listener: MoreItemClickedListener,
        ): MoreViewHolder {
            val view = layoutInflater.inflate(R.layout.item_more, parent, attachToParent)
            return MoreViewHolder(view, listener)
        }
    }
}
