package fi.riista.mobile.pages

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.google.android.material.button.MaterialButton
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.huntingclub.ui.HuntingClubController
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.mobile.R
import fi.riista.mobile.feature.myDetails.MyDetailsHuntingClubMembershipsFragment
import fi.riista.mobile.models.user.UserInfo
import fi.riista.mobile.repository.MetsahallitusPermitRepository
import fi.riista.mobile.riistaSdkHelpers.AppLanguageProvider
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.utils.AppPreferences
import fi.riista.mobile.utils.UserInfoStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * My details/hunting card page.
 * Simply displays stored user information.
 */
class MyDetailsFragment : PageFragment() {

    @Inject
    internal lateinit var mUserInfoStore: UserInfoStore

    @Inject
    internal lateinit var mMHPermitRepository: MetsahallitusPermitRepository

    private lateinit var mContext: Context
    private var mUserInfo: UserInfo? = null
    private lateinit var mHuntingClubController: HuntingClubController
    private val disposeBag = DisposeBag()

    private lateinit var mNameText: TextView
    private lateinit var mDateOfBirthText: TextView
    private lateinit var mHomeMunicipality: TextView
    private lateinit var mAddressText: TextView

    private lateinit var mLicenseButton: Button
    private lateinit var mMetsahallitusPermitsButton: Button
    private lateinit var mShootingTestsButton: Button
    private lateinit var mOccupationsButton: Button
    private lateinit var mHuntingGroupMembershipsButton: MaterialButton

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mContext = requireContext()
        mUserInfo = mUserInfoStore.getUserInfo()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_details, container, false)
        setupActionBar(R.layout.actionbar_my_details, false)

        mNameText = view.findViewById(R.id.my_details_name_value)
        mDateOfBirthText = view.findViewById(R.id.my_details_date_of_birth_value)
        mHomeMunicipality = view.findViewById(R.id.my_details_home_municipality_value)
        mAddressText = view.findViewById(R.id.my_details_address_value)

        mLicenseButton = view.findViewById(R.id.my_details_license_button)
        mLicenseButton.setOnClickListener { onLicenseClick() }

        mMetsahallitusPermitsButton = view.findViewById(R.id.my_details_mh_permits_button)
        mMetsahallitusPermitsButton.visibility = View.GONE
        mMetsahallitusPermitsButton.setOnClickListener { onMetsahallitusPermitsClick() }

        mShootingTestsButton = view.findViewById(R.id.my_details_shooting_tests_button)
        mShootingTestsButton.setOnClickListener { onShootingTestsClick() }

        mOccupationsButton = view.findViewById(R.id.my_details_occupations_button)
        mOccupationsButton.setOnClickListener { onOccupationsClick() }

        mHuntingGroupMembershipsButton = view.findViewById(R.id.my_details_hunting_group_memberships_button)
        mHuntingGroupMembershipsButton.setOnClickListener { onHuntingGroupMembershipsClick() }

        mUserInfo?.username?.let { username ->
            mMHPermitRepository.findMetsahallitusPermits(username).observe(viewLifecycleOwner, Observer { list ->
                mMetsahallitusPermitsButton.visibility = if (list == null || list.isEmpty()) View.GONE else View.VISIBLE
            })
        }

        mHuntingClubController = HuntingClubController(
            huntingClubsContext = RiistaSDK.currentUserContext.huntingClubsContext,
            languageProvider = AppLanguageProvider(requireContext()),
            stringProvider = ContextStringProviderFactory.createForContext(requireContext()),
        )

        return view
    }

    override fun onResume() {
        super.onResume()

        refreshDisplayInfo()

        mHuntingClubController.viewModelLoadStatus.bindAndNotify { viewModelLoadStatus ->
            when (viewModelLoadStatus) {
                is ViewModelLoadStatus.NotLoaded,
                ViewModelLoadStatus.Loading,
                ViewModelLoadStatus.LoadFailed -> {
                }
                is ViewModelLoadStatus.Loaded -> {
                    val buttonIcon: Drawable? = if (viewModelLoadStatus.viewModel.hasOpenInvitations) {
                        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_alert_circle)
                    } else {
                        null // Don't show an icon
                    }
                    buttonIcon?.let { drawable ->
                        DrawableCompat.setTint(
                            drawable,
                            ResourcesCompat.getColor(resources, R.color.traffic_light_red, null)
                        )
                    }
                    mHuntingGroupMembershipsButton.setCompoundDrawablesWithIntrinsicBounds(
                        null, null, buttonIcon, null
                    )
                }
            }
        }.disposeBy(disposeBag)

        loadHuntingClubsIfNotLoaded()
    }

    private fun loadHuntingClubsIfNotLoaded() {
        if (mHuntingClubController.viewModelLoadStatus.value is ViewModelLoadStatus.Loaded) {
            return
        }
        loadViewModel()
    }

    private fun loadViewModel(refresh: Boolean = false) {
        MainScope().launch {
            mHuntingClubController.loadViewModel(refresh)
        }
    }

    override fun onPause() {
        super.onPause()
        disposeBag.disposeAll()
    }

    private fun refreshDisplayInfo() {
        mUserInfo?.let { user ->

            mOccupationsButton.visibility = if (user.hasOccupations()) View.VISIBLE else View.GONE

            // Timestamp check is to make sure the data from server is not in outdated format.
            if (user.timestamp != null) {

                val df = SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.getDefault())
                df.timeZone = TimeZone.getTimeZone("EET")

                mNameText.text = user.firstName + " " + user.lastName
                mDateOfBirthText.text = df.format(user.birthDate)

                val homeMunicipality = user.homeMunicipality
                val languageCode = AppPreferences.getLanguageCodeSetting(mContext)

                if (homeMunicipality == null || homeMunicipality.isEmpty()) {
                    mHomeMunicipality.text = null
                } else if (homeMunicipality.containsKey(languageCode)) {
                    mHomeMunicipality.text = homeMunicipality[languageCode]
                } else {
                    mHomeMunicipality.text = homeMunicipality[AppPreferences.LANGUAGE_CODE_FI]
                }

                user.address?.let { address ->
                    mAddressText.text = String.format(ADDRESS_FORMAT,
                            address.streetAddress,
                            address.postalCode,
                            address.city,
                            address.country ?: "")
                }

                mOccupationsButton.isEnabled = user.occupations.size > 0
            }
        }
    }

    private fun onLicenseClick() {
        val fragment = MyDetailsLicenseFragment.newInstance()
        activity?.supportFragmentManager?.let { fragment.show(it, MyDetailsLicenseFragment.TAG) }
    }

    private fun onShootingTestsClick() {
        val fragment = MyDetailsShootingTestsFragment.newInstance()
        activity?.supportFragmentManager?.let { fragment.show(it, MyDetailsShootingTestsFragment.TAG) }
    }

    private fun onMetsahallitusPermitsClick() {
        mUserInfo?.let { u ->
            val fragment = MyDetailsMhPermitListFragment.newInstance(u.username)
            activity?.supportFragmentManager?.let { fragment.show(it, MyDetailsMhPermitListFragment.TAG) }
        }
    }

    private fun onOccupationsClick() {
        val fragment = MyDetailsOccupationsFragment.newInstance()
        activity?.supportFragmentManager?.let { fragment.show(it, MyDetailsOccupationsFragment.TAG) }
    }

    private fun onHuntingGroupMembershipsClick() {
        val fragment = MyDetailsHuntingClubMembershipsFragment()
        activity?.supportFragmentManager?.let { fragmentManager ->
            fragment.show(fragmentManager, MyDetailsHuntingClubMembershipsFragment.TAG)

            // Refresh viewmodel after fragment has been closed, as model might have changed
            fragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                    super.onFragmentViewDestroyed(fm, f)
                    loadViewModel()
                    activity?.supportFragmentManager?.unregisterFragmentLifecycleCallbacks(this)
                }
            }, false)

        }
    }

    companion object {
        internal const val SIMPLE_DATE_FORMAT = "dd.MM.yyyy"
        internal const val SIMPLE_DURATION_FORMAT = "%s - %s"
        internal const val ADDRESS_FORMAT = "%s\n%s %s\n%s"

        @JvmStatic
        fun newInstance(): MyDetailsFragment {
            return MyDetailsFragment()
        }
    }
}
