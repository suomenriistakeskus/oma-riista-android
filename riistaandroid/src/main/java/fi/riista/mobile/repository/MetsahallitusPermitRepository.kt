package fi.riista.mobile.repository

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import fi.riista.mobile.database.room.MetsahallitusPermitDAO
import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME
import fi.riista.mobile.models.MetsahallitusPermit
import fi.riista.mobile.network.FetchMetsahallitusPermitsTask
import fi.riista.mobile.network.json.MetsahallitusPermitResponse
import fi.riista.mobile.utils.AppPreferences.LANGUAGE_CODE_EN
import fi.riista.mobile.utils.AppPreferences.LANGUAGE_CODE_FI
import fi.riista.mobile.utils.AppPreferences.LANGUAGE_CODE_SV
import fi.riista.mobile.utils.Consumer
import fi.vincit.androidutilslib.context.WorkContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class MetsahallitusPermitRepository @Inject constructor(
        @Named(APPLICATION_WORK_CONTEXT_NAME) private val appWorkContext: WorkContext,
        private val metsahallitusPermitDao: MetsahallitusPermitDAO) {

    fun findMetsahallitusPermit(permitIdentifier: String): LiveData<MetsahallitusPermit> {
        return metsahallitusPermitDao.findOneByPermitIdentifier(permitIdentifier)
    }

    fun findMetsahallitusPermits(username: String): LiveData<List<MetsahallitusPermit>> {
        remoteFetchMetsahallitusPermits(username)

        return metsahallitusPermitDao.findAllByUsername(username)
    }

    fun remoteFetchMetsahallitusPermits(username: String) {
        remoteFetchMetsahallitusPermits(username, null)
    }

    fun remoteFetchMetsahallitusPermits(username: String, resultCallback: Runnable?) {
        // Remote fetch is initiated by a Handler instance because WorkAsyncTask is involved.
        // This guarantees that this method can be called from a non-UI thread (e.g. Worker instance).
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            val task = SyncMetsahallitusPermitsTask(appWorkContext, username, metsahallitusPermitDao, resultCallback)
            task.start()
        }
    }

    private class SyncMetsahallitusPermitsTask(workContext: WorkContext,
                                               private val username: String,
                                               private val metsahallitusPermitDao: MetsahallitusPermitDAO,
                                               private val callback: Runnable?
    ) : FetchMetsahallitusPermitsTask(workContext) {

        // onFinishObjects is invoked in main (UI) thread.
        override fun onFinishObjects(results: List<MetsahallitusPermitResponse>) {

            // Save remote results into local database in an IO thread.
            CoroutineScope(IO).launch {

                val newPermits = results.map { transform(it, username) }
                metsahallitusPermitDao.update(username, newPermits)
            }
        }

        override fun onEnd() {
            callback?.run()
        }

        private fun transform(permit: MetsahallitusPermitResponse, username: String): MetsahallitusPermit {
            return MetsahallitusPermit(
                    permit.permitIdentifier,
                    permit.permitType[LANGUAGE_CODE_FI],
                    permit.permitType[LANGUAGE_CODE_SV],
                    permit.permitType[LANGUAGE_CODE_EN],
                    permit.permitName[LANGUAGE_CODE_FI],
                    permit.permitName[LANGUAGE_CODE_SV],
                    permit.permitName[LANGUAGE_CODE_EN],
                    permit.areaNumber,
                    permit.areaName[LANGUAGE_CODE_FI],
                    permit.areaName[LANGUAGE_CODE_SV],
                    permit.areaName[LANGUAGE_CODE_EN],
                    permit.beginDate?.let { LocalDate(it) },
                    permit.endDate?.let { LocalDate(it) },
                    permit.harvestFeedbackUrl?.get(LANGUAGE_CODE_FI),
                    permit.harvestFeedbackUrl?.get(LANGUAGE_CODE_SV),
                    permit.harvestFeedbackUrl?.get(LANGUAGE_CODE_EN),
                    username)
        }
    }
}
