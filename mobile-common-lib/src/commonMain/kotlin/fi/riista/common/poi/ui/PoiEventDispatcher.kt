package fi.riista.common.poi.ui

interface PoiEventDispatcher {
    fun dispatchPoiFilterChanged(newPoiFilter: PoiFilter)
}
