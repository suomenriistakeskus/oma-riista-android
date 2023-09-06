package fi.riista.common.domain.srva.ui.view

import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.dto.UserInfoDTO
import fi.riista.common.helpers.*
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.metadata.MockMetadataProvider
import fi.riista.common.domain.model.*
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.localized
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.domain.srva.SrvaContext
import fi.riista.common.domain.srva.SrvaEventOperationResponse
import fi.riista.common.domain.srva.model.*
import fi.riista.common.domain.srva.ui.SrvaEventField
import fi.riista.common.domain.userInfo.CurrentUserContextProviderFactory
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.io.CommonFileProviderMock
import fi.riista.common.model.*
import fi.riista.common.network.BackendAPI
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.preferences.MockPreferences
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.util.MockDateTimeProvider
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class ViewSrvaEventControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() = runBlockingTest {
        val (controller, _) = getController()
        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val (controller, initialSrva) = getController(srvaEvent = getSrvaEvent())

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        assertEquals(initialSrva.toSrvaEventData(), viewModel.srvaEvent)
    }

    @Test
    fun testProducedFieldsMatchData() = runBlockingTest {
        val (controller, initialSrva) = getController(srvaEvent = getSrvaEvent())

        controller.loadViewModel()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(12, fields.size)
        var expectedIndex = 0
        fields.getLocationField(expectedIndex++, SrvaEventField.Type.LOCATION.toField()).let {
            assertEquals(initialSrva.location.asKnownLocation(), it.location)
            assertTrue(it.settings.readOnly)
        }
        fields.getSpeciesField(expectedIndex++, SrvaEventField.Type.SPECIES_CODE.toField()).let {
            assertEquals(initialSrva.species, it.species)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.showEntityImage)
            assertEquals("serverId", it.entityImage?.serverId)
            assertEquals("localIdentifier", it.entityImage?.localIdentifier)
            assertEquals("localUrl", it.entityImage?.localUrl)
        }
        fields.getDateTimeField(expectedIndex++, SrvaEventField.Type.DATE_AND_TIME.toField()).let {
            assertEquals(initialSrva.pointOfTime, it.dateAndTime)
            assertNull(it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.SPECIMEN_AMOUNT.toField()).let {
            assertEquals("${initialSrva.specimens.count()}", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_specimen_amount)
            assertTrue(it.settings.readOnly)
        }
        fields.getSpecimenField(expectedIndex++, SrvaEventField.Type.SPECIMEN.toField()).let {
            with (it.specimenData) {
                assertEquals(initialSrva.species, species)
                assertEquals(
                    initialSrva.specimens.map { specimen -> specimen.toCommonSpecimenData() },
                    specimens
                )
                assertEquals(2, fieldSpecifications.count())
                assertEquals(SpecimenFieldType.GENDER, fieldSpecifications[0].fieldType)
                fieldSpecifications[0].label.assertEquals(RR.string.gender_label)
                assertEquals(SpecimenFieldType.AGE, fieldSpecifications[1].fieldType)
                fieldSpecifications[1].label.assertEquals(RR.string.age_label)
            }
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_CATEGORY.toField()).let {
            it.value.assertEquals(initialSrva.eventCategory.localized(getStringProvider()))
            it.settings.label.assertEquals(RR.string.srva_event_label_event_category)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_TYPE.toField()).let {
            it.value.assertEquals(initialSrva.eventType.localized(getStringProvider()))
            it.settings.label.assertEquals(RR.string.srva_event_label_event_type)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_RESULT.toField()).let {
            it.value.assertEquals(initialSrva.eventResult.localized(getStringProvider()))
            it.settings.label.assertEquals(RR.string.srva_event_label_event_result)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.SELECTED_METHODS.toField()).let {
            it.value.assertEquals(
                initialSrva.methods.selectedMethods.joinToString(separator = "\n") { method ->
                    method.localized(getStringProvider())
                }
            )
            it.settings.label.assertEquals(RR.string.srva_event_label_method)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.PERSON_COUNT.toField()).let {
            assertEquals("${initialSrva.personCount}", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_person_count)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.HOURS_SPENT.toField()).let {
            assertEquals("${initialSrva.hoursSpent}", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_hours_spent)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex, SrvaEventField.Type.DESCRIPTION.toField()).let {
            assertEquals(initialSrva.description, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_description)
            assertTrue(it.settings.readOnly)
        }
    }

    @Test
    fun testProducedFieldsMatchDataForApprover() = runBlockingTest {
        val (controller, _) = getController(
            srvaEvent = getSrvaEvent().copy(
                state = SrvaEventState.APPROVED.toBackendEnum()
            )
        )

        controller.loadViewModel()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(13, fields.size)
        fields.getStringField(3, SrvaEventField.Type.APPROVER_OR_REJECTOR.toField()).let {
            assertEquals("Asko Partanen", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_approver)
            assertTrue(it.settings.readOnly)
        }
    }

    @Test
    fun testProducedFieldsMatchDataForRejector() = runBlockingTest {
        val (controller, _) = getController(
            srvaEvent = getSrvaEvent().copy(
                state = SrvaEventState.REJECTED.toBackendEnum()
            )
        )

        controller.loadViewModel()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(13, fields.size)
        fields.getStringField(3, SrvaEventField.Type.APPROVER_OR_REJECTOR.toField()).let {
            assertEquals("Asko Partanen", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_rejector)
            assertTrue(it.settings.readOnly)
        }
    }

    @Test
    fun testFieldsAreProducedForMissingData() = runBlockingTest {
        val (controller, initialSrva) = getController(
            srvaEvent = getSrvaEvent().copy(
                eventCategory = BackendEnum.create(null),
                eventType = BackendEnum.create(null),
                eventResult = BackendEnum.create(null),
                images = EntityImages(
                    remoteImageIds = listOf(),
                    localImages = listOf(),
                )
            )
        )

        controller.loadViewModel()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(12, fields.size)
        var expectedIndex = 0
        fields.getLocationField(expectedIndex++, SrvaEventField.Type.LOCATION.toField()).let {
            assertEquals(initialSrva.location.asKnownLocation(), it.location)
            assertTrue(it.settings.readOnly)
        }
        fields.getSpeciesField(expectedIndex++, SrvaEventField.Type.SPECIES_CODE.toField()).let {
            assertEquals(initialSrva.species, it.species)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.showEntityImage)
            assertNull(it.entityImage)
        }
        fields.getDateTimeField(expectedIndex++, SrvaEventField.Type.DATE_AND_TIME.toField()).let {
            assertEquals(initialSrva.pointOfTime, it.dateAndTime)
            assertNull(it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.SPECIMEN_AMOUNT.toField()).let {
            assertEquals("${initialSrva.specimens.count()}", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_specimen_amount)
            assertTrue(it.settings.readOnly)
        }
        fields.getSpecimenField(expectedIndex++, SrvaEventField.Type.SPECIMEN.toField()).let {
            with (it.specimenData) {
                assertEquals(initialSrva.species, species)
                assertEquals(
                    initialSrva.specimens.map { specimen -> specimen.toCommonSpecimenData() },
                    specimens
                )
                assertEquals(2, fieldSpecifications.count())
                assertEquals(SpecimenFieldType.GENDER, fieldSpecifications[0].fieldType)
                fieldSpecifications[0].label.assertEquals(RR.string.gender_label)
                assertEquals(SpecimenFieldType.AGE, fieldSpecifications[1].fieldType)
                fieldSpecifications[1].label.assertEquals(RR.string.age_label)
            }
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_CATEGORY.toField()).let {
            assertEquals("-", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_event_category)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_TYPE.toField()).let {
            assertEquals("-", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_event_type)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_RESULT.toField()).let {
            assertEquals("-", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_event_result)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.SELECTED_METHODS.toField()).let {
            it.value.assertEquals(
                initialSrva.methods.selectedMethods.joinToString(separator = "\n") { method ->
                    method.localized(getStringProvider())
                }
            )
            it.settings.label.assertEquals(RR.string.srva_event_label_method)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.PERSON_COUNT.toField()).let {
            assertEquals("${initialSrva.personCount}", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_person_count)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.HOURS_SPENT.toField()).let {
            assertEquals("${initialSrva.hoursSpent}", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_hours_spent)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex, SrvaEventField.Type.DESCRIPTION.toField()).let {
            assertEquals(initialSrva.description, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_description)
            assertTrue(it.settings.readOnly)
        }
    }

    @Test
    fun testDescriptionsAreDisplayedForOtherValues() = runBlockingTest {
        val (controller, initialSrva) = getController(
            srvaEvent = getSrvaEvent().copy(
                eventType = SrvaEventType.OTHER.toBackendEnum(),
                otherEventTypeDescription = "Lehmä katolla",
                methods = listOf(
                    SrvaMethodType.TRACED_WITH_DOG.toBackendEnum().toCommonSrvaMethod(selected = true),
                    SrvaMethodType.OTHER.toBackendEnum().toCommonSrvaMethod(selected = true),
                ),
                otherMethodDescription = "Kiikareilla katsottiin"
            )
        )

        controller.loadViewModel()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(14, fields.size)
        var expectedIndex = 0
        fields.getLocationField(expectedIndex++, SrvaEventField.Type.LOCATION.toField()).let {
            assertEquals(initialSrva.location.asKnownLocation(), it.location)
            assertTrue(it.settings.readOnly)
        }
        fields.getSpeciesField(expectedIndex++, SrvaEventField.Type.SPECIES_CODE.toField()).let {
            assertEquals(initialSrva.species, it.species)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.showEntityImage)
            assertEquals("serverId", it.entityImage?.serverId)
            assertEquals("localIdentifier", it.entityImage?.localIdentifier)
            assertEquals("localUrl", it.entityImage?.localUrl)
        }
        fields.getDateTimeField(expectedIndex++, SrvaEventField.Type.DATE_AND_TIME.toField()).let {
            assertEquals(initialSrva.pointOfTime, it.dateAndTime)
            assertNull(it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.SPECIMEN_AMOUNT.toField()).let {
            assertEquals("${initialSrva.specimens.count()}", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_specimen_amount)
            assertTrue(it.settings.readOnly)
        }
        fields.getSpecimenField(expectedIndex++, SrvaEventField.Type.SPECIMEN.toField()).let {
            with (it.specimenData) {
                assertEquals(initialSrva.species, species)
                assertEquals(
                    initialSrva.specimens.map { specimen -> specimen.toCommonSpecimenData() },
                    specimens
                )
                assertEquals(2, fieldSpecifications.count())
                assertEquals(SpecimenFieldType.GENDER, fieldSpecifications[0].fieldType)
                fieldSpecifications[0].label.assertEquals(RR.string.gender_label)
                assertEquals(SpecimenFieldType.AGE, fieldSpecifications[1].fieldType)
                fieldSpecifications[1].label.assertEquals(RR.string.age_label)
            }
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_CATEGORY.toField()).let {
            it.value.assertEquals(initialSrva.eventCategory.localized(getStringProvider()))
            it.settings.label.assertEquals(RR.string.srva_event_label_event_category)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_TYPE.toField()).let {
            it.value.assertEquals(initialSrva.eventType.localized(getStringProvider()))
            it.settings.label.assertEquals(RR.string.srva_event_label_event_type)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_OTHER_TYPE_DESCRIPTION.toField()).let {
            assertEquals(initialSrva.otherEventTypeDescription, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_other_event_type_description)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_RESULT.toField()).let {
            it.value.assertEquals(initialSrva.eventResult.localized(getStringProvider()))
            it.settings.label.assertEquals(RR.string.srva_event_label_event_result)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.SELECTED_METHODS.toField()).let {
            it.value.assertEquals(
                initialSrva.methods.selectedMethods.joinToString(separator = "\n") { method ->
                    method.localized(getStringProvider())
                }
            )
            it.settings.label.assertEquals(RR.string.srva_event_label_method)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.OTHER_METHOD_DESCRIPTION.toField()).let {
            assertEquals(initialSrva.otherMethodDescription, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_other_method_description)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.PERSON_COUNT.toField()).let {
            assertEquals("${initialSrva.personCount}", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_person_count)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.HOURS_SPENT.toField()).let {
            assertEquals("${initialSrva.hoursSpent}", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_hours_spent)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex, SrvaEventField.Type.DESCRIPTION.toField()).let {
            assertEquals(initialSrva.description, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_description)
            assertTrue(it.settings.readOnly)
        }
    }

    @Test
    fun testProducedFieldsMatchDataForDeportation() = runBlockingTest {
        val (controller, initialSrva) = getController(
            srvaEvent = getSrvaEvent().copy(
                eventCategory = SrvaEventCategoryType.DEPORTATION.toBackendEnum(),
                deportationOrderNumber = "deportationOrderNumber",
                eventType = SrvaEventType.ANIMAL_NEAR_HOUSES_AREA.toBackendEnum(),
                eventTypeDetail = SrvaEventTypeDetail.OTHER.toBackendEnum(),
                otherEventTypeDetailDescription = "otherEventTypeDetailDescription",
                eventResult = SrvaEventResult.ANIMAL_DEPORTED.toBackendEnum(),
                eventResultDetail = SrvaEventResultDetail.ANIMAL_CONTACTED.toBackendEnum(),
            )
        )

        controller.loadViewModel()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(16, fields.size)
        var expectedIndex = 0
        fields.getLocationField(expectedIndex++, SrvaEventField.Type.LOCATION.toField()).let {
            assertEquals(initialSrva.location.asKnownLocation(), it.location)
            assertTrue(it.settings.readOnly)
        }
        fields.getSpeciesField(expectedIndex++, SrvaEventField.Type.SPECIES_CODE.toField()).let {
            assertEquals(initialSrva.species, it.species)
            assertTrue(it.settings.readOnly)
            assertTrue(it.settings.showEntityImage)
            assertEquals("serverId", it.entityImage?.serverId)
            assertEquals("localIdentifier", it.entityImage?.localIdentifier)
            assertEquals("localUrl", it.entityImage?.localUrl)
        }
        fields.getDateTimeField(expectedIndex++, SrvaEventField.Type.DATE_AND_TIME.toField()).let {
            assertEquals(initialSrva.pointOfTime, it.dateAndTime)
            assertNull(it.settings.label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.SPECIMEN_AMOUNT.toField()).let {
            assertEquals("${initialSrva.specimens.count()}", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_specimen_amount)
            assertTrue(it.settings.readOnly)
        }
        fields.getSpecimenField(expectedIndex++, SrvaEventField.Type.SPECIMEN.toField()).let {
            with (it.specimenData) {
                assertEquals(initialSrva.species, species)
                assertEquals(
                    initialSrva.specimens.map { specimen -> specimen.toCommonSpecimenData() },
                    specimens
                )
                assertEquals(2, fieldSpecifications.count())
                assertEquals(SpecimenFieldType.GENDER, fieldSpecifications[0].fieldType)
                fieldSpecifications[0].label.assertEquals(RR.string.gender_label)
                assertEquals(SpecimenFieldType.AGE, fieldSpecifications[1].fieldType)
                fieldSpecifications[1].label.assertEquals(RR.string.age_label)
            }
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_CATEGORY.toField()).let {
            it.value.assertEquals(initialSrva.eventCategory.localized(getStringProvider()))
            it.settings.label.assertEquals(RR.string.srva_event_label_event_category)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.DEPORTATION_ORDER_NUMBER.toField()).let {
            assertEquals("deportationOrderNumber", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_deportation_order_number)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_TYPE.toField()).let {
            it.value.assertEquals(initialSrva.eventType.localized(getStringProvider()))
            it.settings.label.assertEquals(RR.string.srva_event_label_event_type)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_TYPE_DETAIL.toField()).let {
            it.value.assertEquals(initialSrva.eventTypeDetail.localized(getStringProvider()))
            it.settings.label.assertEquals(RR.string.srva_event_label_event_type_detail)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_OTHER_TYPE_DETAIL_DESCRIPTION.toField()).let {
            assertEquals("otherEventTypeDetailDescription", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_other_event_type_detail_description)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_RESULT.toField()).let {
            it.value.assertEquals(initialSrva.eventResult.localized(getStringProvider()))
            it.settings.label.assertEquals(RR.string.srva_event_label_event_result)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_RESULT_DETAIL.toField()).let {
            it.value.assertEquals(initialSrva.eventResultDetail.localized(getStringProvider()))
            it.settings.label.assertEquals(RR.string.srva_event_label_event_result_detail)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.SELECTED_METHODS.toField()).let {
            it.value.assertEquals(
                initialSrva.methods.selectedMethods.joinToString(separator = "\n") { method ->
                    method.localized(getStringProvider())
                }
            )
            it.settings.label.assertEquals(RR.string.srva_event_label_method)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.PERSON_COUNT.toField()).let {
            assertEquals("${initialSrva.personCount}", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_person_count)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.HOURS_SPENT.toField()).let {
            assertEquals("${initialSrva.hoursSpent}", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_hours_spent)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex, SrvaEventField.Type.DESCRIPTION.toField()).let {
            assertEquals(initialSrva.description, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_description)
            assertTrue(it.settings.readOnly)
        }
    }

    @Test
    fun testDeletingSrvaEventMarksItDeleted() = runBlockingTest {
        val srvaContext = getSrvaContext()
        val (controller, srvaEvent) = getController(srvaContext = srvaContext)

        // not loaded yet
        assertNull(srvaContext.srvaEventProvider.srvaEvents)

        controller.loadViewModel(refresh = false)

        assertEquals(1, srvaContext.srvaEventProvider.srvaEvents?.size)

        assertTrue(controller.deleteSrvaEvent(updateToBackend = false), "deletion")

        srvaContext.srvaEventProvider.fetch(refresh = true)

        assertEquals(0, srvaContext.srvaEventProvider.srvaEvents?.size)

        assertNotNull(srvaEvent.localId, "missing srva local id")
        val deletedSrvaEvent = srvaContext.repository.getByLocalId(srvaEvent.localId!!)
        assertTrue(deletedSrvaEvent.deleted, "srvaEvent deleted")
    }

    @Test
    fun testDeletingSrvaEventCallsBackend() = runBlockingTest {
        val srvaContext = getSrvaContext()
        val (controller, srvaEvent) = getController(srvaContext = srvaContext)

        assertEquals(0, backendAPIMock.callCount(BackendAPI::deleteSrvaEvent))

        controller.loadViewModel(refresh = false)
        assertTrue(controller.deleteSrvaEvent(updateToBackend = true), "deletion")

        assertEquals(
            expected = 1,
            actual = backendAPIMock.callCount(BackendAPI::deleteSrvaEvent),
            message = "delete count"
        )
        assertEquals(
            expected = srvaEvent.remoteId,
            actual = backendAPIMock.callParameter(BackendAPI::deleteSrvaEvent),
            message = "deleted srvaEvent"
        )
    }

    private fun getSrvaEvent(): CommonSrvaEvent {
        return CommonSrvaEvent(
            localId = null,
            localUrl = null,
            remoteId = 100,
            revision = 2,
            mobileClientRefId = 33L,
            srvaSpecVersion = Constants.SRVA_SPEC_VERSION,
            state = SrvaEventState.UNFINISHED.toBackendEnum(),
            rhyId = 12,
            canEdit = true,
            modified = false,
            deleted = false,
            location = ETRMSGeoLocation(
                latitude = 12,
                longitude =  13,
                source = GeoLocationSource.MANUAL.toBackendEnum()
            ),
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
            totalSpecimenAmount = 1,
            specimens = listOf(
                CommonSrvaSpecimen(
                    gender = Gender.MALE.toBackendEnum(),
                    age = GameAge.ADULT.toBackendEnum(),
                )
            ),
            eventCategory = SrvaEventCategoryType.ACCIDENT.toBackendEnum(),
            deportationOrderNumber = null,
            eventType = SrvaEventType.INJURED_ANIMAL.toBackendEnum(),
            otherEventTypeDescription = null,
            eventTypeDetail = BackendEnum.create(null),
            otherEventTypeDetailDescription = null,
            eventResult = SrvaEventResult.ANIMAL_NOT_FOUND.toBackendEnum(),
            eventResultDetail = BackendEnum.create(null),
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
                        serverId = "serverId (not shown)",
                        localIdentifier = "localIdentifier (not shown)",
                        localUrl = "localUrl (not shown)",
                        status = EntityImage.Status.UPLOADED,
                    ),
                    EntityImage(
                        serverId = "serverId",
                        localIdentifier = "localIdentifier",
                        localUrl = "localUrl",
                        status = EntityImage.Status.UPLOADED,
                    ),
                )
            ),
        )
    }

    private fun String?.assertEquals(expected: RR.string) {
        assertEquals(getStringProvider().getString(expected))
    }

    private fun String?.assertEquals(expected: String) {
        assertEquals(expected, this)
    }


    private suspend fun getController(
        srvaEvent: CommonSrvaEvent = getSrvaEvent(),
        srvaContext: SrvaContext = getSrvaContext(),
        metadataProvider: MetadataProvider = getMetadataProvider(),
        stringProvider: StringProvider = getStringProvider()
    ): Pair<ViewSrvaEventController, CommonSrvaEvent> {
        val response = srvaContext.saveSrvaEvent(srvaEvent)
        val resultingEvent = (response as? SrvaEventOperationResponse.Success)?.srvaEvent

        assertNotNull(resultingEvent, "Failed to save srva event")

        val controller = ViewSrvaEventController(
            srvaEventId = resultingEvent.localId!!,
            srvaContext = srvaContext,
            metadataProvider = metadataProvider,
            stringProvider = stringProvider,
        )

        return controller to resultingEvent
    }

    private fun getSrvaContext(): SrvaContext {
        val dbDriverFactory = createDatabaseDriverFactory()
        val database = RiistaDatabase(driver = dbDriverFactory.createDriver())

        val mockUserContextProvider = CurrentUserContextProviderFactory.createMocked()
        runBlocking {
            mockUserContextProvider.userLoggedIn(MOCK_USER_INFO)
        }

        return SrvaContext(
            backendApiProvider = object : BackendApiProvider {
                override val backendAPI: BackendAPI = backendAPIMock
            },
            preferences = MockPreferences(),
            localDateTimeProvider = MockDateTimeProvider(),
            commonFileProvider = CommonFileProviderMock(),
            database = database,
            currentUserContextProvider = mockUserContextProvider,
        )
    }

    private fun getMetadataProvider(): MetadataProvider = MockMetadataProvider.INSTANCE
    private fun getStringProvider(): StringProvider = TestStringProvider.INSTANCE
    private val backendAPIMock = BackendAPIMock()

    companion object {
        private val MOCK_USER_INFO = UserInfoDTO(
            username = "user",
            personId = 123L,
            firstName = "user_first",
            lastName = "user_last",
            birthDate = null,
            address = null,
            homeMunicipality = LocalizedStringDTO(null, null, null),
            rhy = null,
            hunterNumber = null,
            hunterExamDate = null,
            huntingCardStart = null,
            huntingCardEnd = null,
            huntingBanStart = null,
            huntingBanEnd = null,
            huntingCardValidNow = true,
            qrCode = null,
            timestamp = "2022-01-01",
            shootingTests = emptyList(),
            occupations = emptyList(),
            enableSrva = true,
            enableShootingTests = false,
            deerPilotUser = true,
        )
    }
}
