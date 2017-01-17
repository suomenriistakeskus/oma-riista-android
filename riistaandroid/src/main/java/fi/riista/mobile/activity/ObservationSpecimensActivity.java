package fi.riista.mobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import fi.riista.mobile.R;
import fi.riista.mobile.adapter.ObservationSpecimensAdapter;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.GameObservation;
import fi.riista.mobile.models.Species;
import fi.vincit.androidutilslib.util.ViewAnnotations;
import fi.vincit.androidutilslib.util.ViewAnnotations.ViewId;

public class ObservationSpecimensActivity extends BaseActivity {

    public static final String EXTRA_OBSERVATION = "extra_observation";
    public static final String EXTRA_EDIT_MODE = "extra_edit_mode";

    public static final String RESULT_SPECIMENS = "result_specimens";

    private GameObservation mObservation;
    private boolean mEditMode;
    private ObservationSpecimensAdapter mAdapter;

    @ViewId(R.id.list_observation_species)
    private ListView mSpeciesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_observations_specimens);

        ViewAnnotations.apply(this);

        mObservation = (GameObservation) getIntent().getSerializableExtra(EXTRA_OBSERVATION);
        mEditMode = getIntent().getBooleanExtra(EXTRA_EDIT_MODE, false);

        mAdapter = new ObservationSpecimensAdapter(mObservation, mEditMode);

        mSpeciesListView.setAdapter(mAdapter);
        mAdapter.reset();

        updateTitle();
    }

    private void updateTitle() {
        Species species = SpeciesInformation.getSpecies(mObservation.gameSpeciesCode);
        if (species != null) {
            String title = species.mName;
            if (mObservation.specimens != null) {
                title += " (" + mObservation.specimens.size() + ")";
            }
            setCustomTitle(title);
        }
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra(RESULT_SPECIMENS, mAdapter.getSpecimens());
        setResult(Activity.RESULT_OK, result);

        super.onBackPressed();
    }
}
