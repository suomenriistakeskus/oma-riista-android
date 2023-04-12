package fi.riista.mobile.feature.harvest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import dagger.android.AndroidInjection
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.ui.modify.EditableHarvest
import fi.riista.common.extensions.deserializeJson
import fi.riista.common.extensions.serializeToBundleAsJson
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.pages.PageFragment
import fi.riista.mobile.ui.BusyIndicatorView

class HarvestActivity
    : BaseActivity()
    , ViewHarvestFragment.Manager
    , EditHarvestFragment.Manager
    , CreateHarvestFragment.Manager
    , PageFragment.OnFragmentInteractionListener {

    enum class Mode {
        VIEW,
        EDIT,
        CREATE,

        ;

        val fragmentTag = "${this}HarvestFragment"
    }

    private var harvest: CommonHarvest? = null
    private var harvestModifiedOrCreated: Boolean = false

    private lateinit var busyIndicatorView: BusyIndicatorView

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_harvest)

        busyIndicatorView = findViewById(R.id.view_busy_indicator)

        setCustomTitle(getString(R.string.harvest))

        if (savedInstanceState != null) {
            harvest = getHarvestFromBundle(savedInstanceState)
            harvestModifiedOrCreated = getHarvestCreatedOrModified(savedInstanceState)
        } else {
            val mode = getModeFromIntent(intent)

            val harvest = when (mode) {
                Mode.VIEW, Mode.EDIT ->
                    requireHarvestFromBundle(
                        bundle = requireNotNull(intent.extras) {
                            "Harvest is required to exist in Intent extras required for mode $mode"
                        }
                    )
                Mode.CREATE -> null
            }.also { event ->
                this.harvest = event
            }

            val fragment = when (mode) {
                Mode.VIEW -> {
                    ViewHarvestFragment.create()
                }
                Mode.EDIT -> EditHarvestFragment.create(
                    // harvest exists at this point
                    editableHarvest = EditableHarvest(harvest = harvest!!)
                )
                Mode.CREATE -> CreateHarvestFragment.create(
                    speciesCode = getHarvestSpeciesCodeFromIntent(intent)
                )
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

        harvest?.let {
            outState.saveHarvestToBundle(harvest = it)
        }
        outState.saveHarvestModified(harvestModifiedOrCreated)
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
        if (harvestModifiedOrCreated) {
            val result = Intent().also {
                it.putExtras(Bundle().also { bundle ->
                    bundle.saveHarvestModified(modified = true)
                })
            }
            setResult(RESULT_OK, result)
        }
    }

    override fun onCreateHarvestCompleted(harvest: CommonHarvest) {
        busyIndicatorView.hide {
            this.harvest = harvest
            harvestModifiedOrCreated = true

            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    ViewHarvestFragment(),
                    Mode.VIEW.fragmentTag,
                )
                .commit()
        }
    }

    override fun onHarvestSaveCompleted(harvest: CommonHarvest) {
        busyIndicatorView.hide {
            this.harvest = harvest
            harvestModifiedOrCreated = true

            supportFragmentManager.popBackStack()
        }
    }

    override fun cancelHarvestOperation() {
        onBackPressed()
    }

    override fun indicateBusy() {
        busyIndicatorView.show()
    }

    override fun hideBusyIndicators(indicatorsDismissed: () -> Unit) {
        busyIndicatorView.hide {
            indicatorsDismissed()
        }
    }

    override fun getHarvestForViewing(): CommonHarvest {
        return requireNotNull(harvest) {
            "Harvest required to exist for viewing!"
        }
    }

    override fun startEditHarvest(editableHarvest: EditableHarvest) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                EditHarvestFragment.create(editableHarvest),
                Mode.EDIT.fragmentTag,
            )
            .addToBackStack(null)
            .commit()
    }

    override fun onHarvestDeleted() {
        harvest = null // clear so that it won't be stored to result
        harvestModifiedOrCreated = true
        onBackPressed()
    }

    companion object {
        private const val EXTRAS_PREFIX = "HarvestActivity"
        private const val KEY_MODE = "${EXTRAS_PREFIX}_mode"
        private const val KEY_HARVEST = "${EXTRAS_PREFIX}_harvest"
        private const val KEY_HARVEST_SPECIES_CODE = "${EXTRAS_PREFIX}_harvest_species_code"
        private const val KEY_HARVEST_MODIFIED = "${KEY_HARVEST}_modified"

        @JvmStatic
        fun getLaunchIntentForViewing(packageContext: Context,
                                      harvest: CommonHarvest): Intent {
            return getLaunchIntentForMode(packageContext, Mode.VIEW, harvest)
        }

        @JvmStatic
        fun getLaunchIntentForCreating(packageContext: Context, speciesCode: SpeciesCode? = null): Intent {
            return getLaunchIntentForMode(packageContext, Mode.CREATE, harvest = null)
                .also { intent ->
                    if (speciesCode != null) {
                        intent.putExtra(KEY_HARVEST_SPECIES_CODE, speciesCode)
                    }
                }
        }

        private fun getLaunchIntentForMode(
            packageContext: Context,
            mode: Mode,
            harvest: CommonHarvest?,
        ): Intent {
            return Intent(packageContext, HarvestActivity::class.java)
                .apply {
                    putExtras(Bundle().also { bundle ->
                        bundle.putMode(mode)
                        if (harvest != null) {
                            bundle.saveHarvestToBundle(harvest)
                        }
                    })
                }
        }

        @JvmStatic
        fun getHarvestCreatedOrModified(bundle: Bundle?): Boolean {
            return bundle?.getBoolean(KEY_HARVEST_MODIFIED, false) ?: false
        }

        private fun Bundle.saveHarvestModified(modified: Boolean) {
            putBoolean(KEY_HARVEST_MODIFIED, modified)
        }

        private fun Bundle.saveHarvestToBundle(harvest: CommonHarvest) {
            harvest.serializeToBundleAsJson(this, key = KEY_HARVEST)
        }

        private fun requireHarvestFromBundle(bundle: Bundle): CommonHarvest {
            return requireNotNull(getHarvestFromBundle(bundle)) {
                "Harvest required to be exist in bundle extras"
            }
        }

        private fun getHarvestFromBundle(bundle: Bundle): CommonHarvest? {
            return bundle.deserializeJson(key = KEY_HARVEST)
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

        private fun getHarvestSpeciesCodeFromIntent(intent: Intent): SpeciesCode? {
            return intent.getIntExtra(KEY_HARVEST_SPECIES_CODE, -100)
                .takeIf { it != -100 }
        }
    }
}
