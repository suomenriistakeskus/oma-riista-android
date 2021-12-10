package fi.riista.mobile.feature.groupHunting.harvests

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import fi.riista.common.RiistaSDK
import fi.riista.common.extensions.loadGroupHuntingHarvestTarget
import fi.riista.common.extensions.loadHuntingGroupTarget
import fi.riista.common.extensions.saveToBundle
import fi.riista.common.groupHunting.model.*
import fi.riista.common.groupHunting.ui.groupHarvest.modify.CreateGroupHarvestController
import fi.riista.common.groupHunting.ui.groupHarvest.modify.EditGroupHarvestController
import fi.riista.common.groupHunting.ui.groupHarvest.view.ViewGroupHarvestController
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.feature.groupHunting.observations.GroupObservationActivity
import fi.riista.mobile.pages.PageFragment
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.ui.BusyIndicatorView

class GroupHarvestActivity
    : BaseActivity()
    , ViewGroupHarvestFragment.Manager
    , EditGroupHarvestFragment.Manager
    , CreateGroupHarvestFragment.Manager
    , PageFragment.OnFragmentInteractionListener {

    private enum class Mode {
        VIEW,
        CREATE
    }

    private var _groupHuntingHarvestTarget: GroupHuntingHarvestTarget? = null
    override val groupHuntingHarvestTarget: GroupHuntingHarvestTarget
        get() {
            return requireNotNull(_groupHuntingHarvestTarget) {
                "Harvest target not set!"
            }
        }

    override val viewGroupHarvestController: ViewGroupHarvestController
        get() {
            return ViewGroupHarvestController(
                    groupHuntingContext = RiistaSDK.currentUserContext.groupHuntingContext,
                    harvestTarget = groupHuntingHarvestTarget,
                    stringProvider = ContextStringProviderFactory.createForContext(this)
            )
        }

    override val editGroupHarvestController: EditGroupHarvestController
        get() {
            return EditGroupHarvestController(
                    groupHuntingContext = RiistaSDK.currentUserContext.groupHuntingContext,
                    harvestTarget = groupHuntingHarvestTarget,
                    stringProvider = ContextStringProviderFactory.createForContext(this)
            )
        }

    private var _huntingGroupTarget: HuntingGroupTarget? = null
    override val huntingGroupTarget: HuntingGroupTarget
        get() {
            return requireNotNull(_huntingGroupTarget) {
                "Hunting group target not set!"
            }
        }

    override val createGroupHarvestController: CreateGroupHarvestController
        get() {
            return CreateGroupHarvestController(
                    groupHuntingContext = RiistaSDK.currentUserContext.groupHuntingContext,
                    huntingGroupTarget = huntingGroupTarget,
                    stringProvider = ContextStringProviderFactory.createForContext(this)
            )
        }

    private lateinit var busyIndicatorView: BusyIndicatorView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_harvest)

        busyIndicatorView = findViewById(R.id.view_busy_indicator)

        _groupHuntingHarvestTarget = getHarvestTargetFromIntent(intent)
        _huntingGroupTarget = getHuntingGroupTargetFromIntent(intent)

        if (savedInstanceState == null) {
            val mode = getModeFromIntent(intent)
            val fragment = when (mode) {
                Mode.VIEW -> ViewGroupHarvestFragment.create(
                        harvestAcceptStatus = getHarvestAcceptStatusFromIntent(intent),
                )
                Mode.CREATE -> CreateGroupHarvestFragment()
            }

            supportFragmentManager.beginTransaction()
                .add(
                        R.id.fragment_container,
                        fragment,
                        "${mode}GroupHarvestFragment"
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

    override fun onSavingHarvest() {
        busyIndicatorView.show()
    }

    override fun onHarvestSaveCompleted(
        success: Boolean,
        harvestId: GroupHuntingHarvestId?,
        createObservation: Boolean,
        indicatorsDismissed: () -> Unit
    ) {
        busyIndicatorView.hide {
            indicatorsDismissed()
            if (!isFinishing && success) {
                finish()
            }
        }
        if (harvestId != null && createObservation) {
            startCreatingObservation(huntingGroupTarget.createTargetForHarvest(harvestId))
        }
    }

    override fun startEditGroupHarvest() {
        supportFragmentManager.beginTransaction()
            .replace(
                    R.id.fragment_container,
                    EditGroupHarvestFragment.create(EditGroupHarvestFragment.Mode.EDIT),
                    "EditGroupHarvestFragment"
            )
            .addToBackStack(null)
            .commit()

    }

    override fun startApproveProposedGroupHarvest() {
        supportFragmentManager.beginTransaction()
            .replace(
                    R.id.fragment_container,
                    EditGroupHarvestFragment.create(EditGroupHarvestFragment.Mode.APPROVE),
                    "EditGroupHarvestFragment"
            )
            .addToBackStack(null)
            .commit()
    }

    override fun proposedGroupHarvestRejected() {
        finish()
    }

    override fun cancelHarvestOperation() {
        onBackPressed()
    }

    override fun onCreatingNewHarvest() {
        busyIndicatorView.show()
    }

    override fun onNewHarvestCreateCompleted(
        success: Boolean,
        harvestId: GroupHuntingHarvestId?,
        createObservation: Boolean,
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

            if (harvestId != null) {
                _groupHuntingHarvestTarget = huntingGroupTarget.createTargetForHarvest(harvestId)

                supportFragmentManager.beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            ViewGroupHarvestFragment.create(harvestAcceptStatus = AcceptStatus.ACCEPTED),
                            "ViewGroupHarvestFragment"
                    )
                    .commit()

                if (createObservation) {
                    startCreatingObservation(_groupHuntingHarvestTarget)
                }

            } else {
                supportFragmentManager.popBackStack()
            }
        }
    }

    private fun startCreatingObservation(harvestTarget: GroupHuntingHarvestTarget?) {
        _huntingGroupTarget?.let { target ->
            val intent = GroupObservationActivity.getLaunchIntentForCreating(
                this,
                target,
                harvestTarget,
            )
            startActivity(intent)
        }
    }

    companion object {
        private const val EXTRAS_PREFIX = "GroupHarvestActivity"
        private const val KEY_MODE = "${EXTRAS_PREFIX}_mode"
        private const val KEY_HARVEST_ACCEPT_STATUS = "${EXTRAS_PREFIX}_harvestAcceptStatus"

        fun getLaunchIntentForViewing(packageContext: Context,
                                      groupHuntingHarvestTarget: GroupHuntingHarvestTarget,
                                      harvestAcceptStatus: AcceptStatus): Intent {
            return Intent(packageContext, GroupHarvestActivity::class.java)
                .apply {
                    putExtras(Bundle().also {
                        groupHuntingHarvestTarget.saveToBundle(it, EXTRAS_PREFIX)
                        it.putMode(Mode.VIEW)
                        it.putString(KEY_HARVEST_ACCEPT_STATUS, harvestAcceptStatus.name)
                    })
                }
        }

        fun getLaunchIntentForCreating(packageContext: Context,
                                       huntingGroupTarget: HuntingGroupTarget
        ): Intent {
            return Intent(packageContext, GroupHarvestActivity::class.java)
                .apply {
                    putExtras(Bundle().also {
                        huntingGroupTarget.saveToBundle(it, EXTRAS_PREFIX)
                        it.putMode(Mode.CREATE)
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

        fun getHarvestAcceptStatusFromIntent(intent: Intent): AcceptStatus {
            val stringValue = intent.getStringExtra(KEY_HARVEST_ACCEPT_STATUS) ?: AcceptStatus.PROPOSED.name
            return AcceptStatus.valueOf(stringValue)
        }

        fun getHarvestTargetFromIntent(intent: Intent): GroupHuntingHarvestTarget? {
            return intent.extras?.loadGroupHuntingHarvestTarget(EXTRAS_PREFIX)
        }

        fun getHuntingGroupTargetFromIntent(intent: Intent): HuntingGroupTarget? {
            return intent.extras?.loadHuntingGroupTarget(EXTRAS_PREFIX)
        }
    }
}
