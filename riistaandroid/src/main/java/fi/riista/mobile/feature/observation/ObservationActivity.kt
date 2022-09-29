package fi.riista.mobile.feature.observation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import dagger.android.AndroidInjection
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.observation.model.CommonObservation
import fi.riista.common.domain.observation.ui.modify.EditableObservation
import fi.riista.common.extensions.deserializeJson
import fi.riista.common.extensions.serializeToBundleAsJson
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.message.EventUpdateMessage
import fi.riista.mobile.models.observation.GameObservation
import fi.riista.mobile.observation.ObservationDatabase
import fi.riista.mobile.pages.PageFragment
import fi.riista.mobile.riistaSdkHelpers.toAppObservation
import fi.riista.mobile.ui.BusyIndicatorView
import fi.riista.mobile.utils.BaseDatabase
import fi.riista.mobile.utils.DateTimeUtils
import java.util.*
import javax.inject.Inject

class ObservationActivity
    : BaseActivity()
    , ViewObservationFragment.Manager
    , EditObservationFragment.Manager
    , CreateObservationFragment.Manager
    , PageFragment.OnFragmentInteractionListener {

    enum class Mode {
        VIEW,
        EDIT,
        CREATE,

        ;

        val fragmentTag = "${this}ObservationFragment"
    }

    private var observation: CommonObservation? = null
    private var observationModifiedOrCreated: Boolean = false

    private lateinit var busyIndicatorView: BusyIndicatorView

    @Inject
    lateinit var observationDatabase: ObservationDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observation)

        busyIndicatorView = findViewById(R.id.view_busy_indicator)

        setCustomTitle(getString(R.string.observation))

        if (savedInstanceState != null) {
            observation = getObservationFromBundle(savedInstanceState)
            observationModifiedOrCreated = getObservationCreatedOrModified(savedInstanceState)
        } else {
            val mode = getModeFromIntent(intent)

            val observation = when (mode) {
                Mode.VIEW, Mode.EDIT ->
                    requireObservationFromBundle(
                        bundle = requireNotNull(intent.extras) {
                            "Observation is required to exist in Intent extras required for mode $mode"
                        }
                    )
                Mode.CREATE -> null
            }.also { event ->
                this.observation = event
            }

            val fragment = when (mode) {
                Mode.VIEW -> {
                    ViewObservationFragment.create()
                }
                Mode.EDIT -> EditObservationFragment.create(
                    // observation exists at this point
                    editableObservation = EditableObservation(observation = observation!!)
                )
                Mode.CREATE -> CreateObservationFragment.create(
                    speciesCode = getObservationSpeciesCodeFromIntent(intent)
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

        observation?.let {
            outState.saveObservationToBundle(observation = it)
        }
        outState.saveObservationModified(observationModifiedOrCreated)
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
        if (observationModifiedOrCreated) {
            val result = Intent().also {
                it.putExtras(Bundle().also { bundle ->
                    bundle.saveObservationModified(modified = true)
                })
            }
            setResult(RESULT_OK, result)
        }
    }

    override fun cancelObservationOperation() {
        onBackPressed()
    }

    override fun onSaveObservation(observation: CommonObservation) {
        saveObservation(observation) {
            // pop the edit fragment
            onBackPressed()
        }
    }

    override fun onCreateObservation(observation: CommonObservation) {
        saveObservation(observation) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    ViewObservationFragment.create(),
                    Mode.VIEW.fragmentTag
                )
                .commit()
        }
    }

    private fun saveObservation(observation: CommonObservation, completion: () -> Unit) {
        val appObservation = observation.toAppObservation(observationModified = true)
        if (appObservation == null) {
            Toast.makeText(this, R.string.error_operation_failed, Toast.LENGTH_SHORT)
                .show()
            return
        }

        observationDatabase.saveObservation(appObservation, object : BaseDatabase.SaveListener {
            override fun onSaved(id: Long) {
                onObservationSaved(
                    observation = observation.copy(localId = id),
                    appObservation = appObservation.apply {
                        localId = id
                    },
                    completion = completion
                )
            }

            override fun onError() {
                Toast.makeText(this@ObservationActivity, R.string.error_operation_failed, Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun onObservationSaved(
        observation: CommonObservation,
        appObservation: GameObservation,
        completion: () -> Unit,
    ) {
        this.observation = observation
        observationModifiedOrCreated = true

        val huntingYear = DateTimeUtils.getHuntingYearForCalendar(
            appObservation.toDateTime().toCalendar(Locale.getDefault())
        )
        workContext.sendGlobalMessage(
            EventUpdateMessage(appObservation.type, appObservation.localId, huntingYear)
        )

        completion()
    }

    override fun getObservationForViewing(): CommonObservation {
        return requireNotNull(observation) {
            "Observation required to exist for viewing!"
        }
    }

    override fun startEditObservation(editableObservation: EditableObservation) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                EditObservationFragment.create(editableObservation),
                Mode.EDIT.fragmentTag,
            )
            .addToBackStack(null)
            .commit()
    }

    override fun deleteObservation() {
        val observationToDelete = getObservationForViewing()
        observationDatabase.deleteObservation(
            observationToDelete.localId,
            observationToDelete.remoteId,
            false,
            object : BaseDatabase.DeleteListener {
                override fun onDelete() {
                    observation = null // clear so that it won't be stored to result
                    observationModifiedOrCreated = true
                    onBackPressed()
                }

                override fun onError() {
                    // nop
                }
            }
        )
    }

    companion object {
        private const val EXTRAS_PREFIX = "ObservationActivity"
        private const val KEY_MODE = "${EXTRAS_PREFIX}_mode"
        private const val KEY_OBSERVATION = "${EXTRAS_PREFIX}_observation"
        private const val KEY_OBSERVATION_SPECIES_CODE = "${EXTRAS_PREFIX}_observation_species_code"
        private const val KEY_OBSERVATION_MODIFIED = "${KEY_OBSERVATION}_modified"

        @JvmStatic
        fun getLaunchIntentForViewing(packageContext: Context,
                                      observation: CommonObservation): Intent {
            return getLaunchIntentForMode(packageContext, Mode.VIEW, observation)
        }

        @JvmStatic
        fun getLaunchIntentForCreating(packageContext: Context, speciesCode: SpeciesCode? = null): Intent {
            return getLaunchIntentForMode(packageContext, Mode.CREATE, observation = null)
                .also { intent ->
                    if (speciesCode != null) {
                        intent.putExtra(KEY_OBSERVATION_SPECIES_CODE, speciesCode)
                    }
                }
        }

        private fun getLaunchIntentForMode(
            packageContext: Context,
            mode: Mode,
            observation: CommonObservation?,
        ): Intent {
            return Intent(packageContext, ObservationActivity::class.java)
                .apply {
                    putExtras(Bundle().also { bundle ->
                        bundle.putMode(mode)
                        if (observation != null) {
                            bundle.saveObservationToBundle(observation)
                        }
                    })
                }
        }

        @JvmStatic
        fun getObservationCreatedOrModified(bundle: Bundle?): Boolean {
            return bundle?.getBoolean(KEY_OBSERVATION_MODIFIED, false) ?: false
        }

        private fun Bundle.saveObservationModified(modified: Boolean) {
            putBoolean(KEY_OBSERVATION_MODIFIED, modified)
        }

        private fun Bundle.saveObservationToBundle(observation: CommonObservation) {
            observation.serializeToBundleAsJson(this, key = KEY_OBSERVATION)
        }

        private fun requireObservationFromBundle(bundle: Bundle): CommonObservation {
            return requireNotNull(getObservationFromBundle(bundle)) {
                "Observation required to be exist in bundle extras"
            }
        }

        private fun getObservationFromBundle(bundle: Bundle): CommonObservation? {
            return bundle.deserializeJson(key = KEY_OBSERVATION)
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

        private fun getObservationSpeciesCodeFromIntent(intent: Intent): SpeciesCode? {
            return intent.getIntExtra(KEY_OBSERVATION_SPECIES_CODE, -100)
                .takeIf { it != -100 }
        }
    }
}
