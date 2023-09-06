package fi.riista.common.domain.permit.metsahallitusPermit.ui.view

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.permit.metsahallitusPermit.MetsahallitusPermitProvider
import fi.riista.common.domain.userInfo.UsernameProvider
import fi.riista.common.network.sync.SyncDataPiece
import fi.riista.common.network.sync.SynchronizationConfig
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ViewMetsahallitusPermitController(
    private val permitIdentifier: String,
    private val usernameProvider: UsernameProvider,
    private val permitProvider: MetsahallitusPermitProvider,
): ControllerWithLoadableModel<ViewMetsahallitusPermitViewModel>() {

    override fun createLoadViewModelFlow(
        refresh: Boolean,
    ): Flow<ViewModelLoadStatus<ViewMetsahallitusPermitViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        val username = usernameProvider.username ?: kotlin.run {
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        if (refresh || !permitProvider.hasPermits(username)) {
            RiistaSDK.synchronize(
                syncDataPiece = SyncDataPiece.METSAHALLITUS_PERMITS,
                config = SynchronizationConfig(
                    forceContentReload = true
                )
            )
        }

        val permit = permitProvider.getPermit(username = username, permitIdentifier = permitIdentifier)

        if (permit != null) {
            emit(ViewModelLoadStatus.Loaded(
                viewModel = ViewMetsahallitusPermitViewModel(permit = permit)
            ))
        } else {
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }
}
