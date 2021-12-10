package fi.riista.mobile.pages

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import dagger.android.support.AndroidSupportInjection
import fi.riista.mobile.R
import fi.riista.mobile.models.user.UserInfo
import fi.riista.mobile.utils.UserInfoStore
import fi.riista.mobile.utils.Utils
import java.util.*
import javax.inject.Inject

class ContactDetailsFragment : PageFragment() {

    @Inject
    internal lateinit var mUserInfoStore: UserInfoStore

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_contact_details, container, false)
        setupActionBar(R.layout.actionbar_contact_details, false)

        val supportPhoneBtn = view.findViewById<CardView>(R.id.contact_support_phone_btn)
        supportPhoneBtn.setOnClickListener {
            val phoneNumber = resources.getString(R.string.customer_service_phone_number)
            val url = "tel:" + phoneNumber.replace(" ", "")
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
            startActivitySafe(intent)
        }

        val userInfo = mUserInfoStore.getUserInfo()

        if (userInfo != null) {
            setupEmailSupportButton(view, userInfo)
        }

        val licensePhoneButton = view.findViewById<CardView>(R.id.contact_license_phone_btn)
        licensePhoneButton.setOnClickListener {
            val phoneNumber = resources.getString(R.string.contact_details_license_tel_number)
            val url = "tel:" + phoneNumber.replace(" ", "")
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
            startActivitySafe(intent)
        }

        if (userInfo != null) {
            setupLicenseEmailButton(view, userInfo)
        }

        return view
    }

    private fun setupEmailSupportButton(view: View, user: UserInfo) {
        val versionName = Utils.getAppVersionName()
        val version = versionName ?: resources.getString(R.string.unknown)

        val emailSupportBtn = view.findViewById<CardView>(R.id.contact_support_email_btn)
        emailSupportBtn.setOnClickListener {
            val emailAddress = resources.getString(R.string.customer_service_email_address)

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_title))

            val name = String.format("%s %s", user.firstName, user.lastName)

            val textFormat = resources.getString(R.string.support_email_template)
            val manufacturer = android.os.Build.MANUFACTURER.replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase(Locale.getDefault())
                } else {
                    it.toString()
                }
            }
            val deviceString = manufacturer + " " + android.os.Build.MODEL
            val androidVersion = android.os.Build.VERSION.RELEASE
            val emailText = String.format(textFormat, name, user.hunterNumber, version, deviceString, androidVersion)
            intent.putExtra(Intent.EXTRA_TEXT, emailText)

            startActivitySafe(Intent.createChooser(intent, resources.getString(R.string.sendemail)))
        }
    }

    private fun setupLicenseEmailButton(view: View, user: UserInfo) {
        val emailBtn = view.findViewById<CardView>(R.id.contact_license_email_btn)

        emailBtn.setOnClickListener {
            val emailAddress = resources.getString(R.string.contact_details_license_email)

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.license_email_title))

            val name = String.format("%s %s", user.firstName, user.lastName)

            val textFormat = resources.getString(R.string.license_email_template)
            val emailText = String.format(textFormat, name, user.hunterNumber)
            intent.putExtra(Intent.EXTRA_TEXT, emailText)

            startActivitySafe(Intent.createChooser(intent, resources.getString(R.string.sendemail)))
        }
    }

    private fun startActivitySafe(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Utils.LogMessage("Can't start activity: " + e.message)
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(): ContactDetailsFragment {
            return ContactDetailsFragment()
        }
    }
}
