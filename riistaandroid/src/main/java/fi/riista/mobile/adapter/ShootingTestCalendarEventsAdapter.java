package fi.riista.mobile.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.models.shootingTest.ShootingTestCalendarEvent;
import fi.riista.mobile.models.shootingTest.ShootingTestOfficial;
import fi.riista.mobile.utils.DateTimeUtils;

import static java.lang.String.format;

public class ShootingTestCalendarEventsAdapter extends ArrayAdapter<ShootingTestCalendarEvent> {

    public ShootingTestCalendarEventsAdapter(final List<ShootingTestCalendarEvent> data, final Context context) {
        super(context, R.layout.view_shooting_test_calendar_event_item, data);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.view_shooting_test_calendar_event_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.eventTitle = convertView.findViewById(R.id.shooting_test_list_event_title);
            viewHolder.datetime = convertView.findViewById(R.id.shooting_test_list_datetime);
            viewHolder.location = convertView.findViewById(R.id.shooting_test_list_location);
            viewHolder.state = convertView.findViewById(R.id.shooting_test_list_state);
            viewHolder.officialsTitle = convertView.findViewById(R.id.shooting_test_officials_title);
            viewHolder.officials = convertView.findViewById(R.id.shooting_test_officials_list);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ShootingTestCalendarEvent item = getItem(position);

        if (item != null) {
            final Context context = getContext();
            final String localisedDateText = DateTimeUtils.convertDateStringToFinnishFormat(item.date);

            viewHolder.eventTitle.setText(format("%s %s",
                    ShootingTestCalendarEvent.localisedEventTypeText(context, item.calendarEventType).toUpperCase(),
                    TextUtils.isEmpty(item.name) ? "" : item.name));
            viewHolder.datetime.setText(format("%s %s %s",
                    localisedDateText,
                    item.beginTime,
                    TextUtils.isEmpty(item.endTime) ? "" : "- " + item.endTime));
            viewHolder.location.setText(format("%s\n%s\n%s",
                    item.venue.name != null ? item.venue.name : "",
                    item.venue.address.streetAddress != null ? item.venue.address.streetAddress : "",
                    item.venue.address.city != null ? item.venue.address.city : ""));

            final String localisedStateText;
            if (item.isClosed()) {
                localisedStateText = context.getString(R.string.shooting_test_state_closed);
            } else if (item.isOngoing()) {
                localisedStateText = context.getString(R.string.shooting_test_state_ongoing);
            } else {
                localisedStateText = context.getString(R.string.shooting_test_state_waiting);
            }
            viewHolder.state.setText(localisedStateText);

            viewHolder.officialsTitle.setVisibility(item.officials.size() > 0 ? View.VISIBLE : View.GONE);

            viewHolder.officials.removeAllViews();
            for (final ShootingTestOfficial official : item.officials) {
                final TextView textView = new TextView(context, null, R.style.shooting_test_text);
                textView.setText(format("%s %s", official.firstName, official.lastName));

                viewHolder.officials.addView(textView);
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView eventTitle;
        TextView datetime;
        TextView location;
        TextView state;
        TextView officialsTitle;
        LinearLayout officials;
    }
}
