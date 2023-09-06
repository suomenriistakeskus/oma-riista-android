package fi.riista.common.ui.controller

import fi.riista.common.RiistaSDK
import fi.riista.common.reactive.Observable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

abstract class ControllerWithLoadableModel<ViewModelType: Any> {
    val viewModelLoadStatus: Observable<ViewModelLoadStatus<ViewModelType>> =
        Observable(ViewModelLoadStatus.NotLoaded)

    suspend fun loadViewModel(refresh: Boolean = false) {
        val loadViewModelFlow = createLoadViewModelFlow(refresh)

        loadViewModelFlow.collect {
            updateViewModel(it)
        }
    }

    protected abstract fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<ViewModelType>>

    protected fun updateViewModel(viewModelLoadStatus: ViewModelLoadStatus<ViewModelType>) {
        this.viewModelLoadStatus.set(viewModelLoadStatus)
    }

    protected fun updateViewModel(viewModel: ViewModelType) {
        this.viewModelLoadStatus.set(ViewModelLoadStatus.Loaded(viewModel))
    }

    fun getLoadedViewModelOrNull(): ViewModelType? {
        return (viewModelLoadStatus.value as? ViewModelLoadStatus.Loaded)?.viewModel
    }

    protected fun updateViewModelSuspended(updateOperation: suspend () -> Unit) {
        RiistaSDK.mainScopeProvider.scope.launch {
            updateOperation()
        }
    }
}

