package fi.riista.mobile.feature.groupHunting.huntingDays

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import fi.riista.common.RiistaSDK
import fi.riista.common.extensions.*
import fi.riista.common.groupHunting.model.*
import fi.riista.common.groupHunting.ui.huntingDays.ViewGroupHuntingDayController
import fi.riista.common.groupHunting.ui.huntingDays.modify.CreateGroupHuntingDayController
import fi.riista.common.groupHunting.ui.huntingDays.modify.EditGroupHuntingDayController
import fi.riista.common.model.LocalDate
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.feature.groupHunting.huntingDays.modify.CreateGroupHuntingDayFragment
import fi.riista.mobile.feature.groupHunting.huntingDays.modify.EditGroupHuntingDayFragment
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.ui.BusyIndicatorView


interface ViewOrEditGroupHuntingDayFragmentManager {
    fun onViewModelLoading()
    fun onViewModelLoaded()
    fun onViewModelLoadFailed()
}

class ViewOrEditGroupHuntingDayActivity
    : BaseActivity()
    , ViewGroupHuntingDayFragment.Manager
    , EditGroupHuntingDayFragment.ControllerProvider
    , CreateGroupHuntingDayFragment.ControllerProvider {

    enum class Mode {
        VIEW,
        EDIT,
        CREATE
    }

    override val viewGroupHuntingDayController: ViewGroupHuntingDayController
        get() {
            return ViewGroupHuntingDayController(
                    groupHuntingContext = RiistaSDK.currentUserContext.groupHuntingContext,
                    huntingDayTarget = requireNotNull(huntingDayTarget)
            )
        }

    override val editGroupHuntingDayController: EditGroupHuntingDayController
        get() {
            return EditGroupHuntingDayController(
                    groupHuntingContext = RiistaSDK.currentUserContext.groupHuntingContext,
                    huntingDayTarget = requireNotNull(huntingDayTarget),
                    stringProvider = ContextStringProviderFactory.createForContext(this)
            )
        }

    override val createGroupHuntingDayController: CreateGroupHuntingDayController
        get() {
            return CreateGroupHuntingDayController(
                    groupHuntingContext = RiistaSDK.currentUserContext.groupHuntingContext,
                    huntingGroupTarget = huntingGroupTarget,
                    stringProvider = ContextStringProviderFactory.createForContext(this)
            )
        }

    private lateinit var huntingGroupTarget: HuntingGroupTarget
    private var huntingDayTarget: GroupHuntingDayTarget? = null

    private lateinit var layoutHuntingDayFragmentHolder: View
    private lateinit var layoutNoHuntingDay: View
    private lateinit var noHuntingDayMessageTextView: TextView
    private lateinit var noHuntingDayActionButton: MaterialButton
    private lateinit var busyIndicatorView: BusyIndicatorView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_or_edit_group_hunting_day)

        huntingGroupTarget = getHuntingGroupTargetFromIntent(intent)
                ?: run {
                    finish()
                    return
                }
        huntingDayTarget = getHuntingDayIdFromIntent(intent)?.let {
            huntingGroupTarget.createTargetForHuntingDay(it)
        }

        layoutHuntingDayFragmentHolder = findViewById(R.id.layout_hunting_day_fragment_holder)
        layoutNoHuntingDay = findViewById(R.id.layout_no_hunting_day)
        noHuntingDayMessageTextView = findViewById(R.id.tv_no_hunting_day_message)
        noHuntingDayActionButton = findViewById(R.id.btn_no_hunting_day_action)
        busyIndicatorView = findViewById(R.id.view_busy_indicator)

        if (savedInstanceState == null) {
            val modeFromIntent = getModeFromIntent(intent) ?: Mode.VIEW
            val fragment = when (modeFromIntent) {
                Mode.VIEW -> ViewGroupHuntingDayFragment()
                Mode.EDIT -> EditGroupHuntingDayFragment()
                Mode.CREATE -> CreateGroupHuntingDayFragment.create(
                        getPreferredHuntingDayDateFromIntent(intent)
                )
            }
            supportFragmentManager.beginTransaction()
                .add(R.id.layout_hunting_day_fragment_holder, fragment, "${modeFromIntent}HuntingDay")
                .commit()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // don't wait for busy indicator to hide. Back press should happen immediately.
        // - fragment needs to support this though as it is not allowed to manipulate UI
        //   at later point!
        busyIndicatorView.visibility = View.GONE
    }

    override fun getTargetForHarvest(harvestId: GroupHuntingHarvestId): GroupHuntingHarvestTarget? {
        return huntingDayTarget?.createTargetForHarvest(harvestId)
    }

    override fun getTargetForObservation(observationId: GroupHuntingObservationId): GroupHuntingObservationTarget? {
        return huntingDayTarget?.createTargetForObservation(observationId)
    }

    override fun onViewModelLoading() {
        layoutHuntingDayFragmentHolder.visibility = View.GONE
        layoutNoHuntingDay.visibility = View.VISIBLE
        noHuntingDayMessageTextView.text = getString(R.string.group_hunting_loading_hunting_days)
        noHuntingDayActionButton.visibility = View.GONE
    }

    override fun onViewModelLoaded() {
        layoutHuntingDayFragmentHolder.visibility = View.VISIBLE
        layoutNoHuntingDay.visibility = View.GONE
    }

    override fun onViewModelLoadFailed() {
        layoutHuntingDayFragmentHolder.visibility = View.GONE
        layoutNoHuntingDay.visibility = View.VISIBLE
        noHuntingDayMessageTextView.text = getString(R.string.group_hunting_failed_to_load_hunting_days)
        noHuntingDayActionButton.visibility = View.GONE
    }

    override fun createHuntingDay() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                                 android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.layout_hunting_day_fragment_holder,
                     CreateGroupHuntingDayFragment.create(
                             getPreferredHuntingDayDateFromIntent(intent)
                     ),
                     "CreateGroupHuntingDayFragment")
            .addToBackStack(null)
            .commit()
    }

    override fun editHuntingDay() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                                 android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.layout_hunting_day_fragment_holder,
                     EditGroupHuntingDayFragment(),
                     "EditGroupHuntingDayFragment")
            .addToBackStack(null)
            .commit()
    }

    override fun cancelHuntingDayModification() {
        onBackPressed()
    }

    override fun onSavingHuntingDay() {
        busyIndicatorView.show()
    }

    override fun onHuntingDaySaveCompleted(success: Boolean, indicatorsDismissed: () -> Unit) {
        busyIndicatorView.hide {
            indicatorsDismissed()

            if (!isFinishing && success) {
                onBackPressed()
            }
        }
    }

    companion object {
        private const val EXTRAS_KEY_TARGET = "VEGHDA_extras_target_prefix"
        private const val EXTRAS_KEY_HUNTING_DAY_ID = "VEGHDA_hunting_day_id"
        private const val EXTRAS_KEY_PREFERRED_HUNTING_DAY_DATE = "VEGHDA_preferred_hunting_day_date"
        private const val EXTRAS_KEY_MODE = "VEGHDA_extras_mode"

        fun getIntentForViewing(packageContext: Context, huntingDayTarget: GroupHuntingDayTarget) =
            getLaunchIntent(
                    packageContext = packageContext,
                    huntingGroupTarget = huntingDayTarget.asGroupTarget(),
                    huntingDayId = huntingDayTarget.huntingDayId,
                    // it is possible that the hunting day is a local one. Prefer the date
                    // of the local hunting day.
                    preferredHuntingDayDate = huntingDayTarget.huntingDayId.date,
                    mode = Mode.VIEW
            )

        fun getIntentForEditing(packageContext: Context, huntingDayTarget: GroupHuntingDayTarget) =
            getLaunchIntent(
                    packageContext = packageContext,
                    huntingGroupTarget = huntingDayTarget.asGroupTarget(),
                    huntingDayId = huntingDayTarget.huntingDayId,
                    preferredHuntingDayDate = null,
                    mode = Mode.EDIT
            )

        /**
         * Gets an intent in order to start creating new hunting day.
         *
         * @param   preferredHuntingDayDate     The preferred date for the hunting day. Optional.
         */
        fun getIntentForCreating(packageContext: Context,
                                 huntingGroupTarget: HuntingGroupTarget,
                                 preferredHuntingDayDate: LocalDate?) =
            getLaunchIntent(
                    packageContext = packageContext,
                    huntingGroupTarget = huntingGroupTarget,
                    huntingDayId = null,
                    preferredHuntingDayDate = preferredHuntingDayDate,
                    mode = Mode.CREATE
            )

        private fun getLaunchIntent(packageContext: Context,
                                    huntingGroupTarget: HuntingGroupTarget,
                                    huntingDayId: GroupHuntingDayId?,
                                    preferredHuntingDayDate: LocalDate?,
                                    mode: Mode): Intent {
            return Intent(packageContext, ViewOrEditGroupHuntingDayActivity::class.java)
                .apply {
                    putExtras(Bundle().also { bundle ->
                        huntingGroupTarget.saveToBundle(bundle, EXTRAS_KEY_TARGET)
                        huntingDayId?.let {
                            bundle.putGroupHuntingDayId(EXTRAS_KEY_HUNTING_DAY_ID, it)
                        }
                        preferredHuntingDayDate?.let {
                            bundle.putLocalDate(EXTRAS_KEY_PREFERRED_HUNTING_DAY_DATE, it)
                        }
                        bundle.putString(EXTRAS_KEY_MODE, mode.toString())
                    })
                }
        }

        fun getHuntingGroupTargetFromIntent(intent: Intent): HuntingGroupTarget? {
            return intent.extras?.loadHuntingGroupTarget(EXTRAS_KEY_TARGET)
        }

        fun getHuntingDayIdFromIntent(intent: Intent): GroupHuntingDayId? {
            return intent.extras?.getGroupHuntingDayId(EXTRAS_KEY_HUNTING_DAY_ID)
        }

        fun getPreferredHuntingDayDateFromIntent(intent: Intent): LocalDate? {
            return intent.extras?.getLocalDate(EXTRAS_KEY_PREFERRED_HUNTING_DAY_DATE)
        }

        fun getModeFromIntent(intent: Intent): Mode? {
            return intent.extras?.getString(EXTRAS_KEY_MODE)?.let {
                Mode.valueOf(it)
            }
        }
    }
}
