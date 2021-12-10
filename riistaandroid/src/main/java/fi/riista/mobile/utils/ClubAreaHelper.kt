package fi.riista.mobile.utils

import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME
import fi.riista.mobile.models.ClubAreaMap
import fi.riista.mobile.models.user.UserInfo
import fi.vincit.androidutilslib.context.WorkContext
import fi.vincit.androidutilslib.util.HashDigest
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ClubAreaHelper @Inject constructor(
        @Named(APPLICATION_WORK_CONTEXT_NAME) private val appWorkContext: WorkContext,
        private val userInfoStore: UserInfoStore) {

    fun getMapCacheFileName(): String {
        val userInfo: UserInfo? = userInfoStore.getUserInfo()

        return userInfo?.username?.let { username ->
            val digest = HashDigest(HashDigest.SHA1)
            digest.update(username)
            return "maps_" + digest.hexDigest() + ".json"
        } ?: ""
    }

    fun saveAreasToFile(areaList: MutableList<ClubAreaMap>?) {
        val areas = areaList ?: ArrayList()
        JsonUtils.writeToFileAsync(appWorkContext, areas, getMapCacheFileName())
    }

    fun mergeServerResultToLocal(results: List<ClubAreaMap>, locals: MutableList<ClubAreaMap>) {
        // Remove all old remote areas
        val iter = locals.iterator()

        while (iter.hasNext()) {
            val area = iter.next()
            if (!area.manuallyAdded) {
                iter.remove()
            }
        }

        // Add new remote areas
        for (remote in results) {
            ClubAreaUtils.addRemoteAreaMapToList(remote, locals)
        }

        saveAreasToFile(locals)
    }
}
