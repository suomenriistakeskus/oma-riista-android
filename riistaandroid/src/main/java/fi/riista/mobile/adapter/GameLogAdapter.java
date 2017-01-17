package fi.riista.mobile.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.database.EventItem;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.LogEventBase;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.utils.UiUtils;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.view.WebImageView;

public class GameLogAdapter extends ArrayAdapter<EventItem> {

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("dd.MM.yyyy  HH:mm");
    private static final String HARVEST_AMOUNT_FORMAT = " (%d)";

    private List<EventItem> mItems;
    private WorkContext mWorkContext;
    private Context mContext;

    public GameLogAdapter(WorkContext context, List<EventItem> events) {
        super(context.getContext(), R.layout.view_logitem, events);

        mWorkContext = context;
        mContext = context.getContext();
        mItems = events;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        EventItem item = mItems.get(position);
        if (item.isHeader) {
            return ViewType.HEADER.ordinal();
        } else if (item.isSeparator) {
            return ViewType.SEPARATOR.ordinal();
        }
        return ViewType.ITEM.ordinal();
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == ViewType.ITEM.ordinal();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = convertView;
        ViewType type = ViewType.values()[getItemViewType(position)];
        EventItem event = mItems.get(position);
        if (type == ViewType.HEADER) {
            if (view == null)
                view = inflator.inflate(R.layout.view_logitem_header, parent, false);
            TextView textView = (TextView) view.findViewById(R.id.itemMonth);
            String timeString = "";

            // If the year differs from previous type, add year prefix
            if (position > 1) {
                ViewType previousItemType = ViewType.values()[getItemViewType(position - 2)];
                if (previousItemType == ViewType.ITEM) {
                    int currentItemYear = mItems.get(position).year;
                    int previousItemYear = mItems.get(position - 2).year;
                    if (currentItemYear != previousItemYear) {
                        timeString = String.valueOf(currentItemYear) + " ";
                    }
                }
            }
            String monthString = mContext.getResources().getStringArray(R.array.months)[event.month].toUpperCase();
            timeString += monthString;
            textView.setText(timeString);
        } else if (type == ViewType.ITEM) {
            view = inflator.inflate(R.layout.view_logitem, parent, false);

            WebImageView speciesImageView = (WebImageView) view.findViewById(R.id.speciesimage);
            speciesImageView.setPadding(0, 0, 0, 0);
            if (event.mEvent.mImages != null && event.mEvent.mImages.size() > 0) {
                // Use first image
                int width = (int) mContext.getResources().getDimension(R.dimen.logimage_size);
                int height = (int) mContext.getResources().getDimension(R.dimen.logimage_size);
                Utils.setupImage(mWorkContext, speciesImageView, event.mEvent.mImages.get(0), width, height, false, null);
            } else {
                if (event.mEvent.mType.equals(LogEventBase.TYPE_SRVA) && event.mEvent.mSrvaEvent.otherSpeciesDescription != null) {
                    int pad = UiUtils.dipToPixels(getContext(), 5);
                    speciesImageView.setPadding(pad, pad, pad, pad);
                }
                speciesImageView.setImageDrawable(Utils.getSpeciesImage(mContext, event.mEvent.mSpeciesID));
            }

            TextView textView = (TextView) view.findViewById(R.id.itemText);
            textView.setText(speciesTitle(event.mEvent));
            TextView textView2 = (TextView) view.findViewById(R.id.itemDescription);

            if (event.mEvent.mType.equals(LogEventBase.TYPE_SRVA) && event.mEvent.mSrvaEvent != null) {
                setupSrvaState(view, event.mEvent.mSrvaEvent);
            } else {
                setupHarvestReportState(view, event.mEvent);
            }

            String date = sDateFormat.format(event.mEvent.mTime.getTime());
            textView2.setText(date);

            ImageView uploadedImageView = (ImageView) view.findViewById(R.id.logitemuploadedimage);
            if (!event.mEvent.mSent) {
                uploadedImageView.setVisibility(View.VISIBLE);
                uploadedImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_stat_upload));
            } else {
                uploadedImageView.setVisibility(View.INVISIBLE);
            }

