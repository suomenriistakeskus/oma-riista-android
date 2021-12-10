package fi.riista.mobile.sync

import fi.riista.mobile.repository.MetsahallitusPermitRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetsaHallitusPermitSync @Inject constructor(
        private val metsahallitusPermitRepository: MetsahallitusPermitRepository) {

    fun sync(username: String, onFinishListener: Runnable) {
        metsahallitusPermitRepository.remoteFetchMetsahallitusPermits(username, onFinishListener)
    }
}
