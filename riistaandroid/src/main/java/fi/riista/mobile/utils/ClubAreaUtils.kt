package fi.riista.mobile.utils

import fi.riista.mobile.models.ClubAreaMap

object ClubAreaUtils {

    @JvmStatic
    fun addRemoteAreaMapToList(remote: ClubAreaMap, locals: MutableList<ClubAreaMap>) {
        for (i in 0 until locals.size) {
            val local = locals[i]

            if (local.externalId != null && remote.externalId != null && local.externalId == remote.externalId) {
                // Remove old version
                locals.removeAt(i)
                break
            }
        }

        locals.add(remote)
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
