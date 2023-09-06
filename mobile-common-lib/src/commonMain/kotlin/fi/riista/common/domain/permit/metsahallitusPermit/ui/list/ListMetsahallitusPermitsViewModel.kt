package fi.riista.common.domain.permit.metsahallitusPermit.ui.list

import fi.riista.common.domain.permit.metsahallitusPermit.model.CommonMetsahallitusPermit

data class ListMetsahallitusPermitsViewModel(
    val permits: List<CommonMetsahallitusPermit>,
)
