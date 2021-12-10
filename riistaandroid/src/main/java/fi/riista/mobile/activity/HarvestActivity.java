package fi.riista.mobile.activity;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import fi.riista.mobile.R;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.viewmodel.HarvestViewModel;

public class HarvestActivity extends BaseActivity {

    public static final String EXTRA_HARVEST = "extra_harvest";
    public static final String RESULT_DID_SAVE = "result_harvest_saved";

    public static final int NEW_HARVEST_REQUEST_CODE = 201;
    public static final int EDIT_HARVEST_REQUEST_CODE = 202;

    private static final String HARVEST_KEY = "harvest";
    private static final String EDIT_ENABLED_KEY = "editEnabled";

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private HarvestViewModel model;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_harvest);

        model = new ViewModelProvider(this, viewModelFactory).get(HarvestViewModel.class);

        GameHarvest harvest = null;
        boolean editEnabled = true;

        if (savedInstanceState != null) {
            harvest = (GameHarvest) savedInstanceState.getSerializable(HARVEST_KEY);
            editEnabled = savedInstanceState.getBoolean(EDIT_ENABLED_KEY);
        }
        if (harvest == null) {
            harvest = (GameHarvest) getIntent().getSerializableExtra(EXTRA_HARVEST);
            editEnabled = !harvest.isPersistedLocally();
        }

        if (harvest != null) {
            setCustomTitle(getString(R.string.loggame));
            model.initWith(harvest, editEnabled);

        } else {
            finish();
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        // Should never be null, but do sanity check anyway.
        final Boolean nullableEditFlag = model.getEditEnabled().getValue();
        final boolean editEnabled = nullableEditFlag != null && nullableEditFlag.booleanValue();
        outState.putBoolean(EDIT_ENABLED_KEY, editEnabled);

        if (editEnabled) {
            outState.putSerializable(HARVEST_KEY, model.getResultHarvest());
        }
    }
}
