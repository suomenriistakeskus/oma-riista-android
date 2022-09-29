package fi.riista.common.domain.specimens.ui.view

import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.Species
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ViewSpecimensControllerTest {

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
        controller.loadSpecimenData(specimenData)

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(12, fields.size)
        var expectedIndex = 0

        var speciesIndex = 0
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1}", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getStringField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            it.value.assertEquals(RR.string.gender_male)
            it.settings.label.assertEquals(RR.string.gender_label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            it.value.assertEquals(RR.string.age_adult)
            it.settings.label.assertEquals(RR.string.age_label)
            assertTrue(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1}", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getStringField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            it.value.assertEquals(RR.string.gender_female)
            it.settings.label.assertEquals(RR.string.gender_label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            it.value.assertEquals(RR.string.age_young)
            it.settings.label.assertEquals(RR.string.age_label)
            assertTrue(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1}", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getStringField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            it.value.assertEquals(RR.string.gender_unknown)
            it.settings.label.assertEquals(RR.string.gender_label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            it.value.assertEquals(RR.string.age_unknown)
            it.settings.label.assertEquals(RR.string.age_label)
            assertTrue(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1}", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getStringField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            it.value.assertEquals("-")
            it.settings.label.assertEquals(RR.string.gender_label)
            assertTrue(it.settings.readOnly)
        }
        fields.getStringField(expectedIndex, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            it.value.assertEquals("-")
            it.settings.label.assertEquals(RR.string.age_label)
            assertTrue(it.settings.readOnly)
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
        controller.loadSpecimenData(specimenData)

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(8, fields.size)
        var expectedIndex = 0

        var speciesIndex = 0
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1}", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getStringField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            it.value.assertEquals(RR.string.gender_male)
            it.settings.label.assertEquals(RR.string.gender_label)
            assertTrue(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1}", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getStringField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            it.value.assertEquals(RR.string.gender_female)
            it.settings.label.assertEquals(RR.string.gender_label)
            assertTrue(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1}", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getStringField(expectedIndex++, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            it.value.assertEquals(RR.string.gender_unknown)
            it.settings.label.assertEquals(RR.string.gender_label)
            assertTrue(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1}", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getStringField(expectedIndex, SpecimenFieldType.GENDER.toField(speciesIndex)).let {
            it.value.assertEquals("-")
            it.settings.label.assertEquals(RR.string.gender_label)
            assertTrue(it.settings.readOnly)
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
        controller.loadSpecimenData(specimenData)

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)

        val fields = viewModel.fields
        assertEquals(8, fields.size)
        var expectedIndex = 0

        var speciesIndex = 0
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1}", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getStringField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            it.value.assertEquals(RR.string.age_adult)
            it.settings.label.assertEquals(RR.string.age_label)
            assertTrue(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1}", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getStringField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            it.value.assertEquals(RR.string.age_young)
            it.settings.label.assertEquals(RR.string.age_label)
            assertTrue(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1}", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getStringField(expectedIndex++, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            it.value.assertEquals(RR.string.age_unknown)
            it.settings.label.assertEquals(RR.string.age_label)
            assertTrue(it.settings.readOnly)
        }

        speciesIndex++
        fields.getLabelField(expectedIndex++, SpecimenFieldType.SPECIMEN_HEADER.toField(speciesIndex)).let {
            assertEquals("${specimenData.speciesName} ${speciesIndex + 1}", it.text)
            assertEquals(LabelField.Type.CAPTION, it.type)
        }
        fields.getStringField(expectedIndex, SpecimenFieldType.AGE.toField(speciesIndex)).let {
            it.value.assertEquals("-")
            it.settings.label.assertEquals(RR.string.age_label)
            assertTrue(it.settings.readOnly)
        }
    }

    private fun getSpecimenData(
        speciesCode: SpeciesCode = 99,
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
            specimens = listOf(
                CommonSpecimenData(
                    gender = Gender.MALE.toBackendEnum(),
                    age = GameAge.ADULT.toBackendEnum()
                ),
                CommonSpecimenData(
                    gender = Gender.FEMALE.toBackendEnum(),
                    age = GameAge.YOUNG.toBackendEnum()
                ),
                CommonSpecimenData(
                    gender = Gender.UNKNOWN.toBackendEnum(),
                    age = GameAge.UNKNOWN.toBackendEnum()
                ),
                CommonSpecimenData(
                    gender = BackendEnum.create(null),
                    age = BackendEnum.create(null)
                ),
            ),
            fieldSpecifications = fieldSpecifications,
        )
    }

    private fun String?.assertEquals(expected: RR.string) {
        assertEquals(getStringProvider().getString(expected))
    }

    private fun String?.assertEquals(expected: String) {
        assertEquals(expected, this)
    }

    private val SpecimenFieldDataContainer.speciesName
        get() = when (species) {
            is Species.Known -> getSpeciesResolver().getSpeciesName(species.speciesCode)
            Species.Other -> "Other"
            Species.Unknown -> "Unknown"
        }

    private fun getController(stringProvider: StringProvider = getStringProvider()) =
        ViewSpecimensController(
            speciesResolver = getSpeciesResolver(),
            stringProvider = stringProvider
        )

    private fun getSpeciesResolver(): SpeciesResolver = TestSpeciesResolver.INSTANCE

    private fun getStringProvider(): StringProvider = TestStringProvider.INSTANCE

}
