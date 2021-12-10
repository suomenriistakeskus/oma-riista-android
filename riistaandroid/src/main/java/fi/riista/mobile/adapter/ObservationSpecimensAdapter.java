package fi.riista.mobile.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import fi.riista.mobile.models.observation.GameObservation;
import fi.riista.mobile.models.observation.ObservationSpecimen;
import fi.riista.mobile.models.observation.metadata.ObservationSpecimenMetadata;
import fi.riista.mobile.ui.ObservationSpecimenView;

import static java.util.Objects.requireNonNull;

public class ObservationSpecimensAdapter extends BaseAdapter {

    public interface ObservationSpecimensChangedCallback {
        void onRemoveSpecimen(int index);
    }

    private final GameObservation mObservation;

    private final boolean mEditMode;
    private final boolean mIsCarnivoreAuthority;
    private final ObservationSpecimenMetadata mObservationSpecimenMetadata;

    private final ArrayList<ObservationSpecimen> mSpecimens = new ArrayList<>();

    private final ObservationSpecimensChangedCallback mListener;

    public ObservationSpecimensAdapter(@NonNull final GameObservation observation,
                                       final boolean editMode,
                                       final boolean isCarnivoreAuthority,
                                       @Nullable final ObservationSpecimenMetadata observationSpecimenMetadata,
                                       @Nullable final ObservationSpecimensChangedCallback listener) {

        mObservation = requireNonNull(observation);

        mEditMode = editMode;
        mIsCarnivoreAuthority = isCarnivoreAuthority;
        mObservationSpecimenMetadata = observationSpecimenMetadata;

        mListener = listener;
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
    public Object getItem(final int position) {
        return mSpecimens.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return 0;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final ObservationSpecimen specimen = mSpecimens.get(position);
        final ObservationSpecimenView view = new ObservationSpecimenView(
                parent.getContext(), mObservation, specimen, mObservationSpecimenMetadata, mIsCarnivoreAuthority,
                position, mEditMode);

        view.setOnRemoveListener(position, v -> {
            final Integer tag = (Integer) view.getTag();

            if (mListener != null) {
                mListener.onRemoveSpecimen(tag);
            }
        });

        return view;
    }
}
