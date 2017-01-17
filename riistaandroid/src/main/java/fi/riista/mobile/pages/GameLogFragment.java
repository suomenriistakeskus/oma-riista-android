package fi.riista.mobile.pages;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.R;
import fi.riista.mobile.activity.EditActivity;
import fi.riista.mobile.activity.HarvestActivity;
import fi.riista.mobile.activity.MainActivity;
import fi.riista.mobile.adapter.CalendarPagerAdapter;
import fi.riista.mobile.adapter.CalendarPagerAdapter.CalendarSource;
import fi.riista.mobile.adapter.GameLogAdapter;
import fi.riista.mobile.database.DiaryEntryUpdate;
import fi.riista.mobile.database.EventItem;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.database.GameDatabase.DatabaseUpdateListener;
import fi.riista.mobile.database.GameDatabase.Statistics;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.message.EventUpdateMessage;
import fi.riista.mobile.models.GameHarvest;
import fi.riista.mobile.models.GameObservation;
import fi.riista.mobile.models.LogEventBase;
import fi.riista.mobile.models.SpeciesCategory;
import fi.riista.mobile.models.srva.SrvaEvent;
import fi.riista.mobile.observation.ObservationDatabase;
import fi.riista.mobile.observation.ObservationDatabase.ObservationsListener;
import fi.riista.mobile.srva.SrvaDatabase;
import fi.riista.mobile.srva.SrvaDatabase.SrvaEventListener;
import fi.riista.mobile.ui.GameCalendar;
import fi.riista.mobile.ui.HeaderTextView;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.UiUtils;
import fi.vincit.androidutilslib.message.WorkMessageHandler;
import fi.vincit.androidutilslib.util.ViewAnnotations;
import fi.vincit.androidutilslib.util.ViewAnnotations.ViewId;
import fi.vincit.androidutilslib.util.ViewAnnotations.ViewOnClick;

public class GameLogFragment extends PageFragment implements DatabaseUpdateListener, CalendarSource {

    public static String START_YEAR_KEY = "startYear";

    private static SimpleDateFormat OBSERVATION_DATE_FORMAT = new SimpleDateFormat(AppConfig.SERVER_DATE_FORMAT);

    private static final int TAB_HARVESTS = 0;
    private static final int TAB_OBSERVATIONS = 1;
    private static final int TAB_SRVAS = 2;

    // Update could have been received while view was destroyed. Complete updates after view has been recreated
    private List<DiaryEntryUpdate> mPendingEvents = null;
    private ArrayAdapter<EventItem> mAdapter = null;
    private ListView mListView = null;
    private List<EventItem> mEvents = new ArrayList<>();
    private EventTask mEventTask = null;

    private ImageView mSelectedYearImageView = null;
    private ViewPager mCalendarPager = null;
    private SparseArray<CalendarYear> mCalendarYears = new SparseArray<>();
    private CalendarPagerAdapter mCalendarAdapter = null;
    private int mCalendarYear = -1;
    private int mShowTab = TAB_HARVESTS;
    private long mUpdatedEventLocalId = -1;

    @ViewId(R.id.btn_show_harvest)
    private ToggleButton mButtonHarvest;

    @ViewId(R.id.btn_show_observations)
    private ToggleButton mButtonObservations;

    @ViewId(R.id.btn_show_srvas)
    private ToggleButton mButtonSrvas;

    public static GameLogFragment newInstance() {
        return new GameLogFragment();
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mAdapter = new GameLogAdapter(((MainActivity) getActivity()).getWorkContext(), mEvents);
    }

    @ViewOnClick(R.id.btn_show_harvest)
    private void onClickShowHarvests(View view) {
        mShowTab = TAB_HARVESTS;
        mCalendarYear = -1;

        refreshList();
    }

    @ViewOnClick(R.id.btn_show_observations)
    private void onClickShowObservations(View view) {
        mShowTab = TAB_OBSERVATIONS;
        mCalendarYear = -1;

        refreshList();
    }

    @ViewOnClick(R.id.btn_show_srvas)
    private void onClickSrvas(View view) {
        mShowTab = TAB_SRVAS;
        mCalendarYear = -1;

        refreshList();
    }

