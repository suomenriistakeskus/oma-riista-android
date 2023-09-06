package fi.riista.common.domain.observation.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommonObservationFieldTest {

    @Test
    fun testAlwaysPresentFields() {
        val fields = CommonObservationField.fieldsWithPresence(CommonObservationField.Presence.ALWAYS)
        assertEquals(28, CommonObservationField.values().size) // for ensuring test is updated if new fields are added
        assertEquals(5, fields.size)
        assertTrue(fields.contains(CommonObservationField.LOCATION))
        assertTrue(fields.contains(CommonObservationField.SPECIES_AND_IMAGE))
        assertTrue(fields.contains(CommonObservationField.DATE_AND_TIME))
        assertTrue(fields.contains(CommonObservationField.OBSERVATION_TYPE))
        assertTrue(fields.contains(CommonObservationField.DESCRIPTION))
    }

    @Test
    fun testFieldsDependingOnMetadata() {
        val fields = CommonObservationField.fieldsWithPresence(CommonObservationField.Presence.DEPENDING_ON_METADATA)
        assertEquals(28, CommonObservationField.values().size) // for ensuring test is updated if new fields are added
        assertEquals(19, fields.size)

        assertTrue(fields.contains(CommonObservationField.OBSERVATION_CATEGORY))
        assertTrue(fields.contains(CommonObservationField.WITHIN_MOOSE_HUNTING))
        assertTrue(fields.contains(CommonObservationField.WITHIN_DEER_HUNTING))
        assertTrue(fields.contains(CommonObservationField.DEER_HUNTING_TYPE))
        assertTrue(fields.contains(CommonObservationField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION))
        assertTrue(fields.contains(CommonObservationField.SPECIMEN_AMOUNT))
        assertTrue(fields.contains(CommonObservationField.SPECIMENS))
        assertTrue(fields.contains(CommonObservationField.MOOSE_LIKE_MALE_AMOUNT))
        assertTrue(fields.contains(CommonObservationField.MOOSE_LIKE_FEMALE_AMOUNT))
        assertTrue(fields.contains(CommonObservationField.MOOSE_LIKE_FEMALE_1CALF_AMOUNT))
        assertTrue(fields.contains(CommonObservationField.MOOSE_LIKE_FEMALE_2CALFS_AMOUNT))
        assertTrue(fields.contains(CommonObservationField.MOOSE_LIKE_FEMALE_3CALFS_AMOUNT))
        assertTrue(fields.contains(CommonObservationField.MOOSE_LIKE_FEMALE_4CALFS_AMOUNT))
        assertTrue(fields.contains(CommonObservationField.MOOSE_LIKE_CALF_AMOUNT))
        assertTrue(fields.contains(CommonObservationField.MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT))
        assertTrue(fields.contains(CommonObservationField.TASSU_VERIFIED_BY_CARNIVORE_AUTHORITY))
        assertTrue(fields.contains(CommonObservationField.TASSU_OBSERVER_NAME))
        assertTrue(fields.contains(CommonObservationField.TASSU_OBSERVER_PHONENUMBER))
        assertTrue(fields.contains(CommonObservationField.TASSU_OFFICIAL_ADDITIONAL_INFO))
    }

    @Test
    fun testFieldsDependingOnCurrentValue() {
        val fields = CommonObservationField.fieldsWithPresence(CommonObservationField.Presence.DEPENDING_ON_CURRENT_VALUE)
        assertEquals(28, CommonObservationField.values().size) // for ensuring test is updated if new fields are added
        assertEquals(3, fields.size)

        assertTrue(fields.contains(CommonObservationField.TASSU_IN_YARD_DISTANCE_TO_RESIDENCE))
        assertTrue(fields.contains(CommonObservationField.TASSU_LITTER))
        assertTrue(fields.contains(CommonObservationField.TASSU_PACK))
    }

    @Test
    fun testErrorFields() {
        val fields = CommonObservationField.fieldsWithPresence(CommonObservationField.Presence.ERROR)
        assertEquals(28, CommonObservationField.values().size) // for ensuring test is updated if new fields are added
        assertEquals(1, fields.size)

        assertTrue(fields.contains(CommonObservationField.ERROR_SPECIMEN_AMOUNT_AT_LEAST_TWO))
    }
}
