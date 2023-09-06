package fi.riista.common.domain.groupHunting.ui.groupObservation.modify

import fi.riista.common.domain.harvest.model.CommonHarvestSpecimen
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.toBackendEnum
import kotlin.test.Test
import kotlin.test.assertEquals

class SpecimenCounterTest {

    @Test
    fun testOneAdultMale() {
        val specimen = createCommonHarvestSpecimen(
            gender = Gender.MALE.toBackendEnum(),
            age = GameAge.ADULT.toBackendEnum(),
        )

        assertEquals(1, SpecimenCounter.adultMaleAmount(listOf(specimen)))
        assertEquals(0, SpecimenCounter.adultFemaleAmount(listOf(specimen)))
        assertEquals(0, SpecimenCounter.aloneCalfAmount(listOf(specimen)))
    }

    @Test
    fun testOneAdultFemale() {
        val specimen = createCommonHarvestSpecimen(
            gender = Gender.FEMALE.toBackendEnum(),
            age = GameAge.ADULT.toBackendEnum(),
        )

        assertEquals(0, SpecimenCounter.adultMaleAmount(listOf(specimen)))
        assertEquals(1, SpecimenCounter.adultFemaleAmount(listOf(specimen)))
        assertEquals(0, SpecimenCounter.aloneCalfAmount(listOf(specimen)))
    }

    @Test
    fun testOneAloneCalf() {
        val specimen = createCommonHarvestSpecimen(
            gender = Gender.FEMALE.toBackendEnum(),
            age = GameAge.YOUNG.toBackendEnum(),
            alone = true,
        )

        assertEquals(0, SpecimenCounter.adultMaleAmount(listOf(specimen)))
        assertEquals(0, SpecimenCounter.adultFemaleAmount(listOf(specimen)))
        assertEquals(1, SpecimenCounter.aloneCalfAmount(listOf(specimen)))
    }

    @Test
    fun testOneNotAloneCalf() {
        val specimen = createCommonHarvestSpecimen(
            gender = Gender.FEMALE.toBackendEnum(),
            age = GameAge.YOUNG.toBackendEnum(),
        )

        assertEquals(0, SpecimenCounter.adultMaleAmount(listOf(specimen)))
        assertEquals(0, SpecimenCounter.adultFemaleAmount(listOf(specimen)))
        assertEquals(0, SpecimenCounter.aloneCalfAmount(listOf(specimen)))
    }

    @Test
    fun testOneFemaleTwoCalfs() {
        val mother = createCommonHarvestSpecimen(
            gender = Gender.FEMALE.toBackendEnum(),
            age = GameAge.ADULT.toBackendEnum(),
        )

        val calf1 = createCommonHarvestSpecimen(
            gender = Gender.FEMALE.toBackendEnum(),
            age = GameAge.YOUNG.toBackendEnum(),
        )

        val calf2 = createCommonHarvestSpecimen(
            gender = Gender.MALE.toBackendEnum(),
            age = GameAge.YOUNG.toBackendEnum(),
        )

        assertEquals(0, SpecimenCounter.adultMaleAmount(listOf(mother, calf1, calf2)))
        assertEquals(1, SpecimenCounter.adultFemaleAmount(listOf(mother, calf1, calf2)))
        assertEquals(0, SpecimenCounter.aloneCalfAmount(listOf(mother, calf1, calf2)))
    }

    private fun createCommonHarvestSpecimen(gender: BackendEnum<Gender>, age: BackendEnum<GameAge>, alone: Boolean? = null) =
        CommonHarvestSpecimen(
            id = null,
            rev = null,
            gender = gender,
            age = age,
            antlersLost = null,
            notEdible = null,
            alone = alone,
            weightEstimated = null,
            weightMeasured = null,
            fitnessClass = BackendEnum.create(null),
            antlersType = BackendEnum.create(null),
            antlersWidth = null,
            antlerPointsLeft = null,
            antlerPointsRight = null,
            antlersGirth = null,
            antlersLength = null,
            antlersInnerWidth = null,
            antlerShaftWidth = null,
            additionalInfo = null,
            weight = null,
        )
}
