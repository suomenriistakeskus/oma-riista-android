package fi.riista.mobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.adapter.HarvestSpecimensAdapter;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.GameLog;
import fi.riista.mobile.models.HarvestSpecimen;
import fi.riista.mobile.utils.UiUtils;

public class HarvestSpecimensActivity extends BaseActivity
        implements HarvestSpecimensAdapter.HarvestSpecimenAmountChangedListener {

    public static final String EXTRA_SPECIES_ID = "extra_species_id";
    public static final String EXTRA_AMOUNT = "extra_amount";
    public static final String EXTRA_SPECIMENS = "extra_specimens";
    public static final String EXTRA_EDIT_MODE = "extra_edit_mode";

    public static final String RESULT_SPECIMENS = "result_specimens";

    private static final String SPECIES_CODE_KEY = "speciesCode";
    private static final String SPECIMEN_LIST_KEY = "specimens";
    private static final String EDIT_ENABLED_KEY = "editEnabled";

    private int mSpeciesCode;
    private String mSpeciesName;
    private boolean mEditEnabled;

    private ListView mSpeciesListView;
    private HarvestSpecimensAdapter mAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_harvest_specimens);

        final List<HarvestSpecimen> harvestSpecimens;
        final int harvestAmount;

        if (savedInstanceState != null) {
            harvestSpecimens = (List<HarvestSpecimen>) savedInstanceState.getSerializable(SPECIMEN_LIST_KEY);
            harvestAmount = harvestSpecimens.size();

            mSpeciesCode = savedInstanceState.getInt(SPECIES_CODE_KEY);
            mEditEnabled = savedInstanceState.getBoolean(EDIT_ENABLED_KEY);

        } else {
            final Intent intent = getIntent();

            mSpeciesCode = intent.getIntExtra(EXTRA_SPECIES_ID, -1);
            mEditEnabled = intent.getBooleanExtra(EXTRA_EDIT_MODE, false);

            harvestAmount = intent.getIntExtra(EXTRA_AMOUNT, -1);

            final List<HarvestSpecimen> specimensExtra = (List<HarvestSpecimen>) intent.getSerializableExtra(EXTRA_SPECIMENS);

            // Check presence and validity of parameters.
            if (mSpeciesCode < 0 || harvestAmount < 0 || specimensExtra == null) {
                finish();
            }

            harvestSpecimens = prepareSpecimenList(specimensExtra, harvestAmount, mEditEnabled);
        }

        mSpeciesName = SpeciesInformation.getSpeciesName(mSpeciesCode);

        mAdapter = new HarvestSpecimensAdapter(harvestSpecimens, mSpeciesCode, mEditEnabled);
        mAdapter.setListener(this);

        mSpeciesListView = findViewById(R.id.list_harvest_specimens);
        mSpeciesListView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        mSpeciesListView.setAdapter(mAdapter);

        updateTitle(harvestAmount);
    }

    private static List<HarvestSpecimen> prepareSpecimenList(final List<HarvestSpecimen> specimens,
                                                             final int amount,
                                                             final boolean addEmptySpecimens) {

        final ArrayList<HarvestSpecimen> preparedList = new ArrayList<>(specimens);

        if (addEmptySpecimens) {
            final int limit = Math.min(amount, GameLog.SPECIMEN_DETAILS_MAX);

            while (preparedList.size() < limit) {
                preparedList.add(new HarvestSpecimen());
            }
        }

        return preparedList;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Hide keyboard from opening automatically
        mSpeciesListView.requestFocus();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putInt(SPECIES_CODE_KEY, mSpeciesCode);
        outState.putSerializable(SPECIMEN_LIST_KEY, mAdapter.getSpecimens());
        outState.putBoolean(EDIT_ENABLED_KEY, mEditEnabled);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        mAdapter.setListener(null);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (mEditEnabled) {
            getMenuInflater().inflate(R.menu.menu_add, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.item_add:
                mAdapter.addNewSpecimen();
                UiUtils.scrollToListviewBottom(mSpeciesListView);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        final Intent result = new Intent();
        result.putExtra(RESULT_SPECIMENS, mAdapter.getSpecimens());
        setResult(mEditEnabled ? Activity.RESULT_OK : Activity.RESULT_CANCELED, result);

        super.onBackPressed();
    }

    @Override
    public void onHarvestSpecimenAmountChanged(final int updatedAmountOfSpecimens) {
        updateTitle(updatedAmountOfSpecimens);
    }

    private void updateTitle(final Integer amountOfSpecimens) {
        if (mSpeciesName != null) {
            final String title = amountOfSpecimens == null
                    ? mSpeciesName
                    : String.format("%s (%d)", mSpeciesName, amountOfSpecimens);
            setCustomTitle(title);
        }
    }
}
