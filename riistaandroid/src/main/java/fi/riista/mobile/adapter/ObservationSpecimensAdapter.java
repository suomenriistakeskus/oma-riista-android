package fi.riista.mobile.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import fi.riista.mobile.models.GameObservation;
import fi.riista.mobile.models.ObservationSpecimen;
import fi.riista.mobile.ui.ObservationSpecimenView;

public class ObservationSpecimensAdapter extends BaseAdapter {

    private GameObservation mObservation;
    private boolean mEditMode;
    private ArrayList<ObservationSpecimen> mSpecimens = new ArrayList<>();

    public ObservationSpecimensAdapter(GameObservation observation, boolean editMode) {
        mObservation = observation;
        mEditMode = editMode;
    }

    public ArrayList<ObservationSpecimen> getSpecimens() {
        return new ArrayList<>(mSpecimens);
    }

    public void reset() {
        mSpecimens.clear();
        mSpecimens.addAll(mObservation.specimens);

        notifyDataSetChanged();
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
        ObservationSpecimen specimen = mSpecimens.get(position);
        ObservationSpecimenView view = new ObservationSpecimenView(parent.getContext(),
                mObservation, specimen, position, mEditMode);
        return view;
    }
}
