package fi.riista.common.io

class CommonFileProviderMock(private val file: CommonFile? = null) : CommonFileProvider {
    override fun getFile(directory: CommonFileProvider.Directory, fileUuid: String): CommonFile? {
        return file
    }

    override fun getAllFilesIn(directory: CommonFileProvider.Directory): List<CommonFile> {
        return listOfNotNull(file)
    }

    override fun moveTemporaryFileTo(
        targetDirectory: CommonFileProvider.Directory,
        fileUuid: String,
        onFileSaveCompleted: (result: FileSaveResult) -> Unit
    ) {
        if (file != null) {
            onFileSaveCompleted(FileSaveResult.Saved(file))
        } else {
            onFileSaveCompleted(FileSaveResult.SaveFailed(exception = null))
        }
    }

    override fun removeTemporaryFiles() {
        // nop
    }
}
