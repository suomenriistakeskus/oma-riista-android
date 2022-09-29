package fi.riista.common.io

import fi.riista.common.util.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import kotlinx.cinterop.*
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.lastPathComponent
import platform.posix.memcpy

actual class CommonFileImpl actual constructor(override val path: String): CommonFile {

    private val fileManager: NSFileManager
        get() = NSFileManager.defaultManager

    actual override val fileUuid: String
        get() {
            val fileUrl = NSURL(fileURLWithPath = path)
            return fileUrl.lastPathComponent ?: "<missing_file_uuid>"
        }

    actual override fun delete() {
        wrapNSError { errorPtr ->
            fileManager.removeItemAtPath(path = path, error = errorPtr)
        }
    }

    actual override fun exists(): Boolean {
        return fileManager.fileExistsAtPath(path)
    }

    actual override fun appendFile(formBuilder: FormBuilder, key: String, headers: Headers) {
        val contents = readBytes()

        // prevent InvalidMutabilityException by adopting workaround (writeFully) from
        // https://youtrack.jetbrains.com/issue/KTOR-2947
        val inputProvider = InputProvider(size = contents.size.toLong()) {
            buildPacket {
                writeFully(contents)
            }
        }

        formBuilder.append(key, inputProvider, headers)
    }

    private fun readBytes(): ByteArray {
        val fileContents = fileManager.contentsAtPath(path)
        return fileContents?.let { contents ->
            val contentsLength: Int = contents.length.toInt()
            ByteArray(
                size = contentsLength
            ).apply {
                usePinned {
                    memcpy(it.addressOf(0), contents.bytes, contentsLength.convert())
                }
            }
        } ?: ByteArray(size = 0)
    }
}

