package fi.riista.common.domain.harvest.ui.fields

import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.ui.dataField.FieldSpecification

internal interface SpeciesSpecificHarvestFields {
    /**
     * Gets the species specific fields based on given context.
     *
     * Will return null if there are no species specific fields.
     */
    fun getSpeciesSpecificFields(
        context: CommonHarvestFields.Context
    ): List<FieldSpecification<CommonHarvestField>>?
}
