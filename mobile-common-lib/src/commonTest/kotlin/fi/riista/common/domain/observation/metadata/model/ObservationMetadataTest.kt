package fi.riista.common.domain.observation.metadata.model

import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.ObservationCategory
import fi.riista.common.domain.model.ObservationType
import fi.riista.common.domain.observation.metadata.dto.ObservationMetadataDTO
import fi.riista.common.domain.observation.metadata.dto.toObservationMetadata
import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.domain.observation.model.ObservationSpecimenField
import fi.riista.common.domain.observation.model.ObservationSpecimenMarking
import fi.riista.common.domain.observation.model.ObservationSpecimenState
import fi.riista.common.model.toBackendEnum
import fi.riista.common.util.deserializeFromJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ObservationMetadataTest {

    @Test
    fun testRootLevelParsing() {
        val metadata = """
            {
              "lastModified" : "2020-03-18T11:11:47.703+0200",
              "speciesList" : [],
              "observationSpecVersion": 4
            }
        """.trimIndent().deserializeFromJson<ObservationMetadataDTO>()?.toObservationMetadata()

        assertNotNull(metadata)
        assertEquals("2020-03-18T11:11:47.703+0200", metadata.lastModified)
        assertEquals(4, metadata.observationSpecVersion)
    }


    @Test
    fun testSpeciesParsing() {
        val metadata = """
            {
                "lastModified": "2020-03-18T11:11:47.703+0200",
                "speciesList": [
                    {
                        "gameSpeciesCode": 46549,
                        "baseFields": {
                            "withinMooseHunting": "YES"
                        },
                        "specimenFields": {
                            "widthOfPaw": "VOLUNTARY"
                        },
                        "contextSensitiveFieldSets": [
                            {
                                "category": "MOOSE_HUNTING",
                                "type": "NAKO",
                                "baseFields": {
                                    "amount": "YES"
                                },
                                "specimenFields": {
                                    "age": "YES",
                                    "gender": "VOLUNTARY",
                                    "state": "VOLUNTARY_CARNIVORE_AUTHORITY",
                                    "marking": "NO",
                                    "lengthOfPaw": "NO"
                                },
                                "allowedAges": ["ADULT","LT1Y","_1TO2Y","UNKNOWN"],
                                "allowedStates": ["HEALTHY","ILL","WOUNDED","CARCASS","DEAD"],
                                "allowedMarkings": ["NOT_MARKED","COLLAR_OR_RADIO_TRANSMITTER","EARMARK"]
                            }
                        ]
                    }
                ],
                "observationSpecVersion": 4
            }
        """.trimIndent().deserializeFromJson<ObservationMetadataDTO>()?.toObservationMetadata()

        assertNotNull(metadata)
        assertEquals(1, metadata.speciesMetadata.size)
        assertEquals(46549, metadata.speciesMetadata.entries.first().key)
        with (metadata.speciesMetadata[46549]!!) {
            assertEquals(46549, speciesCode)

            assertEquals(1, observationFields.size)
            assertEquals(ObservationFieldRequirement.YES, observationFields[CommonObservationField.WITHIN_MOOSE_HUNTING])

            assertEquals(1, specimenFields.size)
            assertEquals(ObservationFieldRequirement.VOLUNTARY, specimenFields[ObservationSpecimenField.WIDTH_OF_PAW])

            assertEquals(1, contextSensitiveFieldSets.size)
            with (contextSensitiveFieldSets.first()) {
                assertEquals(ObservationCategory.MOOSE_HUNTING.toBackendEnum(), observationCategory)
                assertEquals(ObservationType.NAKO.toBackendEnum(), observationType)

                assertEquals(1, observationFields.size)
                assertEquals(ObservationFieldRequirement.YES, observationFields[CommonObservationField.SPECIMEN_AMOUNT])

                assertEquals(5, specimenFields.size)
                assertEquals(ObservationFieldRequirement.YES, specimenFields[ObservationSpecimenField.AGE])
                assertEquals(ObservationFieldRequirement.VOLUNTARY, specimenFields[ObservationSpecimenField.GENDER])
                assertEquals(ObservationFieldRequirement.VOLUNTARY_CARNIVORE_AUTHORITY, specimenFields[ObservationSpecimenField.STATE_OF_HEALTH])
                assertEquals(ObservationFieldRequirement.NO, specimenFields[ObservationSpecimenField.MARKING])
                assertEquals(ObservationFieldRequirement.NO, specimenFields[ObservationSpecimenField.LENGTH_OF_PAW])

                assertEquals(4, allowedAges.size)
                listOf(
                    GameAge.ADULT, GameAge.LESS_THAN_ONE_YEAR,
                    GameAge.BETWEEN_ONE_AND_TWO_YEARS, GameAge.UNKNOWN
                ).forEach { assertTrue(allowedAges.contains(it.toBackendEnum())) }

                assertEquals(5, allowedStates.size)
                listOf(
                    ObservationSpecimenState.HEALTHY, ObservationSpecimenState.ILL,
                    ObservationSpecimenState.WOUNDED, ObservationSpecimenState.CARCASS,
                    ObservationSpecimenState.DEAD,
                ).forEach { assertTrue(allowedStates.contains(it.toBackendEnum())) }

                assertEquals(3, allowedMarkings.size)
                listOf(
                    ObservationSpecimenMarking.NOT_MARKED,
                    ObservationSpecimenMarking.COLLAR_OR_RADIO_TRANSMITTER,
                    ObservationSpecimenMarking.EARMARK,
                ).forEach { assertTrue(allowedMarkings.contains(it.toBackendEnum())) }
            }
        }
    }
}
