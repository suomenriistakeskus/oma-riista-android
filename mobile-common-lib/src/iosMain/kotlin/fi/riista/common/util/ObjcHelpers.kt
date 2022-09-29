@file:Suppress("unused")

package fi.riista.common.util

import kotlinx.cinterop.*
import platform.Foundation.NSError

sealed class ObjcResult<out ResultType>(val isSuccess: Boolean) {
    class Success<ResultType>(val result: ResultType): ObjcResult<ResultType>(isSuccess = true)
    class Failure(val error: NSError): ObjcResult<Nothing>(isSuccess = false)
}

class NSErrorException(val error: NSError): Exception()

/**
 * Calls the given block in [memScoped].
 *
 * The return value depends on whether there is a NSError after calling the given [block]:
 * - no error: return value will be [ObjcResult.Success]
 * - error: return value will be [ObjcResult.Failure]
 */
internal fun <R> wrapNSError(
    block: (errorPtr: CPointer<ObjCObjectVar<NSError?>>) -> R,
): ObjcResult<R> {
    return memScoped {
        val errorPtr: CPointer<ObjCObjectVar<NSError?>> = alloc<ObjCObjectVar<NSError?>>().ptr

        val result = block(errorPtr)

        val error = errorPtr.pointed.value

        if (error == null) {
            ObjcResult.Success(result)
        } else {
            ObjcResult.Failure(error = error)
        }
    }
}

/**
 * Calls the given block in [memScoped]. The possible NSError is thrown.
 */
@Throws(NSErrorException::class)
internal fun <R> throwNSError(
    block: (errorPtr: CPointer<ObjCObjectVar<NSError?>>) -> R,
): R {
    return when (val result = wrapNSError(block)) {
        is ObjcResult.Failure -> throw NSErrorException(result.error)
        is ObjcResult.Success -> result.result
    }
}