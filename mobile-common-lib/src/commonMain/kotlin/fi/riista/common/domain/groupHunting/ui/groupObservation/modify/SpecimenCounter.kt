package fi.riista.common.domain.groupHunting.ui.groupObservation.modify

import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.HarvestSpecimen

object SpecimenCounter {
    fun adultMaleAmount(specimens: List<HarvestSpecimen>): Int {
        return specimens
            .filter { s -> s.age?.value == GameAge.ADULT }
            .filter { s -> s.gender?.value == Gender.MALE }
            .count()
    }

    fun adultFemaleAmount(specimens: List<HarvestSpecimen>): Int {
        return specimens
            .filter { s -> s.age?.value == GameAge.ADULT }
            .filter { s -> s.gender?.value == Gender.FEMALE }
            .count()
    }

    fun aloneCalfAmount(specimens: List<HarvestSpecimen>): Int {
        return specimens
            .filter { s -> s.age?.value == GameAge.YOUNG }
            .filter { s -> s.alone == true }
            .count()
    }
}
