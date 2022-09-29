package fi.riista.common.domain.srva.metadata

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.srva.metadata.model.SrvaEventCategory
import fi.riista.common.domain.srva.metadata.model.SrvaMetadata
import fi.riista.common.domain.srva.model.SrvaEventCategoryType
import fi.riista.common.domain.srva.model.SrvaEventResult
import fi.riista.common.domain.srva.model.SrvaEventType
import fi.riista.common.domain.srva.model.SrvaMethodType
import fi.riista.common.model.toBackendEnum
import fi.riista.common.domain.srva.model.CommonSrvaTypeDetail
import fi.riista.common.domain.srva.model.SrvaEventResultDetail
import fi.riista.common.domain.srva.model.SrvaEventTypeDetail

internal object HardcodedSrvaMetadataProvider {
    init {
        require(Constants.SRVA_SPEC_VERSION == 2) {
            "SRVA spec version bumped without updating HardcodedSrvaMetadataProvider"
        }
    }

    /**
     * The hardcoded SRVA metadata for spec version 2
     */
    val metadata = SrvaMetadata(
        species = listOf(47503, 47629, 47507, 200556, 47484, 47926, 46615, 47348, 46549, 47212)
            .map { speciesCode ->
                Species.Known(speciesCode)
                 },
        ages = listOf(GameAge.ADULT, GameAge.YOUNG, GameAge.UNKNOWN).map { it.toBackendEnum() },
        genders = listOf(Gender.FEMALE, Gender.MALE, Gender.UNKNOWN).map { it.toBackendEnum() },
        eventCategories = listOf(
            SrvaEventCategory(
                categoryType = SrvaEventCategoryType.ACCIDENT.toBackendEnum(),
                possibleEventTypes = listOf(
                    SrvaEventType.TRAFFIC_ACCIDENT,
                    SrvaEventType.RAILWAY_ACCIDENT,
                    SrvaEventType.OTHER,
                ).map { it.toBackendEnum() },
                possibleEventTypeDetails = mapOf(),
                possibleEventResults = listOf(
                    SrvaEventResult.ANIMAL_FOUND_DEAD,
                    SrvaEventResult.ANIMAL_FOUND_AND_TERMINATED,
                    SrvaEventResult.ANIMAL_FOUND_AND_NOT_TERMINATED,
                    SrvaEventResult.ACCIDENT_SITE_NOT_FOUND,
                    SrvaEventResult.ANIMAL_NOT_FOUND,
                    SrvaEventResult.UNDUE_ALARM,
                ).map { it.toBackendEnum() },
                possibleEventResultDetails = mapOf(),
                possibleMethods = listOf(
                    SrvaMethodType.TRACED_WITH_DOG,
                    SrvaMethodType.TRACED_WITHOUT_DOG,
                    SrvaMethodType.OTHER
                ).map { it.toBackendEnum() },
            ),

            SrvaEventCategory(
                categoryType = SrvaEventCategoryType.DEPORTATION.toBackendEnum(),
                possibleEventTypes = listOf(
                    SrvaEventType.ANIMAL_NEAR_HOUSES_AREA,
                    SrvaEventType.ANIMAL_AT_FOOD_DESTINATION,
                    SrvaEventType.OTHER,
                ).map { it.toBackendEnum() },
                possibleEventTypeDetails = mapOf(
                    SrvaEventType.ANIMAL_NEAR_HOUSES_AREA.toBackendEnum() to listOf(
                        CommonSrvaTypeDetail(SrvaEventTypeDetail.CARED_HOUSE_AREA),
                        CommonSrvaTypeDetail(SrvaEventTypeDetail.FARM_ANIMAL_BUILDING),
                        CommonSrvaTypeDetail(SrvaEventTypeDetail.URBAN_AREA),
                        CommonSrvaTypeDetail(SrvaEventTypeDetail.OTHER),
                    ),
                    SrvaEventType.ANIMAL_AT_FOOD_DESTINATION.toBackendEnum() to listOf(
                        CommonSrvaTypeDetail(SrvaEventTypeDetail.CARCASS_AT_FOREST),
                        CommonSrvaTypeDetail(SrvaEventTypeDetail.CARCASS_NEAR_HOUSES_AREA),
                        CommonSrvaTypeDetail(SrvaEventTypeDetail.GARBAGE_CAN),
                        CommonSrvaTypeDetail(
                            detailType = SrvaEventTypeDetail.BEEHIVE.toBackendEnum(),
                            speciesCodes = listOf(SpeciesCodes.BEAR_ID)
                        ),
                        CommonSrvaTypeDetail(SrvaEventTypeDetail.OTHER),
                    ),
                ),
                possibleEventResults = listOf(
                    SrvaEventResult.ANIMAL_TERMINATED,
                    SrvaEventResult.ANIMAL_DEPORTED,
                    SrvaEventResult.ANIMAL_NOT_FOUND,
                    SrvaEventResult.UNDUE_ALARM,
                ).map { it.toBackendEnum() },
                possibleEventResultDetails = mapOf(
                    SrvaEventResult.ANIMAL_DEPORTED.toBackendEnum() to listOf(
                        SrvaEventResultDetail.ANIMAL_CONTACTED_AND_DEPORTED,
                        SrvaEventResultDetail.ANIMAL_CONTACTED,
                        SrvaEventResultDetail.UNCERTAIN_RESULT,
                    ).map { it.toBackendEnum() }
                ),
                possibleMethods = listOf(
                    SrvaMethodType.DOG,
                    SrvaMethodType.PAIN_EQUIPMENT,
                    SrvaMethodType.SOUND_EQUIPMENT,
                    SrvaMethodType.VEHICLE,
                    SrvaMethodType.CHASING_WITH_PEOPLE,
                    SrvaMethodType.OTHER,
                ).map { it.toBackendEnum() },
            ),

            SrvaEventCategory(
                categoryType = SrvaEventCategoryType.INJURED_ANIMAL.toBackendEnum(),
                possibleEventTypes = listOf(
                    SrvaEventType.INJURED_ANIMAL,
                    SrvaEventType.ANIMAL_ON_ICE,
                    SrvaEventType.OTHER,
                ).map { it.toBackendEnum() },
                possibleEventTypeDetails = mapOf(),
                possibleEventResults = listOf(
                    SrvaEventResult.ANIMAL_FOUND_DEAD,
                    SrvaEventResult.ANIMAL_FOUND_AND_TERMINATED,
                    SrvaEventResult.ANIMAL_FOUND_AND_NOT_TERMINATED,
                    SrvaEventResult.ANIMAL_NOT_FOUND,
                    SrvaEventResult.UNDUE_ALARM,
                ).map { it.toBackendEnum() },
                possibleEventResultDetails = mapOf(),
                possibleMethods = listOf(
                    SrvaMethodType.TRACED_WITH_DOG,
                    SrvaMethodType.TRACED_WITHOUT_DOG,
                    SrvaMethodType.OTHER,
                ).map { it.toBackendEnum() },
            )
        )

    )
}