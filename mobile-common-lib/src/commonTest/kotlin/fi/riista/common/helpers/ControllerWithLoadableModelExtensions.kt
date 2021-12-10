package fi.riista.common.helpers

import fi.riista.common.ui.controller.ControllerWithLoadableModel

fun <ViewModelType : Any> ControllerWithLoadableModel<ViewModelType>.getLoadedViewModel(): ViewModelType {
    return requireNotNull(getLoadedViewModelOrNull()) { "ViewModel not loaded" }
}