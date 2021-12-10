package fi.riista.common.util

import fi.riista.common.model.LoadStatus
import fi.riista.common.reactive.Observable

interface DataFetcher {
    /**
     * The load status for the data (exact data and type specified in implementing classes).
     *
     * It is possible that the loadStatus indicates data loading error but the data still exists.
     * This is probably the case when refreshing the data was attempted ([fetch] with refresh = true)
     * but the later [fetch] operation failed and loaded data is the data that was loaded previously.
     */
    val loadStatus: Observable<LoadStatus>

    /**
     * Fetches the data from some place.
     *
     * @param   refresh     Should the already fetched data be updated?
     */
    suspend fun fetch(refresh: Boolean)
}
