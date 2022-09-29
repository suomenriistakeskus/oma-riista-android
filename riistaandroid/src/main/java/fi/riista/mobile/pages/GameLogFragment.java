package fi.riista.mobile.pages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import fi.riista.common.domain.observation.model.CommonObservation;
import fi.riista.common.domain.srva.model.CommonSrvaEvent;
import fi.riista.mobile.AppConfig;
import fi.riista.mobile.R;
import fi.riista.mobile.activity.HarvestActivity;
import fi.riista.mobile.adapter.GameLogAdapter;
import fi.riista.mobile.database.HarvestDatabase;
import fi.riista.mobile.database.HarvestDatabase.SeasonStats;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.event.HarvestChangeEvent;
import fi.riista.mobile.event.HarvestChangeListener;
import fi.riista.mobile.feature.observation.ObservationActivity;
import fi.riista.mobile.feature.srva.SrvaActivity;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.GameLog;
import fi.riista.mobile.models.SpeciesCategory;
import fi.riista.mobile.models.observation.GameObservation;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.observation.ObservationDatabase;
import fi.riista.mobile.riistaSdkHelpers.ObservationExtensionsKt;
import fi.riista.mobile.riistaSdkHelpers.SrvaEventExtensionsKt;
import fi.riista.mobile.service.harvest.HarvestEventEmitter;
import fi.riista.mobile.srva.SrvaDatabase;
import fi.riista.mobile.sync.AppSync;
import fi.riista.mobile.ui.GameLogFilterView;
import fi.riista.mobile.ui.GameLogListItem;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.UiUtils;
import fi.riista.mobile.utils.UserInfoStore;
import fi.riista.mobile.viewmodel.GameLogViewModel;

