package fi.riista.common.io

interface CommonFileProvider {
    enum class Directory {
        ATTACHMENTS,
        TEMPORARY_FILES,
        LOCAL_IMAGES,
    }

    /**
     * Tries to obtain a file identified by [fileUuid] from the given [directory].
     *
     * NOTE: it is possible that file does not exist even if [CommonFile] instance
     *       is returned. Check CommonFile.exists() afterwards if necessary.
     */
    fun getFile(directory: Directory, fileUuid: String): CommonFile?

    fun getAllFilesIn(directory: Directory): List<CommonFile>

    /**
     * Moves a temporary file specified by the [fileUuid] to the specified [targetDirectory].
     */
    fun moveTemporaryFileTo(
        targetDirectory: Directory,
        fileUuid: String,
        onFileSaveCompleted: (result: FileSaveResult) -> Unit
    )

    /**
     * Removes all temporary files
     */
    fun removeTemporaryFiles()
}

sealed class FileSaveResult {
    class Saved(val targetFile: CommonFile): FileSaveResult()
    class SaveFailed(val exception: Exception?): FileSaveResult()
}
