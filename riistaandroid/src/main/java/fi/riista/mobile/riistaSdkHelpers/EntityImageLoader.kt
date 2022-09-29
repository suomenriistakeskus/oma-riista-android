package fi.riista.mobile.riistaSdkHelpers

import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.model.EntityImage
import fi.riista.mobile.utils.DiaryImageUtil
import java.io.File

sealed class EntityImageUrl {
    class RemoteUrl(val url: String): EntityImageUrl()
    class LocalPath(val path: String): EntityImageUrl()
}

fun EntityImage.getImageUrl(
    heightPixels: Int,
    widthPixels: Int,
    keepAspectRatio: Boolean,
    affix: String?
): EntityImageUrl? {
    localUrl?.let {
        // prefer local image if available
        return EntityImageUrl.LocalPath(path = it)
    }

    return serverId?.let { remoteId ->
        DiaryImageUtil.getImageUri(
            imageUuid = remoteId,
            reqWidth = widthPixels,
            reqHeight = heightPixels,
            keepRatio = keepAspectRatio,
            affix = affix
        )
    }?.let { EntityImageUrl.RemoteUrl(url = it) }
}

fun <TranscodeType> RequestBuilder<TranscodeType>.loadEntityImage(
    entityImageUrl: EntityImageUrl
): RequestBuilder<TranscodeType> {
    return when (entityImageUrl) {
        is EntityImageUrl.LocalPath -> load(File(entityImageUrl.path))
        is EntityImageUrl.RemoteUrl -> {
            val cookies = RiistaSDK.getNetworkCookies(requestUrl = entityImageUrl.url)
                .joinToString(separator = "; ") { cookieData ->
                    "${cookieData.name}=${cookieData.value}"
                }
            val headers = LazyHeaders.Builder()
                .addHeader("Cookie", cookies)
                .build()

            load(GlideUrl(entityImageUrl.url, headers))
        }
    }
}
