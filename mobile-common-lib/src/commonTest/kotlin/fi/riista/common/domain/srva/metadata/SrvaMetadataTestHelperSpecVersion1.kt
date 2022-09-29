package fi.riista.common.domain.srva.metadata

import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.srva.metadata.model.SrvaMetadata
import fi.riista.common.domain.srva.model.SrvaEventCategoryType
import fi.riista.common.domain.srva.model.SrvaEventResult
import fi.riista.common.domain.srva.model.SrvaEventType
import fi.riista.common.domain.srva.model.SrvaMethodType
import kotlin.test.assertEquals
import kotlin.test.assertTrue


object SrvaMetadataTestHelperSpecVersion1 {
    fun testMetadataHasSpecies(metadata: SrvaMetadata) {
        val speciesCodes = listOf(
            47503, 47629, 47507, 200556, 47484, 47926, 46615, 47348, 46549, 47212,
        )
        assertEquals(speciesCodes.count(), metadata.species.count())
        speciesCodes.forEach {
            assertTrue(metadata.species.contains(Species.Known(it)), "$it is missing")
        }
    }

    fun testMetadataHasAges(metadata: SrvaMetadata) {
        assertEquals(3, metadata.ages.count())
        assertEquals(GameAge.ADULT, metadata.ages[0].value)
        assertEquals(GameAge.YOUNG, metadata.ages[1].value)
        assertEquals(GameAge.UNKNOWN, metadata.ages[2].value)
    }

    fun testMetadataHasGenders(metadata: SrvaMetadata) {
        assertEquals(3, metadata.genders.count())
        assertEquals(Gender.FEMALE, metadata.genders[0].value)
        assertEquals(Gender.MALE, metadata.genders[1].value)
        assertEquals(Gender.UNKNOWN, metadata.genders[2].value)
    }

    fun testMetadataHasCategories(metadata: SrvaMetadata) {
        assertEquals(3, metadata.eventCategories.count())
    }

    fun testMetadataHasAccidentCategory(metadata: SrvaMetadata, categoryIndex: Int) {
        assertEquals(SrvaEventCategoryType.ACCIDENT, metadata.eventCategories[categoryIndex].categoryType.value)

        assertEquals(3, metadata.eventCategories[categoryIndex].possibleEventTypes.count())
        assertEquals(SrvaEventType.TRAFFIC_ACCIDENT, metadata.eventCategories[categoryIndex].possibleEventTypes[0].value)
        assertEquals(SrvaEventType.RAILWAY_ACCIDENT, metadata.eventCategories[categoryIndex].possibleEventTypes[1].value)
        assertEquals(SrvaEventType.OTHER, metadata.eventCategories[categoryIndex].possibleEventTypes[2].value)

        assertEquals(6, metadata.eventCategories[categoryIndex].possibleEventResults.count())
        assertEquals(SrvaEventResult.ANIMAL_FOUND_DEAD, metadata.eventCategories[categoryIndex].possibleEventResults[0].value)
        assertEquals(SrvaEventResult.ANIMAL_FOUND_AND_TERMINATED, metadata.eventCategories[categoryIndex].possibleEventResults[1].value)
        assertEquals(SrvaEventResult.ANIMAL_FOUND_AND_NOT_TERMINATED, metadata.eventCategories[categoryIndex].possibleEventResults[2].value)
        assertEquals(SrvaEventResult.ACCIDENT_SITE_NOT_FOUND, metadata.eventCategories[categoryIndex].possibleEventResults[3].value)
        assertEquals(SrvaEventResult.ANIMAL_NOT_FOUND, metadata.eventCategories[categoryIndex].possibleEventResults[4].value)
        assertEquals(SrvaEventResult.UNDUE_ALARM, metadata.eventCategories[categoryIndex].possibleEventResults[5].value)

        assertEquals(3, metadata.eventCategories[categoryIndex].possibleMethods.count())
        assertEquals(SrvaMethodType.TRACED_WITH_DOG, metadata.eventCategories[categoryIndex].possibleMethods[0].value)
        assertEquals(SrvaMethodType.TRACED_WITHOUT_DOG, metadata.eventCategories[categoryIndex].possibleMethods[1].value)
        assertEquals(SrvaMethodType.OTHER, metadata.eventCategories[categoryIndex].possibleMethods[2].value)
    }

