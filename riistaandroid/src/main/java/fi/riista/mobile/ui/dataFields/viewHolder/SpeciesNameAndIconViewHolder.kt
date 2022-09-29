package fi.riista.mobile.ui.dataFields.viewHolder

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.Species
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.SpeciesField
import fi.riista.mobile.R
import fi.riista.mobile.SpeciesMapping
import fi.riista.mobile.database.SpeciesResolver
import fi.riista.mobile.riistaSdkHelpers.getImageUrl
import fi.riista.mobile.riistaSdkHelpers.loadEntityImage
import fi.riista.mobile.ui.FullScreenEntityImageDialogLauncher
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.utils.toVisibility


class SpeciesNameAndIconViewHolder<FieldId : DataFieldId>(
    private val speciesResolver: SpeciesResolver,
    private val fullscreenDialogLauncher: FullScreenEntityImageDialogLauncher?,
    view: View
) : DataFieldViewHolder<FieldId, SpeciesField<FieldId>>(view) {

    private val speciesContainer: LinearLayout = view.findViewById(R.id.l_species_container)
    private val speciesImageView: AppCompatImageView = view.findViewById(R.id.iv_species_image)
    private val speciesNameView: TextView = view.findViewById(R.id.tv_species_name)
    private val entityImageView: AppCompatImageView = view.findViewById(R.id.iv_entity_image)

    init {
        // enforce rounded corners for the container and image. Clipping requires view to have
        // a background shape that has an outline: https://stackoverflow.com/a/30692466
        speciesContainer.clipToOutline = true
        speciesImageView.clipToOutline = true
        entityImageView.clipToOutline = true

        if (fullscreenDialogLauncher != null) {
            entityImageView.setOnClickListener {
                boundDataField?.entityImage?.let { entityImage ->
                    fullscreenDialogLauncher.showEntityImageInFullscreen(entityImage)
                }
            }
        }
    }

    override fun onBeforeUpdateBoundData(dataField: SpeciesField<FieldId>) {
        when (val species = dataField.species) {
            is Species.Known -> displayKnownSpeciesInformation(knownSpecies = species)
            Species.Other -> displayOtherSpeciesInformation()
            Species.Unknown -> displayUnknownSpeciesInformation()
        }

        updateEntityImage(
            showImage = dataField.settings.showEntityImage,
            entityImage = dataField.entityImage,
        )
    }

    private fun displayKnownSpeciesInformation(knownSpecies: Species.Known) {
        speciesResolver.findSpecies(knownSpecies.speciesCode)
            ?.let { species ->
                displaySpeciesInformation(species)
            } ?: displayInvalidSpecies()
    }

    private fun displayOtherSpeciesInformation() {
        speciesImageView.setImageResource(R.drawable.ic_question_mark)
        ImageViewCompat.setImageTintList(
            speciesImageView,
            AppCompatResources.getColorStateList(context, R.color.colorText)
        )
        speciesNameView.text = context.getString(R.string.species_other)
    }

    private fun displayUnknownSpeciesInformation() {
        speciesImageView.setImageResource(R.drawable.ic_question_mark)
        ImageViewCompat.setImageTintList(
            speciesImageView,
            AppCompatResources.getColorStateList(context, R.color.colorText)
        )
        speciesNameView.text = context.getString(R.string.species_unknown)
    }

    private fun displaySpeciesInformation(species: fi.riista.mobile.models.Species) {
        val speciesDrawable = SpeciesMapping.species[species.mId]
        speciesImageView.setImageResource(speciesDrawable)
        speciesNameView.text = species.mName

        ImageViewCompat.setImageTintList(speciesImageView, null)
    }

    private fun displayInvalidSpecies() {
        speciesImageView.setImageDrawable(null)
        speciesNameView.text = "-"

        ImageViewCompat.setImageTintList(speciesImageView, null)
    }

    private fun updateEntityImage(
        showImage: Boolean,
        entityImage: EntityImage?,
    ) {
        val imageEdgeLen = context.resources.getDimensionPixelSize(R.dimen.attach_image_button_size)
        val imageUrl = entityImage?.getImageUrl(
            widthPixels = imageEdgeLen,
            heightPixels = imageEdgeLen,
            keepAspectRatio = true,
            affix = null,
        )

        if (showImage) {
            speciesContainer.background = ResourcesCompat.getDrawable(
                context.resources, R.drawable.bg_rounded_readonly_4dp, null)
        } else {
            speciesContainer.background = null
        }
        entityImageView.visibility = showImage.toVisibility()

        if (imageUrl != null) {
            ImageViewCompat.setImageTintList(entityImageView, null)

            Glide.with(context)
                .asBitmap()
                .placeholder(R.drawable.ic_image_placeholder_24)
                .error(R.drawable.ic_image_placeholder_24)
                .loadEntityImage(imageUrl)
                .centerCrop()
                .transition(BitmapTransitionOptions.withCrossFade())
                .listener(object: RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        setEntityImageViewBordersVisibility(displayBorders = true)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        setEntityImageViewBordersVisibility(displayBorders = (resource == null))
                        return false
                    }
                })
                .into(entityImageView)
        } else {
            entityImageView.setImageResource(R.drawable.ic_camera_padded)
            ImageViewCompat.setImageTintList(
                entityImageView,
                AppCompatResources.getColorStateList(context, R.color.colorLightGrey)
            )

            setEntityImageViewBordersVisibility(displayBorders = true)
        }

    }

    private fun setEntityImageViewBordersVisibility(displayBorders: Boolean) {
        @DrawableRes
        val bgDrawableResId = when (displayBorders) {
            true -> R.drawable.bg_rounded_readonly_4dp
            false -> R.drawable.bg_rounded_backround
        }

        entityImageView.background = ResourcesCompat.getDrawable(
            context.resources, bgDrawableResId, null)
    }

    class Factory<FieldId : DataFieldId>(
        private val speciesResolver: SpeciesResolver,
        private val fullscreenDialogLauncher: FullScreenEntityImageDialogLauncher? = null,
    ) : DataFieldViewHolderFactory<FieldId, SpeciesField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.SPECIES_NAME_AND_ICON
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, SpeciesField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_species_code_read_only, container, attachToRoot)
            return SpeciesNameAndIconViewHolder(speciesResolver, fullscreenDialogLauncher, view)
        }
    }
}
