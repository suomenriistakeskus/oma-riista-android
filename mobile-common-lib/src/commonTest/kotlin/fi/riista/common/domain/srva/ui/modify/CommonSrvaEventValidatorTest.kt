@file:Suppress("ComplexRedundantLet")

package fi.riista.common.domain.srva.ui.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.metadata.MockMetadataProvider
import fi.riista.common.domain.model.*
import fi.riista.common.domain.srva.model.*
import fi.riista.common.domain.srva.ui.SrvaEventFields
import fi.riista.common.model.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommonSrvaEventValidatorTest {

    private val srvaEventFields = SrvaEventFields(metadataProvider = getMetadataProvider())

    @Test
    fun testValidSrvaEvent() {
        val validationErrors = validate(srvaEvent = getSrvaEvent())
        assertTrue(validationErrors.isEmpty())
    }

    @Test
    fun testMissingLocation() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            location = CommonLocation.Unknown
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.MISSING_LOCATION))
    }

    @Test
    fun testMissingSpecies() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            species = Species.Unknown
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.MISSING_SPECIES))
    }

    @Test
    fun testValidOtherSpeciesDescription() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            species = Species.Other,
            otherSpeciesDescription = "description",
        ))
        assertTrue(validationErrors.isEmpty())
    }

    @Test
    fun testMissingSpecimens() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            specimens = listOf(),
        ))
        assertFalse(validationErrors.contains(CommonSrvaEventValidator.Error.INVALID_SPECIMEN_AMOUNT))
    }

    @Test
    fun testTooManySpecimens() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            specimenAmount = 1000,
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.INVALID_SPECIMEN_AMOUNT))
    }

    @Test
    fun testMissingOtherSpeciesDescription() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            species = Species.Other,
            otherSpeciesDescription = null,
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.MISSING_OTHER_SPECIES_DESCRIPTION))
    }

    @Test
    fun testMissingCategory() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            eventCategory = BackendEnum.create(null)
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.MISSING_EVENT_CATEGORY))
    }

    @Test
    fun testInvalidCategory() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            eventCategory = "NonExistentCategory".toBackendEnum()
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.INVALID_EVENT_CATEGORY))
    }

    @Test
    fun testMissingDeportationOrderNumber() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            eventCategory = SrvaEventCategoryType.DEPORTATION.toBackendEnum(),
            deportationOrderNumber = null,
        ))

        // not required currently
        assertFalse(validationErrors.contains(CommonSrvaEventValidator.Error.MISSING_DEPORTATION_ORDER_NUMBER))
    }

    @Test
    fun testMissingEventType() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            eventType = BackendEnum.create(null)
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.MISSING_EVENT_TYPE))
    }

    @Test
    fun testInvalidEventType() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            eventType = "UnknownType".toBackendEnum()
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.INVALID_EVENT_TYPE))
    }

    @Test
    fun testMissingEventTypeDetail() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            eventCategory = SrvaEventCategoryType.DEPORTATION.toBackendEnum(),
            eventType = SrvaEventType.ANIMAL_NEAR_HOUSES_AREA.toBackendEnum(),
            eventTypeDetail = BackendEnum.create(null),
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.MISSING_EVENT_TYPE_DETAIL))
    }

    @Test
    fun testInvalidEventTypeDetail() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            eventCategory = SrvaEventCategoryType.DEPORTATION.toBackendEnum(),
            eventType = SrvaEventType.ANIMAL_NEAR_HOUSES_AREA.toBackendEnum(),
            eventTypeDetail = "UnsupportedDetail".toBackendEnum(),
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.INVALID_EVENT_TYPE_DETAIL))
    }

    @Test
    fun testMissingEventResult() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            eventResult = BackendEnum.create(null)
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.MISSING_EVENT_RESULT))
    }

    @Test
    fun testInvalidEventResult() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            eventResult = "UnknownType".toBackendEnum()
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.INVALID_EVENT_RESULT))
    }
    @Test
    fun testMissingEventResultDetail() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            eventCategory = SrvaEventCategoryType.DEPORTATION.toBackendEnum(),
            eventResult = SrvaEventResult.ANIMAL_DEPORTED.toBackendEnum(),
            eventResultDetail = BackendEnum.create(null),
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.MISSING_EVENT_RESULT_DETAIL))
    }

    @Test
    fun testInvalidEventResultDetail() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            eventCategory = SrvaEventCategoryType.DEPORTATION.toBackendEnum(),
            eventResult = SrvaEventResult.ANIMAL_DEPORTED.toBackendEnum(),
            eventResultDetail = "UnsupportedDetail".toBackendEnum(),
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.INVALID_EVENT_RESULT_DETAIL))
    }

    @Test
    fun testTooBigPersonCount() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            personCount = 101,
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.INVALID_PERSON_COUNT))
    }

    @Test
    fun testTooManyHoursSpent() {
        val validationErrors = validate(srvaEvent = getSrvaEvent().copy(
            hoursSpent = 1000,
        ))
        assertTrue(validationErrors.contains(CommonSrvaEventValidator.Error.INVALID_HOURS_SPENT))
    }

    private fun validate(srvaEvent: CommonSrvaEventData): List<CommonSrvaEventValidator.Error> {
        val fields = srvaEventFields.getFieldsToBeDisplayed(
            SrvaEventFields.Context(
                srvaEvent = srvaEvent,
                mode = SrvaEventFields.Context.Mode.EDIT
            )
        )

        return CommonSrvaEventValidator.validate(
            srvaEvent = srvaEvent,
            srvaMetadata = getMetadataProvider().srvaMetadata,
            displayedFields = fields
        )
    }

    private fun getSrvaEvent(): CommonSrvaEventData {
        return CommonSrvaEventData(
            localId = 99,
            localUrl = null,
            remoteId = 100,
            revision = 2,
            mobileClientRefId = 33L,
            srvaSpecVersion = Constants.SRVA_SPEC_VERSION,
            state = SrvaEventState.UNFINISHED.toBackendEnum(),
            rhyId = 12,
            canEdit = true,
            modified = true,
            deleted = false,
            location = ETRMSGeoLocation(
                latitude = 12,
                longitude =  13,
                source = GeoLocationSource.MANUAL.toBackendEnum()
            ).asKnownLocation(),
            pointOfTime = LocalDateTime(2022, 1, 1, 13, 14, 15),
            author = CommonSrvaEventAuthor(
                id = 13,
                revision = 14,
                byName = "Pena",
                lastName = "Mujunen"
            ),
            approver = CommonSrvaEventApprover(
                firstName = "Asko",
                lastName = "Partanen"
            ),
            species = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID),
            otherSpeciesDescription = null,
            specimenAmount = 1,
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    gender = Gender.MALE.toBackendEnum(),
                    age = GameAge.ADULT.toBackendEnum(),
                )
            ),
            eventCategory = SrvaEventCategoryType.ACCIDENT.toBackendEnum(),
            deportationOrderNumber = null,
            eventType = SrvaEventType.TRAFFIC_ACCIDENT.toBackendEnum(),
            otherEventTypeDescription = null,
            eventTypeDetail = SrvaEventTypeDetail.FARM_ANIMAL_BUILDING.toBackendEnum(),
            otherEventTypeDetailDescription = null,
            eventResult = SrvaEventResult.ANIMAL_NOT_FOUND.toBackendEnum(),
            eventResultDetail = SrvaEventResultDetail.ANIMAL_CONTACTED.toBackendEnum(),
            methods = listOf(
                SrvaMethodType.TRACED_WITH_DOG.toBackendEnum().toCommonSrvaMethod(selected = true),
            ),
            otherMethodDescription = null,
            personCount = 2,
            hoursSpent = 3,
            description = "Käytiin etsimässä, ei löytynyt",
            images = EntityImages(
                remoteImageIds = listOf(),
                localImages = listOf(
                    EntityImage(
                        serverId = "serverId",
                        localIdentifier = "localIdentifier",
                        localUrl = "localUrl",
                        status = EntityImage.Status.UPLOADED,
                    ),
                    EntityImage(
                        serverId = "serverId (not shown)",
                        localIdentifier = "localIdentifier (not shown)",
                        localUrl = "localUrl (not shown)",
                        status = EntityImage.Status.UPLOADED,
                    )
                )
            ),
        )
    }

    private fun getMetadataProvider(): MetadataProvider = MockMetadataProvider.INSTANCE
}
