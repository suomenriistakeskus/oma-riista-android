package fi.riista.common.domain.harvest.ui

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.harvest.ui.fields.CommonHarvestFields.Context.Mode
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.voluntary
import kotlin.test.Test
import kotlin.test.assertEquals


internal class WildBoardSpecificHarvestFieldsAfter2020Test: SpeciesSpecificHarvestFieldsAfter2020Test() {

    private val speciesCode = SpeciesCodes.WILD_BOAR_ID

    @Test
    fun testView() {
        val context = createContext(
            speciesCode = speciesCode,
            gender = null,
            age = null,
            mode = Mode.VIEW,
        )

        with (speciesSpecificFields.getSpeciesSpecificFields(context)) {
            assertEquals(
                expected = listOf(
                    CommonHarvestField.WILD_BOAR_FEEDING_PLACE.voluntary(),
                    CommonHarvestField.GENDER.required(),
                    CommonHarvestField.AGE.required(),
                    CommonHarvestField.WEIGHT_ESTIMATED.voluntary(),
                    CommonHarvestField.WEIGHT_MEASURED.voluntary(),
                ),
                actual = this,
            )
        }
    }
}
