package fi.riista.mobile.pages;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.activity.BaseActivity;
import fi.riista.mobile.activity.EditActivity;
import fi.riista.mobile.activity.HarvestActivity;
import fi.riista.mobile.activity.MainActivity;
import fi.riista.mobile.database.DiaryEntryUpdate;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.database.GameDatabase.DatabaseUpdateListener;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.GameObservation;
import fi.riista.mobile.models.Species;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.observation.ObservationDatabase;
import fi.riista.mobile.observation.ObservationDatabase.ObservationsListener;
import fi.riista.mobile.observation.ObservationStrings;
import fi.riista.mobile.srva.SrvaDatabase;
import fi.riista.mobile.srva.SrvaDatabase.SrvaEventListener;
import fi.riista.mobile.utils.UiUtils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.task.WorkAsyncTask;
import fi.vincit.androidutilslib.util.ViewAnnotations;
import fi.vincit.androidutilslib.util.ViewAnnotations.ViewId;
import fi.vincit.androidutilslib.util.ViewAnnotations.ViewOnClick;

public class HomeViewFragment extends PageFragment implements DatabaseUpdateListener {

    // Button defaults if there aren't any previous entries
    private final int QUICKBUTTON1_DEFAULT = 47503; // Hirvi
    private final int QUICKBUTTON2_DEFAULT = 50106; // Metsajanis

    private final int SRVA_QUICKBUTTON1_DEFAULT = 47503; // Hirvi
    private final int SRVA_QUICKBUTTON2_DEFAULT = 47629; // Valkohantapeura

    private boolean mManualSyncInProgress = false;
    private Menu mMenu = null;

    @ViewId(R.id.loggame)
    private Button mLogGameButton;

    @ViewId(R.id.layout_quick_srva)
    private LinearLayout mSrvaLayout;

    @ViewId(R.id.srvaquickbutton1)
    private Button mSrvaButton1;

    @ViewId(R.id.srvaquickbutton2)
    private Button mSrvaButton2;

