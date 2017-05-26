package fi.riista.mobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import fi.riista.mobile.R;
import fi.riista.mobile.adapter.HarvestSpecimensAdapter;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.Species;
import fi.vincit.androidutilslib.util.ViewAnnotations;

public class HarvestSpecimensActivity extends BaseActivity {

    public static final String EXTRA_HARVEST = "extra_harvest";
    public static final String EXTRA_EDIT_MODE = "extra_edit_mode";

    public static final String RESULT_SPECIMENS = "result_specimens";

    private GameHarvest mHarvest;
    private boolean mEditMode;
    private HarvestSpecimensAdapter mAdapter;

    @ViewAnnotations.ViewId(R.id.list_harvest_specimens)
    private ListView mSpeciesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_harvest_specimens);

        ViewAnnotations.apply(this);

        mHarvest = (GameHarvest) getIntent().getSerializableExtra(EXTRA_HARVEST);
        mEditMode = getIntent().getBooleanExtra(EXTRA_EDIT_MODE, false);

        mAdapter = new HarvestSpecimensAdapter(mHarvest, mEditMode);

        mSpeciesListView.setAdapter(mAdapter);
        mAdapter.reset();

        updateTitle();
    }

    private void updateTitle() {
        Species species = SpeciesInformation.getSpecies(mHarvest.mSpeciesID);
        if (species != null) {
            String title = species.mName;
            if (mHarvest.mSpecimen != null) {
                title += " (" + mHarvest.mAmount + ")";
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
        result.putExtra(RESULT_SPECIMENS, mAdapter.getSpecimens());
        setResult(Activity.RESULT_OK, result);

        super.onBackPressed();
    }
}