    @WorkMessageHandler(EventUpdateMessage.class)
    public void onEventUpdate(EventUpdateMessage message) {
        //Scroll to this event once list has been refreshed
        mUpdatedEventLocalId = message.localId;

        switch (message.type) {
            case LogEventBase.TYPE_HARVEST:
                mShowTab = TAB_HARVESTS;
                break;
            case LogEventBase.TYPE_OBSERVATION:
                mShowTab = TAB_OBSERVATIONS;
                break;
            default:
                mShowTab = TAB_SRVAS;
                break;
        }
        refreshList();

        //Show the year this event belongs to
        mCalendarYear = message.huntingYear;
    }

    private void refreshList() {
        if (mShowTab == TAB_HARVESTS) {
            loadHarvests();
        } else if (mShowTab == TAB_OBSERVATIONS) {
            loadObservations();
        } else {
            loadSrvas();
        }
    }

    private void loadSrvas() {
        clearButtonsState();
        mButtonSrvas.setSelected(true);

        clearList();

        SrvaDatabase.getInstance().loadEvents(new SrvaEventListener() {
            @Override
            public void onEvents(List<SrvaEvent> events) {
                ArrayList<GameHarvest> items = new ArrayList<>();
                for (SrvaEvent event : events) {
                    items.add(srvaToEvent(event));
                }
                addItems(0, items);
            }
        });
    }

    public GameHarvest srvaToEvent(SrvaEvent srva) {
        Calendar time = srva.toDateTime().toCalendar(null);

        GameHarvest event = new GameHarvest(srva.gameSpeciesCode, srva.totalSpecimenAmount,
                srva.description, time, srva.type, srva.toLocation(),
                srva.getAllImages());
        event.mSent = !srva.modified;
        event.mSrvaEvent = srva;
        return event;
    }

    private void loadHarvests() {
        clearButtonsState();
        mButtonHarvest.setSelected(true);

        clearList();

        startHarvestItemsTask();
    }

    private void loadObservations() {
        clearButtonsState();
        mButtonObservations.setSelected(true);

        clearList();

        ObservationDatabase.getInstance().loadObservations(new ObservationsListener() {
            @Override
            public void onObservations(List<GameObservation> observations) {
                ArrayList<GameHarvest> items = new ArrayList<>();

                for (GameObservation observation : observations) {
                    items.add(observationToEvent(observation));
                }
                addItems(0, items);
            }
        });
    }

    private GameHarvest observationToEvent(GameObservation observation) {
        int amount = observation.getMooselikeSpecimenCount();
        if (amount == 0 && observation.totalSpecimenAmount != null) {
            amount = observation.totalSpecimenAmount;
        }
        Calendar time = observation.toDateTime().toCalendar(null);

        GameHarvest event = new GameHarvest(observation.gameSpeciesCode, amount,
                observation.description, time, observation.type, observation.toLocation(),
                observation.getAllImages());
        event.mSent = !observation.modified;
        event.mObservation = observation;

        return event;
    }

    private void clearButtonsState() {
        mButtonHarvest.setSelected(false);
        mButtonObservations.setSelected(false);
        mButtonSrvas.setSelected(false);
    }

    private void clearList() {
        //Cancel task
        if (mEventTask != null) {
            mEventTask.cancel(false);
            mEventTask = null;
        }

        mEvents.clear();

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gamelog, container, false);
        setHasOptionsMenu(true);

        mCalendarPager = (ViewPager) view.findViewById(R.id.calendar);
        mCalendarPager.setPageMargin(20);
        mCalendarAdapter = new CalendarPagerAdapter(this, getActivity());
        mCalendarPager.setAdapter(mCalendarAdapter);

        //Reparent the view pager container into the list as a header view
        ViewGroup pagerParent = (ViewGroup) mCalendarPager.getParent();
        ViewGroup pagerRoot = (ViewGroup) pagerParent.getParent();
        pagerRoot.removeView(pagerParent);
        pagerParent.setLayoutParams(new ListView.LayoutParams(pagerParent.getLayoutParams()));

        mListView = (ListView) view.findViewById(R.id.historyListView);
        mListView.addHeaderView(pagerParent);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Adding header causes positions to increase by one.
                position = position - 1;