public class GameLogFragment extends PageFragment
        implements HarvestChangeListener, GameLogFilterView.GameLogFilterListener, GameLogListItem.OnClickListItemListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    UserInfoStore mUserInfoStore;

    @Inject
    HarvestDatabase mHarvestDatabase;

    @Inject
    ObservationDatabase mObservationDatabase;

    @Inject
    AppSync mAppSync;

    @Inject
    HarvestEventEmitter mHarvestEventEmitter;

    private GameLogAdapter mAdapter;
    private List<GameLogListItem> mDisplayItems = new ArrayList<>();
    private GetHarvestsTask mGetHarvestsTask = null;
    private final SparseArray<CalendarYear> mCalendarYears = new SparseArray<>();
    private GameLogFilterView mFilterView;
    private GameLogViewModel mModel;

    private final ActivityResultLauncher<Intent> harvestActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> onHarvestActivityResult(result.getResultCode(), result.getData())
    );

    private final ActivityResultLauncher<Intent> observationActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> onObservationActivityResult(result.getResultCode(), result.getData())
    );

    private final ActivityResultLauncher<Intent> srvaActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> onSrvaActivityResult(result.getResultCode(), result.getData())
    );

    public static GameLogFragment newInstance() {
        return new GameLogFragment();
    }

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    @Override
    public void onAttach(@NonNull final Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_gamelog, container, false);
        setupActionBar(R.layout.actionbar_gamelog, true);

        final Context context = getActivity();

        final RecyclerView recyclerView = view.findViewById(R.id.historyListView);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new GameLogAdapter(context, mDisplayItems, this);
        recyclerView.setAdapter(mAdapter);

        mFilterView = view.findViewById(R.id.log_filter_view);
        mFilterView.setListener(this);

        mModel = new ViewModelProvider(requireActivity(), mViewModelFactory).get(GameLogViewModel.class);
        if (GameLog.TYPE_POI.equals(mModel.getTypeSelected().getValue())) {
            // POIs are not supported here, default to harvests
            mModel.selectLogType(GameLog.TYPE_HARVEST);
        }
        mModel.refreshSeasons();

        mFilterView.setupTypes(
                UiUtils.isSrvaVisible(mUserInfoStore.getUserInfo()),
                false,
                mModel.getTypeSelected().getValue()
        );
        mFilterView.setupSeasons(mModel.getSeasons().getValue(), mModel.getSeasonSelected().getValue());
        mFilterView.setupSpecies(mModel.getSpeciesSelected().getValue(), mModel.getCategorySelected().getValue());

        final LifecycleOwner lifecycleOwner = getViewLifecycleOwner();

        mModel.getTypeSelected().observe(lifecycleOwner, type -> refreshList());
        mModel.getSeasonSelected().observe(lifecycleOwner, season -> refreshList());
        mModel.getSpeciesSelected().observe(lifecycleOwner, speciesIds -> {
            mFilterView.setupSpecies(speciesIds, mModel.getCategorySelected().getValue());
            refreshList();
        });

        return view;
    }

    private void refreshList() {
        mModel.refreshSeasons();

        final String selection = mModel.getTypeSelected().getValue();

        if (GameLog.TYPE_HARVEST.equals(selection)) {
            loadHarvests();
        } else if (GameLog.TYPE_OBSERVATION.equals(selection)) {
            loadObservations();
        } else if (GameLog.TYPE_SRVA.equals(selection)) {
            loadSrvas();
        } else if (selection != null) {
            throw new RuntimeException("Unsupported selection type: " + selection);
        }
    }

    private void loadHarvests() {
        clearList();
        startHarvestItemsTask();
    }

    private void loadObservations() {
        clearList();

        mObservationDatabase.loadObservations(observations -> {
            final List<GameLogListItem> items = new ArrayList<>(observations.size());

            for (final GameObservation observation : observations) {
                items.add(GameLogListItem.fromObservation(observation));
            }

            final List<Integer> speciesSelected = mModel.getSpeciesSelected().getValue();
            addItems(items, speciesSelected != null && speciesSelected.size() > 0);
        });
    }

    private void loadSrvas() {
        clearList();

        SrvaDatabase.getInstance().loadEvents(events -> {
            final List<GameLogListItem> items = new ArrayList<>(events.size());

            for (final SrvaEvent event : events) {
                items.add(GameLogListItem.fromSrva(event));
            }

            addItems(items, true);
        });
    }

    private void clearList() {
        // Cancel task
        if (mGetHarvestsTask != null) {
            mGetHarvestsTask.cancel(false);
            mGetHarvestsTask = null;
        }

        final List<GameLogListItem> list = mDisplayItems;

        if (list != null) {
            list.clear();
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final String typeSelected = mModel.getTypeSelected().getValue();

        if (item.getItemId() == R.id.item_add) {
            if (GameLog.TYPE_HARVEST.equals(typeSelected)) {
                final Intent intent = new Intent(getActivity(), HarvestActivity.class);
                intent.putExtra(HarvestActivity.EXTRA_HARVEST,
                        GameHarvest.createNew(AppConfig.HARVEST_SPEC_VERSION));
                harvestActivityResultLaunch.launch(intent);

            } else if (GameLog.TYPE_OBSERVATION.equals(typeSelected)) {
                final Intent intent = ObservationActivity.getLaunchIntentForCreating(requireActivity(), null);
                observationActivityResultLaunch.launch(intent);

            } else if (GameLog.TYPE_SRVA.equals(typeSelected)) {
                final Intent intent = SrvaActivity.getLaunchIntentForCreating(requireActivity());
                srvaActivityResultLaunch.launch(intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        setViewTitle(getString(R.string.title_game_log));

        mHarvestEventEmitter.addListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        mHarvestEventEmitter.removeListener(this);
    }

    private void onHarvestActivityResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (data.getBooleanExtra(HarvestActivity.RESULT_DID_SAVE, false)) {
                refreshList();
            }
            mAppSync.syncImmediatelyIfAutomaticSyncEnabled();
        }
    }

    private void onObservationActivityResult(int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (ObservationActivity.getObservationCreatedOrModified(data.getExtras())) {
                refreshListAndStartAutomaticSync();
            }
        }
    }

    private void onSrvaActivityResult(int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (SrvaActivity.getSrvaEventCreatedOrModified(data.getExtras())) {
                refreshListAndStartAutomaticSync();
            }
        }
    }

    private void refreshListAndStartAutomaticSync() {
        refreshList();
        mAppSync.syncImmediatelyIfAutomaticSyncEnabled();
    }

    private void startHarvestItemsTask() {
        mGetHarvestsTask = new GetHarvestsTask();
        mGetHarvestsTask.execute();
    }

    private List<GameLogListItem> filterCurrentYearItems(final List<GameLogListItem> items) {
        final List<GameLogListItem> filtered = new ArrayList<>(items.size());

        final int season = mModel.getSeasonSelected().getValue();
        final DateTime startDate = DateTimeUtils.getHuntingYearStart(season);
        final DateTime endDate = DateTimeUtils.getHuntingYearEnd(season);

        for (final GameLogListItem event : items) {
            final DateTime eventTime = new DateTime(event.dateTime);

            if (GameLog.TYPE_SRVA.equals(event.type)) {
                if (eventTime.getYear() == season) {
                    filtered.add(event);
                }
            } else {
                if (eventTime.isAfter(startDate) && eventTime.isBefore(endDate)) {
                    filtered.add(event);
                }
            }
        }
        return filterSpeciesItems(filtered);
    }

    private List<GameLogListItem> filterSpeciesItems(final List<GameLogListItem> items) {
        final List<Integer> speciesCodes = mModel.getSpeciesSelected().getValue();

        if (speciesCodes == null || speciesCodes.isEmpty()) {
            return items;
        }

        final List<GameLogListItem> filtered = new ArrayList<>(items.size());

        for (final GameLogListItem event : items) {
            if (speciesCodes.contains(event.speciesCode)) {
                filtered.add(event);
            }
        }

        return filtered;
    }

    private void sortEventsByTime(final List<GameLogListItem> events) {
        Collections.sort(events, (lhs, rhs) -> rhs.dateTime.compareTo(lhs.dateTime));
    }

    private void addItems(final List<GameLogListItem> allEvents, final boolean hideStats) {
        if (getActivity() != null) {
            clearList();

            sortEventsByTime(allEvents);

            setupCalendarYears(allEvents);

            final List<GameLogListItem> newItems = filterCurrentYearItems(allEvents);

            mDisplayItems = newItems;

            for (int i = newItems.size() - 1; i >= 0; i--) {
                final GameLogListItem item = newItems.get(i);

                if (i == 0) {
                    final GameLogListItem sectionItem = new GameLogListItem();
                    sectionItem.isHeader = true;
                    sectionItem.month = newItems.get(i).dateTime.get(Calendar.MONTH);
                    sectionItem.year = newItems.get(i).dateTime.get(Calendar.YEAR);

                    mDisplayItems.add(i, sectionItem);
                } else {
                    final GameLogListItem prevItem = newItems.get(i - 1);

                    if (item.month != prevItem.month) {
                        final GameLogListItem sectionItem = new GameLogListItem();
                        sectionItem.isHeader = true;
                        sectionItem.month = newItems.get(i).dateTime.get(Calendar.MONTH);
                        sectionItem.year = newItems.get(i).dateTime.get(Calendar.YEAR);

                        mDisplayItems.add(i, sectionItem);
                    }
                }
            }
            insertSeparators(mDisplayItems);

            if (!hideStats) {
                final GameLogListItem statsItem = new GameLogListItem();
                statsItem.isStats = true;
                mDisplayItems.add(0, statsItem);
            }

            mAdapter.setItems(mDisplayItems);
        }
    }

    private void insertSeparators(final List<GameLogListItem> items) {
        for (int i = 0; i < items.size(); i++) {
            final GameLogListItem item = items.get(i);

            if (i == 0) {
                // First item is always month header
                item.isTimelineTopVisible = false;
                item.isTimelineBottomVisible = false;
            } else if (i == 1 && i == items.size() - 1) {
                item.isTimelineTopVisible = false;
                item.isTimelineBottomVisible = false;
            } else if (i == 1) {
                item.isTimelineTopVisible = false;
                item.isTimelineBottomVisible = true;
            } else if (i == items.size() - 1) {
                item.isTimelineTopVisible = true;
                item.isTimelineBottomVisible = false;
            } else {
                item.isTimelineTopVisible = true;
                item.isTimelineBottomVisible = true;
            }
        }
    }

    private void setupCalendarYears(final List<GameLogListItem> events) {
        mCalendarYears.clear();

        int newestYear = 0;
        int newestHuntingYear = -1;

        for (final GameLogListItem event : events) {
            int huntingYear = DateTimeUtils.getHuntingYearForCalendar(event.dateTime);
            if (huntingYear > newestHuntingYear) {
                newestHuntingYear = huntingYear;
            }

            final int realYear = event.dateTime.get(Calendar.YEAR);
            if (realYear > newestYear) {
                newestYear = realYear;
            }

            // Some observation types don't have amounts, but count them as 1 anyway
            int amount = Math.max(event.totalSpecimenAmount, 1);
            if (GameLog.TYPE_SRVA.equals(event.type)) {
                // SRVA events use normal years for grouping and each event
                // should be counted as one
                huntingYear = realYear;
                amount = 1;
            }

            CalendarYear calendarYear = mCalendarYears.get(huntingYear);
            if (calendarYear == null) {
                calendarYear = new CalendarYear();
                calendarYear.year = huntingYear;
                calendarYear.statistics = new SeasonStats();
                mCalendarYears.put(huntingYear, calendarYear);
            }

            final SpeciesCategory category = SpeciesInformation.categoryForSpecies(event.speciesCode);
            if (category != null) {
                final int currentValue = calendarYear.statistics.mCategoryData.get(category.mId);
                final int resultValue = amount + currentValue;
                calendarYear.statistics.mCategoryData.put(category.mId, resultValue);
            }
        }

        final int selectedSeason = mModel.getSeasonSelected().getValue();

        if (mCalendarYears.get(selectedSeason) == null) {
            final CalendarYear year = new CalendarYear();
            year.year = selectedSeason;
            year.statistics = new SeasonStats();
            mCalendarYears.put(year.year, year);
        }

        mFilterView.setupSeasons(mModel.getSeasons().getValue(), selectedSeason);

        final CalendarYear calendarYear = mCalendarYears.get(selectedSeason);
        mAdapter.setStats(calendarYear.statistics);
    }

    @Override
    public void onHarvestsChanged(@NonNull final Collection<HarvestChangeEvent> changeEvents) {
        if (getActivity() != null && getView() != null) {
            refreshList();
        }
    }

    @Override
    public void onLogTypeSelected(@Nullable final String type) {
        mModel.selectLogType(type);
    }

    @Override
    public void onLogSeasonSelected(final int season) {
        mModel.selectLogSeason(season);
    }

    @Override
    public void onLogSpeciesSelected(@Nullable final List<Integer> speciesIds) {
        mModel.selectSpeciesIds(speciesIds);
    }

    @Override
    public void onLogSpeciesCategorySelected(final int categoryId) {
        mModel.selectSpeciesCategory(categoryId);
    }

    @Override
    public void onItemClick(final GameLogListItem item) {
        switch (item.type) {
            case GameLog.TYPE_HARVEST: {
                final Intent intent = new Intent(getActivity(), HarvestActivity.class);
                intent.putExtra(HarvestActivity.EXTRA_HARVEST, item.mHarvest);
                harvestActivityResultLaunch.launch(intent);
                break;
            }
            case GameLog.TYPE_OBSERVATION: {
                // todo: ensure observation category remains selected (same as previously)
                // item.mObservation.observationCategorySelected = true; // This selection is not stored so set it to true, as it must have been selected as observation is saved.
                final CommonObservation observation = ObservationExtensionsKt.toCommonObservation(item.mObservation);
                if (observation != null) {
                    final Intent intent = ObservationActivity.getLaunchIntentForViewing(requireActivity(), observation);
                    observationActivityResultLaunch.launch(intent);
                }
                break;
            }
            case GameLog.TYPE_SRVA: {
                final CommonSrvaEvent srvaEvent = SrvaEventExtensionsKt.toCommonSrvaEvent(item.mSrva);
                if (srvaEvent != null) {
                    final Intent intent = SrvaActivity.getLaunchIntentForViewing(requireActivity(), srvaEvent);
                    srvaActivityResultLaunch.launch(intent);
                }
                break;
            }
        }
    }

    private class GetHarvestsTask extends AsyncTask<Void, Void, Integer> {

        private List<GameHarvest> mNewItems = null;

        @Override
        protected Integer doInBackground(final Void... params) {
            mNewItems = mHarvestDatabase.getAllHarvests();
            return 0;
        }

        @Override
        protected void onPostExecute(final Integer result) {
            final List<GameLogListItem> items = new ArrayList<>(mNewItems.size());

            for (final GameHarvest harvest : mNewItems) {
                items.add(GameLogListItem.fromHarvest(harvest));
            }

            final List<Integer> speciesSelected = mModel.getSpeciesSelected().getValue();
            addItems(items, speciesSelected != null && speciesSelected.size() > 0);
        }
    }

    private static class CalendarYear {
        public int year;

        SeasonStats statistics;
    }
}
