package fi.riista.mobile.feature.groupHunting.dataFields.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.SpeciesCodeField
import fi.riista.mobile.R
import fi.riista.mobile.SpeciesMapping
import fi.riista.mobile.database.SpeciesResolver
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldViewHolder
import fi.riista.mobile.models.Species

class SpeciesNameAndIconViewHolder<FieldId : DataFieldId>(
    private val speciesResolver: SpeciesResolver,
    view: View
) : DataFieldViewHolder<FieldId, SpeciesCodeField<FieldId>>(view) {

    private val speciesImageView: ImageView = view.findViewById(R.id.iv_species_image)
    private val speciesNameView: TextView = view.findViewById(R.id.tv_species_name)


    override fun onBeforeUpdateBoundData(dataField: SpeciesCodeField<FieldId>) {
        speciesResolver.findSpecies(dataField.speciesCode)
            ?.let { species ->
                displaySpeciesInformation(species)
            } ?: displayInvalidSpecies()
    }

    private fun displaySpeciesInformation(species: Species) {
        val speciesDrawable = SpeciesMapping.species[species.mId]
        speciesImageView.setImageResource(speciesDrawable)
        speciesNameView.text = species.mName
    }

    private fun displayInvalidSpecies() {
        speciesImageView.setImageDrawable(null)
        speciesNameView.text = "-"
    }

    class Factory<FieldId : DataFieldId>(private val speciesResolver: SpeciesResolver)
        : DataFieldViewHolderFactory<FieldId, SpeciesCodeField<FieldId>>(
            viewHolderType = DataFieldViewHolderType.SPECIES_NAME_AND_ICON
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, SpeciesCodeField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_species_code_read_only, container, attachToRoot)
            return SpeciesNameAndIconViewHolder(speciesResolver, view)
        }
    }
}