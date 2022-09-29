package fi.riista.mobile.feature.groupHunting.observations

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import fi.riista.common.RiistaSDK
import fi.riista.common.extensions.loadGroupHuntingHarvestTarget
import fi.riista.common.extensions.loadGroupHuntingObservationTarget
import fi.riista.common.extensions.loadHuntingGroupTarget
import fi.riista.common.extensions.saveToBundle
import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.domain.groupHunting.ui.groupObservation.modify.CreateGroupObservationController
import fi.riista.common.domain.groupHunting.ui.groupObservation.modify.EditGroupObservationController
import fi.riista.common.domain.groupHunting.ui.groupObservation.view.ViewGroupObservationController
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.pages.PageFragment
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.ui.BusyIndicatorView

class GroupObservationActivity
    : BaseActivity()
    , ViewGroupObservationFragment.InteractionManager
    , EditGroupObservationFragment.InteractionManager
    , CreateGroupObservationFragment.InteractionManager
    , PageFragment.OnFragmentInteractionListener {

    private enum class Mode {
        VIEW,
        CREATE
    }

    private var _groupHuntingObservationTarget: GroupHuntingObservationTarget? = null
    override val groupHuntingObservationTarget: GroupHuntingObservationTarget
        get() {
            return requireNotNull(_groupHuntingObservationTarget) {
                "Observation target not set!"
            }
        }

    private var _huntingGroupTarget: HuntingGroupTarget? = null
    override val huntingGroupTarget: HuntingGroupTarget
        get() {
            return requireNotNull(_huntingGroupTarget) {
                "Hunting group target not set!"
            }
        }

    private var sourceHarvestTarget: GroupHuntingHarvestTarget? = null
    private lateinit var busyIndicatorView: BusyIndicatorView

    override val viewGroupObservationController: ViewGroupObservationController
        get() {
            return ViewGroupObservationController(
                groupHuntingContext = RiistaSDK.currentUserContext.groupHuntingContext,
                observationTarget = groupHuntingObservationTarget,
                stringProvider = ContextStringProviderFactory.createForContext(this)
            )
        }

    override val editGroupObservationController: EditGroupObservationController
        get() {
            return EditGroupObservationController(
                groupHuntingContext = RiistaSDK.currentUserContext.groupHuntingContext,
                observationTarget = groupHuntingObservationTarget,
                stringProvider = ContextStringProviderFactory.createForContext(this)
            )
        }

    override val createGroupObservationController: CreateGroupObservationController
        get() {
            return CreateGroupObservationController(
                groupHuntingContext = RiistaSDK.currentUserContext.groupHuntingContext,
                huntingGroupTarget = huntingGroupTarget,
                sourceHarvestTarget = sourceHarvestTarget,
                stringProvider = ContextStringProviderFactory.createForContext(this)
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_proposed_group_observation)

        busyIndicatorView = findViewById(R.id.view_busy_indicator)

        _groupHuntingObservationTarget = getObservationTargetFromIntent(intent)
        _huntingGroupTarget = getHuntingGroupTargetFromIntent(intent)
        sourceHarvestTarget = getHarvestTargetFromIntent(intent)

        if (savedInstanceState == null) {
            val mode = getModeFromIntent(intent)
            val fragment = when (mode) {
                Mode.VIEW -> ViewGroupObservationFragment.create(
                    observationAcceptStatus = getObservationAcceptStatusFromIntent(intent),
                )
                Mode.CREATE -> CreateGroupObservationFragment()
            }

            supportFragmentManager.beginTransaction()
                .add(
                    R.id.fragment_container,
                    fragment,
                    "${mode}GroupObservationFragment"
                )
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

    override fun onSavingProposedObservation() {
        busyIndicatorView.show()
    }

    override fun onProposedObservationSaveCompleted(success: Boolean, indicatorsDismissed: () -> Unit) {
        busyIndicatorView.hide {
            indicatorsDismissed()
            if (!isFinishing && success) {
                finish()
            }
        }
    }

    override fun startApproveGroupObservation() {
        val editGroupObservationFragment = EditGroupObservationFragment.create(
            observationAlreadyAccepted = (getObservationAcceptStatusFromIntent(intent) == AcceptStatus.ACCEPTED)
        )
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                editGroupObservationFragment,
                "EditGroupObservationFragment"
            )
            .addToBackStack(null)
            .commit()
    }

    override fun startEditingGroupObservation() {
        val editGroupObservationFragment = EditGroupObservationFragment.create(
            observationAlreadyAccepted = (getObservationAcceptStatusFromIntent(intent) == AcceptStatus.ACCEPTED)
        )
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                editGroupObservationFragment,
                "EditGroupObservationFragment"
            )
            .addToBackStack(null)
            .commit()

    }

    override fun groupObservationRejected() {
        finish()
    }

    override fun cancelProposedObservationApproval() {
        supportFragmentManager.popBackStack()
    }

    override fun onCreatingNewObservation() {
        busyIndicatorView.show()
    }

    override fun onNewObservationCreateCompleted(
        success: Boolean,
        observationId: GroupHuntingObservationId?,
        indicatorsDismissed: () -> Unit
    ) {
        busyIndicatorView.hide {
            if (isFinishing) {
                return@hide
            }

            if (!success) {
                indicatorsDismissed()
                return@hide
            }

            if (observationId != null) {
                _groupHuntingObservationTarget = huntingGroupTarget.createTargetForObservation(observationId)

                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        ViewGroupObservationFragment.create(observationAcceptStatus = AcceptStatus.ACCEPTED),
                        "VIEWGroupObservationFragment"
                    )
                    .commit()
            } else {
                supportFragmentManager.popBackStack()
            }
        }
    }

    override fun cancelCreateNewObservation() {
        onBackPressed()
    }

    companion object {
        private const val EXTRAS_PREFIX = "GroupObservationActivity"
        private const val KEY_MODE = "${EXTRAS_PREFIX}_mode"
        private const val KEY_OBSERVATION_ACCEPT_STATUS = "${EXTRAS_PREFIX}_observationAcceptStatus"

        fun getLaunchIntentForViewing(
            packageContext: Context,
            groupHuntingObservationTarget: GroupHuntingObservationTarget,
            observationAcceptStatus: AcceptStatus,
        ): Intent {
            return Intent(packageContext, GroupObservationActivity::class.java)
                .apply {
                    putExtras(Bundle().also {
                        groupHuntingObservationTarget.saveToBundle(it, EXTRAS_PREFIX)
                        it.putMode(Mode.VIEW)
                        it.putString(KEY_OBSERVATION_ACCEPT_STATUS, observationAcceptStatus.name)
                    })
                }
        }

        fun getLaunchIntentForCreating(
            packageContext: Context,
            huntingGroupTarget: HuntingGroupTarget,
            harvestTarget: GroupHuntingHarvestTarget?,
        ): Intent {
            return Intent(packageContext, GroupObservationActivity::class.java)
                .apply {
                    putExtras(Bundle().also {
                        huntingGroupTarget.saveToBundle(it, EXTRAS_PREFIX)
                        it.putMode(Mode.CREATE)
                        harvestTarget?.saveToBundle(it, EXTRAS_PREFIX)
                    })
                }
        }

        private fun Bundle.putMode(mode: Mode) {
            putString(KEY_MODE, mode.toString())
        }

        private fun getModeFromIntent(intent: Intent): Mode {
            val mode = intent.getStringExtra(KEY_MODE)
                ?.let {
                    Mode.valueOf(it)
                }

            return requireNotNull(mode) { "Mode must exist in intent!" }
        }

        private fun getHarvestTargetFromIntent(intent: Intent): GroupHuntingHarvestTarget? {
            return intent.extras?.loadGroupHuntingHarvestTarget(EXTRAS_PREFIX)
        }

        fun getObservationAcceptStatusFromIntent(intent: Intent): AcceptStatus {
            val stringValue = intent.getStringExtra(KEY_OBSERVATION_ACCEPT_STATUS) ?: AcceptStatus.PROPOSED.name
            return AcceptStatus.valueOf(stringValue)
        }

        fun getObservationTargetFromIntent(intent: Intent): GroupHuntingObservationTarget? {
            return intent.extras?.loadGroupHuntingObservationTarget(EXTRAS_PREFIX)
        }

        fun getHuntingGroupTargetFromIntent(intent: Intent): HuntingGroupTarget? {
            return intent.extras?.loadHuntingGroupTarget(EXTRAS_PREFIX)
        }
    }
}
