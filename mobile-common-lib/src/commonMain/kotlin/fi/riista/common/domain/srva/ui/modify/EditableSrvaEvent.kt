package fi.riista.common.domain.srva.ui.modify

import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.model.CommonSrvaEventData
import fi.riista.common.domain.srva.model.toSrvaEventData
import kotlinx.serialization.Serializable

@Serializable
data class EditableSrvaEvent internal constructor(
    internal val srvaEventData: CommonSrvaEventData
) {
    constructor(srvaEvent: CommonSrvaEvent) : this(srvaEventData = srvaEvent.toSrvaEventData())
}