package fi.riista.common.io

import io.ktor.client.request.forms.*
import io.ktor.http.*

expect class CommonFileImpl(path: String): CommonFile {
    override val fileUuid: String

    override fun delete()
    override fun exists(): Boolean

    override fun appendFile(formBuilder: FormBuilder, key: String, headers: Headers)
}
