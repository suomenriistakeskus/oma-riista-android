package fi.riista.mobile.activity;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.FragmentTransaction;
import fi.riista.mobile.R;
import fi.riista.mobile.pages.MapViewer;

public class MapViewerActivity extends BaseActivity implements MapViewer.FullScreenExpand {

    public static final String EXTRA_EDIT_MODE = "edit_mode";
    public static final String EXTRA_START_LOCATION = "start_location";
    public static final String EXTRA_NEW = "new";
    public static final String EXTRA_SHOW_ITEMS = "show_items";
    public static final String EXTRA_LOCATION_SOURCE = "location_source";
    static public final String EXTRA_EXTERNAL_ID = "map_external_id";

    public static final String RESULT_LOCATION = "result_location";
    public static final String RESULT_LOCATION_SOURCE = "result_location_source";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment);

        final MapViewer viewer = new MapViewer();
        viewer.setArguments(getIntent().getExtras());

        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.layout_fragment_container, viewer);
        transaction.commit();
    }

    @Override
    public void setFullscreenMode(final boolean fullscreen) {
        final WindowManager.LayoutParams attrs = getWindow().getAttributes();
        final ActionBar actionBar = getSupportActionBar();

        if (fullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;

            actionBar.hide();
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;

            actionBar.show();
        }

        getWindow().setAttributes(attrs);
    }
}
