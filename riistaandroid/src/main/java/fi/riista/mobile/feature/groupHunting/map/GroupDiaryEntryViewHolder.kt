package fi.riista.mobile.feature.groupHunting.map

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.domain.groupHunting.model.AcceptStatus
import fi.riista.common.domain.groupHunting.model.DiaryEntryType
import fi.riista.common.domain.groupHunting.ui.diary.GroupDiaryEntryViewModel
import fi.riista.mobile.R
import fi.riista.mobile.database.SpeciesResolver
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime
import fi.riista.mobile.utils.DateTimeUtils
import fi.riista.mobile.utils.toVisibility
import java.util.*

class GroupDiaryEntryViewHolder(
    private val view: View,
    private val speciesResolver: SpeciesResolver,
    private val listener: Listener
): RecyclerView.ViewHolder(view) {
    interface Listener {
        fun onViewGroupDiaryEntry(entry: GroupDiaryEntryViewModel)
    }

    private val imageViewEntryType = view.findViewById<AppCompatImageView>(R.id.iv_entry_type)
    private val textViewDateTime = view.findViewById<TextView>(R.id.tv_datetime)
    private val textViewSpeciesName = view.findViewById<TextView>(R.id.tv_species_name)
    private val textViewActorName = view.findViewById<TextView>(R.id.tv_actor)

    private var diaryEntry: GroupDiaryEntryViewModel? = null

    init {
        view.setOnClickListener {
            diaryEntry?.let {
                listener.onViewGroupDiaryEntry(it)
            }
        }
    }


    fun bind(diaryEntry: GroupDiaryEntryViewModel) {
        this.diaryEntry = diaryEntry

        indicateDiaryEntryTypeAndAcceptStatus(diaryEntry.type, diaryEntry.acceptStatus)

        textViewDateTime.text = diaryEntry.pointOfTime.toJodaDateTime().let { datetime ->
            val dateText = DateTimeUtils.formatLocalDateUsingShortFinnishFormat(datetime.toLocalDate())
            val timeText = DateTimeUtils.formatTime(datetime)

            // add intentionally multiple spaces between date and time
            "$dateText   $timeText"
        }

        val typeAndStatusPostfix =
            getEntryTypeAndStatusText(diaryEntry.type, diaryEntry.acceptStatus)?.let {
                ", ${it.lowercase(Locale.getDefault())}"
            } ?: ""

        textViewSpeciesName.text = speciesResolver.findSpecies(diaryEntry.speciesCode)?.mName
        @SuppressLint("SetTextI18n")
        textViewActorName.text = diaryEntry.actorName + typeAndStatusPostfix
        textViewActorName.visibility = (diaryEntry.actorName != null).toVisibility()
    }

    private fun indicateDiaryEntryTypeAndAcceptStatus(
        diaryEntryType: DiaryEntryType,
        acceptStatus: AcceptStatus
    ) {
        @DrawableRes
        val icon: Int = when (diaryEntryType) {
            DiaryEntryType.HARVEST -> R.drawable.ic_harvest
            DiaryEntryType.OBSERVATION -> R.drawable.ic_observation
            DiaryEntryType.SRVA -> R.drawable.ic_srva
        }

        @ColorRes
        val tintColor: Int = when (acceptStatus) {
            AcceptStatus.PROPOSED -> R.color.colorProposedEntry
            AcceptStatus.ACCEPTED -> R.color.colorPrimary
            AcceptStatus.REJECTED -> R.color.colorDarkGrey
        }

        imageViewEntryType.setImageResource(icon)
        ImageViewCompat.setImageTintList(
                imageViewEntryType,
                AppCompatResources.getColorStateList(view.context, tintColor)
        )
    }

    private fun getEntryTypeAndStatusText(entryType: DiaryEntryType,
                                          acceptStatus: AcceptStatus): String? {
        @StringRes
        val typeAndStatusTextResId = when (entryType) {
            DiaryEntryType.HARVEST -> when (acceptStatus) {
                AcceptStatus.PROPOSED -> R.string.group_hunting_proposed_harvest
                AcceptStatus.ACCEPTED -> R.string.group_hunting_accepted_harvest
                AcceptStatus.REJECTED -> R.string.group_hunting_rejected_harvest
            }
            DiaryEntryType.OBSERVATION -> when (acceptStatus) {
                AcceptStatus.PROPOSED -> R.string.group_hunting_proposed_observation
                AcceptStatus.ACCEPTED -> R.string.group_hunting_accepted_observation
                AcceptStatus.REJECTED -> R.string.group_hunting_rejected_observation
            }
            DiaryEntryType.SRVA -> return null // not supported
        }

        return view.context.getString(typeAndStatusTextResId)
    }

    companion object {
        fun create(
            layoutInflater: LayoutInflater,
            parent: ViewGroup,
            attachToParent: Boolean,
            speciesResolver: SpeciesResolver,
            listener: Listener
        ): GroupDiaryEntryViewHolder {
            val view = layoutInflater.inflate(R.layout.item_group_diary_entry, parent, attachToParent)
            return GroupDiaryEntryViewHolder(view, speciesResolver, listener)
        }
    }
}