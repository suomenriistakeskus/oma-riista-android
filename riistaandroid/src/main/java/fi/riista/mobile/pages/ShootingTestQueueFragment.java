package fi.riista.mobile.pages;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import fi.riista.mobile.R;
import fi.riista.mobile.activity.ShootingTestUserAttemptsActivity;
import fi.riista.mobile.adapter.ShootingTestQueueAdapter;
import fi.riista.mobile.models.shootingTest.ShootingTestParticipant;
import fi.riista.mobile.viewmodel.ShootingTestMainViewModel;
import fi.vincit.androidutilslib.util.ViewAnnotations;

import static fi.riista.mobile.activity.ShootingTestUserAttemptsActivity.EXTRA_PARTICIPANT_ID;
import static fi.riista.mobile.activity.ShootingTestUserAttemptsActivity.EXTRA_TEST_COMPLETED;
import static java.util.Objects.requireNonNull;

public class ShootingTestQueueFragment extends ShootingTestTabContentFragment {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private ShootingTestMainViewModel mModel;
    private ShootingTestQueueAdapter mAdapter;

    public static ShootingTestQueueFragment newInstance() {
        final ShootingTestQueueFragment fragment = new ShootingTestQueueFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    @Override
    public void onAttach(@NonNull final Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mModel = new ViewModelProvider(mActivity, mViewModelFactory).get(ShootingTestMainViewModel.class);

        final Observer<List<ShootingTestParticipant>> participantObserver = shootingTestParticipants -> {
            mAdapter.clear();

            if (shootingTestParticipants != null) {
                for (final ShootingTestParticipant participant : shootingTestParticipants) {
                    if (!participant.completed) {
                        mAdapter.add(participant);
                    }
                }
            }

            mAdapter.sort((o1, o2) -> {
                if (o1 == null) {
                    return -1;
                } else if (o2 == null) {
                    return 1;
                } else if (o1.attempts.size() == 0 && o2.attempts.size() > 0) {
                    return -1;
                } else if (o1.attempts.size() > 0 && o2.attempts.size() == 0) {
                    return 1;
                } else {
                    return o1.registrationTime.compareTo(o2.registrationTime);
                }
            });
            mAdapter.notifyDataSetChanged();
        };

        mModel.getParticipants().observe(this, participantObserver);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_shooting_test_queue, container, false);

        ViewAnnotations.apply(this, view);

        mAdapter = new ShootingTestQueueAdapter(new ArrayList<>(), getActivity());

        final ListView listView = view.findViewById(R.id.shooting_test_queue);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            final ShootingTestParticipant clickedItem = requireNonNull(mAdapter.getItem(position));

            final Intent intent = new Intent(getActivity(), ShootingTestUserAttemptsActivity.class);
            intent.putExtra(EXTRA_PARTICIPANT_ID, clickedItem.id);
            intent.putExtra(EXTRA_TEST_COMPLETED, clickedItem.completed);

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
        if (mModel != null) {
            mModel.refreshCalendarEvent();
            mModel.refreshParticipants();
        }
    }

    @Override
    public void onTabSelected() {
        refreshData();
    }
}