                GameHarvest event = mEvents.get(position).mEvent;
                String eventType = "" + event.mType;
                switch (eventType) {
                    case LogEventBase.TYPE_HARVEST: {
                        Intent intent = new Intent(getActivity(), HarvestActivity.class);
                        intent.putExtra(HarvestActivity.EXTRA_HARVEST, event);
                        startActivityForResult(intent, HarvestActivity.EDIT_HARVEST_REQUEST_CODE);
                        break;
                    }
                    case LogEventBase.TYPE_OBSERVATION: {
                        Intent intent = new Intent(getActivity(), EditActivity.class);
                        intent.putExtra(EditActivity.EXTRA_OBSERVATION, event.mObservation);
                        startActivityForResult(intent, EditActivity.EDIT_OBSERVATION_REQUEST_CODE);
                        break;
                    }
                    case LogEventBase.TYPE_SRVA: {
                        Intent intent = new Intent(getActivity(), EditActivity.class);
                        intent.putExtra(EditActivity.EXTRA_SRVA_EVENT, event.mSrvaEvent);
                        startActivityForResult(intent, EditActivity.EDIT_SRVA_REQUEST_CODE);
                        break;
                    }
                }
            }
        });

        ViewAnnotations.apply(this, view);

        int colorize = getActivity().getResources().getColor(R.color.icon_colorize);
        setBackgroudFilter(mButtonHarvest, colorize);
        setBackgroudFilter(mButtonObservations, colorize);
        setBackgroudFilter(mButtonSrvas, colorize);

        //noinspection ResourceType
        mButtonSrvas.setVisibility(UiUtils.getSrvaVisibility(getActivity()));

        refreshList();

        return view;
    }

    private static void setBackgroudFilter(Button button, int color) {
        button.getBackground().mutate().setColorFilter(color, Mode.MULTIPLY);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.gamelog, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                if (mShowTab == TAB_HARVESTS) {
                    Intent intent = new Intent(getActivity(), HarvestActivity.class);
                    intent.putExtra(HarvestActivity.EXTRA_HARVEST, GameHarvest.createNew());
                    intent.putExtra(HarvestActivity.EXTRA_NEW, true);
                    startActivityForResult(intent, HarvestActivity.NEW_HARVEST_REQUEST_CODE);
                } else if (mShowTab == TAB_OBSERVATIONS) {
                    Intent intent = new Intent(getActivity(), EditActivity.class);
                    intent.putExtra(EditActivity.EXTRA_OBSERVATION, GameObservation.createNew());
                    intent.putExtra(EditActivity.EXTRA_NEW, true);
                    startActivityForResult(intent, EditActivity.NEW_OBSERVATION_REQUEST_CODE);
                } else if (mShowTab == TAB_SRVAS) {
                    Intent intent = new Intent(getActivity(), EditActivity.class);
                    intent.putExtra(EditActivity.EXTRA_SRVA_EVENT, SrvaEvent.createNew());
                    intent.putExtra(EditActivity.EXTRA_NEW, true);
                    startActivityForResult(intent, EditActivity.NEW_SRVA_REQUEST_CODE);
                }
                return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        setViewTitle(getString(R.string.title_game_log));

        // Update with pending updates if any
        if (mPendingEvents != null) {
            eventsUpdated(mPendingEvents);
            mPendingEvents = null;
        }

        setupCalendarArrow(getView().findViewById(R.id.calendar_previous), -1);
        setupCalendarArrow(getView().findViewById(R.id.calendar_next), 1);

        GameDatabase.getInstance().registerListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        GameDatabase.getInstance().unregisterListener(this);
    }

    void setupCalendarArrow(View view, final int offset) {
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCalendarYear(offset);
            }
        });
    }

    public void changeCalendarYear(int offset) {
        int index = mCalendarPager.getCurrentItem();
        if (index + offset >= 0 && index + offset < mCalendarYears.size()) {
            mCalendarPager.setCurrentItem(index + offset);
            calendarYearSelected(index + offset, true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == HarvestActivity.NEW_HARVEST_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                boolean didSave = data.getBooleanExtra(HarvestActivity.RESULT_DID_SAVE, false);

                if (didSave) {
                    refreshList();
                }

                GameDatabase db = GameDatabase.getInstance();
                if (db.getSyncMode(getContext()) == GameDatabase.SyncMode.SYNC_AUTOMATIC) {
                    db.doSyncAndResetTimer();
                }
            }
        } else if (requestCode == HarvestActivity.EDIT_HARVEST_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                boolean didSave = data.getBooleanExtra(HarvestActivity.RESULT_DID_SAVE, false);

                if (didSave) {
                    refreshList();
                }
            }
        } else if (requestCode == EditActivity.NEW_OBSERVATION_REQUEST_CODE
                || requestCode == EditActivity.EDIT_OBSERVATION_REQUEST_CODE
                || requestCode == EditActivity.NEW_SRVA_REQUEST_CODE
                || requestCode == EditActivity.EDIT_SRVA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                boolean didSave = data.getBooleanExtra(EditActivity.RESULT_DID_SAVE, false);

                if (didSave) {
                    refreshList();
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

    void startHarvestItemsTask() {
        mEventTask = new EventTask();
        mEventTask.execute();
    }

    private List<GameHarvest> filterCurrentYearItems(List<GameHarvest> items) {
        List<GameHarvest> filtered = new ArrayList<>();

        DateTime startDate = DateTimeUtils.getHuntingYearStart(mCalendarYear);
        DateTime endDate = DateTimeUtils.getHuntingYearEnd(mCalendarYear);

        for (GameHarvest event : items) {
            DateTime eventTime = new DateTime(event.mTime);

            if (LogEventBase.TYPE_SRVA.equals(event.mType)) {
                if (eventTime.getYear() == mCalendarYear) {
                    filtered.add(event);
                }
            } else {
                if (eventTime.isAfter(startDate) && eventTime.isBefore(endDate)) {
                    filtered.add(event);
                }
            }
        }
        return filtered;
    }

    private void sortEventsByTime(List<GameHarvest> events) {
        Collections.sort(events, new Comparator<GameHarvest>() {
            @Override
            public int compare(GameHarvest lhs, GameHarvest rhs) {
                return lhs.mTime.compareTo(rhs.mTime);
            }
        });
    }

    void addItems(int offset, List<GameHarvest> allEvents) {
        if (getActivity() != null) {
            clearList();

            sortEventsByTime(allEvents);

            setupCalendarYears(allEvents);

            List<GameHarvest> newItems = filterCurrentYearItems(allEvents);

            EventItem separator = new EventItem();
            separator.isSeparator = true;

            for (int i = 0; i < newItems.size(); i++) {
                EventItem event = new EventItem();
                event.mEvent = newItems.get(i);
                event.year = newItems.get(i).mTime.get(Calendar.YEAR);
                event.month = newItems.get(i).mTime.get(Calendar.MONTH);

                if (offset > 0) {
                    if (mEvents.size() > 0) {
                        mEvents.add(separator);
                    }
                    if (mEvents.size() == 0 || (mEvents.size() > 1 && (mEvents.get(mEvents.size() - 2).month != event.month || mEvents.get(mEvents.size() - 2).year != event.year))) {
                        EventItem header = new EventItem();
                        header.isHeader = true;
                        header.year = event.year;
                        header.month = event.month;
                        mEvents.add(header);
                        mEvents.add(separator);
                    }
                    mEvents.add(event);
                } else {
                    boolean headerRemoved = false;
                    if (mEvents.size() > 0 && mEvents.get(0).isHeader && mEvents.get(0).month == event.month && mEvents.get(0).year == event.year) {
                        mEvents.remove(0);
                        headerRemoved = true;
                    } else if (mEvents.size() > 0 && !mEvents.get(0).isHeader && (mEvents.get(0).month != event.month || mEvents.get(0).year != event.year)) {
                        EventItem header = new EventItem();
                        header.isHeader = true;
                        header.month = mEvents.get(0).month;
                        header.year = mEvents.get(0).year;
                        mEvents.add(0, separator);
                        mEvents.add(0, header);
                    }
                    if (!headerRemoved && mEvents.size() > 0)
                        mEvents.add(0, separator);
                    mEvents.add(0, event);
                }

                if (offset <= 0) {
                    if (i == newItems.size() - 1) {
                        EventItem header = new EventItem();
                        header.isHeader = true;
                        header.month = event.month;
                        header.year = event.year;
                        mEvents.add(0, separator);
                        mEvents.add(0, header);
                    }
                }
            }
            mAdapter.notifyDataSetChanged();

            scrollToUpdatedEvent();
        }
    }

    private class EventTask extends AsyncTask<Void, Void, Integer> {

        List<GameHarvest> mNewItems = null;

        EventTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Ensure that DB instance gets created in UI thread. If trying to create instance in background thread an
            // exception is thrown. DB instance creates Handler which needs to be associated with UI thread looper.
            GameDatabase.getInstance();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            mNewItems = GameDatabase.getInstance().getAllEvents();
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            addItems(0, mNewItems);
        }
    }

    void setupCalendarYears(List<GameHarvest> events) {
        mCalendarYears.clear();

        int newestYear = 0;
        int newestHuntingYear = -1;
        for (GameHarvest event : events) {
            int year = DateTimeUtils.getSeasonStartYearFromDate(event.mTime);
            if (year > newestHuntingYear) {
                newestHuntingYear = year;
            }

            int realYear = event.mTime.get(Calendar.YEAR);
            if (realYear > newestYear) {
                newestYear = realYear;
            }

            //Some observation types don't have amounts, but count them as 1 anyway
            int amount = Math.max(event.mAmount, 1);
            if (LogEventBase.TYPE_SRVA.equals(event.mType)) {
                //SRVA events use normal years for grouping and each event
                //should be counted as one
                year = realYear;
                amount = 1;
            }

            CalendarYear calendarYear = mCalendarYears.get(year);
            if (calendarYear == null) {
                calendarYear = new CalendarYear();
                calendarYear.year = year;
                calendarYear.statistics = new Statistics();
                mCalendarYears.put(year, calendarYear);
            }

            int month = event.mTime.get(Calendar.MONTH);
            calendarYear.statistics.mMonthlyData.set(month, calendarYear.statistics.mMonthlyData.get(month) + amount);

            SpeciesCategory category = SpeciesInformation.categoryForSpecies(event.mSpeciesID);
            if (category != null) {
                Integer resultValue = amount;
                Integer currentValue = calendarYear.statistics.mCategoryData.get(category.mId);
                if (currentValue != null) {
                    resultValue += currentValue;
                }
                calendarYear.statistics.mCategoryData.put(category.mId, resultValue);
            }
            calendarYear.statistics.mTotalCatches += amount;
        }

        if (mCalendarYears.size() == 0) {
            //No events, create a dummy year
            mCalendarYear = DateTimeUtils.getSeasonStartYearFromDate(Calendar.getInstance());

            CalendarYear year = new CalendarYear();
            year.year = mCalendarYear;
            year.statistics = new Statistics();
            mCalendarYears.put(year.year, year);
        }

        if (mCalendarYear < 0) {
            mCalendarYear = newestHuntingYear;
        }

        mCalendarAdapter.notifyDataSetChanged();

        int index = mCalendarYears.indexOfKey(mCalendarYear);
        if (index < 0) {
            index = mCalendarYears.indexOfKey(newestHuntingYear);
        }

        if (index > -1) {
            mCalendarPager.setCurrentItem(index);
            setupCalendarYearViews(index);
        }

        mCalendarAdapter.forceUpdate();
    }

    /**
     * Setups the calendar dots under statistics view
     * Default selection is used to highlight the current year
     *
     * @param startYearIndex Year's array index (0...mCalendarYears.size()-1)
     */
    void setupCalendarYearViews(int startYearIndex) {
        if (mCalendarYears.size() > 0) {
            LinearLayout calendarYears = (LinearLayout) getView().findViewById(R.id.calendar_years);
            calendarYears.removeAllViews();
            final Drawable circle = getResources().getDrawable(R.drawable.circle);
            final Drawable hollowcircle = getResources().getDrawable(R.drawable.hollowcircle);
            int circleSize = (int) getActivity().getResources().getDimension(R.dimen.year_circle_size);
            for (int i = 0; i < mCalendarYears.size(); i++) {

                final ImageView imageView = new ImageView(getActivity());
                imageView.setImageDrawable(circle);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        circleSize,
                        circleSize
                );

                // FIXME: This fixes dots alignment to center. Need to rethink layout and use display pixels.
                if (i == 0) {
                    params.leftMargin = 20;
                }
                params.rightMargin = 20;
                imageView.setLayoutParams(params);
                if (i == startYearIndex) {
                    imageView.setImageDrawable(circle);
                    mSelectedYearImageView = imageView;
                } else {
                    imageView.setImageDrawable(hollowcircle);
                }
                mCalendarYears.get(mCalendarYears.keyAt(i)).dot = imageView;
                calendarYears.addView(imageView);
            }
        }
        if (mCalendarYears.size() > 0)
            selectCalendarYearDot(startYearIndex);
    }

    void selectCalendarYearDot(final int yearIndex) {
        final Drawable circle = getResources().getDrawable(R.drawable.circle);
        final Drawable hollowcircle = getResources().getDrawable(R.drawable.hollowcircle);
        if (mSelectedYearImageView != null) {
            mSelectedYearImageView.setImageDrawable(hollowcircle);
        }

        if (mCalendarYears.get(mCalendarYears.keyAt(yearIndex)).dot != null) {
            mSelectedYearImageView = mCalendarYears.get(mCalendarYears.keyAt(yearIndex)).dot;
            mSelectedYearImageView.setImageDrawable(circle);
        }
    }

    @Override
    public void eventsUpdated(List<DiaryEntryUpdate> updatedEvents) {
        if (getActivity() != null && getView() != null) {
            refreshList();
        } else {
            mPendingEvents = updatedEvents;
        }
    }

    @Override
    public void setupCalendar(final GameCalendar calendar, final int position) {
        final int year = mCalendarYears.get(mCalendarYears.keyAt(position)).year;
        if (getView() != null && mCalendarYears.get(year) != null && mCalendarYears.get(year).statistics != null && calendar != null) {
            SparseArray<SpeciesCategory> categories = SpeciesInformation.getSpeciesCategories();
            calendar.setData(mCalendarYears.get(year).statistics.mMonthlyData);
            calendar.setCategories(categories, mCalendarYears.get(year).statistics.mCategoryData);

            float statsHeight = getResources().getDimension(R.dimen.season_stats_height);
            if (mShowTab != TAB_SRVAS) {
                calendar.showCategories(true);
                mCalendarPager.getLayoutParams().height = (int) statsHeight;
            } else {
                //Hide categories for SRVA events. We need to set the view visibility and
                //also manually adjust the view height.
                calendar.showCategories(false);
                mCalendarPager.getLayoutParams().height = (int) (statsHeight - (statsHeight - getResources().getDimension(R.dimen.season_months_height)));
            }

            if (mCalendarPager.getCurrentItem() == position) {
                calendarYearSelected(position, false);
            }
        }
    }

    private void scrollToUpdatedEvent() {
        if (mUpdatedEventLocalId <= 0) {
            return;
        }

        int position = 0;
        for (EventItem item : mEvents) {
            if (item.mEvent != null && item.mEvent.mLocalId == mUpdatedEventLocalId) {
                mListView.setSelection(position);
                break;
            }
            position++;
        }

        //Only scroll here one time
        mUpdatedEventLocalId = -1;
    }

    private void calendarYearSelected(int yearIndex, boolean interact) {
        CalendarYear calendarYear = mCalendarYears.get(mCalendarYears.keyAt(yearIndex));
        View view = getView();

        HeaderTextView textView = (HeaderTextView) view.findViewById(R.id.season_text);
        String textFormat = getResources().getString(R.string.season);

        if (mShowTab == TAB_SRVAS) {
            String text = String.format(getResources().getString(R.string.season_srva), "" + (calendarYear.year));
            textView.setText(text);
        } else {
            //Setup season title
            String text = String.format(textFormat, calendarYear.year + " - " + (calendarYear.year + 1));
            textView.setText(text);
        }
        textView.setLeftPadding(0);

        // Statistics data
        TextView catchView = (TextView) view.findViewById(R.id.catch_total);

        if (mShowTab == TAB_HARVESTS) {
            textFormat = getResources().getString(R.string.catch_total);
        } else if (mShowTab == TAB_OBSERVATIONS) {
            textFormat = getResources().getString(R.string.observation_total);
        } else {
            textFormat = getResources().getString(R.string.srva_total);
        }

        if (calendarYear.statistics != null) {
            catchView.setText(String.format(textFormat, calendarYear.statistics.mTotalCatches.toString()));
        }
        selectCalendarYearDot(yearIndex);

        // Calendar arrows
        view.findViewById(R.id.calendar_previous).setVisibility(yearIndex > 0 ? View.VISIBLE : View.INVISIBLE);
        view.findViewById(R.id.calendar_next).setVisibility(yearIndex < mCalendarYears.size() - 1 ? View.VISIBLE : View.INVISIBLE);

        if (interact) {
            mCalendarYear = mCalendarYears.keyAt(yearIndex);

            refreshList();
        }
    }

    @Override
    public int getCount() {
        return mCalendarYears.size();
    }

    @Override
    public int getStartMonth() {
        if (mShowTab == TAB_SRVAS) {
            return 0;
        } else {
            return 7;
        }
    }

    private class CalendarYear {
        public int year;

        ImageView dot;
        GameDatabase.Statistics statistics;
    }
}
