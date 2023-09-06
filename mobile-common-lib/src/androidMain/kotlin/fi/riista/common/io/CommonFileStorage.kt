package fi.riista.common.io

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import fi.riista.common.ApplicationContextHolder
import fi.riista.common.logging.getLogger
import java.io.*
import java.util.concurrent.Executors


actual object CommonFileStorage : CommonFileProvider {
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    actual override fun getFile(
        directory: CommonFileProvider.Directory,
        fileUuid: String
    ): CommonFile? {
        val context = requireNotNull(ApplicationContextHolder.applicationContext) {
            "Application context is required for getting file"
        }

        val directoryFile = directory.path(context)
        return if (directoryFile.exists() && directoryFile.isDirectory) {
            CommonFileImpl(
                file = File(directoryFile, fileUuid)
            )
        } else {
            null
        }
    }

    actual override fun getAllFilesIn(directory: CommonFileProvider.Directory): List<CommonFile> {
        val context = requireNotNull(ApplicationContextHolder.applicationContext) {
            "Application context is required listing all files"
        }

        return directory
            .path(context).listFiles()
            ?.map { CommonFileImpl(it) }
            ?: listOf()
    }

    actual override fun moveTemporaryFileTo(
        targetDirectory: CommonFileProvider.Directory,
        fileUuid: String,
        onFileSaveCompleted: (result: FileSaveResult) -> Unit
    ) {
        val context = requireNotNull(ApplicationContextHolder.applicationContext) {
            "Application context is required for saving file"
        }

        val sourceFile = getFile(
            directory = CommonFileProvider.Directory.TEMPORARY_FILES,
            fileUuid = fileUuid,
        ) ?: kotlin.run {
            logger.d { "No such temporary file: $fileUuid" }
            onFileSaveCompleted(FileSaveResult.SaveFailed(exception = null))
            return
        }

        if (!sourceFile.exists()) {
            logger.d { "Source file ${sourceFile.path} does not exist" }
            onFileSaveCompleted(FileSaveResult.SaveFailed(exception = null))
            return
        }

        executeFileOperationAsynchronously(onFileSaveCompleted) {
            try {
                val result = FileInputStream(sourceFile.path).use { inputStream ->
                    saveInputStreamSynchronously(
                        context = context,
                        sourceInputStream = inputStream,
                        targetDirectory = targetDirectory,
                        targetFileUuid = fileUuid
                    )
                }

                sourceFile.delete()

                result
            } catch (e : Exception) {
                logger.w { "An exception when trying to move temporary file to $targetDirectory" }
                FileSaveResult.SaveFailed(exception = e)
            }
        }
    }

    actual override fun removeTemporaryFiles() {
        getAllFilesIn(directory = CommonFileProvider.Directory.TEMPORARY_FILES).forEach { file ->
            file.delete()
        }
    }

    fun saveFileToTemporaryFiles(
        sourceUri: Uri,
        targetFileUuid: String,
        onFileSaveCompleted: (result: FileSaveResult) -> Unit
    ) {
        val context = requireNotNull(ApplicationContextHolder.applicationContext) {
            "Application context is required for saving file"
        }

        executeFileOperationAsynchronously(onFileSaveCompleted) {
            try {
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    saveInputStreamSynchronously(
                        context = context,
                        sourceInputStream = inputStream,
                        targetDirectory = CommonFileProvider.Directory.TEMPORARY_FILES,
                        targetFileUuid = targetFileUuid,
                    )
                } ?: kotlin.run {
                    logger.w { "Failed to open input stream for $sourceUri" }
                    FileSaveResult.SaveFailed(exception = null)
                }
            } catch(e : Exception) {
                logger.w { "An exception while opening an input stream for $sourceUri" }
                FileSaveResult.SaveFailed(exception = e)
            }
        }
    }

    private fun saveInputStreamSynchronously(
        context: Context,
        sourceInputStream: InputStream,
        targetDirectory: CommonFileProvider.Directory,
        targetFileUuid: String,
    ): FileSaveResult {
        val destinationDir = targetDirectory.path(context)
        if (!destinationDir.ensureDirectoryExists()) {
            return FileSaveResult.SaveFailed(exception = null)
        }

        // IMPORTANT: use target file uuid as filename. That allows getting files based on uuid
        val targetFile = File(destinationDir, targetFileUuid)
        var fileSaveResult: FileSaveResult?
        try {
            fileSaveResult = sourceInputStream.buffered().use { inputStream ->
                FileOutputStream(targetFile).buffered().use { outputStream ->
                    inputStream.copyTo(outputStream)
                    outputStream.flush()

                    // probably succeeded.. at least no exceptions were thrown if this point
                    // has been reached
                    FileSaveResult.Saved(
                        targetFile = CommonFileImpl(file = targetFile)
                    )
                }
            }
        } catch (e: Exception) {
            logger.w { "Error while copying file: ${e.message}" }
            fileSaveResult = FileSaveResult.SaveFailed(exception = e)
        }

        return fileSaveResult ?: FileSaveResult.SaveFailed(exception = null)
    }

    private fun executeFileOperationAsynchronously(
        onFileSaveCompleted: (result: FileSaveResult) -> Unit,
        fileOperation: () -> FileSaveResult,
    ) {
        executor.execute {
            val result = fileOperation()

            handler.post {
                onFileSaveCompleted(result)
            }
        }
    }

    private fun CommonFileProvider.Directory.path(context: Context): File {
        val child = when (this) {
            CommonFileProvider.Directory.ATTACHMENTS -> "attachments"
            CommonFileProvider.Directory.TEMPORARY_FILES -> "tmp"
            CommonFileProvider.Directory.LOCAL_IMAGES -> "images"
        }

        return File(context.filesDir, child)
    }

    private fun File.ensureDirectoryExists(): Boolean {
        if (!this.exists()) {
            return mkdirs()
        }

        if (isDirectory) {
            return true
        }

        // try to replace file with directory
        return if (delete()) {
            mkdirs()
        } else {
            false
        }
    }

    private val logger by getLogger(CommonFileStorage::class)
}

