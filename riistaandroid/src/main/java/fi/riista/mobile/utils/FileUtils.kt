package fi.riista.mobile.utils

import android.webkit.MimeTypeMap
import java.net.URLEncoder


object FileUtils {

    fun getMimeType(url: String): String? {
        var type: String? = null
        val encodedUrl = URLEncoder.encode(url, "UTF-8").replace("+", "%20")
        val extension = MimeTypeMap.getFileExtensionFromUrl(encodedUrl)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }
}
