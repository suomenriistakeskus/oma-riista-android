package fi.riista.common.network

import fi.riista.common.model.LoadStatus
import fi.riista.common.reactive.Observable
import fi.riista.common.util.DataFetcher

open class MockDataFetcher<MockData>(
    var mockData: MockData,
): DataFetcher {
    private var _fetchedData: MockData? = null

    var fetchCount: Int = 0
        private set

    val fetchedData: MockData?
        get() = _fetchedData

    override val loadStatus: Observable<LoadStatus> = Observable(LoadStatus.NotLoaded())

    override suspend fun fetch(refresh: Boolean) {
        _fetchedData = mockData
        fetchCount++
        loadStatus.set(LoadStatus.Loaded())
    }
}