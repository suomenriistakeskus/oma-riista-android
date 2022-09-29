package fi.riista.common.domain.observation.ui.modify

import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.model.CommonObservationData
import fi.riista.common.domain.observation.model.toObservationData
import kotlinx.serialization.Serializable

@Serializable
data class EditableObservation internal constructor(
    internal val observation: CommonObservationData
) {
    constructor(observation: CommonObservation) : this(observation = observation.toObservationData())
}