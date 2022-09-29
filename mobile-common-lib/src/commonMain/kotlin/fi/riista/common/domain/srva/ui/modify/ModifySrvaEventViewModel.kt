package fi.riista.common.domain.srva.ui.modify

import fi.riista.common.domain.srva.model.CommonSrvaEventData
import fi.riista.common.domain.srva.ui.SrvaEventField
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class ModifySrvaEventViewModel internal constructor(
    internal val srvaEvent: CommonSrvaEventData,
    override val fields: DataFields<SrvaEventField> = listOf(),
    val srvaEventIsValid: Boolean,
): DataFieldViewModel<SrvaEventField>()

