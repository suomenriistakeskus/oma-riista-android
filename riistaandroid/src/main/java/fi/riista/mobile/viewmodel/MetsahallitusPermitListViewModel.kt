package fi.riista.mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import fi.riista.mobile.models.MetsahallitusPermit
import fi.riista.mobile.repository.MetsahallitusPermitRepository
import fi.riista.mobile.utils.AbsentLiveData
import javax.inject.Inject

class MetsahallitusPermitListViewModel @Inject constructor(
        private val repository: MetsahallitusPermitRepository) : ViewModel() {

    // Mutable LiveData is kept private and not exposed to views.
    private val _username = MutableLiveData<String>()

    // Input LiveData could also be used to hold other filtering data for view.
    val username: LiveData<String>
        get() = _username

    val metsahallitusPermits: LiveData<List<MetsahallitusPermit>> = Transformations
            .switchMap(_username) { username ->
                if (username == null) {
                    AbsentLiveData.create()
                } else {
                    repository.findMetsahallitusPermits(username)
                }
            }

    fun setUsername(username: String?) {
        username?.takeIf { it != _username.value }
            ?.let { _username.value = it }
    }

    fun triggerUpdate() {
        _username.value?. let { repository.remoteFetchMetsahallitusPermits(it) }
    }
}
