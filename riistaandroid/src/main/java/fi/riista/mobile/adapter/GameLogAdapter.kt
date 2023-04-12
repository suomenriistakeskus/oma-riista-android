package fi.riista.mobile.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.model.indicatorColor
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.model.SrvaEventState
import fi.riista.common.model.toBackendEnum
import fi.riista.common.ui.dataField.IndicatorColor
import fi.riista.mobile.R
import fi.riista.mobile.database.HarvestDatabase.SeasonStats
import fi.riista.mobile.database.SpeciesInformation
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.observation.ObservationStrings
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.ui.GameLogListItem
import fi.riista.mobile.ui.GameLogListItem.OnClickListItemListener
import fi.riista.mobile.utils.DiaryImageUtil.setupImage
import fi.riista.mobile.utils.UiUtils.dipToPixels
import fi.vincit.androidutilslib.view.WebImageView
import java.text.SimpleDateFormat
import java.util.*

class GameLogAdapter(
    private val mContext: Context,
    private var mItems: List<GameLogListItem>,
    private val mListener: OnClickListItemListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private enum class ViewType {
        HEADER, ITEM, STATS
    }

    private val stringProvider = ContextStringProviderFactory.createForContext(mContext)
    private var mSeasonStats: SeasonStats? = null
    override fun getItemCount(): Int {
        return mItems.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = mItems[position]
        if (item.isHeader) {
            return ViewType.HEADER.ordinal
        } else if (item.isStats) {
            return ViewType.STATS.ordinal
        }
        return ViewType.ITEM.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewHolder: RecyclerView.ViewHolder = when (viewType) {
            ViewType.HEADER.ordinal -> {
                val view = inflater.inflate(R.layout.view_log_item_section, parent, false)
                LogHeaderViewHolder(view)
            }
            ViewType.ITEM.ordinal -> {
                val view = inflater.inflate(R.layout.view_log_item, parent, false)
                LogItemViewHolder(view)
            }
            ViewType.STATS.ordinal -> {
                val view = inflater.inflate(R.layout.view_log_stats, parent, false)
                LogStatsViewHolder(view)
            }
            else -> {
                throw RuntimeException("Unknown view type: $viewType")
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = mItems[position]
        if (ViewType.HEADER.ordinal == holder.itemViewType) {
            val text = mContext.resources.getStringArray(R.array.months)[item.month]
            val headerViewHolder = holder as LogHeaderViewHolder
            headerViewHolder.textView.text = text
            headerViewHolder.timeline.visibility =
                if (item.isTimelineTopVisible && item.isTimelineBottomVisible) View.VISIBLE else View.GONE
        } else if (ViewType.ITEM.ordinal == holder.itemViewType) {
            val itemViewHolder = holder as LogItemViewHolder
            itemViewHolder.bind(item, mListener)

            // Reset image to prevent wrong species flashing before loading finishes.
            itemViewHolder.imageView.setImageDrawable(null)
            if (item.images != null && item.images!!.isNotEmpty()) {
                itemViewHolder.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                val edgeLen = mContext.resources.getDimension(R.dimen.log_image_size).toInt()
                setupImage(
                    mContext, itemViewHolder.imageView, item.images!![0], edgeLen, edgeLen, false, null
                )
            } else {
                itemViewHolder.imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                if (GameLog.TYPE_SRVA == item.type && item.mSrva!!.otherSpeciesDescription != null) {
                    val padding = dipToPixels(mContext, 5)
                    itemViewHolder.imageView.setPadding(padding, padding, padding, padding)
                }
                itemViewHolder.imageView.setImageDrawable(
                    SpeciesInformation.getSpeciesImage(
                        mContext,
                        item.speciesCode
                    )
                )
            }
            itemViewHolder.dateTimeView.text = sDateFormat.format(item.dateTime!!.time)
            itemViewHolder.speciesView.text = speciesTitle(item)
            if (GameLog.TYPE_HARVEST == item.type) {
                itemViewHolder.descriptionView.text = null
                itemViewHolder.descriptionView.visibility = View.GONE
                setupHarvestReportState(
                    itemViewHolder.stateImageView,
                    itemViewHolder.stateTextView,
                    itemViewHolder.stateGroup,
                    item.mHarvest
                )
            } else if (GameLog.TYPE_OBSERVATION == item.type) {
                itemViewHolder.descriptionView.text = ObservationStrings.get(
                    mContext,
                    item.mObservation?.observationType?.rawBackendEnumValue
                )
                itemViewHolder.descriptionView.visibility = View.VISIBLE
                itemViewHolder.stateTextView.text = null
                itemViewHolder.stateGroup.visibility = View.GONE
            } else if (GameLog.TYPE_SRVA == item.type) {
                itemViewHolder.descriptionView.text = ObservationStrings.get(
                    mContext,
                    item.mSrva?.eventCategory?.rawBackendEnumValue
                )
                itemViewHolder.descriptionView.visibility = View.VISIBLE
                setupSrvaState(
                    itemViewHolder.stateImageView,
                    itemViewHolder.stateTextView,
                    itemViewHolder.stateGroup,
                    item.mSrva
                )
            }
            itemViewHolder.uploadImageView.visibility = if (item.sent) View.GONE else View.VISIBLE
            itemViewHolder.timelineTop.visibility = if (item.isTimelineTopVisible) View.VISIBLE else View.GONE
            itemViewHolder.timelineBottom.visibility = if (item.isTimelineBottomVisible) View.VISIBLE else View.GONE
        } else if (ViewType.STATS.ordinal == holder.itemViewType) {
            val itemViewHolder = holder as LogStatsViewHolder
            if (mSeasonStats != null) {
                val categories = mSeasonStats!!.mCategoryData
                itemViewHolder.category1Amount.text = categories[1].toString()
                itemViewHolder.category2Amount.text = categories[2].toString()
                itemViewHolder.category3Amount.text = categories[3].toString()
            } else {
                itemViewHolder.category1Amount.text = "0"
                itemViewHolder.category2Amount.text = "0"
                itemViewHolder.category3Amount.text = "0"
            }
        }
    }

    fun setItems(mItems: List<GameLogListItem>) {
        this.mItems = mItems
        notifyDataSetChanged()
    }

    fun setStats(stats: SeasonStats?) {
        mSeasonStats = stats
    }

    private fun speciesTitle(item: GameLogListItem): String? {
        val species = SpeciesInformation.getSpecies(item.speciesCode)
        var title = if (species != null) species.mName else mContext.getString(R.string.srva_other)
        if (title != null && item.totalSpecimenAmount!! > 1) {
            title += String.format(Locale.getDefault(), AMOUNT_SUFFIX_FORMAT, item.totalSpecimenAmount)
        }
        if (species == null && item.mSrva != null && item.mSrva!!.otherSpeciesDescription != null) {
            title += " - " + item.mSrva!!.otherSpeciesDescription
        }
        return title
    }

    private fun setupHarvestReportState(
        stateImage: ImageView,
        stateText: TextView,
        stateGroup: Group,
        event: CommonHarvest?
    ) {
        val trafficLightColor = event?.harvestState?.let { state ->
            stateText.text = stringProvider.getString(state.resourcesStringId)
            when (state.indicatorColor) {
                IndicatorColor.GREEN -> ContextCompat.getColor(mContext, R.color.harvest_approved)
                IndicatorColor.YELLOW -> ContextCompat.getColor(mContext, R.color.harvest_sent_for_approval)
                IndicatorColor.RED -> ContextCompat.getColor(mContext, R.color.harvest_rejected)
                IndicatorColor.INVISIBLE -> Color.TRANSPARENT
            }
        } ?: Color.TRANSPARENT

        if (trafficLightColor != Color.TRANSPARENT) {
            stateImage.setColorFilter(trafficLightColor, PorterDuff.Mode.SRC_ATOP)
            stateGroup.visibility = View.VISIBLE
        } else {
            stateText.text = null
            stateGroup.visibility = View.GONE
        }
    }

    private fun setupSrvaState(
        stateImage: ImageView,
        stateText: TextView,
        stateGroup: Group,
        event: CommonSrvaEvent?
    ) {
        var trafficLightColor = Color.TRANSPARENT
        if (SrvaEventState.APPROVED.toBackendEnum() == event?.state) {
            trafficLightColor = ContextCompat.getColor(mContext, R.color.harvest_approved)
            stateText.setText(R.string.srva_approved)
        } else if (SrvaEventState.REJECTED.toBackendEnum() == event?.state) {
            trafficLightColor = ContextCompat.getColor(mContext, R.color.harvest_rejected)
            stateText.setText(R.string.srva_rejected)
        }
        if (trafficLightColor != Color.TRANSPARENT) {
            stateImage.setColorFilter(trafficLightColor, PorterDuff.Mode.SRC_ATOP)
            stateGroup.visibility = View.VISIBLE
        } else {
            stateText.text = null
            stateGroup.visibility = View.GONE
        }
    }

    class LogHeaderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView
        val timeline: View

        init {
            textView = itemView.findViewById(R.id.log_section_header)
            timeline = itemView.findViewById(R.id.log_section_timeline)
        }
    }

    class LogItemViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: WebImageView
        val dateTimeView: TextView
        val speciesView: TextView
        val descriptionView: TextView
        val stateImageView: ImageView
        val stateTextView: TextView
        val stateGroup: Group
        val uploadImageView: ImageView
        val timelineTop: View
        val timelineBottom: View
        fun bind(item: GameLogListItem?, listener: OnClickListItemListener) {
            itemView.setOnClickListener {
                listener.onItemClick(
                    item!!
                )
            }
        }

        init {
            imageView = itemView.findViewById(R.id.log_item_species_image)
            dateTimeView = itemView.findViewById(R.id.log_item_date)
            speciesView = itemView.findViewById(R.id.log_item_species)
            descriptionView = itemView.findViewById(R.id.log_item_description)
            stateImageView = itemView.findViewById(R.id.log_item_state_image)
            stateTextView = itemView.findViewById(R.id.log_item_state_text)
            stateGroup = itemView.findViewById(R.id.log_item_state_group)
            uploadImageView = itemView.findViewById(R.id.log_item_upload_image)
            timelineTop = itemView.findViewById(R.id.log_item_timeline_top)
            timelineBottom = itemView.findViewById(R.id.log_item_timeline_bottom)
        }
    }

    class LogStatsViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val category1Amount: TextView
        val category2Amount: TextView
        val category3Amount: TextView

        init {
            category1Amount = itemView.findViewById(R.id.stats_category1_amount)
            category2Amount = itemView.findViewById(R.id.stats_category2_amount)
            category3Amount = itemView.findViewById(R.id.stats_category3_amount)
        }
    }

    companion object {
        @SuppressLint("SimpleDateFormat")
        private val sDateFormat = SimpleDateFormat("dd.MM.yyyy   HH:mm")
        private const val AMOUNT_SUFFIX_FORMAT = " (%d)"
    }
}
