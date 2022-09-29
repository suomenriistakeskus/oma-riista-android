package fi.riista.common.domain.poi.ui

interface PoiEventDispatcher {
    fun dispatchPoiFilterChanged(newPoiFilter: PoiFilter)
}
