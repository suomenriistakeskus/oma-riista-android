package fi.riista.mobile.pages

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import dagger.android.support.AndroidSupportInjection
import fi.riista.mobile.R
import fi.riista.mobile.models.shootingTest.ShootingTestType
import fi.riista.mobile.models.shootingTest.ShootingTestType.*
import fi.riista.mobile.models.user.UserInfo
import fi.riista.mobile.utils.UserInfoStore
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MyDetailsShootingTestsFragment : DialogFragment() {

    @Inject
    internal lateinit var mUserInfoStore: UserInfoStore

    private var mContext: Context? = null

    private lateinit var mNameText: TextView
    private lateinit var mHunterNumberText: TextView
    private lateinit var mShootingTestsContainer: ViewGroup
    private lateinit var mShootingTestsGroup: View
    private lateinit var mNoShootingTestAttemptsText: TextView

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_my_details_shooting_tests, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { dismiss() }

        mNameText = view.findViewById(R.id.my_details_shooting_tests_name_value)
        mHunterNumberText = view.findViewById(R.id.my_details_shooting_tests_hunter_number_value)
        mShootingTestsContainer = view.findViewById(R.id.my_details_shooting_tests_container)
        mShootingTestsGroup = view.findViewById(R.id.my_details_shooting_tests_group)
        mNoShootingTestAttemptsText = view.findViewById(R.id.my_details_no_shooting_test_attempts)

        mContext = inflater.context

        return view
    }

    override fun onResume() {
        super.onResume()

        mUserInfoStore.getUserInfo()?.let { userInfo ->
            // Timestamp check is to make sure the data from server is not in outdated format.
            if (userInfo.timestamp != null) {
                refreshDisplayInfo(userInfo)
            }
        }
    }

    private fun refreshDisplayInfo(info: UserInfo) {
        val df = SimpleDateFormat(MyDetailsFragment.SIMPLE_DATE_FORMAT, Locale.getDefault())
        df.timeZone = TimeZone.getTimeZone("EET")

        mNameText.text = "${info.firstName} ${info.lastName}"
        mHunterNumberText.text = if (info.hunterNumber.isNullOrBlank()) {
            "-"
        } else {
            info.hunterNumber
        }

        refreshShootingTestsInfo(info, df)
    }

    private fun refreshShootingTestsInfo(info: UserInfo, df: DateFormat) {
        mShootingTestsContainer.removeAllViews()

        if (info.shootingTests != null && info.shootingTests.isNotEmpty()) {
            mShootingTestsGroup.visibility = View.VISIBLE
            mNoShootingTestAttemptsText.visibility = View.GONE

            mShootingTestsContainer.removeAllViews()

            val inflater = LayoutInflater.from(mContext)

            for (shootingTest in info.shootingTests) {
                val view = inflater.inflate(R.layout.view_user_shooting_test_item, mShootingTestsContainer, false)
                val rhyText = view.findViewById<TextView>(R.id.user_shooting_test_rhy)
                val typeText = view.findViewById<TextView>(R.id.user_shooting_test_type)
                val datesText = view.findViewById<TextView>(R.id.user_shooting_test_valid_dates)

                var shootingTestValidDates = ""

                if (shootingTest.begin == null && shootingTest.end == null) {
                    shootingTestValidDates = getText(R.string.duration_indefinite).toString()
                } else {
                    try {
                        shootingTestValidDates = String.format(MyDetailsFragment.SIMPLE_DURATION_FORMAT,
                                if (shootingTest.begin != null) df.format(SimpleDateFormat("yyyy-MM-dd", Locale("fi")).parse(shootingTest.begin)) else "",
                                if (shootingTest.end != null) df.format(SimpleDateFormat("yyyy-MM-dd", Locale("fi")).parse(shootingTest.end)) else ""
                        )
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                }

                typeText.text = shootingTestTypeToText(shootingTest.type)
                datesText.text = shootingTestValidDates
                rhyText.text = shootingTest.rhyName

                mShootingTestsContainer.addView(view)
            }

        } else {
            mShootingTestsGroup.visibility = View.GONE
            mNoShootingTestAttemptsText.visibility = View.VISIBLE
        }
    }

    private fun shootingTestTypeToText(type: ShootingTestType): String {
        @StringRes val strResId: Int = when (type) {
            MOOSE -> R.string.shooting_test_type_moose
            BEAR -> R.string.shooting_test_type_bear
            ROE_DEER -> R.string.shooting_test_type_roe_deer
            BOW -> R.string.shooting_test_type_bow
        }

        return getString(strResId)
    }

    companion object {
        const val TAG = "FullScreenShootingTestsDialog"

        @JvmStatic
        fun newInstance(): MyDetailsShootingTestsFragment {
            return MyDetailsShootingTestsFragment()
        }
    }
}
