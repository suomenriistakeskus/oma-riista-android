package fi.riista.mobile.feature.permits

import android.content.Context
import android.content.Intent
import android.os.Bundle
import fi.riista.common.domain.permit.metsahallitusPermit.model.CommonMetsahallitusPermit
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.feature.permits.metsahallitusPermits.view.ViewMetsahallitusPermitDetailsFragment

class ViewPermitActivity
    : BaseActivity()
{
    private enum class PermitType {
        METSAHALLITUS,
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_permits)

        val permitType = getPermitType(intent.extras)

        val fragment = when (permitType) {
            PermitType.METSAHALLITUS -> {
                ViewMetsahallitusPermitDetailsFragment.newInstance(
                    permitIdentifier = getPermitIdentifier(intent.extras)
                )
            }
        }

        setCustomTitle(getString(R.string.my_details_mh_permit_title))

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment, "VIEW_${permitType}_PERMIT_FRAGMENT")
            .commit()
    }

    companion object {
        private const val EXTRAS_PREFIX = "ViewPermitActivity"
        private const val KEY_PERMIT_TYPE = "${EXTRAS_PREFIX}_permit_type"
        private const val KEY_PERMIT_IDENTIFIER = "${EXTRAS_PREFIX}_permit_identifier"

        fun getIntentForViewing(context: Context, permit: CommonMetsahallitusPermit): Intent {
            return Intent(context, ViewPermitActivity::class.java).apply {
                putExtra(KEY_PERMIT_TYPE, PermitType.METSAHALLITUS.name)
                putExtra(KEY_PERMIT_IDENTIFIER, permit.permitIdentifier)
            }
        }

        private fun getPermitType(extras: Bundle?): PermitType {
            return requireNotNull(extras?.getString(KEY_PERMIT_TYPE)?.let { PermitType.valueOf(it) }) {
                "permit type is required to exist in activity extras"
            }
        }

        private fun getPermitIdentifier(extras: Bundle?): String {
            return requireNotNull(extras?.getString(KEY_PERMIT_IDENTIFIER)) {
                "permit identifier is required to exist in activity extras"
            }
        }
    }
}
