package fi.riista.common.domain.observation.ui.modify

import fi.riista.common.domain.observation.model.CommonObservationData
import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class ModifyObservationViewModel internal constructor(
    internal val observation: CommonObservationData,
    override val fields: DataFields<CommonObservationField> = listOf(),
    val observationIsValid: Boolean,
): DataFieldViewModel<CommonObservationField>()

