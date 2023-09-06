package fi.riista.mobile.feature.permits.metsahallitusPermits.view

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.permit.metsahallitusPermit.model.CommonMetsahallitusPermit
import fi.riista.common.domain.permit.metsahallitusPermit.ui.view.ViewMetsahallitusPermitController
import fi.riista.common.model.localizedWithFallbacks
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.util.prefixed
import fi.riista.mobile.R
import fi.riista.mobile.pages.PageFragment
import fi.riista.mobile.riistaSdkHelpers.AppLanguageProvider
import fi.riista.mobile.riistaSdkHelpers.formattedPeriodDates
import fi.riista.mobile.riistaSdkHelpers.loadViewModelIfNotLoaded
import fi.riista.mobile.utils.openInBrowserWithConfirmation

class ViewMetsahallitusPermitDetailsFragment : PageFragment() {

    private lateinit var permitTypeTextView: TextView
    private lateinit var permitAreaTextView: TextView
    private lateinit var permitNameTextView: TextView
    private lateinit var permitPeriodTextView: TextView
    private lateinit var harvestFeedbackUrlButton: MaterialButton

    private lateinit var controller: ViewMetsahallitusPermitController
    private val disposeBag = DisposeBag()

    private lateinit var languageProvider: LanguageProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        languageProvider = AppLanguageProvider(requireContext())
        controller = ViewMetsahallitusPermitController(
            permitIdentifier = getPermitIdentifier(requireArguments()),
            usernameProvider = RiistaSDK.currentUserContext,
            permitProvider = RiistaSDK.metsahallitusPermits,
        )
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_view_metsahallitus_permit_details, container, false)

        permitTypeTextView = view.findViewById(R.id.tv_permit_type)
        permitAreaTextView = view.findViewById(R.id.tv_permit_area_value)
        permitNameTextView = view.findViewById(R.id.tv_permit_name_value)
        permitPeriodTextView = view.findViewById(R.id.tv_permit_period_value)
        harvestFeedbackUrlButton = view.findViewById(R.id.btn_permit_harvest_feedback_button)

        return view
    }

    override fun onResume() {
        super.onResume()

        controller.viewModelLoadStatus.bindAndNotify { viewModelLoadStatus ->
            when (viewModelLoadStatus) {
                ViewModelLoadStatus.NotLoaded,
                ViewModelLoadStatus.Loading,
                ViewModelLoadStatus.LoadFailed -> {
                    // nop
                }
                is ViewModelLoadStatus.Loaded -> {
                    viewPermit(permit = viewModelLoadStatus.viewModel.permit)
                }
            }
        }.disposeBy(disposeBag)

        controller.loadViewModelIfNotLoaded()
    }

    override fun onPause() {
        disposeBag.disposeAll()
        super.onPause()
    }

    private fun viewPermit(permit: CommonMetsahallitusPermit) {
        val titleText = when (val permitType = permit.permitType.localizedWithFallbacks(languageProvider)) {
            null -> getString(R.string.my_details_mh_card_title)
            else -> permitType
        }

        @SuppressLint("SetTextI18n")
        permitTypeTextView.text = "$titleText, ${permit.permitIdentifier}"
        permitAreaTextView.text = permit.areaNumber.plus(
            permit.areaName.localizedWithFallbacks(languageProvider)?.prefixed(" ")
        )
        permitNameTextView.text = permit.permitName.localizedWithFallbacks(languageProvider)
        permitPeriodTextView.text = permit.formattedPeriodDates

        val harvestFeedbackUrl = permit.harvestFeedbackUrl?.localizedWithFallbacks(languageProvider)?.let {
            Uri.parse(it)
        }
        when (harvestFeedbackUrl) {
            null -> {
                harvestFeedbackUrlButton.setOnClickListener(null)
                harvestFeedbackUrlButton.visibility = View.GONE
            }
            else -> {
                harvestFeedbackUrlButton.setOnClickListener {
                    harvestFeedbackUrl.openInBrowserWithConfirmation(
                        context = requireContext(),
                        confirmationMessage = getString(R.string.my_details_mh_permit_harvest_feedback_confirmation)
                    )
                }
                harvestFeedbackUrlButton.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private const val EXTRAS_PREFIX = "ViewMetsahallitusPermitDetailsFragment"
        private const val KEY_PERMIT_IDENTIFIER = "${EXTRAS_PREFIX}_permit_identifier"

        fun newInstance(permitIdentifier: String) = ViewMetsahallitusPermitDetailsFragment().apply {
            arguments = Bundle().apply {
                putString(KEY_PERMIT_IDENTIFIER, permitIdentifier)
            }
        }

        private fun getPermitIdentifier(arguments: Bundle?): String {
            return requireNotNull(arguments?.getString(KEY_PERMIT_IDENTIFIER)) {
                "permit identifier is required to exist in fragment args"
            }
        }
    }
}
