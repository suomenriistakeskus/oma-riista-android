package fi.riista.common.domain.srva.ui

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.domain.model.createForTests
import fi.riista.common.domain.srva.model.CommonSrvaEventApprover
import fi.riista.common.domain.srva.model.CommonSrvaEventAuthor
import fi.riista.common.domain.srva.model.CommonSrvaEventData
import fi.riista.common.domain.srva.model.SrvaEventCategoryType
import fi.riista.common.domain.srva.model.SrvaEventResult
import fi.riista.common.domain.srva.model.SrvaEventResultDetail
import fi.riista.common.domain.srva.model.SrvaEventState
import fi.riista.common.domain.srva.model.SrvaEventType
import fi.riista.common.domain.srva.model.SrvaEventTypeDetail
import fi.riista.common.domain.srva.model.SrvaMethodType
import fi.riista.common.domain.srva.model.toCommonSrvaMethod
import fi.riista.common.metadata.MockMetadataProvider
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.ui.dataField.voluntary
import kotlin.test.Test
import kotlin.test.assertEquals

class SrvaEventFieldsTest {

    private val srvaFields = SrvaEventFields(metadataProvider = MockMetadataProvider.INSTANCE)

    @Test
    fun testViewFields() {
        val srvaEvent = getSrvaEvent()

        val fields = srvaFields.getFieldsToBeDisplayed(
            context = SrvaEventFields.Context(
                srvaEvent = srvaEvent,
                mode = SrvaEventFields.Context.Mode.VIEW,
            )
        )

        assertEquals(listOf(
            SrvaEventField.Type.LOCATION.noRequirement(),
            SrvaEventField.Type.SPECIES_CODE.noRequirement(),
            SrvaEventField.Type.DATE_AND_TIME.noRequirement(),
            SrvaEventField.Type.SPECIMEN_AMOUNT.noRequirement(),
            SrvaEventField.Type.SPECIMEN.noRequirement(),
            SrvaEventField.Type.EVENT_CATEGORY.noRequirement(),
            SrvaEventField.Type.EVENT_TYPE.noRequirement(),
            SrvaEventField.Type.EVENT_RESULT.noRequirement(),
            SrvaEventField.Type.SELECTED_METHODS.noRequirement(),
            SrvaEventField.Type.PERSON_COUNT.noRequirement(),
            SrvaEventField.Type.HOURS_SPENT.noRequirement(),
            SrvaEventField.Type.DESCRIPTION.noRequirement(),
        ), fields)
    }
    @Test
    fun testViewFieldsForOtherSelections() {
        val srvaEvent = getSrvaEvent()

        val fields = srvaFields.getFieldsToBeDisplayed(
            context = SrvaEventFields.Context(
                srvaEvent = srvaEvent.copy(
                    species = Species.Other,
                    eventType = SrvaEventType.OTHER.toBackendEnum(),
                    methods = srvaEvent.methods +
                            SrvaMethodType.OTHER.toBackendEnum().toCommonSrvaMethod(selected = true)
                ),
                mode = SrvaEventFields.Context.Mode.VIEW,
            )
        )

        assertEquals(listOf(
            SrvaEventField.Type.LOCATION.noRequirement(),
            SrvaEventField.Type.SPECIES_CODE.noRequirement(),
            SrvaEventField.Type.DATE_AND_TIME.noRequirement(),
            SrvaEventField.Type.OTHER_SPECIES_DESCRIPTION.noRequirement(),
            SrvaEventField.Type.SPECIMEN_AMOUNT.noRequirement(),
            SrvaEventField.Type.SPECIMEN.noRequirement(),
            SrvaEventField.Type.EVENT_CATEGORY.noRequirement(),
            SrvaEventField.Type.EVENT_TYPE.noRequirement(),
            SrvaEventField.Type.EVENT_OTHER_TYPE_DESCRIPTION.noRequirement(),
            SrvaEventField.Type.EVENT_RESULT.noRequirement(),
            SrvaEventField.Type.SELECTED_METHODS.noRequirement(),
            SrvaEventField.Type.OTHER_METHOD_DESCRIPTION.noRequirement(),
            SrvaEventField.Type.PERSON_COUNT.noRequirement(),
            SrvaEventField.Type.HOURS_SPENT.noRequirement(),
            SrvaEventField.Type.DESCRIPTION.noRequirement(),
        ), fields)
    }