    fun testMetadataHasDeportationCategory(metadata: SrvaMetadata, categoryIndex: Int) {
        assertEquals(SrvaEventCategoryType.DEPORTATION, metadata.eventCategories[categoryIndex].categoryType.value)

        assertEquals(3, metadata.eventCategories[categoryIndex].possibleEventTypes.count())
        assertEquals(SrvaEventType.ANIMAL_NEAR_HOUSES_AREA, metadata.eventCategories[categoryIndex].possibleEventTypes[0].value)
        assertEquals(SrvaEventType.ANIMAL_AT_FOOD_DESTINATION, metadata.eventCategories[categoryIndex].possibleEventTypes[1].value)
        assertEquals(SrvaEventType.OTHER, metadata.eventCategories[categoryIndex].possibleEventTypes[2].value)

        assertEquals(4, metadata.eventCategories[categoryIndex].possibleEventResults.count())
        assertEquals(SrvaEventResult.ANIMAL_TERMINATED, metadata.eventCategories[categoryIndex].possibleEventResults[0].value)
        assertEquals(SrvaEventResult.ANIMAL_DEPORTED, metadata.eventCategories[categoryIndex].possibleEventResults[1].value)
        assertEquals(SrvaEventResult.ANIMAL_NOT_FOUND, metadata.eventCategories[categoryIndex].possibleEventResults[2].value)
        assertEquals(SrvaEventResult.UNDUE_ALARM, metadata.eventCategories[categoryIndex].possibleEventResults[3].value)

        assertEquals(6, metadata.eventCategories[categoryIndex].possibleMethods.count())
        assertEquals(SrvaMethodType.DOG, metadata.eventCategories[categoryIndex].possibleMethods[0].value)
        assertEquals(SrvaMethodType.PAIN_EQUIPMENT, metadata.eventCategories[categoryIndex].possibleMethods[1].value)
        assertEquals(SrvaMethodType.SOUND_EQUIPMENT, metadata.eventCategories[categoryIndex].possibleMethods[2].value)
        assertEquals(SrvaMethodType.VEHICLE, metadata.eventCategories[categoryIndex].possibleMethods[3].value)
        assertEquals(SrvaMethodType.CHASING_WITH_PEOPLE, metadata.eventCategories[categoryIndex].possibleMethods[4].value)
        assertEquals(SrvaMethodType.OTHER, metadata.eventCategories[categoryIndex].possibleMethods[5].value)
    }

    fun testMetadataHasInjuredAnimalCategory(metadata: SrvaMetadata, categoryIndex: Int) {
        assertEquals(SrvaEventCategoryType.INJURED_ANIMAL, metadata.eventCategories[categoryIndex].categoryType.value)

        assertEquals(3, metadata.eventCategories[categoryIndex].possibleEventTypes.count())
        assertEquals(SrvaEventType.INJURED_ANIMAL, metadata.eventCategories[categoryIndex].possibleEventTypes[0].value)
        assertEquals(SrvaEventType.ANIMAL_ON_ICE, metadata.eventCategories[categoryIndex].possibleEventTypes[1].value)
        assertEquals(SrvaEventType.OTHER, metadata.eventCategories[categoryIndex].possibleEventTypes[2].value)

        assertEquals(5, metadata.eventCategories[categoryIndex].possibleEventResults.count())
        assertEquals(SrvaEventResult.ANIMAL_FOUND_DEAD, metadata.eventCategories[categoryIndex].possibleEventResults[0].value)
        assertEquals(SrvaEventResult.ANIMAL_FOUND_AND_TERMINATED, metadata.eventCategories[categoryIndex].possibleEventResults[1].value)
        assertEquals(SrvaEventResult.ANIMAL_FOUND_AND_NOT_TERMINATED, metadata.eventCategories[categoryIndex].possibleEventResults[2].value)
        assertEquals(SrvaEventResult.ANIMAL_NOT_FOUND, metadata.eventCategories[categoryIndex].possibleEventResults[3].value)
        assertEquals(SrvaEventResult.UNDUE_ALARM, metadata.eventCategories[categoryIndex].possibleEventResults[4].value)

        assertEquals(3, metadata.eventCategories[categoryIndex].possibleMethods.count())
        assertEquals(SrvaMethodType.TRACED_WITH_DOG, metadata.eventCategories[categoryIndex].possibleMethods[0].value)
        assertEquals(SrvaMethodType.TRACED_WITHOUT_DOG, metadata.eventCategories[categoryIndex].possibleMethods[1].value)
        assertEquals(SrvaMethodType.OTHER, metadata.eventCategories[categoryIndex].possibleMethods[2].value)
    }


}
