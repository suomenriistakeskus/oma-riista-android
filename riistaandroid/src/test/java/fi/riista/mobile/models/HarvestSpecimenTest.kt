package fi.riista.mobile.models

import net.andreinc.mockneat.MockNeat
import net.andreinc.mockneat.abstraction.MockUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HarvestSpecimenTest {

    private lateinit var mock: MockNeat
    private lateinit var specimenGenerator: MockUnit<HarvestSpecimen>

    @Before
    fun setUp() {
        mock = MockNeat.threadLocal()

        // Harvest specimen fields are auto-filled using reflection.
        specimenGenerator = mock.reflect(HarvestSpecimen::class.java).useDefaults(true)
    }

    @Test
    fun testCreateCopy() {
        val original = createSpecimen()
        val copy = original.createCopy()

        assertEqualButNotSame(original, copy)
    }

    // Does not return sensible field values but it is not essential in these tests.
    private fun createSpecimen(): HarvestSpecimen {
        return specimenGenerator.`val`()
    }

    companion object {
        internal fun assertEqualButNotSame(first: HarvestSpecimen, second: HarvestSpecimen) {
            // Object identities must differ.
            assertNotSame(first, second)

            assertEquals(first.id, second.id)
            assertEquals(first.rev, second.rev)

            assertEquals(first.age, second.age)
            assertEquals(first.gender, second.gender)

            assertEquals(first.weight, second.weight, 0.00001)
            assertEquals(first.weightEstimated, second.weightEstimated, 0.00001)
            assertEquals(first.weightMeasured, second.weightMeasured, 0.00001)

            assertEquals(first.fitnessClass, second.fitnessClass)
            assertEquals(first.notEdible, second.notEdible)
            assertEquals(first.additionalInfo, second.additionalInfo)

            assertEquals(first.antlersLost, second.antlersLost)
            assertEquals(first.antlersType, second.antlersType)
            assertEquals(first.antlersWidth, second.antlersWidth)
            assertEquals(first.antlerPointsLeft, second.antlerPointsLeft)
            assertEquals(first.antlerPointsRight, second.antlerPointsRight)
            assertEquals(first.antlersGirth, second.antlersGirth)
            assertEquals(first.antlersLength, second.antlersLength)
            assertEquals(first.antlersInnerWidth, second.antlersInnerWidth)
            assertEquals(first.antlerShaftWidth, second.antlerShaftWidth)

            assertEquals(first.alone, second.alone)

            val firstAdditionalProperties: Map<String, Any?>? = first.additionalProperties
            val secondAdditionalProperties: Map<String, Any?> = second.additionalProperties

            assertNotSame(firstAdditionalProperties, secondAdditionalProperties)

            when (firstAdditionalProperties) {
                null -> assertTrue(secondAdditionalProperties.isEmpty())
                else -> assertEquals(firstAdditionalProperties, secondAdditionalProperties)
            }
        }
    }
}
