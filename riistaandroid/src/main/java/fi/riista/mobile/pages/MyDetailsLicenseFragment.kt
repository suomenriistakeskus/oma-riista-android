package fi.riista.mobile.pages

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
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

class MyDetailsLicenseFragment : DialogFragment() {

    @Inject
    internal lateinit var mUserInfoStore: UserInfoStore

    private lateinit var mContext: Context

    private lateinit var mHunterName: TextView
    private lateinit var mHunterIdText: TextView
    private lateinit var mPaymentText: TextView
    private lateinit var mMembershipText: TextView
    private lateinit var mInsuranceText: TextView
    private lateinit var mNoValidLicenceItem: TextView

    private lateinit var mHuntingBanItem: View
    private lateinit var mHuntingBanText: TextView

    private lateinit var mHunterNameGroup: View
    private lateinit var mHunterIdGroup: View
    private lateinit var mPaymentGroup: View
    private lateinit var mMembershipGroup: View

    private lateinit var mQrCodeImageView: ImageView

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}

        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_my_details_license, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { dismiss() }

        mHunterName = view.findViewById(R.id.my_details_name_value)
        mHunterIdText = view.findViewById(R.id.my_details_hunter_id_value)
        mPaymentText = view.findViewById(R.id.my_details_payment_value)
        mMembershipText = view.findViewById(R.id.my_details_membership_value)
        mInsuranceText = view.findViewById(R.id.my_details_insurance_policy)
        mNoValidLicenceItem = view.findViewById(R.id.my_details_no_valid_license)

        mHuntingBanItem = view.findViewById(R.id.my_details_hunting_ban_group)
        mHuntingBanText = view.findViewById(R.id.my_details_hunting_ban_value)

        mHunterNameGroup = view.findViewById(R.id.my_details_name_group)
        mHunterIdGroup = view.findViewById(R.id.my_details_hunter_id_group)
        mPaymentGroup = view.findViewById(R.id.my_details_payment_group)
        mMembershipGroup = view.findViewById(R.id.my_details_membership_group)

        mQrCodeImageView = view.findViewById(R.id.my_details_qr_code_image)

        mContext = inflater.context

        return view
    }

    override fun onResume() {
        super.onResume()

         mUserInfoStore.getUserInfo()?.let { userInfo ->
            if (userInfo.timestamp != null) {
                refreshDisplayInfo(userInfo)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.let { it ->
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            it.window?.setLayout(width, height)
        }
    }

    private fun refreshDisplayInfo(info: UserInfo) {
        val df = SimpleDateFormat(MyDetailsFragment.SIMPLE_DATE_FORMAT, Locale.getDefault())
        df.timeZone = TimeZone.getTimeZone("EET")

        refreshHunterRegistryInfo(info, df)
        refreshQrCode(info)
    }

    private fun refreshHunterRegistryInfo(info: UserInfo, df: DateFormat) {
        if (info.huntingBanStart != null || info.huntingBanEnd != null) {
            refreshHuntingBanInfo(info, df)

            mHunterNameGroup.visibility = View.GONE
            mHunterIdGroup.visibility = View.GONE
            mPaymentGroup.visibility = View.GONE
            mMembershipGroup.visibility = View.GONE
            mInsuranceText.visibility = View.GONE
            mNoValidLicenceItem.visibility = View.GONE
            mHuntingBanItem.visibility = View.VISIBLE
        } else if (info.huntingCardValidNow!!) {
            refreshHuntingCardInfo(info, df)

            mHunterNameGroup.visibility = View.VISIBLE
            mHunterIdGroup.visibility = View.VISIBLE
            mPaymentGroup.visibility = View.VISIBLE
            mMembershipGroup.visibility = View.VISIBLE
            mInsuranceText.visibility = View.VISIBLE
            mNoValidLicenceItem.visibility = View.GONE
            mHuntingBanItem.visibility = View.GONE
        } else {
            mHunterNameGroup.visibility = View.GONE
            mHunterIdGroup.visibility = View.GONE
            mPaymentGroup.visibility = View.GONE
            mMembershipGroup.visibility = View.GONE
            mInsuranceText.visibility = View.GONE
            mNoValidLicenceItem.visibility = View.VISIBLE
            mHuntingBanItem.visibility = View.GONE
        }
    }

    private fun refreshHuntingBanInfo(info: UserInfo, df: DateFormat) {
        mHuntingBanText.text = String.format(MyDetailsFragment.SIMPLE_DURATION_FORMAT,
                if (info.huntingBanStart != null) df.format(info.huntingBanStart) else "",
                if (info.huntingBanEnd != null) df.format(info.huntingBanEnd) else ""
        )
    }

    private fun refreshHuntingCardInfo(info: UserInfo, df: DateFormat) {
        mHunterName.text = String.format("%s %s", info.firstName, info.lastName)
        mHunterIdText.text = info.hunterNumber
        mPaymentText.text = if (info.huntingCardValidNow != null && info.huntingCardValidNow!!)
            String.format(getString(R.string.my_details_fee_paid_format), df.format(info.huntingCardStart), df.format(info.huntingCardEnd))
        else
            getString(R.string.my_details_fee_not_paid)

        val rhy = info.rhy
        val languageCode = AppPreferences.getLanguageCodeSetting(mContext)

        if (rhy != null && rhy.name != null && rhy.name.isNotEmpty() && rhy.name.containsKey(languageCode)) {
            mMembershipText.text = String.format(MEMBERSHIP_NAME_FORMAT, rhy.name[languageCode], rhy.officialCode)
        } else if (rhy != null && rhy.name != null && rhy.name.isNotEmpty()) {
            mMembershipText.text = String.format(MEMBERSHIP_NAME_FORMAT, rhy.name[AppPreferences.LANGUAGE_CODE_FI], rhy.officialCode)
        } else {
            mMembershipText.text = null
        }
    }

    private fun refreshQrCode(info: UserInfo) {
        try {
            if (info.qrCode != null && !info.qrCode.isEmpty()) {
                val bmp = encodeAsBitmap(info.qrCode)
                mQrCodeImageView.setImageBitmap(bmp)
            } else {
                mQrCodeImageView.setImageBitmap(null)
            }
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    @Throws(WriterException::class)
    private fun encodeAsBitmap(str: String): Bitmap? {
        val result: BitMatrix
        try {
            result = MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, 480, 480, null)
        } catch (iae: IllegalArgumentException) {
            // Unsupported format
            return null
        }

        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (result.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, 480, 0, 0, w, h)
        return bitmap
    }

    companion object {
        const val TAG = "FullScreenLicenseDialog"
        private const val MEMBERSHIP_NAME_FORMAT = "%s (%s)"

        @JvmStatic
        fun newInstance(): MyDetailsLicenseFragment {
            return MyDetailsLicenseFragment()
        }
    }
}
