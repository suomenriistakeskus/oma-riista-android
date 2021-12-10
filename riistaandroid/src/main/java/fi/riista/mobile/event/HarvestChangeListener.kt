package fi.riista.mobile.event

interface HarvestChangeListener {

    fun onHarvestsChanged(harvestChanges: Collection<HarvestChangeEvent>)
}
