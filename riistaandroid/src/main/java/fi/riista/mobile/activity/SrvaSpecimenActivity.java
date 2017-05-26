package fi.riista.mobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import fi.riista.mobile.R;
import fi.riista.mobile.adapter.SrvaSpecimensAdapter;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.vincit.androidutilslib.util.ViewAnnotations;
import fi.vincit.androidutilslib.util.ViewAnnotations.ViewId;

public class SrvaSpecimenActivity extends BaseActivity {

    public static final String EXTRA_SRVA_EVENT = "extra_srva_event";
    public static final String EXTRA_EDIT_MODE = "extra_edit_mode";

    public static final String RESULT_SRVA_SPECIMEN = "result_srva_specimens";

    @ViewId(R.id.list_srva_specimen)
    private ListView mSpeciesListView;

    private SrvaEvent mEvent;
    private boolean mEditMode;
    private SrvaSpecimensAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_srva_specimen);

        ViewAnnotations.apply(this);

        mEvent = (SrvaEvent) getIntent().getSerializableExtra(EXTRA_SRVA_EVENT);
        mEditMode = getIntent().getBooleanExtra(EXTRA_EDIT_MODE, false);

        mAdapter = new SrvaSpecimensAdapter(mEvent, mEditMode);
        mSpeciesListView.setAdapter(mAdapter);
        mAdapter.reset();

        updateTitle();
    }

    private void updateTitle() {
        Species species = SpeciesInformation.getSpecies(mEvent.gameSpeciesCode);
        if (species != null) {
            String title = species.mName;
            if (mEvent.specimens != null) {
                title += " (" + mEvent.specimens.size() + ")";
            }
            setCustomTitle(title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra(RESULT_SRVA_SPECIMEN, mAdapter.getSpecimens());
        setResult(Activity.RESULT_OK, result);

        super.onBackPressed();
    }
}
