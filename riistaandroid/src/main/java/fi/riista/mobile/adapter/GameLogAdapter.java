package fi.riista.mobile.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import fi.riista.mobile.R;
import fi.riista.mobile.database.HarvestDatabase;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.GameLog;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.observation.ObservationType;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.observation.ObservationStrings;
import fi.riista.mobile.ui.GameLogListItem;
import fi.riista.mobile.utils.DiaryImageUtil;
import fi.riista.mobile.utils.UiUtils;
import fi.vincit.androidutilslib.view.WebImageView;

public class GameLogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private enum ViewType {
        HEADER,
        ITEM,
        STATS
    }

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("dd.MM.yyyy   HH:mm");
    private static final String AMOUNT_SUFFIX_FORMAT = " (%d)";

    private final Context mContext;
    private final GameLogListItem.OnClickListItemListener mListener;

    private List<GameLogListItem> mItems;
    private HarvestDatabase.SeasonStats mSeasonStats;

    public GameLogAdapter(final Context context,
                          final List<GameLogListItem> items,
                          final GameLogListItem.OnClickListItemListener listener) {

        this.mContext = context;
        this.mItems = items;
        this.mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(final int position) {
        final GameLogListItem item = mItems.get(position);

        if (item.isHeader) {
            return ViewType.HEADER.ordinal();
        } else if (item.isStats) {
            return ViewType.STATS.ordinal();
        }

        return ViewType.ITEM.ordinal();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RecyclerView.ViewHolder viewHolder;

        if (viewType == ViewType.HEADER.ordinal()) {
            final View view = inflater.inflate(R.layout.view_log_item_section, parent, false);
            viewHolder = new LogHeaderViewHolder(view);
        } else if (viewType == ViewType.ITEM.ordinal()) {
            final View view = inflater.inflate(R.layout.view_log_item, parent, false);
            viewHolder = new LogItemViewHolder(view);
        } else if (viewType == ViewType.STATS.ordinal()) {
            final View view = inflater.inflate(R.layout.view_log_stats, parent, false);
            viewHolder = new LogStatsViewHolder(view);
        } else {
            throw new RuntimeException("Unknown view type: " + viewType);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final GameLogListItem item = mItems.get(position);

        if (ViewType.HEADER.ordinal() == holder.getItemViewType()) {
            final String text = mContext.getResources().getStringArray(R.array.months)[item.month];

            final LogHeaderViewHolder headerViewHolder = (LogHeaderViewHolder) holder;
            headerViewHolder.textView.setText(text);
            headerViewHolder.timeline.setVisibility(item.isTimelineTopVisible && item.isTimelineBottomVisible ? View.VISIBLE : View.GONE);

        } else if (ViewType.ITEM.ordinal() == holder.getItemViewType()) {
            final LogItemViewHolder itemViewHolder = (LogItemViewHolder) holder;
            itemViewHolder.bind(item, mListener);

            // Reset image to prevent wrong species flashing before loading finishes.
            itemViewHolder.imageView.setImageDrawable(null);

            if (item.images != null && item.images.size() > 0) {
                itemViewHolder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                final int edgeLen = (int) mContext.getResources().getDimension(R.dimen.log_image_size);
                DiaryImageUtil.setupImage(
                        mContext, itemViewHolder.imageView, item.images.get(0), edgeLen, edgeLen, false, null);
            } else {
                itemViewHolder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                if (GameLog.TYPE_SRVA.equals(item.type) && item.mSrva.otherSpeciesDescription != null) {
                    final int padding = UiUtils.dipToPixels(mContext, 5);
                    itemViewHolder.imageView.setPadding(padding, padding, padding, padding);
                }
                itemViewHolder.imageView.setImageDrawable(SpeciesInformation.getSpeciesImage(mContext, item.speciesCode));
            }

            itemViewHolder.dateTimeView.setText(sDateFormat.format(item.dateTime.getTime()));
            itemViewHolder.speciesView.setText(speciesTitle(item));

            if (GameLog.TYPE_HARVEST.equals(item.type)) {
                itemViewHolder.descriptionView.setText(null);
                itemViewHolder.descriptionView.setVisibility(View.GONE);
                setupHarvestReportState(itemViewHolder.stateImageView, itemViewHolder.stateTextView, itemViewHolder.stateGroup, item.mHarvest);
            } else if (GameLog.TYPE_OBSERVATION.equals(item.type)) {
                itemViewHolder.descriptionView.setText(
                        ObservationStrings.get(mContext, ObservationType.toString(item.mObservation.observationType)));
                itemViewHolder.descriptionView.setVisibility(View.VISIBLE);
                itemViewHolder.stateTextView.setText(null);
                itemViewHolder.stateGroup.setVisibility(View.GONE);
            } else if (GameLog.TYPE_SRVA.equals(item.type)) {
                itemViewHolder.descriptionView.setText(ObservationStrings.get(mContext, item.mSrva.eventName));
                itemViewHolder.descriptionView.setVisibility(View.VISIBLE);
                setupSrvaState(itemViewHolder.stateImageView, itemViewHolder.stateTextView, itemViewHolder.stateGroup, item.mSrva);
            }

            itemViewHolder.uploadImageView.setVisibility(item.sent ? View.GONE : View.VISIBLE);
            itemViewHolder.timelineTop.setVisibility(item.isTimelineTopVisible ? View.VISIBLE : View.GONE);
            itemViewHolder.timelineBottom.setVisibility(item.isTimelineBottomVisible ? View.VISIBLE : View.GONE);

        } else if (ViewType.STATS.ordinal() == holder.getItemViewType()) {
            final LogStatsViewHolder itemViewHolder = (LogStatsViewHolder) holder;

            if (mSeasonStats != null) {
                final SparseIntArray categories = mSeasonStats.mCategoryData;
                itemViewHolder.category1Amount.setText(String.valueOf(categories.get(1)));
                itemViewHolder.category2Amount.setText(String.valueOf(categories.get(2)));
                itemViewHolder.category3Amount.setText(String.valueOf(categories.get(3)));
            } else {
                itemViewHolder.category1Amount.setText("0");
                itemViewHolder.category2Amount.setText("0");
                itemViewHolder.category3Amount.setText("0");
            }
        }
    }

    public void setItems(final List<GameLogListItem> mItems) {
        this.mItems = mItems;
        notifyDataSetChanged();
    }

    public void setStats(final HarvestDatabase.SeasonStats stats) {
        this.mSeasonStats = stats;
    }

    private String speciesTitle(final GameLogListItem item) {
        final Species species = SpeciesInformation.getSpecies(item.speciesCode);
        String title = species != null ? species.mName : mContext.getString(R.string.srva_other);

        if (title != null && item.totalSpecimenAmount > 1) {
            title += String.format(Locale.getDefault(), AMOUNT_SUFFIX_FORMAT, item.totalSpecimenAmount);
        }

        if (species == null && item.mSrva != null && item.mSrva.otherSpeciesDescription != null) {
            title += " - " + item.mSrva.otherSpeciesDescription;
        }

        return title;
    }

    private void setupHarvestReportState(final ImageView stateImage,
                                         final TextView stateText,
                                         final Group stateGroup,
                                         final GameHarvest event) {

        final String permitState = event.mStateAcceptedToHarvestPermit;
        int trafficLightColor = Color.TRANSPARENT;

        if (event.mHarvestReportDone) {
            final String reportState = event.mHarvestReportState;

            if (GameHarvest.HARVEST_PROPOSED.equals(reportState)) {
                trafficLightColor = mContext.getResources().getColor(R.color.harvest_proposed);
                stateText.setText(R.string.harvest_proposed);
            } else if (GameHarvest.HARVEST_SENT_FOR_APPROVAL.equals(reportState)) {
                trafficLightColor = mContext.getResources().getColor(R.color.harvest_sent_for_approval);
                stateText.setText(R.string.harvest_sent_for_approval);
            } else if (GameHarvest.HARVEST_APPROVED.equals(reportState)) {
                trafficLightColor = mContext.getResources().getColor(R.color.harvest_approved);
                stateText.setText(R.string.harvest_approved);
            } else if (GameHarvest.HARVEST_REJECTED.equals(reportState)) {
                trafficLightColor = mContext.getResources().getColor(R.color.harvest_rejected);
                stateText.setText(R.string.harvest_rejected);
            }
        } else if (permitState != null && !permitState.isEmpty()) {
            switch (permitState) {
                case GameHarvest.PERMIT_PROPOSED:
                    trafficLightColor = mContext.getResources().getColor(R.color.permit_proposed);
                    stateText.setText(R.string.harvest_permit_proposed);
                    break;
                case GameHarvest.PERMIT_ACCEPTED:
                    trafficLightColor = mContext.getResources().getColor(R.color.permit_accepted);
                    stateText.setText(R.string.harvest_permit_accepted);
                    break;
                case GameHarvest.PERMIT_REJECTED:
                    trafficLightColor = mContext.getResources().getColor(R.color.permit_rejected);
                    stateText.setText(R.string.harvest_permit_rejected);
                    break;
            }
        } else if (event.mHarvestReportRequired) {
            trafficLightColor = mContext.getResources().getColor(R.color.harvest_create_report);
            stateText.setText(R.string.harvest_create_report);
        }

        if (trafficLightColor != Color.TRANSPARENT) {
            stateImage.setColorFilter(trafficLightColor, PorterDuff.Mode.SRC_ATOP);
            stateGroup.setVisibility(View.VISIBLE);
        } else {
            stateText.setText(null);
            stateGroup.setVisibility(View.GONE);
        }
    }

    private void setupSrvaState(final ImageView stateImage,
                                final TextView stateText,
                                final Group stateGroup,
                                final SrvaEvent event) {

        int trafficLightColor = Color.TRANSPARENT;

        if (SrvaEvent.STATE_APPROVED.equals(event.state)) {
            trafficLightColor = mContext.getResources().getColor(R.color.harvest_approved);
            stateText.setText(R.string.srva_approved);
        } else if (SrvaEvent.STATE_REJECTED.equals(event.state)) {
            trafficLightColor = mContext.getResources().getColor(R.color.harvest_rejected);
            stateText.setText(R.string.srva_rejected);
        }

        if (trafficLightColor != Color.TRANSPARENT) {
            stateImage.setColorFilter(trafficLightColor, PorterDuff.Mode.SRC_ATOP);
            stateGroup.setVisibility(View.VISIBLE);
        } else {
            stateText.setText(null);
            stateGroup.setVisibility(View.GONE);
        }
    }

    public static class LogHeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;
        final View timeline;

        LogHeaderViewHolder(@NonNull final View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.log_section_header);
            timeline = itemView.findViewById(R.id.log_section_timeline);
        }
    }

    public static class LogItemViewHolder extends RecyclerView.ViewHolder {
        final WebImageView imageView;
        final TextView dateTimeView;
        final TextView speciesView;
        final TextView descriptionView;
        final ImageView stateImageView;
        final TextView stateTextView;
        final Group stateGroup;
        final ImageView uploadImageView;

        final View timelineTop;
        final View timelineBottom;

        LogItemViewHolder(@NonNull final View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.log_item_species_image);
            dateTimeView = itemView.findViewById(R.id.log_item_date);
            speciesView = itemView.findViewById(R.id.log_item_species);
            descriptionView = itemView.findViewById(R.id.log_item_description);
            stateImageView = itemView.findViewById(R.id.log_item_state_image);
            stateTextView = itemView.findViewById(R.id.log_item_state_text);
            stateGroup = itemView.findViewById(R.id.log_item_state_group);
            uploadImageView = itemView.findViewById(R.id.log_item_upload_image);

            timelineTop = itemView.findViewById(R.id.log_item_timeline_top);
            timelineBottom = itemView.findViewById(R.id.log_item_timeline_bottom);
        }

        void bind(final GameLogListItem item, final GameLogListItem.OnClickListItemListener listener) {
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    public static class LogStatsViewHolder extends RecyclerView.ViewHolder {
        final TextView category1Amount;
        final TextView category2Amount;
        final TextView category3Amount;

        LogStatsViewHolder(@NonNull final View itemView) {
            super(itemView);

            category1Amount = itemView.findViewById(R.id.stats_category1_amount);
            category2Amount = itemView.findViewById(R.id.stats_category2_amount);
            category3Amount = itemView.findViewById(R.id.stats_category3_amount);
        }
    }
}
