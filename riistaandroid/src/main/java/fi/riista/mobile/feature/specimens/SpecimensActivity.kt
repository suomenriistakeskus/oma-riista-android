package fi.riista.mobile.feature.specimens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import fi.riista.common.extensions.deserializeJson
import fi.riista.common.extensions.serializeToBundleAsJson
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.domain.specimens.ui.edit.EditSpecimensController
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.pages.PageFragment
import fi.riista.mobile.riistaSdkHelpers.AppSpeciesResolver
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory

class SpecimensActivity
    : BaseActivity()
    , EditSpecimensFragment.Manager
    , PageFragment.OnFragmentInteractionListener {

    enum class Mode {
        VIEW,
        EDIT

        ;

        val fragmentTag = "${this}SpecimensFragment"
    }

    override val editSpecimensController by lazy {
        EditSpecimensController(
            speciesResolver = AppSpeciesResolver(),
            stringProvider = ContextStringProviderFactory.createForContext(this)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specimens)

        if (savedInstanceState == null) {
            val mode = getModeFromIntent(intent)
            val fragment = when (mode) {
                Mode.VIEW -> ViewSpecimensFragment.create(
                    specimenData = getSpecimenDataFromIntent(intent),
                )
                Mode.EDIT -> EditSpecimensFragment.create(
                    specimenData = getSpecimenDataFromIntent(intent),
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

    override fun onBackPressed() {
        // save current specimens to activity result if editing
        val viewModel = editSpecimensController.getLoadedViewModelOrNull()
        if (getModeFromIntent(intent) == Mode.EDIT && viewModel != null) {
            val result = Intent().also {
                it.putExtras(Bundle().also { bundle ->
                    bundle.putFieldId(getFieldIdFromIntent(intent))
                    bundle.saveSpecimenDataToBundle(specimenData = viewModel.specimenData)
                })
            }
            setResult(RESULT_OK, result)
        }

        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val EXTRAS_PREFIX = "SpecimensActivity"
        private const val KEY_MODE = "${EXTRAS_PREFIX}_mode"
        private const val KEY_FIELD_ID = "${EXTRAS_PREFIX}_field_id"
        private const val KEY_SPECIMEN_DATA = "${EXTRAS_PREFIX}_specimen_data"

        @JvmStatic
        fun getLaunchIntentForMode(
            packageContext: Context,
            mode: Mode,
            fieldId: Int,
            specimenData: SpecimenFieldDataContainer
        ): Intent {
            return Intent(packageContext, SpecimensActivity::class.java)
                .apply {
                    putExtras(Bundle().also { bundle ->
                        bundle.putMode(mode)
                        bundle.putFieldId(fieldId)
                        bundle.saveSpecimenDataToBundle(specimenData)

                    })
                }
        }

        private fun Bundle.saveSpecimenDataToBundle(specimenData: SpecimenFieldDataContainer) {
            specimenData.serializeToBundleAsJson(this, key = KEY_SPECIMEN_DATA)
        }

        fun getSpecimenDataFromIntent(intent: Intent): SpecimenFieldDataContainer {
            return requireNotNull(intent.extras?.deserializeJson(key = KEY_SPECIMEN_DATA)) {
                "Specimen data required to be exist in intent extras"
            }
        }

        private fun Bundle.putFieldId(fieldId: Int) {
            putInt(KEY_FIELD_ID, fieldId)
        }

        fun getFieldIdFromIntent(intent: Intent): Int {
            val fieldId = intent.getIntExtra(KEY_FIELD_ID, -1).takeIf { it >= 0 }
            requireNotNull(fieldId) {
                "FieldId is required to exist in the intent"
            }
            return fieldId
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
