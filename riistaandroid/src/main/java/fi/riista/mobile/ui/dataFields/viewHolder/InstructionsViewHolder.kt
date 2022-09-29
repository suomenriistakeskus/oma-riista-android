package fi.riista.mobile.ui.dataFields.viewHolder

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.InstructionsField
import fi.riista.common.ui.dataField.InstructionsField.Type
import fi.riista.mobile.R
import fi.riista.mobile.activity.AntlerInstructionsActivity
import fi.riista.mobile.database.SpeciesInformation
import fi.riista.mobile.ui.dataFields.DataFieldViewHolder

class InstructionsViewHolder<FieldId : DataFieldId>(view: View)
    : DataFieldViewHolder<FieldId, InstructionsField<FieldId>>(view) {

    private val buttonView: View = view

    override fun onBeforeUpdateBoundData(dataField: InstructionsField<FieldId>) {
        when (dataField.type) {
            Type.MOOSE_ANTLER_INSTRUCTIONS ->
                configureInstructionsButtonForAntlerInstructions(SpeciesInformation.MOOSE_ID)
            Type.WHITE_TAILED_DEER_ANTLER_INSTRUCTIONS ->
                configureInstructionsButtonForAntlerInstructions(SpeciesInformation.WHITE_TAILED_DEER_ID)
            Type.ROE_DEER_ANTLER_INSTRUCTIONS ->
                configureInstructionsButtonForAntlerInstructions(SpeciesInformation.ROE_DEER_ID)
        }
    }

    private fun configureInstructionsButtonForAntlerInstructions(species: Int) {
        buttonView.setOnClickListener {
            val intent = Intent(context, AntlerInstructionsActivity::class.java)
            intent.putExtra(AntlerInstructionsActivity.EXTRA_SPECIES, species)
            context.startActivity(intent)
        }
    }

    class Factory<FieldId : DataFieldId> : DataFieldViewHolderFactory<FieldId, InstructionsField<FieldId>>(
        viewHolderType = DataFieldViewHolderType.INSTRUCTIONS
    ) {
        override fun createViewHolder(
            layoutInflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DataFieldViewHolder<FieldId, InstructionsField<FieldId>> {
            val view = layoutInflater.inflate(R.layout.item_instructions, container, attachToRoot)
            return InstructionsViewHolder(view)
        }
    }
}