    @Test
    fun testEditFields() {
        val srvaEvent = getSrvaEvent()

        val fields = srvaFields.getFieldsToBeDisplayed(
            context = SrvaEventFields.Context(
                srvaEvent = srvaEvent,
                mode = SrvaEventFields.Context.Mode.EDIT,
            )
        )

        assertEquals(listOf(
            SrvaEventField.Type.LOCATION.required(),
            SrvaEventField.Type.SPECIES_CODE.required(),
            SrvaEventField.Type.DATE_AND_TIME.required(),
            SrvaEventField.Type.SPECIMEN_AMOUNT.required(),
            SrvaEventField.Type.SPECIMEN.required(),
            SrvaEventField.Type.EVENT_CATEGORY.required(),
            SrvaEventField.Type.EVENT_TYPE.required(),
            SrvaEventField.Type.EVENT_RESULT.required(),
            SrvaEventField.Type.METHOD_HEADER.voluntary(),
            SrvaEventField(
                type = SrvaEventField.Type.METHOD_ITEM,
                index = 0
            ).voluntary(),
            SrvaEventField(
                type = SrvaEventField.Type.METHOD_ITEM,
                index = 1
            ).voluntary(),
            SrvaEventField.Type.PERSON_COUNT.voluntary(),
            SrvaEventField.Type.HOURS_SPENT.voluntary(),
            SrvaEventField.Type.DESCRIPTION.voluntary(),
        ), fields)
    }

    @Test
    fun testEditFieldsWhenNoCategorySelected() {
        val srvaEvent = getSrvaEvent()

        val fields = srvaFields.getFieldsToBeDisplayed(
            context = SrvaEventFields.Context(
                srvaEvent = srvaEvent.copy(
                    eventCategory = BackendEnum.create(null),
                ),
                mode = SrvaEventFields.Context.Mode.EDIT,
            )
        )

        assertEquals(listOf(
            SrvaEventField.Type.LOCATION.required(),
            SrvaEventField.Type.SPECIES_CODE.required(),
            SrvaEventField.Type.DATE_AND_TIME.required(),
            SrvaEventField.Type.SPECIMEN_AMOUNT.required(),
            SrvaEventField.Type.SPECIMEN.required(),
            SrvaEventField.Type.EVENT_CATEGORY.required(),
            SrvaEventField.Type.PERSON_COUNT.voluntary(),
            SrvaEventField.Type.HOURS_SPENT.voluntary(),
            SrvaEventField.Type.DESCRIPTION.voluntary(),
        ), fields)
    }

    @Test
    fun testEditFieldsForOtherSelections() {
        val srvaEvent = getSrvaEvent()
        val fields = srvaFields.getFieldsToBeDisplayed(
            context = SrvaEventFields.Context(
                srvaEvent = srvaEvent.copy(
                    species = Species.Other,
                    eventType = SrvaEventType.OTHER.toBackendEnum(),
                    methods = srvaEvent.methods +
                            SrvaMethodType.OTHER.toBackendEnum().toCommonSrvaMethod(selected = true)
                ),
                mode = SrvaEventFields.Context.Mode.EDIT,
            )
        )

        assertEquals(listOf(
            SrvaEventField.Type.LOCATION.required(),
            SrvaEventField.Type.SPECIES_CODE.required(),
            SrvaEventField.Type.DATE_AND_TIME.required(),
            SrvaEventField.Type.OTHER_SPECIES_DESCRIPTION.required(),
            SrvaEventField.Type.SPECIMEN_AMOUNT.required(),
            SrvaEventField.Type.SPECIMEN.required(),
            SrvaEventField.Type.EVENT_CATEGORY.required(),
            SrvaEventField.Type.EVENT_TYPE.required(),
            SrvaEventField.Type.EVENT_OTHER_TYPE_DESCRIPTION.voluntary(),
            SrvaEventField.Type.EVENT_RESULT.required(),
            SrvaEventField.Type.METHOD_HEADER.voluntary(),
            SrvaEventField(
                type = SrvaEventField.Type.METHOD_ITEM,
                index = 0
            ).voluntary(),
            SrvaEventField(
                type = SrvaEventField.Type.METHOD_ITEM,
                index = 1
            ).voluntary(),
            SrvaEventField(
                type = SrvaEventField.Type.METHOD_ITEM,
                index = 2
            ).voluntary(),
            SrvaEventField.Type.OTHER_METHOD_DESCRIPTION.voluntary(),
            SrvaEventField.Type.PERSON_COUNT.voluntary(),
            SrvaEventField.Type.HOURS_SPENT.voluntary(),
            SrvaEventField.Type.DESCRIPTION.voluntary(),
        ), fields)
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
            eventType = SrvaEventType.INJURED_ANIMAL.toBackendEnum(),
            otherEventTypeDescription = null,
            eventTypeDetail = SrvaEventTypeDetail.FARM_ANIMAL_BUILDING.toBackendEnum(),
            otherEventTypeDetailDescription = null,
            eventResult = SrvaEventResult.ANIMAL_NOT_FOUND.toBackendEnum(),
            eventResultDetail = SrvaEventResultDetail.ANIMAL_CONTACTED.toBackendEnum(),
            methods = listOf(
                SrvaMethodType.TRACED_WITH_DOG.toBackendEnum().toCommonSrvaMethod(selected = true),
                SrvaMethodType.SOUND_EQUIPMENT.toBackendEnum().toCommonSrvaMethod(selected = true),
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
}
