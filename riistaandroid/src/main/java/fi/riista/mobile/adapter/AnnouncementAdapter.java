package fi.riista.mobile.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.joda.time.Instant;

import java.text.SimpleDateFormat;
import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.models.announcement.Announcement;
import fi.riista.mobile.utils.AppPreferences;

public class AnnouncementAdapter extends ArrayAdapter<Announcement> {

    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("dd.MM.yyyy  HH:mm");

    private static class ViewHolder {
        TextView subject;
        TextView pointOfTime;
        TextView senderName;
        TextView senderTitle;
        TextView senderOrganization;
        TextView messageBody;
    }

    public AnnouncementAdapter(List<Announcement> data, Context context) {
        super(context, R.layout.view_announcement_item, data);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.view_announcement_item, parent, false);

            viewHolder.subject = (TextView) convertView.findViewById(R.id.announcement_subject);
            viewHolder.pointOfTime = (TextView) convertView.findViewById(R.id.announcement_pointoftime);
            viewHolder.senderName = (TextView) convertView.findViewById(R.id.announcement_sender_name);
            viewHolder.senderTitle = (TextView) convertView.findViewById(R.id.announcement_sender_title);
            viewHolder.senderOrganization = (TextView) convertView.findViewById(R.id.announcement_sender_organization);
            viewHolder.messageBody = (TextView) convertView.findViewById(R.id.announcement_message);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Announcement item = getItem(position);

        if (item != null) {
            String languageCode = AppPreferences.getLanguageCodeSetting(getContext());

            viewHolder.subject.setText(item.subject);
            viewHolder.pointOfTime.setText(sDateFormat.format(Instant.parse(item.pointOfTime).toDate()));
            viewHolder.senderName.setText(item.sender.fullName);
            viewHolder.senderTitle.setText(item.sender.title.get(
                    item.sender.title.containsKey(languageCode) ? languageCode : AppPreferences.LANGUAGE_CODE_FI));
            viewHolder.senderOrganization.setText(item.sender.organisation.get(
                    item.sender.organisation.containsKey(languageCode) ? languageCode : AppPreferences.LANGUAGE_CODE_FI));
            viewHolder.messageBody.setText(item.body);
        }

        return convertView;
    }
}
