package fi.riista.common.domain.harvest.ui

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.harvest.model.HarvestReportingType
import fi.riista.common.domain.harvest.ui.fields.HarvestSpecimenFieldRequirementResolver
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class HarvestSpecimenFieldRequirementResolverTest {

    @Test
    fun testBasic() {
        listOf(
            CommonHarvestField.AGE,
            CommonHarvestField.GENDER,
            CommonHarvestField.WEIGHT,
        ).forEach { field ->
            ALL_SPECIES.forEach { speciesCode ->
                assertFalse(
                    actual = HarvestSpecimenFieldRequirementResolver.isFieldRequired(
                        specimenField = field,
                        speciesCode = speciesCode,
                        harvestReportingType = HarvestReportingType.BASIC
                    ),
                    message = "$field should not be required for species $speciesCode"
                )
            }
        }
    }

    @Test
    fun testAgeForPermit() {
        val field = CommonHarvestField.AGE
        val speciesForWhichShouldBeRequired = HarvestSpecimenFieldRequirementResolver.PERMIT_MANDATORY_AGE

        speciesForWhichShouldBeRequired.forEach { speciesCode ->
            assertTrue(
                actual = HarvestSpecimenFieldRequirementResolver.isFieldRequired(
                    specimenField = field,
                    speciesCode = speciesCode,
                    harvestReportingType = HarvestReportingType.PERMIT
                ),
                message = "$field should be required for species $speciesCode"
            )
        }

        otherSpecies(speciesForWhichShouldBeRequired).forEach { speciesCode ->
            assertFalse(
                actual = HarvestSpecimenFieldRequirementResolver.isFieldRequired(
                    specimenField = field,
                    speciesCode = speciesCode,
                    harvestReportingType = HarvestReportingType.PERMIT
                ),
                message = "$field should not be required for species $speciesCode"
            )
        }
    }

    @Test
    fun testGenderForPermit() {
        val field = CommonHarvestField.GENDER
        val speciesForWhichShouldBeRequired = HarvestSpecimenFieldRequirementResolver.PERMIT_MANDATORY_GENDER

        speciesForWhichShouldBeRequired.forEach { speciesCode ->
            assertTrue(
                actual = HarvestSpecimenFieldRequirementResolver.isFieldRequired(
                    specimenField = field,
                    speciesCode = speciesCode,
                    harvestReportingType = HarvestReportingType.PERMIT
                ),
                message = "$field should be required for species $speciesCode"
            )
        }

        otherSpecies(speciesForWhichShouldBeRequired).forEach { speciesCode ->
            assertFalse(
                actual = HarvestSpecimenFieldRequirementResolver.isFieldRequired(
                    specimenField = field,
                    speciesCode = speciesCode,
                    harvestReportingType = HarvestReportingType.PERMIT
                ),
                message = "$field should not be required for species $speciesCode"
            )
        }
    }

    @Test
    fun testWeightForPermit() {
        val field = CommonHarvestField.WEIGHT
        val speciesForWhichShouldBeRequired = HarvestSpecimenFieldRequirementResolver.PERMIT_MANDATORY_WEIGHT

        speciesForWhichShouldBeRequired.forEach { speciesCode ->
            assertTrue(
                actual = HarvestSpecimenFieldRequirementResolver.isFieldRequired(
                    specimenField = field,
                    speciesCode = speciesCode,
                    harvestReportingType = HarvestReportingType.PERMIT
                ),
                message = "$field should be required for species $speciesCode"
            )
        }

        otherSpecies(speciesForWhichShouldBeRequired).forEach { speciesCode ->
            assertFalse(
                actual = HarvestSpecimenFieldRequirementResolver.isFieldRequired(
                    specimenField = field,
                    speciesCode = speciesCode,
                    harvestReportingType = HarvestReportingType.PERMIT
                ),
                message = "$field should not be required for species $speciesCode"
            )
        }
    }

    @Test
    fun testSeason() {
        val speciesForWhichShouldBeRequired = HarvestSpecimenFieldRequirementResolver.SEASON_COMMON_MANDATORY

        listOf(
            CommonHarvestField.AGE,
            CommonHarvestField.GENDER,
            CommonHarvestField.WEIGHT,
        ).forEach { field ->
            speciesForWhichShouldBeRequired.forEach { speciesCode ->
                assertTrue(
                    actual = HarvestSpecimenFieldRequirementResolver.isFieldRequired(
                        specimenField = field,
                        speciesCode = speciesCode,
                        harvestReportingType = HarvestReportingType.SEASON
                    ),
                    message = "$field should be required for species $speciesCode"
                )
            }

            otherSpecies(speciesForWhichShouldBeRequired).forEach { speciesCode ->
                assertFalse(
                    actual = HarvestSpecimenFieldRequirementResolver.isFieldRequired(
                        specimenField = field,
                        speciesCode = speciesCode,
                        harvestReportingType = HarvestReportingType.SEASON
                    ),
                    message = "$field should not be required for species $speciesCode"
                )
            }
        }
    }

    private fun otherSpecies(excludesSpecies: Set<SpeciesCode>): Set<SpeciesCode> =
        ALL_SPECIES - excludesSpecies

    companion object {
        // from: android's species.json
        private val ALL_SPECIES = setOf(
            47348, 46615, 47503, 47507, 50106, 50386, 48089, 48251, 48250, 48537, 50336, 46549, 46542, 46587,
            47329, 47230, 47240, 47169, 47223, 47243, 47212, 200555, 47305, 47282, 47926, 47484, 47476, 47479,
            47629, 200556, 47774, 53004, 27048, 26298, 26291, 26287, 26373, 26366, 26360, 26382, 26388, 26394,
            26407, 26415, 26419, 26427, 26435, 26440, 26442, 26921, 26922, 26931, 26926, 26928, 27152, 27381,
            27649, 27911, 50114, 46564, 47180, 37178, 37166, 37122, 37142, 27750, 27759, 200535, 33117,
        )
    }
}
