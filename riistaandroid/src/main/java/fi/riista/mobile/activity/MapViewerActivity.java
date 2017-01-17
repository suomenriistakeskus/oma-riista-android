package fi.riista.mobile.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import fi.riista.mobile.R;
import fi.riista.mobile.pages.MapViewer;

public class MapViewerActivity extends BaseActivity {

    public static final String EXTRA_EDIT_MODE = "edit_mode";
    public static final String EXTRA_START_LOCATION = "start_location";
    public static final String EXTRA_NEW = "new";
    public static final String EXTRA_LOCATION_SOURCE = "location_source";

    public static final String RESULT_LOCATION = "result_location";
    public static final String RESULT_LOCATION_SOURCE = "result_location_source";

    public static final int LOCATION_REQUEST_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment);

        MapViewer viewer = new MapViewer();
        viewer.setArguments(getIntent().getExtras());

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.layout_fragment_container, viewer);
        transaction.commit();
    }
}
