package fi.riista.mobile.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    private var dataSet: List<GalleryItem> = emptyList()

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

        val context = context ?: return

        val icon: Drawable? = iconResId?.let {
            ContextCompat.getDrawable(context, it)
        }?.apply {
            // Tint white vector asset to green.
            setTint(ContextCompat.getColor(context, R.color.colorPrimary))
        }

        holder.image.setOnClickListener { viewImageFull(item) }
        holder.openBtn.setImageDrawable(icon)
        holder.openBtn.setOnClickListener { openLogItem(item) }
        holder.viewBtn.setOnClickListener { viewImageFull(item) }
    }

    override fun getItemCount(): Int {
        return this.dataSet.size
    }

    fun setDataSet(dataSet: List<GalleryItem>) {
        this.dataSet = dataSet

        notifyDataSetChanged()
    }

    private fun openLogItem(item: GalleryItem) {
        when (item.type) {
            GameLog.TYPE_HARVEST -> displayHarvest(item.id)
            GameLog.TYPE_OBSERVATION -> displayObservation(item.id)
            GameLog.TYPE_SRVA -> displaySrvaEvent(item.id)
        }
    }

    private fun displayHarvest(localId: Long) {
        MainScope().launch {
            RiistaSDK.harvestContext.getByLocalId(localId)
                ?.letWith(context) { harvest, context ->
                    val intent = HarvestActivity.getLaunchIntentForViewing(context, harvest)
                    context.startActivity(intent)
                }
        }
    }

    private fun displayObservation(localId: Long) {
        RiistaSDK.observationContext.getByLocalId(localId)
            ?.letWith(context) { observation, context ->
                val intent = ObservationActivity.getLaunchIntentForViewing(context, observation)
                context.startActivity(intent)
            }
    }

    private fun displaySrvaEvent(localId: Long) {
        RiistaSDK.srvaContext.getByLocalId(localId)
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
