package fi.riista.mobile.activity;

import android.content.Intent;
import android.os.Bundle;

import fi.riista.mobile.R;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.pages.HarvestFragment;
import fi.vincit.androidutilslib.util.ViewAnnotations;

public class HarvestActivity extends BaseActivity {

    public static final String EXTRA_HARVEST = "extra_harvest";
    public static final String EXTRA_NEW = "extra_new";

    public static final String RESULT_DID_SAVE = "result_harvest_saved";

    public static final int NEW_HARVEST_REQUEST_CODE = 201;
    public static final int EDIT_HARVEST_REQUEST_CODE = 202;

    private boolean mEditMode = false;
    private boolean mNew = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_harvest);

        ViewAnnotations.apply(this);

        Intent intent = getIntent();

        mNew = intent.getBooleanExtra(EXTRA_NEW, false);
        mEditMode = mNew;

        GameHarvest harvest = (GameHarvest) intent.getSerializableExtra(EXTRA_HARVEST);

        if (harvest != null) {
            setCustomTitle(getString(R.string.loggame));

            HarvestFragment fragment = HarvestFragment.newInstance(harvest);
            getSupportFragmentManager().beginTransaction().add(R.id.harvest_content, fragment).commit();
        } else {
            finish();
        }
    }
}
