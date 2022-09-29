package fi.riista.common.io

import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.streams.*
import java.io.File

actual class CommonFileImpl actual constructor(override val path: String): CommonFile {
    constructor(file: File): this(path = file.path)

    private val file: File by lazy { File(path) }

    actual override val fileUuid: String by lazy {
        file.name
    }

    actual override fun delete() {
        file.delete()
    }

    actual override fun exists(): Boolean {
        return file.exists()
    }

    actual override fun appendFile(formBuilder: FormBuilder, key: String, headers: Headers) {
        val inputProvider = InputProvider { file.inputStream().asInput() }
        formBuilder.append(key, inputProvider, headers)
    }
}
