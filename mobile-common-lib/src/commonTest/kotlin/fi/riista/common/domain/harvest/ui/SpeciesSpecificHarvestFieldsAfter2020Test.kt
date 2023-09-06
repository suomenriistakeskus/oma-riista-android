package fi.riista.common.domain.harvest.ui

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.harvest.ui.fields.CommonHarvestFields
import fi.riista.common.domain.harvest.ui.fields.CommonHarvestFields.Context.Mode
import fi.riista.common.domain.harvest.ui.fields.SpeciesSpecificHarvestFieldsAfter2020
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.GreySealHuntingMethod

internal open class SpeciesSpecificHarvestFieldsAfter2020Test {

    protected val speciesSpecificFields = SpeciesSpecificHarvestFieldsAfter2020()

    protected fun createContext(
        speciesCode: SpeciesCode,
        gender: Gender?,
        age: GameAge?,
        antlersLost: Boolean? = null,
        greySealHuntingMethod: GreySealHuntingMethod? = null,
        mode: Mode,
    ): CommonHarvestFields.Context {
        val harvest = CommonHarvestFieldsTest.getHarvest(speciesCode, gender, age, greySealHuntingMethod, antlersLost)
        val fields = CommonHarvestFieldsTest.getHarvestFields(
            speciesCode = harvest.species.knownSpeciesCodeOrNull()!!
        )

        return fields.createContext(harvest, mode, true)
    }
}
