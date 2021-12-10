package fi.riista.mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fi.riista.mobile.R;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.GameLog;
import fi.riista.mobile.models.HarvestSpecimen;
import fi.riista.mobile.ui.HarvestSpecimenView;

public class HarvestSpecimensAdapter extends BaseAdapter {

    private final ArrayList<HarvestSpecimen> mSpecimens;

    private final int mSpeciesCode;
    private final String mSpeciesName;

    private final boolean mEditEnabled;

    private HarvestSpecimenAmountChangedListener mListener;

    public HarvestSpecimensAdapter(final List<HarvestSpecimen> harvestSpecimens,
                                   final int speciesCode,
                                   final boolean editEnabled) {

        this.mSpecimens = new ArrayList<>(harvestSpecimens);

        this.mSpeciesCode = speciesCode;
        this.mSpeciesName = SpeciesInformation.getSpeciesName(speciesCode);

        this.mEditEnabled = editEnabled;
    }

    public void setListener(final HarvestSpecimenAmountChangedListener listener) {
        this.mListener = listener;
    }

    public ArrayList<HarvestSpecimen> getSpecimens() {
        return mSpecimens;
    }

    public void addNewSpecimen() {
        if (mSpecimens.size() < GameLog.SPECIMEN_DETAILS_MAX) {
            mSpecimens.add(new HarvestSpecimen());
        }

        notifyDataSetChanged();
        notifyListener();
    }

    @Override
    public int getCount() {
        return mSpecimens.size();
    }

    @Override
    public Object getItem(final int position) {
        return mSpecimens.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return 0;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View cView = convertView;
        ViewHolder holder;

        if (cView == null) {
            cView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_specimen_list_item, parent, false);

            holder = new ViewHolder(cView);
            cView.setTag(holder);
        } else {
            holder = (ViewHolder) cView.getTag();
        }

        final HarvestSpecimen item = mSpecimens.get(position);

        holder.title.setText(String.format(Locale.getDefault(), "%s %d", mSpeciesName, position + 1));

        holder.specimenView.setupWithSpecimen(item, mSpeciesCode, isWeightFieldVisible());
        holder.specimenView.setEnabled(mEditEnabled);

        holder.removeButton.setTag(position);
        final boolean removeButtonVisible = mEditEnabled && mSpecimens.size() > 1;
        holder.removeButton.setVisibility(removeButtonVisible ? View.VISIBLE : View.GONE);
        holder.removeButton.setEnabled(removeButtonVisible);

        cView.setEnabled(mEditEnabled);

        return cView;
    }

    private boolean isWeightFieldVisible() {
        return !SpeciesInformation.isPermitBasedMooselike(mSpeciesCode);
    }

    private void onDelete(final View view) {
        final Integer tag = (Integer) view.getTag();
        mSpecimens.remove(tag.intValue());

        notifyDataSetChanged();
        notifyListener();
    }

    private void notifyListener() {
        if (mListener != null) {
            mListener.onHarvestSpecimenAmountChanged(mSpecimens.size());
        }
    }

    private class ViewHolder {
        TextView title;
        HarvestSpecimenView specimenView;
        ImageButton removeButton;

        ViewHolder(final View view) {
            title = view.findViewById(R.id.specimen_list_item_title);
            specimenView = view.findViewById(R.id.specimen_list_item);
            removeButton = view.findViewById(R.id.specimen_list_item_remove);

            removeButton.setOnClickListener(HarvestSpecimensAdapter.this::onDelete);
        }
    }

    public interface HarvestSpecimenAmountChangedListener {
        void onHarvestSpecimenAmountChanged(int newAmount);
    }
}
