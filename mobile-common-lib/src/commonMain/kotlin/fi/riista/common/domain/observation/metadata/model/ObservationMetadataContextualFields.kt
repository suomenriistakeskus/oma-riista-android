package fi.riista.common.domain.observation.metadata.model

import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.ObservationCategory
import fi.riista.common.domain.model.ObservationType
import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.domain.observation.model.ObservationSpecimenField
import fi.riista.common.domain.observation.model.ObservationSpecimenMarking
import fi.riista.common.domain.observation.model.ObservationSpecimenState
import fi.riista.common.model.BackendEnum

data class ObservationMetadataContextualFields(
    val observationCategory: BackendEnum<ObservationCategory>,
    val observationType: BackendEnum<ObservationType>,
    val observationFields: Map<CommonObservationField, ObservationFieldRequirement>,
    val specimenFields: Map<ObservationSpecimenField, ObservationFieldRequirement>,
    val allowedAges: List<BackendEnum<GameAge>>,
    val allowedStates: List<BackendEnum<ObservationSpecimenState>>,
    val allowedMarkings: List<BackendEnum<ObservationSpecimenMarking>>,
)
