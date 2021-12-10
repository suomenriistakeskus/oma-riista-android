package fi.riista.mobile.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.models.srva.SrvaSpecimen;
import fi.riista.mobile.ui.SrvaSpecimenView;

public class SrvaSpecimensAdapter extends BaseAdapter {

    private SrvaEvent mEvent;
    private boolean mEditMode;
    private ArrayList<SrvaSpecimen> mSpecimens = new ArrayList<>();

    private SrvaSpecimensChangedCallback mListener;

    public SrvaSpecimensAdapter(SrvaEvent event, boolean editMode, SrvaSpecimensChangedCallback listener) {
        mEvent = event;
        mEditMode = editMode;
        mListener = listener;
    }

    public ArrayList<SrvaSpecimen> getSpecimens() {
        return new ArrayList<>(mSpecimens);
    }

    public void reset() {
        mSpecimens.clear();
        mSpecimens.addAll(mEvent.specimens);

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
        SrvaSpecimen specimen = mSpecimens.get(position);
        SrvaSpecimenView view = new SrvaSpecimenView(parent.getContext(), mEvent, specimen, position, mEditMode);

        view.setOnRemoveListener(position, v -> {
            Integer tag = (Integer) view.getTag();

            if (mListener != null) {
                mListener.onRemoveSpecimen(tag);
            }
        });

        return view;
    }

    public interface SrvaSpecimensChangedCallback {
        void onRemoveSpecimen(int index);
    }
}
