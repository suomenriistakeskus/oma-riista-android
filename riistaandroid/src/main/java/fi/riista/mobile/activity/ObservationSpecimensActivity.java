package fi.riista.mobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import fi.riista.mobile.R;
import fi.riista.mobile.adapter.ObservationSpecimensAdapter;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.GameLog;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.observation.GameObservation;
import fi.riista.mobile.models.observation.ObservationSpecimen;
import fi.riista.mobile.models.observation.metadata.ObservationSpecimenMetadata;
import fi.riista.mobile.models.user.UserInfo;
import fi.riista.mobile.observation.ObservationMetadataHelper;
import fi.riista.mobile.utils.UiUtils;
import fi.riista.mobile.utils.UserInfoStore;
import fi.vincit.androidutilslib.util.ViewAnnotations;
import fi.vincit.androidutilslib.util.ViewAnnotations.ViewId;

public class ObservationSpecimensActivity extends BaseActivity
        implements ObservationSpecimensAdapter.ObservationSpecimensChangedCallback {

    public static final String EXTRA_OBSERVATION = "extra_observation";
    public static final String EXTRA_EDIT_MODE = "extra_edit_mode";

    public static final String RESULT_SPECIMENS = "result_specimens";

    @Inject
    UserInfoStore mUserInfoStore;

    @Inject
    ObservationMetadataHelper mObservationMetadataHelper;

    private GameObservation mObservation;
    private boolean mEditMode;
    private ObservationSpecimensAdapter mAdapter;

    @ViewId(R.id.list_observation_species)
    private ListView mSpeciesListView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_observations_specimens);

        ViewAnnotations.apply(this);

        mObservation = (GameObservation) getIntent().getSerializableExtra(EXTRA_OBSERVATION);
        mEditMode = getIntent().getBooleanExtra(EXTRA_EDIT_MODE, false);

        final ObservationSpecimenMetadata metadata =
                mObservationMetadataHelper.getMetadataForSpecies(mObservation.gameSpeciesCode);

        mAdapter = new ObservationSpecimensAdapter(
                mObservation, mEditMode, isCarnivoreAuthority(), metadata, this);

        mSpeciesListView.setAdapter(mAdapter);
        mAdapter.reset();

        updateTitle();
    }

    private boolean isCarnivoreAuthority() {
        final UserInfo userInfo = mUserInfoStore.getUserInfo();
        return userInfo != null && userInfo.isCarnivoreAuthority();
    }

    private void updateTitle() {
        final Species species = SpeciesInformation.getSpecies(mObservation.gameSpeciesCode);

        if (species != null) {
            String title = species.mName;
            if (mObservation.specimens != null) {
                title += " (" + mObservation.specimens.size() + ")";
            }
            setCustomTitle(title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (mEditMode) {
            final MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_add, menu);
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
                if (mObservation.specimens.size() < GameLog.SPECIMEN_DETAILS_MAX) {
                    mObservation.specimens.add(new ObservationSpecimen());
                }
                mAdapter.reset();
                updateTitle();

                UiUtils.scrollToListviewBottom(mSpeciesListView);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        final Intent result = new Intent();
        result.putExtra(RESULT_SPECIMENS, mAdapter.getSpecimens());
        setResult(Activity.RESULT_OK, result);

        super.onBackPressed();
    }

    @Override
    public void onRemoveSpecimen(final int index) {
        if (mObservation.specimens.size() > 1) {
            mObservation.specimens.remove(index);

            mAdapter.reset();
            updateTitle();
        }
    }
}
