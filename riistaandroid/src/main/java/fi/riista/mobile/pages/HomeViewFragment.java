package fi.riista.mobile.pages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import fi.riista.mobile.R;
import fi.riista.mobile.activity.EditActivity;
import fi.riista.mobile.activity.HarvestActivity;
import fi.riista.mobile.activity.MainActivity;
import fi.riista.mobile.database.HarvestDatabase;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.database.SpeciesResolver;
import fi.riista.mobile.gamelog.HarvestSpecVersionResolver;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.observation.GameObservation;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.observation.ObservationDatabase;
import fi.riista.mobile.sync.AppSync;
import fi.riista.mobile.sync.AppSync.AppSyncListener;
import fi.riista.mobile.sync.SyncConfig;
import fi.riista.mobile.ui.HomeButtonView;
import fi.riista.mobile.utils.UiUtils;
import fi.riista.mobile.utils.UserInfoStore;
import fi.vincit.androidutilslib.task.WorkAsyncTask;

import static java.util.Collections.emptyList;

public class HomeViewFragment extends PageFragment implements AppSyncListener {

    // Button defaults if there aren't any previous entries
    private final int QUICK_BUTTON1_DEFAULT = SpeciesInformation.MOOSE_ID;
    private final int QUICK_BUTTON2_DEFAULT = SpeciesInformation.MOUNTAIN_HARE_ID; // Metsäjänis

    @Inject
    UserInfoStore mUserInfoStore;

    @Inject
    HarvestDatabase mHarvestDatabase;

    @Inject
    ObservationDatabase mObservationDatabase;

    @Inject
    SpeciesResolver mSpeciesResolver;

    @Inject
    HarvestSpecVersionResolver mSpecVersionResolver;

    @Inject
    AppSync mAppSync;

    @Inject
    SyncConfig mSyncConfig;

    private MenuItem mRefreshItem;

    private TextView mHarvestQuickButton1;
    private TextView mHarvestQuickButton2;
    private TextView mObservationQuickButton1;
    private TextView mObservationQuickButton2;

