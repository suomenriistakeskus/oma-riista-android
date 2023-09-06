package fi.riista.mobile.sync

import android.util.Log
import fi.riista.common.RiistaSDK
import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME
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
) {

    suspend fun removeDeletedImagesLocallyAsync() {
        val srvaImageIds = RiistaSDK.srvaContext.getLocalSrvaImageIds()
        val observationImageIds = RiistaSDK.observationContext.getLocalObservationImageIds()
        val harvestImages = RiistaSDK.harvestContext.getLocalHarvestImageIds()
        removeImagesAsync(srvaImageIds + observationImageIds + harvestImages)
    }

    private fun removeImagesAsync(localImagesIds: List<String>) {
        val task: WorkAsyncTask = object : WorkAsyncTask(syncWorkContext) {
            override fun onAsyncRun() {
                val imageUUIDs = HashSet<String>(localImagesIds)

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
