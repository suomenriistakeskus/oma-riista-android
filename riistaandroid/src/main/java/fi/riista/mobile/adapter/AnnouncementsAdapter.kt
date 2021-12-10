package fi.riista.mobile.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import fi.riista.mobile.R
import fi.riista.mobile.models.announcement.Announcement
import fi.riista.mobile.utils.AppPreferences
import org.joda.time.Instant
import java.text.SimpleDateFormat

class AnnouncementsAdapter(data: List<Announcement>, context: Context) : ArrayAdapter<Announcement>(context, R.layout.view_announcement_item, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cView = convertView
        val viewHolder: ViewHolder

        if (cView == null) {
            viewHolder = ViewHolder()
            cView = LayoutInflater.from(context).inflate(R.layout.view_announcement_item, parent, false)

            viewHolder.subject = cView.findViewById(R.id.announcement_subject)
            viewHolder.pointOfTime = cView.findViewById(R.id.announcement_pointoftime)
            viewHolder.senderTitle = cView.findViewById(R.id.announcement_sender_title)
            viewHolder.messageBody = cView.findViewById(R.id.announcement_message)

            cView.tag = viewHolder
        } else {
            viewHolder = cView.tag as ViewHolder
        }

        val item = getItem(position)

        if (item != null) {
            val languageCode = AppPreferences.getLanguageCodeSetting(context)

            val senderTitle = item.sender.title[if (item.sender.title.containsKey(languageCode)) languageCode else AppPreferences.LANGUAGE_CODE_FI]
            val senderOrganisation = item.sender.organisation[if (item.sender.organisation.containsKey(languageCode)) languageCode else AppPreferences.LANGUAGE_CODE_FI]

            viewHolder.subject.text = item.subject
            viewHolder.pointOfTime.text = sDateFormat.format(Instant.parse(item.pointOfTime).toDate())
            viewHolder.senderTitle.text = String.format("%s - %s", senderTitle, senderOrganisation)
            viewHolder.messageBody.text = item.body
        }

        return cView!!
    }

    private class ViewHolder {
        internal lateinit var subject: TextView
        internal lateinit var pointOfTime: TextView
        internal lateinit var senderTitle: TextView
        internal lateinit var messageBody: TextView
    }

    companion object {

        @SuppressLint("SimpleDateFormat")
        private val sDateFormat = SimpleDateFormat("dd.MM.yyyy")
    }
}
