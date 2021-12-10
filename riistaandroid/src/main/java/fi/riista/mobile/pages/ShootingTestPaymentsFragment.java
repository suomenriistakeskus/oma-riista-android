package fi.riista.mobile.pages;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import fi.riista.mobile.R;
import fi.riista.mobile.adapter.ShootingTestPaymentsAdapter;
import fi.riista.mobile.viewmodel.ShootingTestMainViewModel;
import fi.vincit.androidutilslib.util.ViewAnnotations;

public class ShootingTestPaymentsFragment extends ShootingTestTabContentFragment {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private ShootingTestMainViewModel mModel;
    private ShootingTestPaymentsAdapter mAdapter;

    @ViewAnnotations.ViewId(R.id.shooting_test_payments_sum)
    private TextView mSumOfPayments;

    public static ShootingTestPaymentsFragment newInstance() {
        final ShootingTestPaymentsFragment fragment = new ShootingTestPaymentsFragment();
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

        mModel.getParticipants().observe(this, shootingTestParticipants -> {
            mAdapter.clear();

            if (shootingTestParticipants != null) {
                mAdapter.addAll(shootingTestParticipants);
            }

            mAdapter.sort((o1, o2) -> {
                if (o1 == null) {
                    return -1;
                } else if (o2 == null) {
                    return 1;
                } else if (o1.completed != o2.completed) {
                    return o1.completed ? 1 : -1;
                } else if (o1.attempts.isEmpty() != o2.attempts.isEmpty()) {
                    return o1.attempts.isEmpty() ? -1 : 1;
                } else {
                    return o1.registrationTime.compareTo(o2.registrationTime);
                }
            });

            mAdapter.notifyDataSetChanged();
        });

        mModel.getCalendarEvent().observe(this, calendarEvent -> {
            if (calendarEvent != null) {
                mAdapter.setEditEnabled(!calendarEvent.isClosed());
                mSumOfPayments.setText(String.format(getString(R.string.shooting_test_event_sum_of_payments), calendarEvent.totalPaidAmount));
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_shooting_test_payments, container, false);

        ViewAnnotations.apply(this, view);

        mAdapter = new ShootingTestPaymentsAdapter(mActivity, new ArrayList<>(), mModel);

        final ListView listView = view.findViewById(R.id.shooting_test_payments);
        listView.setAdapter(mAdapter);

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