package fi.riista.common.domain.specimens.ui.edit

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.createForTests
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.domain.specimens.ui.SpecimenFieldSpecification
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.helpers.*
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.FieldRequirement
import fi.riista.common.ui.dataField.LabelField
import kotlin.test.*

class EditSpecimensControllerTest {

    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = getController()
        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = getController()

        val specimenData = getSpecimenData()
        controller.loadSpecimenData(specimenData)

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)
        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        assertEquals(specimenData, viewModel.specimenData)
    }

    @Test
    fun testProducedFieldsMatchData() = runBlockingTest {
        val controller = getController()

        val specimenData = getSpecimenData()
        val speciesCount = specimenData.specimens.size
        controller.loadSpecimenData(specimenData)

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(12, fields.size)
        var expectedIndex = 0

        var speciesIndex = 0
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getGenderField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            assertEquals(Gender.MALE, it.gender)
            assertFalse(it.settings.readOnly)
        }
        fields.getAgeField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            assertEquals(GameAge.ADULT, it.age)
            assertFalse(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getGenderField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            assertEquals(Gender.FEMALE, it.gender)
            assertFalse(it.settings.readOnly)
        }
        fields.getAgeField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            assertEquals(GameAge.YOUNG, it.age)
            assertFalse(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getGenderField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            assertEquals(Gender.UNKNOWN, it.gender)
            assertFalse(it.settings.readOnly)
        }
        fields.getAgeField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            assertEquals(GameAge.UNKNOWN, it.age)
            assertFalse(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getGenderField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            assertNull(it.gender)
            assertFalse(it.settings.readOnly)
        }
        fields.getAgeField(expectedIndex, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            assertNull(it.age)
            assertFalse(it.settings.readOnly)
        }
    }

    @Test
    fun testProducedFieldsMatchDataWhenLowerAmount() = runBlockingTest {
        val controller = getController()

        val specimenData = getSpecimenData(specimenAmount = 2)
        val speciesCount = 2
        controller.loadSpecimenData(specimenData)

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(6, fields.size)
        var expectedIndex = 0

        var speciesIndex = 0
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getGenderField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            assertEquals(Gender.MALE, it.gender)
            assertFalse(it.settings.readOnly)
        }
        fields.getAgeField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            assertEquals(GameAge.ADULT, it.age)
            assertFalse(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getGenderField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            assertEquals(Gender.FEMALE, it.gender)
            assertFalse(it.settings.readOnly)
        }
        fields.getAgeField(expectedIndex, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            assertEquals(GameAge.YOUNG, it.age)
            assertFalse(it.settings.readOnly)
        }
    }

    @Test
    fun testProducedFieldsMatchDataWhenHigherSpecimenAMount() = runBlockingTest {
        val controller = getController()

        val specimenData = getSpecimenData(specimenAmount = 5)
        val speciesCount = 5
        controller.loadSpecimenData(specimenData)

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(15, fields.size)
        var expectedIndex = 0

        var speciesIndex = 0
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getGenderField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            assertEquals(Gender.MALE, it.gender)
            assertFalse(it.settings.readOnly)
        }
        fields.getAgeField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            assertEquals(GameAge.ADULT, it.age)
            assertFalse(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getGenderField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            assertEquals(Gender.FEMALE, it.gender)
            assertFalse(it.settings.readOnly)
        }
        fields.getAgeField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            assertEquals(GameAge.YOUNG, it.age)
            assertFalse(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getGenderField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            assertEquals(Gender.UNKNOWN, it.gender)
            assertFalse(it.settings.readOnly)
        }
        fields.getAgeField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            assertEquals(GameAge.UNKNOWN, it.age)
            assertFalse(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getGenderField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            assertNull(it.gender)
            assertFalse(it.settings.readOnly)
        }
        fields.getAgeField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            assertNull(it.age)
            assertFalse(it.settings.readOnly)
        }

        // this one should have been generated by the controller
        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getGenderField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            assertNull(it.gender)
            assertFalse(it.settings.readOnly)
        }
        fields.getAgeField(expectedIndex, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            assertNull(it.age)
            assertFalse(it.settings.readOnly)
        }
    }

    @Test
    fun testProducingGenderField() = runBlockingTest {
        val controller = getController()

        val specimenData = getSpecimenData(
            fieldSpecifications = listOf(
                SpecimenFieldSpecification(
                    fieldType = SpecimenFieldType.GENDER,
                    label = getStringProvider().getString(RR.string.gender_label),
                    requirementStatus = FieldRequirement.required()
                ),
            )
        )
        val speciesCount = specimenData.specimens.size
        controller.loadSpecimenData(specimenData)

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(8, fields.size)
        var expectedIndex = 0

        var speciesIndex = 0
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getGenderField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            assertEquals(Gender.MALE, it.gender)
            assertFalse(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getGenderField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            assertEquals(Gender.FEMALE, it.gender)
            assertFalse(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getGenderField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            assertEquals(Gender.UNKNOWN, it.gender)
            assertFalse(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getGenderField(expectedIndex, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            assertNull(it.gender)
            assertFalse(it.settings.readOnly)
        }
    }

    @Test
    fun testEditingGender() = runBlockingTest {
        val controller = getController()

        val specimenData = getSpecimenData(
            fieldSpecifications = listOf(
                SpecimenFieldSpecification(
                    fieldType = SpecimenFieldType.GENDER,
                    label = getStringProvider().getString(RR.string.gender_label),
                    requirementStatus = FieldRequirement.required()
                ),
            )
        )
        val speciesCount = specimenData.specimens.size
        controller.loadSpecimenData(specimenData)

        val speciesIndex = 0
        with (controller.getLoadedViewModel()) {
            fields.getLabelField(expectedIndex = 0, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
                assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
                assertEquals(LabelField.Type.CAPTION, it.type)
            }
            fields.getGenderField(expectedIndex = 1, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
                assertEquals(Gender.MALE, it.gender)
                assertFalse(it.settings.readOnly)
            }
        }

        controller.eventDispatchers.genderEventDispatcher.dispatchGenderChanged(
            fieldId = SpecimenFieldType.GENDER.toField(speciesIndex),
            value = Gender.FEMALE
        )

        with (controller.getLoadedViewModel()) {
            fields.getLabelField(expectedIndex = 0, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
                assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
                assertEquals(LabelField.Type.CAPTION, it.type)
            }
            fields.getGenderField(expectedIndex = 1, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
                assertEquals(Gender.FEMALE, it.gender)
                assertFalse(it.settings.readOnly)
            }
        }
    }

    @Test
    fun testProducingAgeField() = runBlockingTest {
        val controller = getController()

        val specimenData = getSpecimenData(
            fieldSpecifications = listOf(
                SpecimenFieldSpecification(
                    fieldType = SpecimenFieldType.AGE,
                    label = getStringProvider().getString(RR.string.age_label),
                    requirementStatus = FieldRequirement.required()
                ),
            )
        )
        val speciesCount = specimenData.specimens.size
        controller.loadSpecimenData(specimenData)

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(8, fields.size)
        var expectedIndex = 0

        var speciesIndex = 0
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getAgeField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            assertEquals(GameAge.ADULT, it.age)
            assertFalse(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getAgeField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            assertEquals(GameAge.YOUNG, it.age)
            assertFalse(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getAgeField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            assertEquals(GameAge.UNKNOWN, it.age)
            assertFalse(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getAgeField(expectedIndex, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            assertNull(it.age)
            assertFalse(it.settings.readOnly)
        }
    }

    @Test
    fun testEditingAge() = runBlockingTest {
        val controller = getController()

        val specimenData = getSpecimenData(
            fieldSpecifications = listOf(
                SpecimenFieldSpecification(
                    fieldType = SpecimenFieldType.AGE,
                    label = getStringProvider().getString(RR.string.age_label),
                    requirementStatus = FieldRequirement.required()
                ),
            )
        )
        val speciesCount = specimenData.specimens.size
        controller.loadSpecimenData(specimenData)

        val speciesIndex = 0
        with (controller.getLoadedViewModel()) {
            fields.getLabelField(expectedIndex = 0, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
                assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
                assertEquals(LabelField.Type.CAPTION, it.type)
            }
            fields.getAgeField(expectedIndex = 1, SpecimenFieldType.AGE.toField(speciesIndex)).let {
                assertEquals(GameAge.ADULT, it.age)
                assertFalse(it.settings.readOnly)
            }
        }

        controller.eventDispatchers.ageEventDispatcher.dispatchAgeChanged(
            fieldId = SpecimenFieldType.AGE.toField(speciesIndex),
            value = GameAge.YOUNG
        )

        with (controller.getLoadedViewModel()) {
            fields.getLabelField(expectedIndex = 0, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
                assertEquals("${specimenData.speciesName} ${speciesIndex + 1} / $speciesCount", it.text)
                assertEquals(LabelField.Type.CAPTION, it.type)
            }
            fields.getAgeField(expectedIndex = 1, SpecimenFieldType.AGE.toField(speciesIndex)).let {
                assertEquals(GameAge.YOUNG, it.age)
                assertFalse(it.settings.readOnly)
            }
        }
    }

    private fun getSpecimenData(
        speciesCode: SpeciesCode = 99,
        specimenAmount: Int = 4,
        fieldSpecifications: List<SpecimenFieldSpecification> = listOf(
            SpecimenFieldSpecification(
                fieldType = SpecimenFieldType.GENDER,
                label = getStringProvider().getString(RR.string.gender_label),
                requirementStatus = FieldRequirement.required()
            ),
            SpecimenFieldSpecification(
                fieldType = SpecimenFieldType.AGE,
                label = getStringProvider().getString(RR.string.age_label),
                requirementStatus = FieldRequirement.required()
            ),
        )
    ): SpecimenFieldDataContainer {
        return SpecimenFieldDataContainer.createForSrva(
            species = Species.Known(speciesCode),
            specimenAmount = specimenAmount,
            specimens = listOf(
                CommonSpecimenData.createForTests(
                    gender = Gender.MALE.toBackendEnum(),
                    age = GameAge.ADULT.toBackendEnum()
                ),
                CommonSpecimenData.createForTests(
                    gender = Gender.FEMALE.toBackendEnum(),
                    age = GameAge.YOUNG.toBackendEnum()
                ),
                CommonSpecimenData.createForTests(
                    gender = Gender.UNKNOWN.toBackendEnum(),
                    age = GameAge.UNKNOWN.toBackendEnum()
                ),
                CommonSpecimenData.createForTests(
                    gender = BackendEnum.create(null),
                    age = BackendEnum.create(null)
                ),
            ),
            fieldSpecifications = fieldSpecifications,
        )
    }

    private val SpecimenFieldDataContainer.speciesName
        get() = when (species) {
            is Species.Known -> getSpeciesResolver().getSpeciesName(species.speciesCode)
            Species.Other -> "Other"
            Species.Unknown -> "Unknown"
        }

    private fun getController(stringProvider: StringProvider = getStringProvider()) =
        EditSpecimensController(
            speciesResolver = getSpeciesResolver(),
            stringProvider = stringProvider
        )

    private fun getSpeciesResolver(): SpeciesResolver = TestSpeciesResolver.INSTANCE

    private fun getStringProvider(): StringProvider = TestStringProvider.INSTANCE

}
