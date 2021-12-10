package fi.riista.mobile.feature.groupHunting.huntingDays.modify

import android.content.Context
import android.os.Bundle
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.extensions.getLocalDate
import fi.riista.common.extensions.putLocalDate
import fi.riista.common.groupHunting.model.GroupHuntingDay
import fi.riista.common.groupHunting.ui.huntingDays.modify.CreateGroupHuntingDayController
import fi.riista.common.groupHunting.ui.huntingDays.modify.ModifyGroupHuntingDayController
import fi.riista.common.model.LocalDate

/**
 * A fragment for creating a [GroupHuntingDay]
 */
class CreateGroupHuntingDayFragment
    : ModifyGroupHuntingDayFragment<CreateGroupHuntingDayFragment.ControllerProvider>() {

    interface ControllerProvider: Manager {
        val createGroupHuntingDayController: CreateGroupHuntingDayController
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)

        super.onAttach(context)
        manager = context as ControllerProvider
    }

    override fun getControllerFromManager(): ModifyGroupHuntingDayController {
        return manager.createGroupHuntingDayController.also { controller ->
            controller.preferredDate = getPreferredDateFromArgs(arguments)
        }
    }

    companion object {
        private const val ARGS_PREFERRED_HUNTING_DAY_DATE = "CGHDF_preferred_hunting_day_date"

        /**
         * Creates a new [CreateGroupHuntingDayFragment]. The created day will be initialized
         * to have specified local date (if given and if date is valid/allowed).
         */
        fun create(preferredStartDate: LocalDate?): CreateGroupHuntingDayFragment {
            return CreateGroupHuntingDayFragment().also { fragment ->
                fragment.arguments = Bundle().apply {
                    preferredStartDate?.let { putLocalDate(ARGS_PREFERRED_HUNTING_DAY_DATE, it) }
                }
            }
        }

        fun getPreferredDateFromArgs(args: Bundle?): LocalDate? {
            return args?.getLocalDate(ARGS_PREFERRED_HUNTING_DAY_DATE)
        }
    }
}
