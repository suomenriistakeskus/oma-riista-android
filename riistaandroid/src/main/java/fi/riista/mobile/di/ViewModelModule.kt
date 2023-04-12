package fi.riista.mobile.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import fi.riista.mobile.viewmodel.GameLogViewModel
import fi.riista.mobile.viewmodel.MetsahallitusPermitListViewModel
import fi.riista.mobile.viewmodel.MetsahallitusPermitViewModel
import fi.riista.mobile.viewmodel.ShootingTestMainViewModel

@Suppress("unused")
@Module
interface ViewModelModule {

    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(GameLogViewModel::class)
    fun gameLogViewModel(viewModel: GameLogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MetsahallitusPermitViewModel::class)
    fun metsahallitusPermitViewModel(viewModel: MetsahallitusPermitViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MetsahallitusPermitListViewModel::class)
    fun metsahallitusPermitListViewModel(viewModel: MetsahallitusPermitListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ShootingTestMainViewModel::class)
    fun shootingTestMainViewModel(viewModel: ShootingTestMainViewModel): ViewModel

    // Add more ViewModels here that need injection...
}
