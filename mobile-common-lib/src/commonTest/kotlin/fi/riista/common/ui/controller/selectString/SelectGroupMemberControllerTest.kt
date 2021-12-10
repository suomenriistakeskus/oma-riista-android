package fi.riista.common.ui.controller.selectString

import fi.riista.common.helpers.getLoadedViewModel
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.StringWithId
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SelectGroupMemberControllerTest {
    @Test
    fun testDataInitiallyNotLoaded() {
        val controller = SelectStringWithIdController(
            possibleValues = listOf(),
            initiallySelectedValue = null,
        )

        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.NotLoaded)
    }

    @Test
    fun testDataCanBeLoaded() = runBlockingTest {
        val controller = SelectStringWithIdController(
            possibleValues = listOf(
                StringWithId("Foo", 1),
                StringWithId("Bar", 2),
            ),
            initiallySelectedValue = null,
        )

        controller.loadViewModel()
        assertTrue(controller.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded)

        val viewModel = assertNotNull(controller.viewModelLoadStatus.value.loadedViewModel)
        assertEquals(2, viewModel.allValues.size)
    }

    @Test
    fun testDataCanBeFiltered() = runBlockingTest {
        val controller = SelectStringWithIdController(
            possibleValues = listOf(
                StringWithId("Foo", 1),
                StringWithId("Bar", 2),
                StringWithId("FooBar", 3),
                StringWithId("Yet another", 4),
            ),
            initiallySelectedValue = null,
        )

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
        val controller = SelectStringWithIdController(
            possibleValues = listOf(
                StringWithId("Foo", 1),
                StringWithId("Bar", 2),
                StringWithId("FooBar", 3),
                StringWithId("Yet another", 4),
            ),
            initiallySelectedValue = 2,
        )

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
}
