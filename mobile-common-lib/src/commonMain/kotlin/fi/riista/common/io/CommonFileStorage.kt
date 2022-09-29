package fi.riista.common.io

expect object CommonFileStorage : CommonFileProvider {
    override fun getFile(directory: CommonFileProvider.Directory, fileUuid: String): CommonFile?
    override fun getAllFilesIn(directory: CommonFileProvider.Directory): List<CommonFile>

    /**
     * Moves a temporary file specified by the [fileUuid] to the specified [targetDirectory].
     */
    override fun moveTemporaryFileTo(
        targetDirectory: CommonFileProvider.Directory,
        fileUuid: String,
        onFileSaveCompleted: (result: FileSaveResult) -> Unit
    )

    override fun removeTemporaryFiles()
}
