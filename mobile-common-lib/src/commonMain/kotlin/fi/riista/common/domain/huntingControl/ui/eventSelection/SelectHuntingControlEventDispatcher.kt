package fi.riista.common.domain.huntingControl.ui.eventSelection

import fi.riista.common.domain.model.OrganizationId

interface SelectHuntingControlEventDispatcher {
    fun dispatchRhySelected(id: OrganizationId)
}
