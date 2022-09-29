package fi.riista.common.domain.specimens.ui.view

import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.domain.specimens.ui.SpecimenFieldId
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class ViewSpecimensViewModel(
    internal val specimenData: SpecimenFieldDataContainer,
    override val fields: DataFields<SpecimenFieldId>,
) : DataFieldViewModel<SpecimenFieldId>()
