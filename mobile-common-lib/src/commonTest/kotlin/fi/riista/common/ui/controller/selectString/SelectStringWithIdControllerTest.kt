package fi.riista.common.ui.controller.selectString

import fi.riista.common.helpers.getLoadedViewModel
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.StringId
import fi.riista.common.model.StringWithId
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.StringListField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SelectStringWithIdControllerTest {
    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = SelectStringWithIdController(
            mode = StringListField.Mode.SINGLE,
            possibleValues = listOf(),
            initiallySelectedValues = null,
        )

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = SelectStringWithIdController(
            mode = StringListField.Mode.SINGLE,
            possibleValues = listOf(
                StringWithId("Foo", 1),
                StringWithId("Bar", 2),
            ),
            initiallySelectedValues = null,
        )

        controller.loadViewModel()
        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)
        assertEquals(2, viewModel.allValues.size)
    }

    @Test
    fun testDataCanBeFiltered() = runBlockingTest {
        val controller = getController()

        controller.loadViewModel()
        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        var viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)
        assertEquals(4, viewModel.allValues.size)
        assertEquals(4, viewModel.filteredValues.size)

        controller.eventDispatcher.dispatchFilterChanged("oo")
        viewModel = controller.getLoadedViewModel()
        assertEquals(4, viewModel.allValues.size)
        assertEquals(2, viewModel.filteredValues.size)

        controller.eventDispatcher.dispatchFilterChanged("ano")
        viewModel = controller.getLoadedViewModel()
        assertEquals(4, viewModel.allValues.size)
        assertEquals(1, viewModel.filteredValues.size)

        controller.eventDispatcher.dispatchFilterChanged("asdf")
        viewModel = controller.getLoadedViewModel()
        assertEquals(4, viewModel.allValues.size)
        assertEquals(0, viewModel.filteredValues.size)

        controller.eventDispatcher.dispatchFilterChanged("")
        viewModel = controller.getLoadedViewModel()
        assertEquals(4, viewModel.allValues.size)
        assertEquals(4, viewModel.filteredValues.size)
    }

    @Test
    fun testSelectedValueIsInFiltered() = runBlockingTest {
        val controller = getController(listOf(2L))

        controller.loadViewModel()
        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        var viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)
        assertEquals(4, viewModel.allValues.size)
        assertEquals(4, viewModel.filteredValues.size)

        controller.eventDispatcher.dispatchFilterChanged("oo")
        viewModel = controller.getLoadedViewModel()
        assertEquals(4, viewModel.allValues.size)
        assertEquals(3, viewModel.filteredValues.size, "oo")
        assertNotNull(viewModel.filteredValues.find { it.value.id == 1L })
        assertNotNull(viewModel.filteredValues.find { it.value.id == 3L })
        assertNotNull(viewModel.filteredValues.find { it.value.id == 2L })

        controller.eventDispatcher.dispatchFilterChanged("yet")
        viewModel = controller.getLoadedViewModel()
        assertEquals(4, viewModel.allValues.size)
        assertEquals(2, viewModel.filteredValues.size, "yet")
        assertNotNull(viewModel.filteredValues.find { it.value.id == 4L })
        assertNotNull(viewModel.filteredValues.find { it.value.id == 2L })
    }

    @Test
    fun testSelectingSameItemAgainKeepsItSelectedInSingleMode() = runBlockingTest {
        val selectedValueId = 2L
        val controller = getController(
            initialSelection = listOf(selectedValueId),
        )
        controller.loadViewModel()

        var viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)
        assertEquals(listOf(values[selectedValueId]), viewModel.selectedValues!!)

        controller.eventDispatcher.dispatchSelectedValueChanged(values[selectedValueId]!!)
        viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)
        val selectedValues = viewModel.selectedValues
        assertNotNull(selectedValues)
        assertEquals(1, selectedValues.size)
        assertEquals(listOf(values[selectedValueId]), selectedValues)
    }

    private fun getController(
        initialSelection: List<StringId>? = null,
        mode: StringListField.Mode = StringListField.Mode.SINGLE,
    ): SelectStringWithIdController {
        return SelectStringWithIdController(
            mode = mode,
            possibleValues = values.values.toList(),
            initiallySelectedValues = initialSelection,
        )
    }

    private val values = mapOf<StringId, StringWithId>(
        1L to StringWithId("Foo", 1),
        2L to StringWithId("Bar", 2),
        3L to StringWithId("FooBar", 3),
        4L to StringWithId("Yet another", 4),
    )
}
