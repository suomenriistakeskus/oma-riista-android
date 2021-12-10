package fi.riista.mobile.sync

import android.util.Log
import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME
import fi.riista.mobile.models.announcement.Announcement
import fi.riista.mobile.network.ListAnnouncementsTask
import fi.riista.mobile.storage.StorageDatabase
import fi.riista.mobile.storage.StorageDatabase.UpdateListener
import fi.vincit.androidutilslib.context.WorkContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AnnouncementSync @Inject constructor(
        @Named(APPLICATION_WORK_CONTEXT_NAME) private val syncWorkContext: WorkContext) {

    fun sync(onCompleteTask: () -> Unit) {
        val task: ListAnnouncementsTask = object : ListAnnouncementsTask(syncWorkContext) {
            override fun onFinishObjects(results: List<Announcement>) {
                StorageDatabase.getInstance()?.updateAnnouncements(results, object : UpdateListener {
                    override fun onUpdate() {
                        onCompleteTask()
                    }

                    override fun onError() {
                        onCompleteTask()
                    }
                })
            }

            override fun onError() {
                Log.d(TAG, "Sync error: ${error.message}")
                onCompleteTask()
            }
        }
        task.start()
    }

    companion object {
        private const val TAG = "AnnouncementsSync"
    }
}
