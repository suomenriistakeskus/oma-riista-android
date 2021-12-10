package fi.riista.mobile.sync

import android.util.Log
import fi.riista.mobile.database.HarvestDatabase
import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME
import fi.riista.mobile.models.GameLogImage
import fi.riista.mobile.models.observation.GameObservation
import fi.riista.mobile.models.srva.SrvaEvent
import fi.riista.mobile.observation.ObservationDatabase
import fi.riista.mobile.srva.SrvaDatabase
import fi.riista.mobile.utils.ImageUtils
import fi.vincit.androidutilslib.context.WorkContext
import fi.vincit.androidutilslib.task.WorkAsyncTask
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class LocalImageRemover @Inject constructor(
        @Named(APPLICATION_WORK_CONTEXT_NAME) private val syncWorkContext: WorkContext,
        private val harvestDatabase: HarvestDatabase,
        private val observationDatabase: ObservationDatabase) {

    fun removeDeletedImagesLocallyAsync() {
        val localImages = ArrayList<GameLogImage>()

        observationDatabase.loadObservationsWithLocalImages { observations: List<GameObservation> ->
            for (observation in observations) {
                localImages.addAll(observation.images)
            }

            SrvaDatabase.getInstance().loadEventsWithLocalImages { srvaEvents: List<SrvaEvent> ->
                for (event in srvaEvents) {
                    localImages.addAll(event.images)
                }

                removeImagesAsync(localImages)
            }
        }
    }

    private fun removeImagesAsync(localImages: MutableList<GameLogImage>) {
        val task: WorkAsyncTask = object : WorkAsyncTask(syncWorkContext) {
            override fun onAsyncRun() {
                localImages.addAll(harvestDatabase.allHarvestImages)

                val imageUUIDs = HashSet<String>()

                for (image in localImages) {
                    imageUUIDs.add(image.uuid)
                }

                val imagesDir = ImageUtils.getImagesDir(syncWorkContext.context)
                val files = imagesDir.list()
                var removeCount = 0

                if (files != null) {
                    for (filename in files) {
                        if (!imageUUIDs.contains(filename)) {
                            Log.d(TAG, "Removing image: $filename")

                            val imageFile = File(imagesDir, filename)

                            if (imageFile.delete()) {
                                removeCount++
                            }
                        }
                    }
                }

                Log.d(TAG, "Removed $removeCount images")
            }
        }
        task.startSerial()
    }

    companion object {
        private const val TAG = "LocalImageRemover"
    }
}
