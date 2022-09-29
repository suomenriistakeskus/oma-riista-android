@file:Suppress("ComplexRedundantLet")

package fi.riista.common.domain.srva.ui.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.model.CommonSrvaEventApprover
import fi.riista.common.domain.srva.model.CommonSrvaEventAuthor
import fi.riista.common.domain.srva.model.CommonSrvaSpecimen
import fi.riista.common.domain.srva.model.SrvaEventCategoryType
import fi.riista.common.domain.srva.model.SrvaEventResult
import fi.riista.common.domain.srva.model.SrvaEventResultDetail
import fi.riista.common.domain.srva.model.SrvaEventState
import fi.riista.common.domain.srva.model.SrvaEventType
import fi.riista.common.domain.srva.model.SrvaEventTypeDetail
import fi.riista.common.domain.srva.model.SrvaMethodType
import fi.riista.common.domain.srva.model.toCommonSpecimenData
import fi.riista.common.domain.srva.model.toCommonSrvaMethod
import fi.riista.common.domain.srva.model.toSrvaEventData
import fi.riista.common.domain.srva.ui.SrvaEventField
import fi.riista.common.helpers.TestStringProvider
import fi.riista.common.helpers.getBooleanField
import fi.riista.common.helpers.getDateTimeField
import fi.riista.common.helpers.getIntField
import fi.riista.common.helpers.getLabelField
import fi.riista.common.helpers.getLoadedViewModel
import fi.riista.common.helpers.getLocationField
import fi.riista.common.helpers.getSpeciesField
import fi.riista.common.helpers.getSpecimenField
import fi.riista.common.helpers.getStringField
import fi.riista.common.helpers.getStringListField
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.metadata.MockMetadataProvider
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.EMPTY_BACKEND_ENUM_VALUE_ID
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.toLocalizedStringWithId
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.BooleanField
import fi.riista.common.ui.dataField.LabelField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditSrvaEventControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = getController(srvaEvent = null)
        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoadedWithSpecVersion1() = runBlockingTest {
        val initialSrva = getSrvaEvent().copy(
            srvaSpecVersion = 1
        )
        val controller = getController(srvaEvent = initialSrva)

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        assertEquals(initialSrva.toSrvaEventData().copy(srvaSpecVersion = 2), viewModel.srvaEvent)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val initialSrva = getSrvaEvent()
        val controller = getController(srvaEvent = initialSrva)

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        assertEquals(initialSrva.toSrvaEventData(), viewModel.srvaEvent)
    }

    @Test
    fun testProducedFieldsMatchData() = runBlockingTest {
        val initialSrva = getSrvaEvent()
        val controller = getController(srvaEvent = initialSrva)

        controller.loadViewModel()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(14, fields.size)
        var expectedIndex = 0
        fields.getLocationField(expectedIndex++, SrvaEventField.Type.LOCATION.toField()).let {
            assertEquals(initialSrva.location.asKnownLocation(), it.location)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getSpeciesField(expectedIndex++, SrvaEventField.Type.SPECIES_CODE.toField()).let {
            assertEquals(initialSrva.species, it.species)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
            assertTrue(it.settings.showEntityImage)
            assertEquals("serverId", it.entityImage?.serverId)
            assertEquals("localIdentifier", it.entityImage?.localIdentifier)
            assertEquals("localUrl", it.entityImage?.localUrl)
        }
        fields.getDateTimeField(expectedIndex++, SrvaEventField.Type.DATE_AND_TIME.toField()).let {
            assertEquals(initialSrva.pointOfTime, it.dateAndTime)
            assertNull(it.settings.label)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, SrvaEventField.Type.SPECIMEN_AMOUNT.toField()).let {
            assertEquals(initialSrva.specimens.size, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_specimen_amount)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
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
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, SrvaEventField.Type.EVENT_CATEGORY.toField()).let {
            assertEquals(listOf(initialSrva.eventCategory.value!!.ordinal.toLong()), it.selected)
            assertEquals(
                expected = listOf(
                    SrvaEventCategoryType.ACCIDENT,
                    SrvaEventCategoryType.DEPORTATION,
                    SrvaEventCategoryType.INJURED_ANIMAL
                ).map { type -> type.toLocalizedStringWithId(getStringProvider()) },
                actual = it.values
            )
            it.settings.label.assertEquals(RR.string.srva_event_label_event_category)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, SrvaEventField.Type.EVENT_TYPE.toField()).let {
            assertEquals(listOf(initialSrva.eventType.value!!.ordinal.toLong()), it.selected)
            it.settings.label.assertEquals(RR.string.srva_event_label_event_type)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, SrvaEventField.Type.EVENT_RESULT.toField()).let {
            assertEquals(listOf(initialSrva.eventResult.value!!.ordinal.toLong()), it.selected)
            it.settings.label.assertEquals(RR.string.srva_event_label_event_result)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getLabelField(expectedIndex++, SrvaEventField.Type.METHOD_HEADER.toField()).let {
            it.text.assertEquals(RR.string.srva_event_label_method)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getBooleanField(
            expectedIndex = expectedIndex++,
            id = SrvaEventField(SrvaEventField.Type.METHOD_ITEM, index = 0)
        ).let {
            assertEquals(true, it.value)
            assertEquals(BooleanField.Appearance.CHECKBOX, it.settings.appearance)
            it.settings.label.assertEquals(RR.string.srva_method_traced_with_dog)
            assertFalse(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getBooleanField(
            expectedIndex = expectedIndex++,
            id = SrvaEventField(SrvaEventField.Type.METHOD_ITEM, index = 1)
        ).let {
            assertEquals(true, it.value)
            assertEquals(BooleanField.Appearance.CHECKBOX, it.settings.appearance)
            it.settings.label.assertEquals(RR.string.srva_method_sound_equipment)
            assertFalse(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, SrvaEventField.Type.PERSON_COUNT.toField()).let {
            assertEquals(initialSrva.personCount, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_person_count)
            assertFalse(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, SrvaEventField.Type.HOURS_SPENT.toField()).let {
            assertEquals(initialSrva.hoursSpent, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_hours_spent)
            assertFalse(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex, SrvaEventField.Type.DESCRIPTION.toField()).let {
            assertEquals(initialSrva.description, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_description)
            assertFalse(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
    }

    @Test
    fun testFieldsAreProducedForMissingCategory() = runBlockingTest {
        val initialSrva = getSrvaEvent().copy(
            eventCategory = BackendEnum.create(null),
        )
        val controller = getController(srvaEvent = initialSrva)

        controller.loadViewModel()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(9, fields.size)
        var expectedIndex = 0
        fields.getLocationField(expectedIndex++, SrvaEventField.Type.LOCATION.toField()).let {
            assertEquals(initialSrva.location.asKnownLocation(), it.location)
            assertFalse(it.settings.readOnly)
        }
        fields.getSpeciesField(expectedIndex++, SrvaEventField.Type.SPECIES_CODE.toField()).let {
            assertEquals(initialSrva.species, it.species)
            assertFalse(it.settings.readOnly)
            assertTrue(it.settings.showEntityImage)
        }
        fields.getDateTimeField(expectedIndex++, SrvaEventField.Type.DATE_AND_TIME.toField()).let {
            assertEquals(initialSrva.pointOfTime, it.dateAndTime)
            assertNull(it.settings.label)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, SrvaEventField.Type.SPECIMEN_AMOUNT.toField()).let {
            assertEquals(initialSrva.specimens.size, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_specimen_amount)
            assertFalse(it.settings.readOnly)
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
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, SrvaEventField.Type.EVENT_CATEGORY.toField()).let {
            assertEquals(listOf(-1L), it.selected)
            assertEquals(
                expected = listOf(
                    BackendEnum.create<SrvaEventCategoryType>(null).toLocalizedStringWithId(getStringProvider())
                ) + listOf(
                    SrvaEventCategoryType.ACCIDENT,
                    SrvaEventCategoryType.DEPORTATION,
                    SrvaEventCategoryType.INJURED_ANIMAL
                ).map { type -> type.toLocalizedStringWithId(getStringProvider()) },
                actual = it.values
            )
            it.settings.label.assertEquals(RR.string.srva_event_label_event_category)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, SrvaEventField.Type.PERSON_COUNT.toField()).let {
            assertEquals(initialSrva.personCount, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_person_count)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, SrvaEventField.Type.HOURS_SPENT.toField()).let {
            assertEquals(initialSrva.hoursSpent, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_hours_spent)
            assertFalse(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex, SrvaEventField.Type.DESCRIPTION.toField()).let {
            assertEquals(initialSrva.description, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_description)
            assertFalse(it.settings.readOnly)
        }
    }

    @Test
    fun testFieldsAreProducedForMissingData() = runBlockingTest {
        val initialSrva = getSrvaEvent().copy(
            eventType = BackendEnum.create(null),
            eventResult = BackendEnum.create(null),
            images = EntityImages(
                remoteImageIds = listOf(),
                localImages = listOf(),
            )
        )
        val controller = getController(srvaEvent = initialSrva)

        controller.loadViewModel()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(14, fields.size)
        var expectedIndex = 0
        fields.getLocationField(expectedIndex++, SrvaEventField.Type.LOCATION.toField()).let {
            assertEquals(initialSrva.location.asKnownLocation(), it.location)
            assertFalse(it.settings.readOnly)
        }
        fields.getSpeciesField(expectedIndex++, SrvaEventField.Type.SPECIES_CODE.toField()).let {
            assertEquals(initialSrva.species, it.species)
            assertFalse(it.settings.readOnly)
            assertTrue(it.settings.showEntityImage)
            assertNull(it.entityImage)
        }
        fields.getDateTimeField(expectedIndex++, SrvaEventField.Type.DATE_AND_TIME.toField()).let {
            assertEquals(initialSrva.pointOfTime, it.dateAndTime)
            assertNull(it.settings.label)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, SrvaEventField.Type.SPECIMEN_AMOUNT.toField()).let {
            assertEquals(initialSrva.specimens.size, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_specimen_amount)
            assertFalse(it.settings.readOnly)
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
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, SrvaEventField.Type.EVENT_CATEGORY.toField()).let {
            assertEquals(listOf(0L), it.selected)
            assertEquals(
                expected = listOf(
                    SrvaEventCategoryType.ACCIDENT,
                    SrvaEventCategoryType.DEPORTATION,
                    SrvaEventCategoryType.INJURED_ANIMAL
                ).map { type -> type.toLocalizedStringWithId(getStringProvider()) },
                actual = it.values
            )
            it.settings.label.assertEquals(RR.string.srva_event_label_event_category)
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, SrvaEventField.Type.EVENT_TYPE.toField()).let {
            assertEquals(listOf(-1L), it.selected)
            assertEquals(
                expected = listOf(
                    BackendEnum.create<SrvaEventType>(null).toLocalizedStringWithId(getStringProvider())
                ) + listOf(
                    BackendEnum.create(SrvaEventType.TRAFFIC_ACCIDENT).toLocalizedStringWithId(getStringProvider()),
                    BackendEnum.create(SrvaEventType.RAILWAY_ACCIDENT).toLocalizedStringWithId(getStringProvider()),
                    BackendEnum.create(SrvaEventType.OTHER).toLocalizedStringWithId(getStringProvider()),
                ),
                actual = it.values
            )
            it.settings.label.assertEquals(RR.string.srva_event_label_event_type)
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, SrvaEventField.Type.EVENT_RESULT.toField()).let {
            assertEquals(listOf(-1L), it.selected)
            assertEquals(
                expected = listOf(
                    BackendEnum.create<SrvaEventResult>(null).toLocalizedStringWithId(getStringProvider())
                ) + listOf(
                    BackendEnum.create(SrvaEventResult.ANIMAL_FOUND_DEAD).toLocalizedStringWithId(getStringProvider()),
                    BackendEnum.create(SrvaEventResult.ANIMAL_FOUND_AND_TERMINATED).toLocalizedStringWithId(getStringProvider()),
                    BackendEnum.create(SrvaEventResult.ANIMAL_FOUND_AND_NOT_TERMINATED).toLocalizedStringWithId(getStringProvider()),
                    BackendEnum.create(SrvaEventResult.ACCIDENT_SITE_NOT_FOUND).toLocalizedStringWithId(getStringProvider()),
                    BackendEnum.create(SrvaEventResult.ANIMAL_NOT_FOUND).toLocalizedStringWithId(getStringProvider()),
                    BackendEnum.create(SrvaEventResult.UNDUE_ALARM).toLocalizedStringWithId(getStringProvider()),
                ),
                actual = it.values
            )
            it.settings.label.assertEquals(RR.string.srva_event_label_event_result)
            assertFalse(it.settings.readOnly)
        }
        fields.getLabelField(expectedIndex++, SrvaEventField.Type.METHOD_HEADER.toField()).let {
            it.text.assertEquals(RR.string.srva_event_label_method)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getBooleanField(
            expectedIndex = expectedIndex++,
            id = SrvaEventField(SrvaEventField.Type.METHOD_ITEM, index = 0)
        ).let {
            assertEquals(true, it.value)
            assertEquals(BooleanField.Appearance.CHECKBOX, it.settings.appearance)
            it.settings.label.assertEquals(RR.string.srva_method_traced_with_dog)
            assertFalse(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getBooleanField(
            expectedIndex = expectedIndex++,
            id = SrvaEventField(SrvaEventField.Type.METHOD_ITEM, index = 1)
        ).let {
            assertEquals(true, it.value)
            assertEquals(BooleanField.Appearance.CHECKBOX, it.settings.appearance)
            it.settings.label.assertEquals(RR.string.srva_method_sound_equipment)
            assertFalse(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, SrvaEventField.Type.PERSON_COUNT.toField()).let {
            assertEquals(initialSrva.personCount, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_person_count)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, SrvaEventField.Type.HOURS_SPENT.toField()).let {
            assertEquals(initialSrva.hoursSpent, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_hours_spent)
            assertFalse(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex, SrvaEventField.Type.DESCRIPTION.toField()).let {
            assertEquals(initialSrva.description, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_description)
            assertFalse(it.settings.readOnly)
        }
    }

    @Test
    fun testProducedFieldsHaveApprover() = runBlockingTest {
        val initialSrva = getSrvaEvent().copy(
            state = SrvaEventState.APPROVED.toBackendEnum()
        )
        val controller = getController(srvaEvent = initialSrva)

        controller.loadViewModel()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(15, fields.size)
        fields.getStringField(3, SrvaEventField.Type.APPROVER_OR_REJECTOR.toField()).let {
            assertEquals("Asko Partanen", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_approver)
            assertTrue(it.settings.readOnly)
        }
    }

    @Test
    fun testProducedFieldsHaveRejector() = runBlockingTest {
        val initialSrva = getSrvaEvent().copy(
            state = SrvaEventState.REJECTED.toBackendEnum()
        )
        val controller = getController(srvaEvent = initialSrva)

        controller.loadViewModel()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(15, fields.size)
        fields.getStringField(3, SrvaEventField.Type.APPROVER_OR_REJECTOR.toField()).let {
            assertEquals("Asko Partanen", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_rejector)
            assertTrue(it.settings.readOnly)
        }
    }

    @Test
    fun testProducedFieldsMatchDataForDeportation() = runBlockingTest {
        val initialSrva = getSrvaEvent().copy(
            eventCategory = SrvaEventCategoryType.DEPORTATION.toBackendEnum(),
            deportationOrderNumber = "deportationOrderNumber",
            eventType = SrvaEventType.ANIMAL_NEAR_HOUSES_AREA.toBackendEnum(),
            eventTypeDetail = SrvaEventTypeDetail.OTHER.toBackendEnum(),
            otherEventTypeDetailDescription = "otherEventTypeDetailDescription",
            eventResult = SrvaEventResult.ANIMAL_DEPORTED.toBackendEnum(),
            eventResultDetail = SrvaEventResultDetail.ANIMAL_CONTACTED.toBackendEnum(),
        )
        val controller = getController(srvaEvent = initialSrva)

        controller.loadViewModel()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(18, fields.size)
        var expectedIndex = 0
        fields.getLocationField(expectedIndex++, SrvaEventField.Type.LOCATION.toField()).let {
            assertEquals(initialSrva.location.asKnownLocation(), it.location)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getSpeciesField(expectedIndex++, SrvaEventField.Type.SPECIES_CODE.toField()).let {
            assertEquals(initialSrva.species, it.species)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
            assertTrue(it.settings.showEntityImage)
            assertEquals("serverId", it.entityImage?.serverId)
            assertEquals("localIdentifier", it.entityImage?.localIdentifier)
            assertEquals("localUrl", it.entityImage?.localUrl)
        }
        fields.getDateTimeField(expectedIndex++, SrvaEventField.Type.DATE_AND_TIME.toField()).let {
            assertEquals(initialSrva.pointOfTime, it.dateAndTime)
            assertNull(it.settings.label)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, SrvaEventField.Type.SPECIMEN_AMOUNT.toField()).let {
            assertEquals(initialSrva.specimens.size, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_specimen_amount)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
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
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, SrvaEventField.Type.EVENT_CATEGORY.toField()).let {
            assertEquals(listOf(initialSrva.eventCategory.value!!.ordinal.toLong()), it.selected)
            assertEquals(
                expected = listOf(
                    SrvaEventCategoryType.ACCIDENT,
                    SrvaEventCategoryType.DEPORTATION,
                    SrvaEventCategoryType.INJURED_ANIMAL
                ).map { type -> type.toLocalizedStringWithId(getStringProvider()) },
                actual = it.values
            )
            it.settings.label.assertEquals(RR.string.srva_event_label_event_category)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.DEPORTATION_ORDER_NUMBER.toField()).let {
            assertEquals("deportationOrderNumber", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_deportation_order_number)
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, SrvaEventField.Type.EVENT_TYPE.toField()).let {
            assertEquals(listOf(initialSrva.eventType.value!!.ordinal.toLong()), it.selected)
            it.settings.label.assertEquals(RR.string.srva_event_label_event_type)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, SrvaEventField.Type.EVENT_TYPE_DETAIL.toField()).let {
            assertEquals(listOf(initialSrva.eventTypeDetail.value!!.ordinal.toLong()), it.selected)
            it.settings.label.assertEquals(RR.string.srva_event_label_event_type_detail)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_OTHER_TYPE_DETAIL_DESCRIPTION.toField()).let {
            assertEquals("otherEventTypeDetailDescription", it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_other_event_type_detail_description)
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, SrvaEventField.Type.EVENT_RESULT.toField()).let {
            assertEquals(listOf(initialSrva.eventResult.value!!.ordinal.toLong()), it.selected)
            it.settings.label.assertEquals(RR.string.srva_event_label_event_result)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, SrvaEventField.Type.EVENT_RESULT_DETAIL.toField()).let {
            assertEquals(listOf(initialSrva.eventResultDetail.value!!.ordinal.toLong()), it.selected)
            it.settings.label.assertEquals(RR.string.srva_event_label_event_result_detail)
            assertTrue(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getLabelField(expectedIndex++, SrvaEventField.Type.METHOD_HEADER.toField()).let {
            it.text.assertEquals(RR.string.srva_event_label_method)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getBooleanField(
            expectedIndex = expectedIndex++,
            id = SrvaEventField(SrvaEventField.Type.METHOD_ITEM, index = 0)
        ).let {
            assertEquals(true, it.value)
            assertEquals(BooleanField.Appearance.CHECKBOX, it.settings.appearance)
            it.settings.label.assertEquals(RR.string.srva_method_traced_with_dog)
            assertFalse(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getBooleanField(
            expectedIndex = expectedIndex++,
            id = SrvaEventField(SrvaEventField.Type.METHOD_ITEM, index = 1)
        ).let {
            assertEquals(true, it.value)
            assertEquals(BooleanField.Appearance.CHECKBOX, it.settings.appearance)
            it.settings.label.assertEquals(RR.string.srva_method_sound_equipment)
            assertFalse(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, SrvaEventField.Type.PERSON_COUNT.toField()).let {
            assertEquals(initialSrva.personCount, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_person_count)
            assertFalse(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, SrvaEventField.Type.HOURS_SPENT.toField()).let {
            assertEquals(initialSrva.hoursSpent, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_hours_spent)
            assertFalse(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex, SrvaEventField.Type.DESCRIPTION.toField()).let {
            assertEquals(initialSrva.description, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_description)
            assertFalse(it.settings.requirementStatus.isRequired())
            assertFalse(it.settings.readOnly)
        }
    }

    @Test
    fun testEventTypeDetailIsValidatedAfterChangingSpecies() = runBlockingTest {
        val initialSrva = getSrvaEvent().copy(
            species = Species.Known(SpeciesCodes.BEAR_ID),
            eventCategory = SrvaEventCategoryType.DEPORTATION.toBackendEnum(),
            deportationOrderNumber = "deportationOrderNumber",
            eventType = SrvaEventType.ANIMAL_AT_FOOD_DESTINATION.toBackendEnum(),
            eventTypeDetail = SrvaEventTypeDetail.BEEHIVE.toBackendEnum(),
        )
        val controller = getController(srvaEvent = initialSrva)

        controller.loadViewModel()

        with (controller.getLoadedViewModel()) {
            assertEquals(Species.Known(SpeciesCodes.BEAR_ID), srvaEvent.species)
            assertEquals(SrvaEventTypeDetail.BEEHIVE.toBackendEnum(), srvaEvent.eventTypeDetail)
            fields.getStringListField(8, SrvaEventField.Type.EVENT_TYPE_DETAIL.toField()).let {
                assertEquals(listOf(initialSrva.eventTypeDetail.value!!.ordinal.toLong()), it.selected)
                it.settings.label.assertEquals(RR.string.srva_event_label_event_type_detail)
                assertTrue(it.settings.requirementStatus.isRequired())
                assertFalse(it.settings.readOnly)
            }
        }

        controller.eventDispatchers.speciesEventDispatcher.dispatchSpeciesChanged(
            fieldId = SrvaEventField.Type.SPECIES_CODE.toField(),
            value = Species.Known(SpeciesCodes.MOOSE_ID)
        )

        with (controller.getLoadedViewModel()) {
            assertEquals(Species.Known(SpeciesCodes.MOOSE_ID), srvaEvent.species)
            assertEquals(BackendEnum.create<SrvaEventTypeDetail>(null), srvaEvent.eventTypeDetail)
            fields.getStringListField(8, SrvaEventField.Type.EVENT_TYPE_DETAIL.toField()).let {
                assertEquals(listOf(EMPTY_BACKEND_ENUM_VALUE_ID), it.selected) // -1
                it.settings.label.assertEquals(RR.string.srva_event_label_event_type_detail)
                assertTrue(it.settings.requirementStatus.isRequired())
                assertFalse(it.settings.readOnly)
            }
        }
    }

    @Test
    fun testEventTypeDetailIsValidatedAfterChangingEventType() = runBlockingTest {
        val initialSrva = getSrvaEvent().copy(
            eventCategory = SrvaEventCategoryType.DEPORTATION.toBackendEnum(),
            deportationOrderNumber = "deportationOrderNumber",
            eventType = SrvaEventType.ANIMAL_NEAR_HOUSES_AREA.toBackendEnum(),
            eventTypeDetail = SrvaEventTypeDetail.FARM_ANIMAL_BUILDING.toBackendEnum(),
        )
        val controller = getController(srvaEvent = initialSrva)

        controller.loadViewModel()

        with (controller.getLoadedViewModel()) {
            assertEquals(SrvaEventTypeDetail.FARM_ANIMAL_BUILDING.toBackendEnum(), srvaEvent.eventTypeDetail)
            fields.getStringListField(8, SrvaEventField.Type.EVENT_TYPE_DETAIL.toField()).let {
                assertEquals(listOf(initialSrva.eventTypeDetail.value!!.ordinal.toLong()), it.selected)
                it.settings.label.assertEquals(RR.string.srva_event_label_event_type_detail)
                assertTrue(it.settings.requirementStatus.isRequired())
                assertFalse(it.settings.readOnly)
            }
        }

        controller.eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
            fieldId = SrvaEventField.Type.EVENT_TYPE.toField(),
            value = listOf(
                SrvaEventType.ANIMAL_AT_FOOD_DESTINATION.toBackendEnum().toLocalizedStringWithId(getStringProvider())
            )
        )

        with (controller.getLoadedViewModel()) {
            assertEquals(BackendEnum.create<SrvaEventTypeDetail>(null), srvaEvent.eventTypeDetail)
            fields.getStringListField(8, SrvaEventField.Type.EVENT_TYPE_DETAIL.toField()).let {
                assertEquals(listOf(EMPTY_BACKEND_ENUM_VALUE_ID), it.selected) // -1
                it.settings.label.assertEquals(RR.string.srva_event_label_event_type_detail)
                assertTrue(it.settings.requirementStatus.isRequired())
                assertFalse(it.settings.readOnly)
            }
        }
    }

    @Test
    fun testEventResultDetailIsValidatedAfterChangingEventResult() = runBlockingTest {
        val initialSrva = getSrvaEvent().copy(
            eventCategory = SrvaEventCategoryType.DEPORTATION.toBackendEnum(),
            deportationOrderNumber = "deportationOrderNumber",
            eventResult = SrvaEventResult.ANIMAL_DEPORTED.toBackendEnum(),
            eventResultDetail = SrvaEventResultDetail.ANIMAL_CONTACTED_AND_DEPORTED.toBackendEnum(),
        )
        val controller = getController(srvaEvent = initialSrva)

        controller.loadViewModel()

        with (controller.getLoadedViewModel()) {
            assertEquals(SrvaEventResultDetail.ANIMAL_CONTACTED_AND_DEPORTED.toBackendEnum(), srvaEvent.eventResultDetail)
            fields.getStringListField(9, SrvaEventField.Type.EVENT_RESULT_DETAIL.toField()).let {
                assertEquals(listOf(initialSrva.eventResultDetail.value!!.ordinal.toLong()), it.selected)
                it.settings.label.assertEquals(RR.string.srva_event_label_event_result_detail)
                assertTrue(it.settings.requirementStatus.isRequired())
                assertFalse(it.settings.readOnly)
            }
        }

        controller.eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
            fieldId = SrvaEventField.Type.EVENT_RESULT.toField(),
            value = listOf(
                SrvaEventResult.ANIMAL_TERMINATED.toBackendEnum().toLocalizedStringWithId(getStringProvider())
            )
        )

        with (controller.getLoadedViewModel()) {
            assertEquals(BackendEnum.create<SrvaEventResultDetail>(null), srvaEvent.eventResultDetail)
            assertNull(fields.find { it.id == SrvaEventField.Type.EVENT_RESULT_DETAIL.toField() })
        }
    }

    @Test
    fun testDescriptionsAreDisplayedForOtherValues() = runBlockingTest {
        val initialSrva = getSrvaEvent().copy(
            eventType = SrvaEventType.OTHER.toBackendEnum(),
            otherEventTypeDescription = "Lehmä katolla",
            methods = listOf(
                SrvaMethodType.TRACED_WITH_DOG.toBackendEnum().toCommonSrvaMethod(selected = true),
                SrvaMethodType.OTHER.toBackendEnum().toCommonSrvaMethod(selected = true),
            ),
            otherMethodDescription = "Kiikareilla katsottiin"
        )
        val controller = getController(srvaEvent = initialSrva)
        controller.loadViewModel()

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(16, fields.size)
        var expectedIndex = 0
        fields.getLocationField(expectedIndex++, SrvaEventField.Type.LOCATION.toField()).let {
            assertEquals(initialSrva.location.asKnownLocation(), it.location)
            assertFalse(it.settings.readOnly)
        }
        fields.getSpeciesField(expectedIndex++, SrvaEventField.Type.SPECIES_CODE.toField()).let {
            assertEquals(initialSrva.species, it.species)
            assertFalse(it.settings.readOnly)
            assertTrue(it.settings.showEntityImage)
            assertEquals("serverId", it.entityImage?.serverId)
            assertEquals("localIdentifier", it.entityImage?.localIdentifier)
            assertEquals("localUrl", it.entityImage?.localUrl)
        }
        fields.getDateTimeField(expectedIndex++, SrvaEventField.Type.DATE_AND_TIME.toField()).let {
            assertEquals(initialSrva.pointOfTime, it.dateAndTime)
            assertNull(it.settings.label)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, SrvaEventField.Type.SPECIMEN_AMOUNT.toField()).let {
            assertEquals(initialSrva.specimens.size, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_specimen_amount)
            assertFalse(it.settings.readOnly)
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
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, SrvaEventField.Type.EVENT_CATEGORY.toField()).let {
            assertEquals(listOf(initialSrva.eventCategory.value!!.ordinal.toLong()), it.selected)
            it.settings.label.assertEquals(RR.string.srva_event_label_event_category)
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, SrvaEventField.Type.EVENT_TYPE.toField()).let {
            assertEquals(listOf(initialSrva.eventType.value!!.ordinal.toLong()), it.selected)
            it.settings.label.assertEquals(RR.string.srva_event_label_event_type)
            assertFalse(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.EVENT_OTHER_TYPE_DESCRIPTION.toField()).let {
            assertEquals(initialSrva.otherEventTypeDescription, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_other_event_type_description)
            assertFalse(it.settings.readOnly)
        }
        fields.getStringListField(expectedIndex++, SrvaEventField.Type.EVENT_RESULT.toField()).let {
            assertEquals(listOf(initialSrva.eventResult.value!!.ordinal.toLong()), it.selected)
            it.settings.label.assertEquals(RR.string.srva_event_label_event_result)
            assertFalse(it.settings.readOnly)
        }
        fields.getLabelField(expectedIndex++, SrvaEventField.Type.METHOD_HEADER.toField()).let {
            it.text.assertEquals(RR.string.srva_event_label_method)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getBooleanField(
            expectedIndex = expectedIndex++,
            id = SrvaEventField(SrvaEventField.Type.METHOD_ITEM, index = 0)
        ).let {
            assertEquals(true, it.value)
            assertEquals(BooleanField.Appearance.CHECKBOX, it.settings.appearance)
            it.settings.label.assertEquals(RR.string.srva_method_traced_with_dog)
            assertFalse(it.settings.readOnly)
        }
        fields.getBooleanField(
            expectedIndex = expectedIndex++,
            id = SrvaEventField(SrvaEventField.Type.METHOD_ITEM, index = 1)
        ).let {
            assertEquals(true, it.value)
            assertEquals(BooleanField.Appearance.CHECKBOX, it.settings.appearance)
            it.settings.label.assertEquals(RR.string.srva_method_other)
            assertFalse(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SrvaEventField.Type.OTHER_METHOD_DESCRIPTION.toField()).let {
            assertEquals(initialSrva.otherMethodDescription, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_other_method_description)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, SrvaEventField.Type.PERSON_COUNT.toField()).let {
            assertEquals(initialSrva.personCount, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_person_count)
            assertFalse(it.settings.readOnly)
        }
        fields.getIntField(expectedIndex++, SrvaEventField.Type.HOURS_SPENT.toField()).let {
            assertEquals(initialSrva.hoursSpent, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_hours_spent)
            assertFalse(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex, SrvaEventField.Type.DESCRIPTION.toField()).let {
            assertEquals(initialSrva.description, it.value)
            it.settings.label.assertEquals(RR.string.srva_event_label_description)
            assertFalse(it.settings.readOnly)
        }
    }

    @Test
    fun testSpeciesCanBeChanged() = runBlockingTest {
        val initialSrva = getSrvaEvent()
        val controller = getController(srvaEvent = initialSrva)
        controller.loadViewModel()

        val speciesIndex = 1
        with (controller.getLoadedViewModel()) {
            fields.getSpeciesField(speciesIndex, SrvaEventField.Type.SPECIES_CODE.toField()).let {
                assertEquals(initialSrva.species, it.species)
            }
        }

        var newSpecies: Species = Species.Known(speciesCode = SpeciesCodes.ROE_DEER_ID)
        controller.eventDispatchers.speciesEventDispatcher.dispatchSpeciesChanged(
            fieldId = SrvaEventField.Type.SPECIES_CODE.toField(),
            value = newSpecies
        )

        with (controller.getLoadedViewModel()) {
            assertEquals(newSpecies, srvaEvent.species)
            fields.getSpeciesField(speciesIndex, SrvaEventField.Type.SPECIES_CODE.toField()).let {
                assertEquals(newSpecies, it.species)
            }
        }

        newSpecies = Species.Other
        controller.eventDispatchers.speciesEventDispatcher.dispatchSpeciesChanged(
            fieldId = SrvaEventField.Type.SPECIES_CODE.toField(),
            value = Species.Other
        )

        with (controller.getLoadedViewModel()) {
            assertEquals(newSpecies, srvaEvent.species)
            fields.getSpeciesField(speciesIndex, SrvaEventField.Type.SPECIES_CODE.toField()).let {
                assertEquals(newSpecies, it.species)
            }
        }
    }

    @Test
    fun testSpeciesCannotBeChangedToNonAllowed() = runBlockingTest {
        val initialSrva = getSrvaEvent()
        val controller = getController(srvaEvent = initialSrva)
        controller.loadViewModel()

        val speciesIndex = 1
        with (controller.getLoadedViewModel()) {
            fields.getSpeciesField(speciesIndex, SrvaEventField.Type.SPECIES_CODE.toField()).let {
                assertEquals(initialSrva.species, it.species)
            }
        }

        controller.eventDispatchers.speciesEventDispatcher.dispatchSpeciesChanged(
            fieldId = SrvaEventField.Type.SPECIES_CODE.toField(),
            value = Species.Known(speciesCode = SpeciesCodes.COMMON_EIDER_ID)
        )

        with (controller.getLoadedViewModel()) {
            assertEquals(initialSrva.species, srvaEvent.species)
            fields.getSpeciesField(speciesIndex, SrvaEventField.Type.SPECIES_CODE.toField()).let {
                assertEquals(initialSrva.species, it.species)
            }
        }
    }

    @Test
    fun testImageCanBeChanged() = runBlockingTest {
        val initialSrva = getSrvaEvent()
        val controller = getController(srvaEvent = initialSrva)
        controller.loadViewModel()

        val speciesIndex = 1
        with (controller.getLoadedViewModel()) {
            assertEquals(2, srvaEvent.images.localImages.size)
            assertEquals(initialSrva.images.primaryImage, srvaEvent.images.primaryImage)
            fields.getSpeciesField(speciesIndex, SrvaEventField.Type.SPECIES_CODE.toField()).let {
                assertEquals("serverId", it.entityImage?.serverId)
                assertEquals("localIdentifier", it.entityImage?.localIdentifier)
                assertEquals("localUrl", it.entityImage?.localUrl)
            }
        }

        val newImage1 = EntityImage(
            serverId = "newImage1",
            localIdentifier = null,
            localUrl = "newImage1Url",
            status = EntityImage.Status.LOCAL
        )
        controller.eventDispatchers.imageEventDispatcher.setEntityImage(newImage1)

        with (controller.getLoadedViewModel()) {
            assertEquals(3, srvaEvent.images.localImages.size)
            assertEquals(newImage1, srvaEvent.images.primaryImage)
            assertEquals(newImage1, srvaEvent.images.localImages[2])
            fields.getSpeciesField(speciesIndex, SrvaEventField.Type.SPECIES_CODE.toField()).let {
                assertEquals(newImage1.serverId, it.entityImage?.serverId)
                assertEquals(newImage1.localIdentifier, it.entityImage?.localIdentifier)
                assertEquals(newImage1.localUrl, it.entityImage?.localUrl)
            }
        }

        val newImage2 = EntityImage(
            serverId = "newImage2",
            localIdentifier = null,
            localUrl = "newImage2Url",
            status = EntityImage.Status.LOCAL
        )
        controller.eventDispatchers.imageEventDispatcher.setEntityImage(newImage2)

        with (controller.getLoadedViewModel()) {
            assertEquals(4, srvaEvent.images.localImages.size)
            assertEquals(newImage2, srvaEvent.images.primaryImage)
            assertEquals(newImage1.copy(status = EntityImage.Status.LOCAL_TO_BE_REMOVED), srvaEvent.images.localImages[2])
            assertEquals(newImage2, srvaEvent.images.localImages[3])
            fields.getSpeciesField(speciesIndex, SrvaEventField.Type.SPECIES_CODE.toField()).let {
                assertEquals(newImage2.serverId, it.entityImage?.serverId)
                assertEquals(newImage2.localIdentifier, it.entityImage?.localIdentifier)
                assertEquals(newImage2.localUrl, it.entityImage?.localUrl)
            }
        }
    }

    private fun getSrvaEvent(): CommonSrvaEvent {
        return CommonSrvaEvent(
            localId = 99,
            localUrl = null,
            remoteId = 100,
            revision = 2,
            mobileClientRefId = 33L,
            srvaSpecVersion = Constants.SRVA_SPEC_VERSION,
            state = SrvaEventState.UNFINISHED.toBackendEnum(),
            rhyId = 12,
            canEdit = true,
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

    private fun getController(
        metadataProvider: MetadataProvider = getMetadataProvider(),
        stringProvider: StringProvider = getStringProvider(),
        srvaEvent: CommonSrvaEvent? = getSrvaEvent(),
    ): EditSrvaEventController {
        return EditSrvaEventController(
            metadataProvider = metadataProvider,
            stringProvider = stringProvider,
        ).also {
            srvaEvent?.let { event ->
                it.editableSrvaEvent = EditableSrvaEvent(srvaEvent = event)
            }
        }
    }

    private fun getMetadataProvider(): MetadataProvider = MockMetadataProvider.INSTANCE
    private fun getStringProvider(): StringProvider = TestStringProvider.INSTANCE

}
