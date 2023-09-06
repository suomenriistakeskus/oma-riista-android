package fi.riista.common.domain.permit.metsahallitusPermit.ui.list

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.permit.metsahallitusPermit.MetsahallitusPermitProvider
import fi.riista.common.domain.userInfo.UsernameProvider
import fi.riista.common.network.sync.SyncDataPiece
import fi.riista.common.network.sync.SynchronizationConfig
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ListMetsahallitusPermitsController(
    private val usernameProvider: UsernameProvider,
    private val permitProvider: MetsahallitusPermitProvider,
): ControllerWithLoadableModel<ListMetsahallitusPermitsViewModel>() {

    override fun createLoadViewModelFlow(
        refresh: Boolean,
    ): Flow<ViewModelLoadStatus<ListMetsahallitusPermitsViewModel>> = flow {
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

        val permits = permitProvider.getAllPermits(username = username)

        emit(ViewModelLoadStatus.Loaded(
            viewModel = ListMetsahallitusPermitsViewModel(permits = permits)
        ))
    }
}
