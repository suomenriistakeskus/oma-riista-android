package fi.riista.mobile.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import fi.riista.common.RiistaSDK
import fi.riista.common.util.letWith
import fi.riista.mobile.R
import fi.riista.mobile.RiistaApplication
import fi.riista.mobile.feature.harvest.HarvestActivity
import fi.riista.mobile.feature.observation.ObservationActivity
import fi.riista.mobile.feature.srva.SrvaActivity
import fi.riista.mobile.models.GameLog
import fi.riista.mobile.models.GameLogImage
import fi.riista.mobile.ui.FullScreenImageDialog
import fi.riista.mobile.utils.DiaryImageUtil
import fi.vincit.androidutilslib.view.WebImageView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class GalleryAdapter(private var context: Context?)
    : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    data class GalleryItem(val type: String, val id: Long, val image: GameLogImage)

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val image: WebImageView = view.findViewById(R.id.gallery_item_thumbnail)
        val openBtn: ImageButton = view.findViewById(R.id.gallery_item_open_btn)
        val viewBtn: ImageButton = view.findViewById(R.id.gallery_item_view_btn)
    }

    private var dataSet = ArrayList<GalleryItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_gallery_card_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = this.dataSet[position]

        holder.image.setImageResource(0)

        DiaryImageUtil.setupImage(RiistaApplication.getInstance(), holder.image, item.image, 1024, 1024, true, "b")

        val iconResId: Int? = when (item.type) {
            GameLog.TYPE_HARVEST -> R.drawable.ic_harvest
            GameLog.TYPE_OBSERVATION -> R.drawable.ic_observation
            GameLog.TYPE_SRVA -> R.drawable.ic_srva
            else -> null
        }

        val icon: Drawable? = iconResId?.let {
            context?.resources?.getDrawable(it)
        }?.apply {
            context?.resources?.getColor(R.color.colorPrimary)?.let {
                // Tint white vector asset to green.
                setTint(it)
            }
        }

        holder.image.setOnClickListener { viewImageFull(item) }
        holder.openBtn.setImageDrawable(icon)
        holder.openBtn.setOnClickListener { openLogItem(item) }
        holder.viewBtn.setOnClickListener { viewImageFull(item) }
    }

    override fun getItemCount(): Int {
        return this.dataSet.size
    }

    fun setDataSet(dataSet: ArrayList<GalleryItem>) {
        this.dataSet = dataSet

        notifyDataSetChanged()
    }

    private fun openLogItem(item: GalleryItem) {
        when (item.type) {
            GameLog.TYPE_HARVEST -> {
                val harvestProvider = RiistaSDK.harvestContext.harvestProvider
                if (harvestProvider.harvests.isNullOrEmpty()) {
                    MainScope().launch {
                        harvestProvider.fetch(true)
                        displayHarvest(item.id)
                    }
                } else {
                    displayHarvest(item.id)
                }
            }
            GameLog.TYPE_OBSERVATION -> {
                val observationProvider = RiistaSDK.observationContext.observationProvider
                if (observationProvider.observations.isNullOrEmpty()) {
                    MainScope().launch {
                        observationProvider.fetch(true)
                        displayObservation(item.id)
                    }
                } else {
                    displayObservation(item.id)
                }
            }
            GameLog.TYPE_SRVA -> {
                val srvaEventProvider = RiistaSDK.srvaContext.srvaEventProvider
                if (srvaEventProvider.srvaEvents.isNullOrEmpty()) {
                    MainScope().launch {
                        srvaEventProvider.fetch(true)
                        displaySrvaEvent(item.id)
                    }
                } else {
                    displaySrvaEvent(item.id)
                }
            }
        }
    }

    private fun displayHarvest(localId: Long) {
        val harvestProvider = RiistaSDK.harvestContext.harvestProvider
        harvestProvider.getByLocalId(localId)
            ?.letWith(context) { harvest, context ->
                val intent = HarvestActivity.getLaunchIntentForViewing(context, harvest)
                context.startActivity(intent)
            }
    }

    private fun displayObservation(localId: Long) {
        val observationProvider = RiistaSDK.observationContext.observationProvider
        observationProvider.getByLocalId(localId)
            ?.letWith(context) { observation, context ->
                val intent = ObservationActivity.getLaunchIntentForViewing(context, observation)
                context.startActivity(intent)
            }
    }

    private fun displaySrvaEvent(localId: Long) {
        val srvaEventProvider = RiistaSDK.srvaContext.srvaEventProvider
        srvaEventProvider.getByLocalId(localId)
            ?.letWith(context) { event, context ->
                val intent = SrvaActivity.getLaunchIntentForViewing(context, event)
                context.startActivity(intent)
            }
    }

    private fun viewImageFull(item: GalleryItem) {
        val fragmentTransaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
        val dialog = FullScreenImageDialog.newInstance(item.image)

        dialog.show(fragmentTransaction, FullScreenImageDialog.TAG)
    }
}
