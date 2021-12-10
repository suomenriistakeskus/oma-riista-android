package fi.riista.mobile.event

import fi.riista.mobile.models.GameHarvest

class HarvestChangeEvent(val harvest: GameHarvest, val type: Type) {

    enum class Type { INSERT, UPDATE, DELETE }

    companion object {

        @JvmStatic
        fun inserted(harvest: GameHarvest) = HarvestChangeEvent(harvest, Type.INSERT)

        @JvmStatic
        fun updated(harvest: GameHarvest) = HarvestChangeEvent(harvest, Type.UPDATE)

        @JvmStatic
        fun deleted(harvest: GameHarvest) = HarvestChangeEvent(harvest, Type.DELETE)
    }
}
