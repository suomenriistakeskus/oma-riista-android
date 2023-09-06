package fi.riista.common.domain.groupHunting.ui.groupObservation.modify

import fi.riista.common.domain.harvest.model.CommonHarvestSpecimen
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender

internal object SpecimenCounter {
    fun adultMaleAmount(specimens: List<CommonHarvestSpecimen>): Int {
        return specimens.count { specimen ->
            specimen.age.value == GameAge.ADULT && specimen.gender.value == Gender.MALE
        }
    }

    fun adultFemaleAmount(specimens: List<CommonHarvestSpecimen>): Int {
        return specimens.count { specimen ->
            specimen.age.value == GameAge.ADULT && specimen.gender.value == Gender.FEMALE
        }
    }

    fun aloneCalfAmount(specimens: List<CommonHarvestSpecimen>): Int {
        return specimens.count { specimen ->
            specimen.age.value == GameAge.YOUNG && specimen.alone == true
        }
    }
}
