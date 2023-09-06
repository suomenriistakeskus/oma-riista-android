package fi.riista.common.domain.harvest.ui.modify

import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.model.toCommonHarvestData
import kotlinx.serialization.Serializable

@Serializable
data class EditableHarvest internal constructor(
    internal val harvest: CommonHarvestData
) {
    constructor(harvest: CommonHarvest) : this(harvest = harvest.toCommonHarvestData())
}