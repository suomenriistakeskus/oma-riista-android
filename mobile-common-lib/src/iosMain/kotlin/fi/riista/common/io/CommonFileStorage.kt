package fi.riista.common.io

import fi.riista.common.logging.getLogger
import fi.riista.common.util.ObjcResult
import fi.riista.common.util.wrapNSError
import kotlinx.cinterop.*
import platform.Foundation.*

actual object CommonFileStorage : CommonFileProvider {
    private val logger by getLogger(CommonFileStorage::class)

    private val fileManager: NSFileManager
        get() = NSFileManager.defaultManager

    private const val separator = "/"

    private val filesDir: String? by lazy {
        val paths = NSSearchPathForDirectoriesInDomains(
            directory = NSDocumentDirectory,
            domainMask = NSUserDomainMask, expandTilde = true
        )

        paths[0]?.toString()
    }

    actual override fun getFile(
        directory: CommonFileProvider.Directory,
        fileUuid: String
    ): CommonFile? {
        val filePath = getPathFor(directory, fileUuid)

        return if (filePath != null) {
            CommonFileImpl(path = filePath)
        } else {
            null
        }
    }

    actual override fun getAllFilesIn(directory: CommonFileProvider.Directory): List<CommonFile> {
        val directoryPath = directory.path ?: kotlin.run {
            logger.w { "Cannot list all files in $directory. Could not obtain directory path" }
            return listOf()
        }

        val result = wrapNSError { errorPtr ->
            fileManager.contentsOfDirectoryAtPath(path = directoryPath, error = errorPtr)
        }

        return when (result) {
            is ObjcResult.Failure -> {
                listOf()
            }
            is ObjcResult.Success -> {
                val directoryUrl = NSURL(fileURLWithPath = directoryPath)
                val filePaths: List<String> = result.result?.mapNotNull { fileOrDirectoryName ->
                    (fileOrDirectoryName as? String)?.let { name ->
                        NSURL(fileURLWithPath = name , relativeToURL = directoryUrl).path
                    }
                } ?: listOf()

                filePaths.map { CommonFileImpl(path = it) }
            }
        }
    }

    actual override fun moveTemporaryFileTo(
        targetDirectory: CommonFileProvider.Directory,
        fileUuid: String,
        onFileSaveCompleted: (result: FileSaveResult) -> Unit
    ) {
        val targetDirectoryPath = targetDirectory.path ?: kotlin.run {
            logger.w { "Failed to obtain path for $targetDirectory in order to move file there" }
            onFileSaveCompleted(FileSaveResult.SaveFailed(exception = null))
            return
        }

        val sourceFilePath = getPathFor(
            directory = CommonFileProvider.Directory.TEMPORARY_FILES,
            fileUuid = fileUuid
        ) ?: kotlin.run {
            logger.w { "Failed to obtain path for temporary file $fileUuid in order to move it to $targetDirectory" }
            onFileSaveCompleted(FileSaveResult.SaveFailed(exception = null))
            return
        }

        val result = moveFileTo(
            sourceFileUrl = NSURL(fileURLWithPath = sourceFilePath),
            targetDirectoryPath = targetDirectoryPath,
            fileUuid = fileUuid
        )
        onFileSaveCompleted(result)
    }

    actual override fun removeTemporaryFiles() {
        getAllFilesIn(directory = CommonFileProvider.Directory.TEMPORARY_FILES).forEach { file ->
            file.delete()
        }
    }

    /**
     * Moves the file specified by the [sourceFileUrl] to the specified [directory] and ensures it can later
     * be accessed using given [fileUuid].
     */
    fun moveFileToTemporaryFilesDirectory(
        sourceFileUrl: NSURL,
        fileUuid: String,
    ): FileSaveResult {
        val targetDirectoryPath = CommonFileProvider.Directory.TEMPORARY_FILES.path ?: kotlin.run {
            logger.w { "Failed to obtain path for TEMPORARY_FILES in order to move file there" }
            return FileSaveResult.SaveFailed(exception = null)
        }

        return moveFileTo(sourceFileUrl, targetDirectoryPath, fileUuid)
    }

    /**
     * Gets the path for a file located in [directory] and specified by given [fileUuid].
     *
     * Tries to ensure the directory exists. Will return `null` if directory doesn't exist
     * and cannot be created.
     */
    fun getPathFor(
        directory: CommonFileProvider.Directory,
        fileUuid: String,
    ): String? {
        val directoryPath = directory.path
        if (directoryPath == null) {
            logger.w { "Directory.path required in order to getFile()" }
            return null
        }

        if (!ensureDirectoryExists(directoryPath)) {
            logger.w { "Directory.path required in order to getFile()" }
            return null
        }

        val (exists, isDirectory) = memScoped {
            val isDirectoryPtr: CPointer<BooleanVar> = alloc<BooleanVar>().ptr
            val exists = fileManager.fileExistsAtPath(directoryPath, isDirectory = isDirectoryPtr)

            Pair(exists, isDirectoryPtr.pointed.value)
        }

        return if (exists && isDirectory) {
            directoryPath.appendChild(fileUuid)
        } else {
            null
        }
    }

    /**
     * Moves the file specified by the [sourceFileUrl] to the specified [directory] and ensures it can later
     * be accessed using given [fileUuid].
     */
    private fun moveFileTo(
        sourceFileUrl: NSURL,
        targetDirectoryPath: String,
        fileUuid: String,
    ): FileSaveResult {
        if (!ensureDirectoryExists(targetDirectoryPath)) {
            logger.w { "Cannot move file (failed ensure $targetDirectoryPath exists)" }
            return FileSaveResult.SaveFailed(exception = null)
        }

        val targetFilePath = targetDirectoryPath.appendChild(fileUuid)
        val targetFileUrl = NSURL(fileURLWithPath = targetFilePath, isDirectory = false)

        val result = wrapNSError { errorPtr ->
            fileManager.moveItemAtURL(
                srcURL = sourceFileUrl,
                toURL = targetFileUrl,
                error = errorPtr,
            )
        }

        return if (result.isSuccess) {
            FileSaveResult.Saved(
                targetFile = CommonFileImpl(path = targetFilePath)
            )
        } else {
            logger.v { "Failed to moved file from ${sourceFileUrl.path} to $targetFilePath" }
            FileSaveResult.SaveFailed(exception = null)
        }
    }

    private fun String.appendChild(child: String) = this + separator + child

    private val CommonFileProvider.Directory.path: String?
        get() {
            val directoryName = when (this) {
                CommonFileProvider.Directory.ATTACHMENTS -> "attachments"
                CommonFileProvider.Directory.TEMPORARY_FILES -> {
                    return fileManager.temporaryDirectory.path
                }
                CommonFileProvider.Directory.LOCAL_IMAGES -> "images"
            }

            return if (filesDir != null) {
                filesDir?.appendChild(directoryName)
            } else {
                logger.w { "filesDir required in order to get Directory.path" }
                null
            }
        }

    private fun ensureDirectoryExists(directoryPath: String): Boolean {
        val (exists, isDirectory) = memScoped {
            val isDirectoryPtr: CPointer<BooleanVar> = alloc<BooleanVar>().ptr
            val exists = fileManager.fileExistsAtPath(directoryPath, isDirectory = isDirectoryPtr)

            Pair(exists, isDirectoryPtr.pointed.value)
        }

        if (!exists) {
            val result = createDirectory(directoryPath)
            return result.isSuccess
        }

        if (isDirectory) {
            return true
        }

        // try to replace file with directory
        return if (fileManager.isDeletableFileAtPath(directoryPath)) {
            val removeResult = wrapNSError { errorPtr ->
                fileManager.removeItemAtPath(directoryPath, error = errorPtr)
            }

            if (removeResult.isSuccess) {
                val result = createDirectory(directoryPath)
                result.isSuccess
            } else {
                false
            }
        } else {
            false
        }
    }

    private fun createDirectory(directoryPath: String): ObjcResult<Boolean> {
        return wrapNSError { errorPtr ->
            fileManager.createDirectoryAtPath(
                path = directoryPath,
                withIntermediateDirectories = true,
                attributes = null,
                error = errorPtr
            )
        }
    }
}
