package fi.riista.mobile.ui.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.google.android.material.button.MaterialButton
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.Species
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.SpeciesField
import fi.riista.mobile.R
import fi.riista.mobile.SpeciesMapping
import fi.riista.mobile.database.SpeciesResolver
import fi.riista.mobile.riistaSdkHelpers.getImageUrl
import fi.riista.mobile.riistaSdkHelpers.loadEntityImage
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder
import fi.riista.mobile.utils.toVisibility

interface SpeciesSelectionLauncher<FieldId : DataFieldId> {
    fun launchSpeciesSelection(fieldId: FieldId, selectableSpecies: SpeciesField.SelectableSpecies)
}

interface EntityImageActionLauncher {
    fun launchEntityImageSelection()
}

class SelectSpeciesAndImageViewHolder<FieldId : DataFieldId>(
    private val speciesResolver: SpeciesResolver,
    private val speciesSelectionLauncher: SpeciesSelectionLauncher<FieldId>,
    private val entityImageActionLauncher: EntityImageActionLauncher?,
    private val entryType: EntryType,
    view: View
) : DataFieldViewHolder<FieldId, SpeciesField<FieldId>>(view) {

    enum class EntryType(@DrawableRes val drawableId: Int) {
        HARVEST(drawableId = R.drawable.ic_species_harvest),
        OBSERVATION(drawableId = R.drawable.ic_species_observation),
        SRVA(drawableId = R.drawable.ic_species_srva),
        ;
    }

    private val speciesButton: MaterialButton = view.findViewById(R.id.btn_species)
    private val speciesImageView: AppCompatImageView = view.findViewById(R.id.iv_species_image)
    private val entityImageButton: AppCompatImageButton = view.findViewById(R.id.btn_entity_image)

    init {
        // enforce rounded corners for the container and image. Clipping requires view to have
        // a background shape that has an outline: https://stackoverflow.com/a/30692466
        speciesImageView.clipToOutline = true
        entityImageButton.clipToOutline = true

        speciesButton.setOnClickListener {
            boundDataField?.let { field ->
                speciesSelectionLauncher.launchSpeciesSelection(
                    fieldId = field.id,
                    selectableSpecies = field.settings.selectableSpecies,
                )
            }
        }

        if (entityImageActionLauncher != null) {
            entityImageButton.setOnClickListener {
                entityImageActionLauncher.launchEntityImageSelection()
            }
        }
    }

    override fun onBeforeUpdateBoundData(dataField: SpeciesField<FieldId>) {
        when (val species = dataField.species) {
            is Species.Known -> displayKnownSpeciesInformation(knownSpecies = species)
            Species.Other -> displayOtherSpeciesInformation()
            Species.Unknown -> displayUnknownSpeciesInformation()
        }

        speciesButton.isEnabled = !dataField.settings.readOnly

        updateEntityImage(
            showImage = dataField.settings.showEntityImage,
            entityImage = dataField.entityImage,
        )
    }

    private fun displayKnownSpeciesInformation(knownSpecies: Species.Known) {
        speciesResolver.findSpecies(knownSpecies.speciesCode)
            ?.let { species ->
                displaySpeciesInformation(species)
            } ?: displayUnknownSpeciesInformation()
    }

    private fun displayOtherSpeciesInformation() {
        speciesImageView.setImageResource(R.drawable.ic_question_mark)
        ImageViewCompat.setImageTintList(
            speciesImageView,
            AppCompatResources.getColorStateList(context, R.color.onPrimary)
        )
        speciesButton.text = context.getString(R.string.species_other)
    }

    private fun displayUnknownSpeciesInformation() {
        speciesImageView.setImageResource(entryType.drawableId)
        ImageViewCompat.setImageTintList(
            speciesImageView,
            AppCompatResources.getColorStateList(context, R.color.onPrimary)
        )
        speciesButton.text = context.getString(R.string.species_prompt)
    }

    private fun displaySpeciesInformation(species: fi.riista.mobile.models.Species) {
        val speciesDrawable = SpeciesMapping.species[species.mId]
        speciesImageView.setImageResource(speciesDrawable)
        ImageViewCompat.setImageTintList(speciesImageView, null)

        speciesButton.text = species.mName
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

        entityImageButton.visibility = showImage.toVisibility()

        if (imageUrl != null) {
            Glide.with(context)
                .asBitmap()
                .loadEntityImage(imageUrl)
                .centerCrop()
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(entityImageButton)
        } else {
            entityImageButton.setImageResource(R.drawable.ic_camera_padded)
        }

    }

    class Factory<FieldId : DataFieldId>(
        private val speciesResolver: SpeciesResolver,
        private val speciesSelectionLauncher: SpeciesSelectionLauncher<FieldId>,
        private val entityImageActionLauncher: EntityImageActionLauncher?,
        private val entryType: EntryType,
    ) : DataFieldViewHolderFactory<FieldId, SpeciesField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.SPECIES_NAME_AND_ICON
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, SpeciesField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_species_name_and_image, container, attachToRoot)
            return SelectSpeciesAndImageViewHolder(
                speciesResolver, speciesSelectionLauncher, entityImageActionLauncher, entryType, view
            )
        }
    }
}
