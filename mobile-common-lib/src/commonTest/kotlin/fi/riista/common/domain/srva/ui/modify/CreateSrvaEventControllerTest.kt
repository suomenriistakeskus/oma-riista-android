package fi.riista.common.domain.srva.ui.modify

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.helpers.*
import fi.riista.common.logging.getLogger
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.metadata.MockMetadataProvider
import fi.riista.common.domain.model.*
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.toLocalizedStringWithId
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.domain.srva.model.SrvaEventCategoryType
import fi.riista.common.domain.srva.model.SrvaEventResult
import fi.riista.common.domain.srva.model.SrvaEventType
import fi.riista.common.domain.srva.ui.SrvaEventField
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.StringWithId
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.MockDateTimeProvider
import kotlin.test.*

class CreateSrvaEventControllerTest {

    private val mockMetadataProvider: MetadataProvider = MockMetadataProvider.INSTANCE
    private val mockStringProvider: StringProvider = TestStringProvider.INSTANCE
    private val mockDateTimeProvider = MockDateTimeProvider()

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = getController()
        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = getController()

        controller.loadViewModel()

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        with (viewModel) {
            assertEquals(9, fields.size)
            assertFalse(srvaEventIsValid, "SRVA should not be valid initially")
            var expectedIndex = 0
            fields.getLocationField(expectedIndex++, SrvaEventField.Type.LOCATION.toField()).let {
                assertEquals(CommonLocation.Unknown, it.location)
                assertTrue(it.settings.requirementStatus.isRequired())
                assertFalse(it.settings.readOnly)
            }
            fields.getSpeciesField(expectedIndex++, SrvaEventField.Type.SPECIES_CODE.toField()).let {
                assertEquals(Species.Unknown, it.species)
                assertTrue(it.settings.requirementStatus.isRequired())
                assertFalse(it.settings.readOnly)
                assertTrue(it.settings.showEntityImage)
                assertNull(it.entityImage)
            }
            fields.getDateTimeField(expectedIndex++, SrvaEventField.Type.DATE_AND_TIME.toField()).let {
                assertEquals(mockDateTimeProvider.now, it.dateAndTime)
                assertNull(it.settings.label)
                assertTrue(it.settings.requirementStatus.isRequired())
                assertFalse(it.settings.readOnly)
            }
            fields.getIntField(expectedIndex++, SrvaEventField.Type.SPECIMEN_AMOUNT.toField()).let {
                assertEquals(1, it.value)
                it.settings.label.assertEquals(RR.string.srva_event_label_specimen_amount)
                assertTrue(it.settings.requirementStatus.isRequired())
                assertFalse(it.settings.readOnly)
            }
            fields.getSpecimenField(expectedIndex++, SrvaEventField.Type.SPECIMEN.toField()).let {
                with (it.specimenData) {
                    assertEquals(Species.Unknown, species)
                    assertEquals(listOf(CommonSpecimenData()), specimens)
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
                assertEquals(listOf(-1L), it.selected)
                assertEquals(
                    expected = listOf(
                        BackendEnum.create<SrvaEventCategoryType>(null).toLocalizedStringWithId(mockStringProvider)
                    ) + listOf(
                        SrvaEventCategoryType.ACCIDENT,
                        SrvaEventCategoryType.DEPORTATION,
                        SrvaEventCategoryType.INJURED_ANIMAL
                    ).map { type -> type.toLocalizedStringWithId(mockStringProvider) },
                    actual = it.values
                )
                it.settings.label.assertEquals(RR.string.srva_event_label_event_category)
                assertTrue(it.settings.requirementStatus.isRequired())
                assertFalse(it.settings.readOnly)
            }
            fields.getIntField(expectedIndex++, SrvaEventField.Type.PERSON_COUNT.toField()).let {
                assertEquals(null, it.value)
                it.settings.label.assertEquals(RR.string.srva_event_label_person_count)
                assertFalse(it.settings.requirementStatus.isRequired())
                assertFalse(it.settings.readOnly)
            }
            fields.getIntField(expectedIndex++, SrvaEventField.Type.HOURS_SPENT.toField()).let {
                assertEquals(null, it.value)
                it.settings.label.assertEquals(RR.string.srva_event_label_hours_spent)
                assertFalse(it.settings.requirementStatus.isRequired())
                assertFalse(it.settings.readOnly)
            }
            fields.getStringField(expectedIndex, SrvaEventField.Type.DESCRIPTION.toField()).let {
                assertEquals("", it.value)
                it.settings.label.assertEquals(RR.string.srva_event_label_description)
                assertFalse(it.settings.requirementStatus.isRequired())
                assertFalse(it.settings.readOnly)
            }
        }
    }

    @Test
    fun testSrvaBecomesValid() = runBlockingTest {
        val controller = getController()

        controller.loadViewModel()

        assertFalse(controller.getLoadedViewModel().srvaEventIsValid, "SRVA should not be valid initially")

        // logging helps detecting validation errors since those are not exposed
        // to viewmodel
        logger.d { "Initially" }

        controller.eventDispatchers.locationEventDispatcher.dispatchLocationChanged(
            fieldId = SrvaEventField.Type.LOCATION.toField(),
            value = ETRMSGeoLocation(
                latitude = 6820960,
                longitude = 318112,
                source = BackendEnum.create(GeoLocationSource.MANUAL),
                accuracy = 0.0,
                altitude = null,
                altitudeAccuracy = null,
            )
        )
        logger.d { "Location set" }

        controller.eventDispatchers.speciesEventDispatcher.dispatchSpeciesChanged(
            fieldId = SrvaEventField.Type.SPECIES_CODE.toField(),
            value = Species.Known(speciesCode = SpeciesCodes.MOOSE_ID)
        )
        logger.d { "Species set" }

        controller.eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
            fieldId = SrvaEventField.Type.EVENT_CATEGORY.toField(),
            value = listOf(StringWithId(id = SrvaEventCategoryType.ACCIDENT.ordinal.toLong(), string = "<ignored>"))
        )
        logger.d { "Event category set" }

        controller.eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
            fieldId = SrvaEventField.Type.EVENT_TYPE.toField(),
            value = listOf(StringWithId(id = SrvaEventType.TRAFFIC_ACCIDENT.ordinal.toLong(), string = "<ignored>"))
        )
        logger.d { "Event type set" }

        controller.eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
            fieldId = SrvaEventField.Type.EVENT_RESULT.toField(),
            value = listOf(StringWithId(id = SrvaEventResult.ANIMAL_FOUND_DEAD.ordinal.toLong(), string = "<ignored>"))
        )
        logger.d { "Event result set" }

        assertTrue(controller.getLoadedViewModel().srvaEventIsValid, "SRVA should be valid now")
    }

    private fun String?.assertEquals(expected: RR.string) {
        assertEquals(mockStringProvider.getString(expected))
    }

    private fun String?.assertEquals(expected: String) {
        assertEquals(expected, this)
    }

    private fun getController(
        metadataProvider: MetadataProvider = mockMetadataProvider,
        stringProvider: StringProvider = mockStringProvider,
        dateTimeProvider: LocalDateTimeProvider = mockDateTimeProvider,
    ): CreateSrvaEventController {
        return CreateSrvaEventController(
            metadataProvider = metadataProvider,
            stringProvider = stringProvider,
            dateTimeProvider = dateTimeProvider,
        )
    }

    companion object {
        private val logger by getLogger(CreateSrvaEventControllerTest::class)
    }
}
