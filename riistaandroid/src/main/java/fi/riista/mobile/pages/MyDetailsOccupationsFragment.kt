package fi.riista.mobile.pages

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import dagger.android.support.AndroidSupportInjection
import fi.riista.mobile.R
import fi.riista.mobile.models.user.UserInfo
import fi.riista.mobile.utils.AppPreferences
import fi.riista.mobile.utils.UserInfoStore
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class MyDetailsOccupationsFragment : DialogFragment() {

    @Inject
    internal lateinit var mUserInfoStore: UserInfoStore

    private var mContext: Context? = null

    private lateinit var mOccupationsGroup: View
    private lateinit var mOccupationsContainer: ViewGroup

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

        val view = inflater.inflate(R.layout.fragment_my_details_occupations, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { dismiss() }

        mOccupationsGroup = view.findViewById(R.id.my_details_occupations_group)
        mOccupationsContainer = view.findViewById(R.id.my_details_occupations_container)

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

        refreshOccupationInfo(info, df)
    }

    private fun refreshOccupationInfo(info: UserInfo, df: DateFormat) {
        mOccupationsContainer.removeAllViews()

        if (info.hasOccupations()) {
            mOccupationsGroup.visibility = View.VISIBLE
            mOccupationsContainer.removeAllViews()

            val inflater = LayoutInflater.from(mContext)

            for (occupation in info.occupations) {
                val view = inflater.inflate(R.layout.view_occupation_item, mOccupationsContainer, false)
                val typeText = view.findViewById<TextView>(R.id.occupation_type)
                val rhyText = view.findViewById<TextView>(R.id.occupation_rhy)
                val durationText = view.findViewById<TextView>(R.id.occupation_duration)

                val occupationDuration: String = if (occupation.beginDate == null && occupation.endDate == null) {
                    getText(R.string.duration_indefinite).toString()
                } else {
                    String.format(MyDetailsFragment.SIMPLE_DURATION_FORMAT,
                            if (occupation.beginDate != null) df.format(occupation.beginDate) else "",
                            if (occupation.endDate != null) df.format(occupation.endDate) else ""
                    )
                }

                // Swedish rhy and occupation names are localized. Otherwise use finnish ones
                val occupationTitle = occupation.name
                var organizationTitle: Map<String, String>? = null

                if (occupation.organisation != null) {
                    organizationTitle = occupation.organisation.name
                }

                val languageCode = AppPreferences.getLanguageCodeSetting(mContext)

                if (occupationTitle != null) {
                    typeText.text = if (occupationTitle.containsKey(languageCode)) occupationTitle[languageCode] else occupationTitle[AppPreferences.LANGUAGE_CODE_FI]
                } else {
                    typeText.text = null
                }
                if (organizationTitle != null) {
                    rhyText.text = if (organizationTitle.containsKey(languageCode)) organizationTitle[languageCode] else organizationTitle[AppPreferences.LANGUAGE_CODE_FI]
                } else {
                    rhyText.text = null
                }
                durationText.text = occupationDuration

                mOccupationsContainer.addView(view)
            }

        } else {
            mOccupationsGroup.visibility = View.GONE
        }
    }

    companion object {
        const val TAG = "FullScreenOccupationsDialog"

        fun newInstance(): MyDetailsOccupationsFragment {
            return MyDetailsOccupationsFragment()
        }
    }
}
