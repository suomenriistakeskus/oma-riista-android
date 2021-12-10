package fi.riista.mobile.pages;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dagger.android.support.AndroidSupportInjection;
import fi.riista.mobile.R;
import fi.riista.mobile.activity.ShootingTestMainActivity;
import fi.riista.mobile.adapter.ShootingTestCalendarEventsAdapter;
import fi.riista.mobile.models.shootingTest.ShootingTestCalendarEvent;
import fi.riista.mobile.network.shootingTest.ListShootingCalendarEventsTask;
import fi.vincit.androidutilslib.context.WorkContext;

import static fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME;

public class ShootingTestCalendarEventListFragment extends PageFragment {

    public static final String EXTRA_CALENDAR_EVENT_ID = "extra_calendar_event_id";

    @Inject
    @Named(APPLICATION_WORK_CONTEXT_NAME)
    WorkContext mAppWorkContext;

    private List<ShootingTestCalendarEvent> mDataModels = new ArrayList<>();
    private ShootingTestCalendarEventsAdapter mAdapter;

    public static ShootingTestCalendarEventListFragment newInstance() {
        final ShootingTestCalendarEventListFragment fragment = new ShootingTestCalendarEventListFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    @Override
    public void onAttach(@NonNull final Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_shooting_test_list, container, false);

        setupActionBar(R.layout.actionbar_shooting_test_list, false);

        mAdapter = new ShootingTestCalendarEventsAdapter(mDataModels, getActivity());

        final ListView listView = view.findViewById(R.id.shooting_test_list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            final ShootingTestCalendarEvent clickedEvent = mDataModels.get(position);

            final Intent intent = new Intent(getActivity(), ShootingTestMainActivity.class);
            intent.putExtra(EXTRA_CALENDAR_EVENT_ID, clickedEvent.calendarEventId);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshData();
    }

    private void refreshData() {
        final ListShootingCalendarEventsTask task = new ListShootingCalendarEventsTask(mAppWorkContext) {
            @Override
            protected void onFinishObjects(final List<ShootingTestCalendarEvent> results) {
                mDataModels = results;

                mAdapter.clear();
                mAdapter.addAll(mDataModels);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            protected void onError() {
                super.onError();
            }
        };
        task.start();
    }
}
