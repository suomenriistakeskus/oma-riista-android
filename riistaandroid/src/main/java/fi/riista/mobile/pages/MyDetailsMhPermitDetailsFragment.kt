package fi.riista.mobile.pages

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.AndroidSupportInjection
import fi.riista.mobile.R
import fi.riista.mobile.databinding.FragmentMhPermitDetailsBinding
import fi.riista.mobile.utils.AppPreferences
import fi.riista.mobile.viewmodel.MetsahallitusPermitViewModel
import javax.inject.Inject

class MyDetailsMhPermitDetailsFragment : DialogFragment() {

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private var mhPermitIdentifier: String? = null
    private lateinit var languageCode: String

    private lateinit var permitViewModel: MetsahallitusPermitViewModel

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mhPermitIdentifier = it.getString(EXTRA_MH_PERMIT_ID)
        }
        languageCode = AppPreferences.getLanguageCodeSetting(context)

        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        super.onCreateView(inflater, container, savedInstanceState)

        permitViewModel = ViewModelProvider(this, viewModelFactory).get(MetsahallitusPermitViewModel::class.java)
        permitViewModel.setPermitIdentifier(mhPermitIdentifier)
        permitViewModel.setLanguageCode(languageCode)

        // Inflate view and obtain an instance of the binding class.
        val binding: FragmentMhPermitDetailsBinding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_mh_permit_details, container, false)

        // It is preferable to pass viewLifecycleOwner instead of `this` within fragments.
        binding.lifecycleOwner = viewLifecycleOwner

        // Set data binding variables.
        binding.viewmodel = permitViewModel
        binding.dismissHandler = View.OnClickListener { dismiss() }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EXTRA_MH_PERMIT_ID, mhPermitIdentifier)
        outState.putString(EXTRA_LANG_CODE, languageCode)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        savedInstanceState?.run {
            mhPermitIdentifier = getString(EXTRA_MH_PERMIT_ID)
            getString(EXTRA_LANG_CODE)?.let { languageCode = it }
        }
    }

    companion object {

        private const val EXTRA_MH_PERMIT_ID = "mh_permit_id"
        private const val EXTRA_LANG_CODE = "language_code"

        fun newInstance(mhPermitIdentifier: String) = MyDetailsMhPermitDetailsFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_MH_PERMIT_ID, mhPermitIdentifier)
            }
        }
    }
}
