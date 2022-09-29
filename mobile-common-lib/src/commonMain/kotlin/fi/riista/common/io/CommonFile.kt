package fi.riista.common.io

import io.ktor.client.request.forms.*
import io.ktor.http.*

interface CommonFile {
    val path: String
    val fileUuid: String

    fun delete()
    fun exists(): Boolean

    fun appendFile(formBuilder: FormBuilder, key: String, headers: Headers)
}

fun FormBuilder.appendFile(key: String, file: CommonFile, headers: Headers) {
    file.appendFile(
        formBuilder = this,
        key = key,
        headers = headers,
    )
}
