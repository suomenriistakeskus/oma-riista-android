package fi.riista.mobile.pages

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import fi.riista.mobile.R
import fi.riista.mobile.models.announcement.Announcement
import fi.riista.mobile.utils.AppPreferences
import org.joda.time.Instant
import java.text.SimpleDateFormat

class AnnouncementDialogFragment : DialogFragment() {

    lateinit var item: Announcement

    private lateinit var subject: TextView
    private lateinit var pointOfTime: TextView
    private lateinit var senderTitle: TextView
    private lateinit var message: TextView

    @SuppressLint("SimpleDateFormat")
    private val sDateFormat = SimpleDateFormat("dd.MM.yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            item = it.getSerializable(EXTRA_ANNOUNCEMENT) as Announcement
        }

        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_announcement_dialog, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { dismiss() }

        subject = view.findViewById(R.id.announcement_subject)
        pointOfTime = view.findViewById(R.id.announcement_pointoftime)
        senderTitle = view.findViewById(R.id.announcement_sender_title)
        message = view.findViewById(R.id.announcement_message)

        val languageCode = AppPreferences.getLanguageCodeSetting(context)

        val titleText = item.sender.getTitle(languageCode)
        val organisationText = item.sender.getOrganisation(languageCode)

        subject.text = item.subject
        pointOfTime.text = sDateFormat.format(Instant.parse(item.pointOfTime).toDate())
        senderTitle.text = String.format("%s - %s", titleText, organisationText)
        message.text = item.body

        return view
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    companion object {
        const val TAG = "FullScreenAnnouncementDialog"
        const val EXTRA_ANNOUNCEMENT = "extraAnnouncement"

        @JvmStatic
        fun newInstance(announcement: Announcement) = AnnouncementDialogFragment().apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_ANNOUNCEMENT, announcement)
            }
        }
    }
}
