package fi.riista.common.ui.controller

/**
 * Possible viewmodel load statuses.
 *
 * Don't allow nullable viewmodel types (use [Any] as upper bound for type parameter)
 */
sealed class ViewModelLoadStatus<out ViewModelType: Any> {
    object NotLoaded: ViewModelLoadStatus<Nothing>()
    object Loading: ViewModelLoadStatus<Nothing>()
    object LoadFailed: ViewModelLoadStatus<Nothing>()

    /**
     * The ViewModel data has been loaded.
     */
    class Loaded<ViewModelType: Any>(val viewModel: ViewModelType): ViewModelLoadStatus<ViewModelType>()

    /**
     * A convenience accessor to the [Loaded.viewModel]
     *
     * Value will be null if this [ViewModelLoadStatus] is not [Loaded]
     */
    val loadedViewModel: ViewModelType?
        get() {
            return if (this is Loaded) {
                viewModel
            } else {
                null
            }
        }
}
