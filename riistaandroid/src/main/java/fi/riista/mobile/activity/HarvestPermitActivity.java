package fi.riista.mobile.activity;

import android.content.Intent;
import android.os.Bundle;

import fi.riista.mobile.R;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.pages.PermitList;

public class HarvestPermitActivity extends BaseActivity {

    public static final String EXTRA_PERMIT_NUMBER = "extra_permit_number";

    public static final String RESULT_PERMIT_NUMBER = "result_permit";
    public static final String RESULT_PERMIT_TYPE = "result_type";
    public static final String RESULT_PERMIT_SPECIES = "result_species";

    public static final int PERMIT_REQUEST_CODE = 11;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment);

        final Intent intent = getIntent();
        final String permitNumber = intent.getStringExtra(EXTRA_PERMIT_NUMBER);

        final PermitList fragment = PermitList.newInstance(permitNumber);
        getSupportFragmentManager().beginTransaction().add(R.id.layout_fragment_container, fragment).commit();
    }
}
