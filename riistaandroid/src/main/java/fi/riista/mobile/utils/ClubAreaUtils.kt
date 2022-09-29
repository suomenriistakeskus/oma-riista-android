package fi.riista.mobile.utils

import fi.riista.mobile.models.ClubAreaMap

object ClubAreaUtils {

    @JvmStatic
    fun addRemoteAreaMapToList(remote: ClubAreaMap, locals: MutableList<ClubAreaMap>) {
        // Remove old version
        removeRemoteAreaMapFromList(remote.externalId, locals)

        locals.add(remote)
    }

    @JvmStatic
    fun removeRemoteAreaMapFromList(externalId: String, locals: MutableList<ClubAreaMap>) {
        for (i in 0 until locals.size) {
            val local = locals[i]

            if (local.externalId != null && local.externalId == externalId) {
                locals.removeAt(i)
                break
            }
        }
    }

    @JvmStatic
    fun findAreaById(id: String, areaList: MutableList<ClubAreaMap>): ClubAreaMap? {
        for (area in areaList) {
            if (area.externalId != null && area.externalId == id) {
                return area
            }
        }

        return null
    }
}