    private final ActivityResultLauncher<Intent> newEventActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    mAppSync.syncImmediatelyIfAutomaticSyncEnabled();
                }
            });

    public static HomeViewFragment newInstance() {
        return new HomeViewFragment();
    }

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    @Override
    public void onAttach(@NonNull final Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        setupActionBar(R.layout.actionbar_home, true);

        final HomeButtonView harvestView = view.findViewById(R.id.home_harvest_view);
        harvestView.findViewById(R.id.home_view_main_item).setOnClickListener(v -> startHarvestEditActivity(null));
        mHarvestQuickButton1 = harvestView.findViewById(R.id.home_view_subitem_1_button);
        mHarvestQuickButton2 = harvestView.findViewById(R.id.home_view_subitem_2_button);

        final HomeButtonView observationView = view.findViewById(R.id.home_observation_view);
        observationView.findViewById(R.id.home_view_main_item).setOnClickListener(v -> startObservationEditActivity(null));
        mObservationQuickButton1 = observationView.findViewById(R.id.home_view_subitem_1_button);
        mObservationQuickButton2 = observationView.findViewById(R.id.home_view_subitem_2_button);

        final HomeButtonView srvaView = view.findViewById(R.id.home_srva_view);
        srvaView.findViewById(R.id.home_view_main_item).setOnClickListener(v -> startSrvaEditActivity());
        srvaView.setVisibility(UiUtils.isSrvaVisible(mUserInfoStore.getUserInfo()) ? View.VISIBLE : View.GONE);

        final HomeButtonView mapView = view.findViewById(R.id.home_map_view);
        mapView.findViewById(R.id.home_view_main_item).setOnClickListener(v -> onMapClick());

        final HomeButtonView myDetailsView = view.findViewById(R.id.home_my_details_view);
        myDetailsView.findViewById(R.id.home_view_main_item).setOnClickListener(v -> onMyDetailsClick());
        myDetailsView.findViewById(R.id.home_view_subitem_1_button).setOnClickListener(v -> onHuntingLicenseClick());
        myDetailsView.findViewById(R.id.home_view_subitem_2_button).setOnClickListener(v -> onShootingTestsClick());

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_refresh, menu);

        // Show/hide refresh button according to sync settings.
        mRefreshItem = menu.findItem(R.id.item_refresh);
        mRefreshItem.setVisible(!mSyncConfig.isAutomatic() && !mAppSync.isSyncRunning());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == R.id.item_refresh) {
            if (mAppSync.syncImmediately()) {
                item.setVisible(false);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        setViewTitle(getString(R.string.title_front_page));

        updateQuickButtons();

        mAppSync.addSyncListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAppSync.removeSyncListener(this);
    }

    private void updateQuickButtons() {
        final View view = getView();

        if (view != null) {
            updateHarvestQuickButtons();
            updateObservationQuickButtons();
        }
    }

    private void updateHarvestQuickButtons() {
        setupHarvestQuickButton(mHarvestQuickButton1, mSpeciesResolver.findSpecies(QUICK_BUTTON1_DEFAULT));
        setupHarvestQuickButton(mHarvestQuickButton2, mSpeciesResolver.findSpecies(QUICK_BUTTON2_DEFAULT));

        final WorkAsyncTask task = new WorkAsyncTask(getWorkContext()) {
            private List<Integer> mLatestGame = emptyList();

            @Override
            protected void onAsyncRun() {
                mLatestGame = mHarvestDatabase.getSpeciesIdsOfMostRecentHarvests(2);
            }

            @Override
            protected void onEnd() {
                if (HomeViewFragment.this.isAdded()) {
                    if (mLatestGame.size() >= 2) {
                        setupHarvestQuickButton(mHarvestQuickButton1, mSpeciesResolver.findSpecies(mLatestGame.get(0)));
                        setupHarvestQuickButton(mHarvestQuickButton2, mSpeciesResolver.findSpecies(mLatestGame.get(1)));
                    }
                }
            }
        };
        task.start();
    }

    private void setupHarvestQuickButton(final TextView buttonView, @NonNull final Species species) {
        if (isAdded()) {
            buttonView.setText(species.mName);
            buttonView.setOnClickListener(v -> startHarvestEditActivity(species));
        }
    }

    private void updateObservationQuickButtons() {
        mObservationDatabase.loadLatestObservationSpecimens(2, observations -> {
            setupObservationQuickButton(mObservationQuickButton1, mSpeciesResolver.findSpecies(QUICK_BUTTON1_DEFAULT));
            setupObservationQuickButton(mObservationQuickButton2, mSpeciesResolver.findSpecies(QUICK_BUTTON2_DEFAULT));

            if (isAdded()) {
                final List<Integer> latest = new ArrayList<>();

                for (final GameObservation observation : observations) {
                    latest.add(observation.gameSpeciesCode);
                }

                if (latest.size() >= 2) {
                    // TODO: Check case size == 1 and which button is updated
                    setupObservationQuickButton(mObservationQuickButton1, mSpeciesResolver.findSpecies(latest.get(0)));
                    setupObservationQuickButton(mObservationQuickButton2, mSpeciesResolver.findSpecies(latest.get(1)));
                }
            }
        });
    }

    private void setupObservationQuickButton(final TextView buttonView, @NonNull final Species species) {
        if (isAdded()) {
            buttonView.setText(species.mName);
            buttonView.setOnClickListener(v -> startObservationEditActivity(species));
        }
    }

    private void startHarvestEditActivity(final Species species) {
        final GameHarvest harvest = GameHarvest.createNew(mSpecVersionResolver.resolveHarvestSpecVersion());

        if (species != null) {
            harvest.mSpeciesID = species.mId;
        }

        final Intent intent = new Intent(getActivity(), HarvestActivity.class);
        intent.putExtra(HarvestActivity.EXTRA_HARVEST, harvest);
        newEventActivityResultLaunch.launch(intent);
    }

    private void startObservationEditActivity(final Species species) {
        final GameObservation observation = GameObservation.createNew();

        if (species != null) {
            observation.gameSpeciesCode = species.mId;
        }

        final Intent intent = new Intent(getActivity(), EditActivity.class);
        intent.putExtra(EditActivity.EXTRA_OBSERVATION, observation);
        intent.putExtra(EditActivity.EXTRA_NEW, true);
        newEventActivityResultLaunch.launch(intent);
    }

    private void startSrvaEditActivity() {
        final Intent intent = new Intent(getActivity(), EditActivity.class);
        intent.putExtra(EditActivity.EXTRA_SRVA_EVENT, SrvaEvent.createNew());
        intent.putExtra(EditActivity.EXTRA_NEW, true);
        newEventActivityResultLaunch.launch(intent);
    }

    private void onMyDetailsClick() {
        final MainActivity activity = (MainActivity) getActivity();

        if (activity != null) {
            activity.selectItem(R.id.menu_more);
            activity.replacePageFragment(MyDetailsFragment.newInstance());
        }
    }

    private void onHuntingLicenseClick() {
        final MyDetailsLicenseFragment dialog = MyDetailsLicenseFragment.newInstance();
        dialog.show(requireActivity().getSupportFragmentManager(), MyDetailsLicenseFragment.TAG);
    }

    private void onShootingTestsClick() {
        final MyDetailsShootingTestsFragment dialog = MyDetailsShootingTestsFragment.newInstance();
        dialog.show(requireActivity().getSupportFragmentManager(), MyDetailsShootingTestsFragment.TAG);
    }

    private void onMapClick() {
        final MainActivity activity = (MainActivity) getActivity();

        if (activity != null) {
            activity.selectItem(R.id.menu_map);
            activity.replacePageFragment(MapViewer.newInstance(true));
        }
    }

    @Override
    public void onSyncStarted() {
        if (mRefreshItem != null) {
            mRefreshItem.setVisible(false);
        }
    }

    @Override
    public void onSyncCompleted() {
        if (getActivity() != null && getView() != null) {
            if (mRefreshItem != null) {
                mRefreshItem.setVisible(!mSyncConfig.isAutomatic());
            }
        }

        updateQuickButtons();
    }
}