    public static HomeViewFragment newInstance() {
        return new HomeViewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ViewAnnotations.apply(this, view);

        mSrvaLayout.setVisibility(UiUtils.getSrvaVisibility(getActivity()));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        inflater.inflate(R.menu.mygame, menu);

        // Show/hide refresh button according to sync settings
        MenuItem item = mMenu.findItem(R.id.refresh);
        if (GameDatabase.getInstance().getSyncMode(getActivity()) == GameDatabase.SyncMode.SYNC_MANUAL) {
            item.setVisible(true);
            if (mManualSyncInProgress) {
                item.setVisible(false);
            }
        } else {
            item.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                WorkContext context = ((MainActivity) getActivity()).getWorkContext();
                item.setVisible(false);
                mManualSyncInProgress = true;
                GameDatabase.getInstance().manualSync(context);
                return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        setViewTitle(getString(R.string.title_front_page));
        ((BaseActivity) getActivity()).onHasActionbarMenu((GameDatabase.getInstance().getSyncMode(getActivity()) == GameDatabase.SyncMode.SYNC_MANUAL));

        updateQuickButtons();

        GameDatabase.getInstance().registerListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        GameDatabase.getInstance().unregisterListener(this);
    }

    @ViewOnClick(R.id.loggame)
    protected void OnClickNewHarvestButton(View view) {
        startHarvestEditActivity(null);
    }

    @ViewOnClick(R.id.logobservation)
    protected void onClickNewObservationButton(View view) {
        startObservationEditActivity(null);
    }

    @ViewOnClick(R.id.logsrva)
    protected void onClickNewSrvaEventButton(View view) {
        startSrvaEditActivity(null, null, 0);
    }

    private void updateQuickButtons() {
        final View view = getView();
        if (view == null) {
            return;
        }

        updateHarvestQuickButtons(view);

        updateObservationQuickButtons(view);

        updateSrvaQuickButtons(view);
    }

    private void updateHarvestQuickButtons(final View view) {
        WorkAsyncTask task = new WorkAsyncTask(getWorkContext()) {
            private List<Integer> mLatestGame = new ArrayList<>();

            @Override
            protected void onAsyncRun() throws Exception {
                mLatestGame = GameDatabase.getInstance().getLatestSpecies(2);
            }

            @Override
            protected void onEnd() {
                if (HomeViewFragment.this.isAdded()) {
                    setupQuickButtons(view.findViewById(R.id.layout_quick_game), mLatestGame, R.string.loggame_template, new OnQuickButtonInitListener() {
                        @Override
                        public void onInit(Button button, final Species species) {
                            button.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startHarvestEditActivity(species);
                                }
                            });
                        }
                    });
                }
            }
        };
        task.start();
    }

    private void updateObservationQuickButtons(final View view) {
        ObservationDatabase.getInstance().loadLatestObservationSpecimens(2, new ObservationsListener() {
            @Override
            public void onObservations(List<GameObservation> observations) {
                if (isAdded()) {
                    List<Integer> latest = new ArrayList<>();
                    for (GameObservation observation : observations) {
                        latest.add(observation.gameSpeciesCode);
                    }

                    setupQuickButtons(view.findViewById(R.id.layout_quick_observation), latest, R.string.logobservation_template, new OnQuickButtonInitListener() {
                        @Override
                        public void onInit(Button button, final Species species) {
                            button.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startObservationEditActivity(species);
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void updateSrvaQuickButtons(final View view) {
        SrvaDatabase.getInstance().loadLatestEvents(new SrvaEventListener() {
            @Override
            public void onEvents(List<SrvaEvent> events) {
                setupSrvaDefaultButton(mSrvaButton1, "ACCIDENT", "TRAFFIC_ACCIDENT", SRVA_QUICKBUTTON1_DEFAULT);
                setupSrvaDefaultButton(mSrvaButton2, "ACCIDENT", "TRAFFIC_ACCIDENT", SRVA_QUICKBUTTON2_DEFAULT);

                if (events.size() > 0) {
                    SrvaEvent first = events.remove(0);
                    setupSrvaQuickButton(mSrvaButton1, first);

                    for (SrvaEvent event : events) {
                        if (!event.gameSpeciesCode.equals(first.gameSpeciesCode)) {
                            setupSrvaQuickButton(mSrvaButton2, event);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void setupSrvaDefaultButton(Button button, String eventName, String eventType, int gameSpeciesCode) {
        SrvaEvent dummy = SrvaEvent.createNew();
        dummy.eventName = eventName;
        dummy.eventType = eventType;
        dummy.gameSpeciesCode = gameSpeciesCode;
        setupSrvaQuickButton(button, dummy);
    }

    private void setupSrvaQuickButton(Button button, final SrvaEvent event) {
        Species species = SpeciesInformation.getSpecies(event.gameSpeciesCode);
        if (isAdded() && species != null) {
            String type = ObservationStrings.get(getActivity(), event.eventType);
            button.setText(species.mName + " / " + type);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startSrvaEditActivity(event.eventName, event.eventType, event.gameSpeciesCode);
                }
            });
        }
    }

    private void startHarvestEditActivity(Species species) {
        GameHarvest harvest = GameHarvest.createNew();
        if (species != null) {
            harvest.mSpeciesID = species.mId;
        }

        Intent intent = new Intent(getActivity(), HarvestActivity.class);
        intent.putExtra(HarvestActivity.EXTRA_HARVEST, harvest);
        intent.putExtra(HarvestActivity.EXTRA_NEW, true);
        startActivityForResult(intent, HarvestActivity.NEW_HARVEST_REQUEST_CODE);
    }

    private void startObservationEditActivity(Species species) {
        GameObservation observation = GameObservation.createNew();
        if (species != null) {
            observation.gameSpeciesCode = species.mId;
        }

        Intent intent = new Intent(getActivity(), EditActivity.class);
        intent.putExtra(EditActivity.EXTRA_OBSERVATION, observation);
        intent.putExtra(EditActivity.EXTRA_NEW, true);
        startActivityForResult(intent, EditActivity.NEW_OBSERVATION_REQUEST_CODE);
    }

    private void startSrvaEditActivity(String eventName, String eventType, int gameSpeciesCode) {
        SrvaEvent event = SrvaEvent.createNew();
        event.eventName = eventName;
        event.eventType = eventType;
        event.gameSpeciesCode = gameSpeciesCode;

        Intent intent = new Intent(getActivity(), EditActivity.class);
        intent.putExtra(EditActivity.EXTRA_SRVA_EVENT, event);
        intent.putExtra(EditActivity.EXTRA_NEW, true);
        startActivityForResult(intent, EditActivity.NEW_SRVA_REQUEST_CODE);
    }

    void setupQuickButtons(View container, List<Integer> latestSpecies, int textId, OnQuickButtonInitListener listener) {
        List<Integer> defaultSpecies = new ArrayList<>();
        defaultSpecies.add(QUICKBUTTON1_DEFAULT);
        defaultSpecies.add(QUICKBUTTON2_DEFAULT);
        Button quickItem1 = (Button) container.findViewById(R.id.quickbutton1);
        setupQuickButton(quickItem1, latestSpecies, 0, defaultSpecies, textId, listener);
        Button quickItem2 = (Button) container.findViewById(R.id.quickbutton2);
        setupQuickButton(quickItem2, latestSpecies, 1, defaultSpecies, textId, listener);
    }

    void setupQuickButton(Button quickButton, List<Integer> latestSpecies,
                          int ordinal, List<Integer> defaultSpecies, int textId, OnQuickButtonInitListener listener) {
        String textFormat = "", text = "";
        Species species = null;
        textFormat = getResources().getString(textId);
        if (latestSpecies.size() > ordinal) {
            species = SpeciesInformation.getSpecies(latestSpecies.get(ordinal));
            if (defaultSpecies.contains(latestSpecies.get(ordinal))) {
                defaultSpecies.remove(latestSpecies.get(ordinal));
            }
        } else if (defaultSpecies.size() > 0) {
            int speciesId = defaultSpecies.get(0);
            species = SpeciesInformation.getSpecies(speciesId);
            defaultSpecies.remove(0);
        }
        if (species != null) {
            text = String.format(textFormat, species.mName);
            quickButton.setText(text);
            listener.onInit(quickButton, species);
        } else {
            quickButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void eventsUpdated(List<DiaryEntryUpdate> updatedEvents) {
        if (getActivity() != null && getView() != null) {
            if (mManualSyncInProgress) {
                MenuItem refreshItem = mMenu.findItem(R.id.refresh);
                if (refreshItem != null) {
                    refreshItem.setVisible(true);
                }
                mManualSyncInProgress = false;
            }
            updateQuickButtons();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == HarvestActivity.NEW_HARVEST_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                boolean didSave = data.getBooleanExtra(HarvestActivity.RESULT_DID_SAVE, false);

                if (didSave) {
                    // Move to game log and select harvest tab
                }

                GameDatabase db = GameDatabase.getInstance();
                if (db.getSyncMode(getContext()) == GameDatabase.SyncMode.SYNC_AUTOMATIC) {
                    db.doSyncAndResetTimer();
                }
            }
        } else if (requestCode == EditActivity.NEW_OBSERVATION_REQUEST_CODE ||
                requestCode == EditActivity.NEW_SRVA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                boolean didSave = data.getBooleanExtra(EditActivity.RESULT_DID_SAVE, false);

                if (didSave) {
                    // Move to game log and select observation tab
                }

                GameDatabase db = GameDatabase.getInstance();
                if (db.getSyncMode(getContext()) == GameDatabase.SyncMode.SYNC_AUTOMATIC) {
                    db.doSyncAndResetTimer();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private interface OnQuickButtonInitListener {
        void onInit(Button button, Species species);
    }
}
