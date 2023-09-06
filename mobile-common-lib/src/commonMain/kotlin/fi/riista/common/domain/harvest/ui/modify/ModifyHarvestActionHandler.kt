package fi.riista.common.domain.harvest.ui.modify

import fi.riista.common.domain.model.PermitNumber

sealed class ModifyHarvestAction {
    // A permit should be selected e.g. by using an external view
    data class SelectPermit(val currentPermitNumber: PermitNumber?): ModifyHarvestAction()
}

/**
 * An interface for harvest actions. Actions are something that RiistaCommon cannot perform
 * by itself e.g. navigate to a different view in order to perform a specific task.
 */
interface ModifyHarvestActionHandler {
    fun handleModifyHarvestAction(action: ModifyHarvestAction)
}