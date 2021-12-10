package fi.riista.mobile.network

import fi.riista.mobile.AppConfig
import fi.riista.mobile.models.AreaMap
import fi.riista.mobile.utils.CookieStoreSingleton
import fi.riista.mobile.vectormap.VectorTileProvider
import fi.vincit.androidutilslib.context.WorkContext
import fi.vincit.androidutilslib.task.JsonListTask

abstract class ListAreasTask(context: WorkContext, type: VectorTileProvider.AreaType) : JsonListTask<AreaMap>(context, AreaMap::class.java) {

    init {
        cookieStore = CookieStoreSingleton.INSTANCE.cookieStore

        if (VectorTileProvider.AreaType.PIENRIISTA == type) {
            baseUrl = AppConfig.getBaseUrl() + PIENRIISTA_PATH
        } else if (VectorTileProvider.AreaType.MOOSE == type) {
            baseUrl = AppConfig.getBaseUrl() + MOOSE_PATH
        } else {
            throw UnsupportedOperationException()
        }
    }

    companion object {
        private const val PIENRIISTA_PATH = "/area/mh/pienriista"
        private const val MOOSE_PATH = "/area/mh/hirvi"
    }
}
