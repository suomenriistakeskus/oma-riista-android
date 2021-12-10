package fi.riista.mobile.activity

import android.os.Bundle
import android.util.Log
import android.widget.ScrollView
import fi.riista.mobile.R
import fi.riista.mobile.database.SpeciesInformation

private const val TAG = "AntlerInstructionsAct"

class AntlerInstructionsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_antler_instructions)

        val scrollView = findViewById<ScrollView>(R.id.scroll_view)
        val species = intent.getIntExtra(EXTRA_SPECIES, 0)
        val speciesName = SpeciesInformation.getSpeciesName(species)
        setCustomTitle(getString(R.string.instructions_format, speciesName))

        when (species) {
            SpeciesInformation.MOOSE_ID -> {
                scrollView.addView(
                        layoutInflater.inflate(R.layout.activity_antler_instructions_moose, scrollView, false))
            }
            SpeciesInformation.WHITE_TAILED_DEER_ID -> {
                scrollView.addView(
                        layoutInflater.inflate(R.layout.activity_antler_instructions_white_tailed_deer, scrollView, false))
            }
            SpeciesInformation.ROE_DEER_ID -> {
                scrollView.addView(
                        layoutInflater.inflate(R.layout.activity_antler_instructions_roe_deer, scrollView, false))
            }
            else -> {
                Log.w(TAG,"Trying to show antler instructions for invalid species $species")
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_SPECIES = "SPECIES"
    }
}
