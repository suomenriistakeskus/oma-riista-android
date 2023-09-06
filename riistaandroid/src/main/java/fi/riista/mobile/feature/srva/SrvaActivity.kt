package fi.riista.mobile.feature.srva

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import dagger.android.AndroidInjection
import fi.riista.common.domain.srva.model.CommonSrvaEvent
import fi.riista.common.domain.srva.ui.modify.EditableSrvaEvent
import fi.riista.common.extensions.deserializeJson
import fi.riista.common.extensions.serializeToBundleAsJson
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.feature.filter.SharedEntityFilterState
import fi.riista.mobile.pages.PageFragment
import fi.riista.mobile.ui.BusyIndicatorView
import javax.inject.Inject

class SrvaActivity
    : BaseActivity()
    , ViewSrvaFragment.Manager
    , EditSrvaEventFragment.Manager
    , CreateSrvaEventFragment.Manager
    , PageFragment.OnFragmentInteractionListener {

    enum class Mode {
        VIEW,
        EDIT,
        CREATE

        ;

        val fragmentTag = "${this}SrvaFragment"
    }

    @Inject
    internal lateinit var sharedEntityFilterState: SharedEntityFilterState

    private var srvaEvent: CommonSrvaEvent? = null
    private var srvaEventModifiedOrCreated: Boolean = false

    private lateinit var busyIndicatorView: BusyIndicatorView

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_srva)

        busyIndicatorView = findViewById(R.id.view_busy_indicator)

        setCustomTitle(getString(R.string.srva))

        if (savedInstanceState != null) {
            srvaEvent = getSrvaEventFromBundle(savedInstanceState)
            srvaEventModifiedOrCreated = getSrvaEventCreatedOrModified(savedInstanceState)
        } else {
            val mode = getModeFromIntent(intent)

            val srvaEvent = when (mode) {
                Mode.VIEW, Mode.EDIT ->
                    requireSrvaEventFromBundle(
                        bundle = requireNotNull(intent.extras) {
                            "SRVA event is required to exist in Intent extras required for mode $mode"
                        }
                    )
                Mode.CREATE -> null
            }.also { event ->
                this.srvaEvent = event
            }

            val fragment = when (mode) {
                Mode.VIEW -> {
                    ViewSrvaFragment.create()
                }
                Mode.EDIT -> EditSrvaEventFragment.create(
                    // srva event exists at this point
                    editableSrvaEvent = EditableSrvaEvent(srvaEvent = srvaEvent!!)
                )
                Mode.CREATE -> CreateSrvaEventFragment()
            }

            supportFragmentManager.beginTransaction()
                .add(
                    R.id.fragment_container,
                    fragment,
                    mode.fragmentTag
                )
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                saveEventModifiedOrCreatedResult()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        srvaEvent?.let {
            outState.saveSrvaEventToBundle(srvaEvent = it)
        }
        outState.saveSrvaEventModified(srvaEventModifiedOrCreated)
    }

    override fun onBackPressed() {
        // don't wait for busy indicator to hide. Back press should happen immediately.
        // - fragment needs to support this though as it is not allowed to manipulate UI
        //   at later point!
        busyIndicatorView.visibility = View.GONE

        saveEventModifiedOrCreatedResult()

        super.onBackPressed()
    }

    private fun saveEventModifiedOrCreatedResult() {
        if (srvaEventModifiedOrCreated) {
            val result = Intent().also {
                it.putExtras(Bundle().also { bundle ->
                    bundle.saveSrvaEventModified(modified = true)
                })
            }
            setResult(RESULT_OK, result)
        }
    }

    override fun indicateBusy() {
        busyIndicatorView.show()
    }

    override fun hideBusyIndicators(indicatorsDismissed: () -> Unit) {
        busyIndicatorView.hide {
            indicatorsDismissed()
        }
    }

    override fun cancelSrvaOperation() {
        onBackPressed()
    }

    override fun onNewSrvaEventCreateCompleted(srvaEvent: CommonSrvaEvent) {
        busyIndicatorView.hide {
            this.srvaEvent = srvaEvent
            srvaEventModifiedOrCreated = true

            sharedEntityFilterState.ensureSrvaIsShown(srvaEvent)

            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    ViewSrvaFragment(),
                    "VIEWSrvaFragment"
                )
                .commit()
        }
    }

    override fun onSrvaEventSaveCompleted(srvaEvent: CommonSrvaEvent) {
        busyIndicatorView.hide {
            this.srvaEvent = srvaEvent
            srvaEventModifiedOrCreated = true

            sharedEntityFilterState.ensureSrvaIsShown(srvaEvent)

            supportFragmentManager.popBackStack()
        }
    }

    override fun getSrvaEventForViewing(): CommonSrvaEvent {
        return requireNotNull(srvaEvent) {
            "SRVA event required to exist for viewing!"
        }
    }

    override fun startEditSrvaEvent(editableSrvaEvent: EditableSrvaEvent) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                EditSrvaEventFragment.create(editableSrvaEvent),
                Mode.EDIT.fragmentTag,
            )
            .addToBackStack(null)
            .commit()
    }

    override fun onSrvaEventDeleted() {
        srvaEvent = null // clear so that it won't be stored to result
        srvaEventModifiedOrCreated = true
        onBackPressed()
    }

    companion object {
        private const val EXTRAS_PREFIX = "SrvaActivity"
        private const val KEY_MODE = "${EXTRAS_PREFIX}_mode"
        private const val KEY_SRVA_EVENT = "${EXTRAS_PREFIX}_srva_event"
        private const val KEY_SRVA_EVENT_MODIFIED = "${KEY_SRVA_EVENT}_modified"

        @JvmStatic
        fun getLaunchIntentForViewing(packageContext: Context,
                                              srvaEvent: CommonSrvaEvent): Intent {
            return getLaunchIntentForMode(packageContext, Mode.VIEW, srvaEvent)
        }

        @JvmStatic
        fun getLaunchIntentForCreating(packageContext: Context): Intent {
            return getLaunchIntentForMode(packageContext, Mode.CREATE, srvaEvent = null)
        }

        private fun getLaunchIntentForMode(
            packageContext: Context,
            mode: Mode,
            srvaEvent: CommonSrvaEvent?,
        ): Intent {
            return Intent(packageContext, SrvaActivity::class.java)
                .apply {
                    putExtras(Bundle().also { bundle ->
                        bundle.putMode(mode)
                        if (srvaEvent != null) {
                            bundle.saveSrvaEventToBundle(srvaEvent)
                        }
                    })
                }
        }

        @JvmStatic
        fun getSrvaEventCreatedOrModified(bundle: Bundle?): Boolean {
            return bundle?.getBoolean(KEY_SRVA_EVENT_MODIFIED, false) ?: false
        }

        private fun Bundle.saveSrvaEventModified(modified: Boolean) {
            putBoolean(KEY_SRVA_EVENT_MODIFIED, modified)
        }

        private fun Bundle.saveSrvaEventToBundle(srvaEvent: CommonSrvaEvent) {
            srvaEvent.serializeToBundleAsJson(this, key = KEY_SRVA_EVENT)
        }

        private fun requireSrvaEventFromBundle(bundle: Bundle): CommonSrvaEvent {
            return requireNotNull(getSrvaEventFromBundle(bundle)) {
                "SRVA required to be exist in bundle extras"
            }
        }

        private fun getSrvaEventFromBundle(bundle: Bundle): CommonSrvaEvent? {
            return bundle.deserializeJson(key = KEY_SRVA_EVENT)
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
    }
}
