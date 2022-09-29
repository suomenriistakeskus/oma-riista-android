package fi.riista.mobile.feature.groupHunting.huntingDays.modify

import android.content.Context
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.domain.groupHunting.model.GroupHuntingDay
import fi.riista.common.domain.groupHunting.ui.huntingDays.modify.EditGroupHuntingDayController
import fi.riista.common.domain.groupHunting.ui.huntingDays.modify.ModifyGroupHuntingDayController

/**
 * A fragment for editing a [GroupHuntingDay]
 */
class EditGroupHuntingDayFragment
    : ModifyGroupHuntingDayFragment<EditGroupHuntingDayFragment.ControllerProvider>() {

    interface ControllerProvider: Manager {
        val editGroupHuntingDayController: EditGroupHuntingDayController
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)

        super.onAttach(context)
        manager = context as ControllerProvider
    }

    override fun getControllerFromManager(): ModifyGroupHuntingDayController {
        return manager.editGroupHuntingDayController
    }
}