            ImageView logTypeImageView = (ImageView) view.findViewById(R.id.logitemtypeimage);
            switch (event.mEvent.mType) {
                case LogEventBase.TYPE_HARVEST:
                    logTypeImageView.setColorFilter(null);
                    logTypeImageView.setImageResource(R.drawable.ic_kaato);
                    break;
                case LogEventBase.TYPE_OBSERVATION:
                    logTypeImageView.setColorFilter(mContext.getResources().getColor(R.color.icon_colorize), Mode.MULTIPLY);
                    logTypeImageView.setImageResource(R.drawable.ic_spot);
                    break;
                case LogEventBase.TYPE_SRVA:
                    logTypeImageView.setColorFilter(mContext.getResources().getColor(R.color.icon_colorize), Mode.MULTIPLY);
                    logTypeImageView.setImageResource(R.drawable.ic_srva_white);
                    break;
            }

        } else if (type == ViewType.SEPARATOR) {
            if (view == null)
                view = inflator.inflate(R.layout.view_logitem_pipe, parent, false);
        }
        return view;
    }

    private String speciesTitle(GameHarvest event) {
        String title;

        Species species = SpeciesInformation.getSpecies(event.mSpeciesID);
        if (species != null) {
            title = species.mName;
        } else {
            title = getContext().getString(R.string.srva_other);
        }

        if (title != null && event.mAmount > 1) {
            title += String.format(HARVEST_AMOUNT_FORMAT, event.mAmount);
        }

        if (species == null && event.mSrvaEvent != null && event.mSrvaEvent.otherSpeciesDescription != null) {
            title += " - " + event.mSrvaEvent.otherSpeciesDescription;
        }

        return title;
    }

    private void setupSrvaState(View view, SrvaEvent event) {
        ImageView indicator = ((ImageView) view.findViewById(R.id.harvestStateIndicator));

        int trafficLightColor = Color.TRANSPARENT;
        String state = event.state;

        if (SrvaEvent.STATE_APPROVED.equals(state)) {
            trafficLightColor = getContext().getResources().getColor(R.color.harvest_approved);
        } else if (SrvaEvent.STATE_REJECTED.equals(state)) {
            trafficLightColor = getContext().getResources().getColor(R.color.harvest_rejected);
        }

        if (trafficLightColor != Color.TRANSPARENT) {
            indicator.setColorFilter(trafficLightColor, PorterDuff.Mode.SRC_ATOP);
            indicator.setVisibility(View.VISIBLE);
        } else {
            indicator.setVisibility(View.GONE);
        }
    }

    /**
     * Set log entry state "traffic light" color.
     * Set color according to "added to permit" state if it exist. If not set color according to "harvest report" state
     *
     * @param view  root view
     * @param event log entry
     */
    private void setupHarvestReportState(View view, GameHarvest event) {
        ImageView indicator = ((ImageView) view.findViewById(R.id.harvestStateIndicator));

        int trafficLightColor = Color.TRANSPARENT;
        String permitState = event.mStateAcceptedToHarvestPermit;

        if (permitState != null && !permitState.isEmpty()) {
            if (GameHarvest.PERMIT_PROPOSED.equals(permitState)) {
                trafficLightColor = getContext().getResources().getColor(R.color.permit_proposed);
            } else if (GameHarvest.PERMIT_ACCEPTED.equals(permitState)) {
                trafficLightColor = getContext().getResources().getColor(R.color.permit_accepted);
            } else if (GameHarvest.PERMIT_REJECTED.equals(permitState)) {
                trafficLightColor = getContext().getResources().getColor(R.color.permit_rejected);
            }
        } else if (event.mHarvestReportDone) {
            String reportState = event.mHarvestReportState;

            if (GameHarvest.HARVEST_PROPOSED.equals(reportState)) {
                trafficLightColor = getContext().getResources().getColor(R.color.harvest_proposed);
            } else if (GameHarvest.HARVEST_SENT_FOR_APPROVAL.equals(reportState)) {
                trafficLightColor = getContext().getResources().getColor(R.color.harvest_sent_for_approval);
            } else if (GameHarvest.HARVEST_APPROVED.equals(reportState)) {
                trafficLightColor = getContext().getResources().getColor(R.color.harvest_approved);
            } else if (GameHarvest.HARVEST_REJECTED.equals(reportState)) {
                trafficLightColor = getContext().getResources().getColor(R.color.harvest_rejected);
            }
        } else if (event.mHarvestReportRequired) {
            trafficLightColor = getContext().getResources().getColor(R.color.harvest_create_report);
        }

        if (trafficLightColor != Color.TRANSPARENT) {
            indicator.setColorFilter(trafficLightColor, PorterDuff.Mode.SRC_ATOP);
            indicator.setVisibility(View.VISIBLE);
        } else {
            indicator.setVisibility(View.GONE);
        }
    }

    private enum ViewType {
        HEADER,
        SEPARATOR,
        ITEM
    }
}
