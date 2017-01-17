package fi.riista.mobile.adapter;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import fi.riista.mobile.R;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.Specimen;
import fi.riista.mobile.ui.HarvestSpecimenView;

public class HarvestSpecimensAdapter extends BaseAdapter {

    private GameHarvest mHarvest;
    private boolean mEditMode;
    private ArrayList<Specimen> mSpecimens = new ArrayList<>();
    private String mSpeciesName;

    public HarvestSpecimensAdapter(GameHarvest harvest, boolean editMode) {
        mHarvest = harvest;
        mEditMode = editMode;
    }

    public ArrayList<Specimen> getSpecimens() {
        return new ArrayList<>(mSpecimens);
    }

    public void reset() {
        mSpecimens.clear();
        mSpecimens.addAll(populateSpecimenList(mEditMode));
        mSpeciesName = SpeciesInformation.getSpecies(mHarvest.mSpeciesID).mName;

        notifyDataSetChanged();
    }

    private Collection<Specimen> populateSpecimenList(boolean addEmpty) {
        List<Specimen> populatedList = new ArrayList<>();
        populatedList.addAll(mHarvest.mSpecimen);

        if (addEmpty) {
            while (populatedList.size() < mHarvest.mAmount && populatedList.size() < GameHarvest.SPECIMEN_DETAILS_MAX) {
                populatedList.add(new Specimen());
            }
        }

        return populatedList;
    }

    @Override
    public int getCount() {
        return mSpecimens.size();
    }

    @Override
    public Object getItem(int position) {
        return mSpecimens.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Specimen specimen = mSpecimens.get(position);
        HarvestSpecimenView view = new HarvestSpecimenView(parent.getContext());
        view.setupWithSpecimen(specimen, mHarvest.mSpeciesID);
        view.setEnabled(mEditMode);
        view.setPadding(0, 0, 0, 10);

        TextView textView = new TextView(parent.getContext());
        textView.setText(String.format(Locale.getDefault(), "%s %d", mSpeciesName, position + 1));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setTextColor(parent.getResources().getColor(R.color.text_dark));
        textView.setPadding(5, 0, 0, 0);
        view.setOrientation(LinearLayout.VERTICAL);
        view.addView(textView, 0);

        return view;
    }
}
