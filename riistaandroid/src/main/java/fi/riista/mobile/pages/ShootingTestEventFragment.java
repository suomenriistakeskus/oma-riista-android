package fi.riista.mobile.pages;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.android.support.AndroidSupportInjection;
import fi.riista.mobile.R;
import fi.riista.mobile.models.shootingTest.ShootingTestCalendarEvent;
import fi.riista.mobile.models.shootingTest.ShootingTestOfficial;
import fi.riista.mobile.network.shootingTest.CloseShootingTestEventTask;
import fi.riista.mobile.network.shootingTest.ReopenShootingTestEventTask;
import fi.riista.mobile.network.shootingTest.StartShootingTestEventTask;
import fi.riista.mobile.network.shootingTest.UpdateShootingTestOfficialsTask;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.viewmodel.ShootingTestMainViewModel;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.util.ViewAnnotations;

import static fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME;
import static java.lang.String.format;

public class ShootingTestEventFragment extends ShootingTestTabContentFragment {

    @Inject
    @Named(APPLICATION_WORK_CONTEXT_NAME)
    WorkContext mAppWorkContext;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private ShootingTestMainViewModel mModel;
    private ShootingTestCalendarEvent mEvent;

    private List<ShootingTestOfficial> mSelectedOfficials = new ArrayList<>();
    private List<ShootingTestOfficial> mAvailableOfficials = new ArrayList<>();
    private List<ShootingTestOfficial> mSelectedOfficialsMaster = new ArrayList<>();
    private List<ShootingTestOfficial> mAvailableOfficialsMaster = new ArrayList<>();

    private boolean mIsEditing = false;
    private boolean mHasSelectedOfficials = false;
    private boolean mHasAvailableOfficials = false;

    @ViewAnnotations.ViewId(R.id.shooting_test_event_title)
    private TextView mEventTitle;

    @ViewAnnotations.ViewId(R.id.shooting_test_event_details)
    private TextView mEventDetails;

    @ViewAnnotations.ViewId(R.id.shooting_test_event_sum_of_payments)
    private TextView mEventSumOfPayments;

    @ViewAnnotations.ViewId(R.id.shooting_test_selected_officials)
    private LinearLayout mSelectedOfficialsView;

    @ViewAnnotations.ViewId(R.id.shooting_test_available_officials)
    private LinearLayout mAvailableOfficialsView;

    @ViewAnnotations.ViewId(R.id.shooting_test_start_event_btn)
    private Button mStartEventBtn;

    @ViewAnnotations.ViewId(R.id.shooting_test_edit_event_btn)
    private Button mEditEventBtn;

    @ViewAnnotations.ViewId(R.id.shooting_test_finish_event_btn)
    private Button mCloseEventBtn;

    @ViewAnnotations.ViewId(R.id.shooting_test_reopen_event_btn)
    private Button mReopenEventBtn;

    @ViewAnnotations.ViewId(R.id.edit_button_view)
    private ViewGroup mEditButtonView;

    @ViewAnnotations.ViewId(R.id.save_btn)
    private AppCompatButton mSaveButton;

    private View.OnClickListener mOnRemoveClickListener = view -> {
        if (mIsEditing || mEvent != null && mEvent.isWaitingToStart()) {
            final long personId = (long) view.getTag();

            for (final ShootingTestOfficial official : mSelectedOfficials) {
                if (official.personId.equals(personId)) {
                    mAvailableOfficials.add(official);
                    mSelectedOfficials.remove(official);
                    break;
                }
            }

            populateListView(mSelectedOfficialsView, mSelectedOfficials, true, mIsEditing);
            populateListView(mAvailableOfficialsView, mAvailableOfficials, false, mIsEditing);
        }
        refreshButtonStates();
    };

    private View.OnClickListener mOnAddClickListener = view -> {
        if (mIsEditing || mEvent != null && mEvent.isWaitingToStart()) {
            final long personId = (long) view.getTag();

            for (final ShootingTestOfficial official : mAvailableOfficials) {
                if (official.personId.equals(personId)) {
                    mSelectedOfficials.add(official);
                    mAvailableOfficials.remove(official);
                    break;
                }
            }

            populateListView(mSelectedOfficialsView, mSelectedOfficials, true, mIsEditing);
            populateListView(mAvailableOfficialsView, mAvailableOfficials, false, mIsEditing);
        }
        refreshButtonStates();
    };

    public static ShootingTestEventFragment newInstance() {
        final ShootingTestEventFragment fragment = new ShootingTestEventFragment();
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

        mModel.getSelectedOfficials().observe(this, selectedOfficials -> {
            mSelectedOfficialsMaster.clear();

            if (selectedOfficials != null) {
                mSelectedOfficialsMaster.addAll(selectedOfficials);
            }

            mSelectedOfficials.clear();
            if (mSelectedOfficialsMaster != null) {
                mHasSelectedOfficials = true;
                mSelectedOfficials.addAll(mSelectedOfficialsMaster);

                filterAvailableOfficials(mAvailableOfficials);
                refreshButtonStates();
            }

            mSelectedOfficialsView.removeAllViews();
            populateListView(mSelectedOfficialsView, mSelectedOfficials, true, mIsEditing);
        });

        mModel.getAvailableOfficials().observe(this, availableOfficials -> {
            mAvailableOfficialsMaster.clear();

            if (availableOfficials != null) {
                mAvailableOfficialsMaster.addAll(availableOfficials);
            }

            mAvailableOfficials.clear();
            if (mAvailableOfficialsMaster != null) {
                mHasAvailableOfficials = true;

                filterAvailableOfficials(mAvailableOfficialsMaster);
                refreshButtonStates();
            }

            mAvailableOfficialsView.removeAllViews();
            populateListView(mAvailableOfficialsView, mAvailableOfficials, false, mIsEditing);
        });

        mModel.getCalendarEvent().observe(this, calendarEvent -> {
            if (calendarEvent != null) {
                mEvent = calendarEvent;
                setEditingOfficials(false);

                final boolean waitingtoStart = calendarEvent.isWaitingToStart();

                populateListView(mAvailableOfficialsView, mAvailableOfficials, false, waitingtoStart);
                populateListView(mSelectedOfficialsView, mSelectedOfficials, true, waitingtoStart);

                updateTitle(calendarEvent);
                updateButtonVisibility(calendarEvent);

                mAvailableOfficialsView.setVisibility(waitingtoStart ? View.VISIBLE : View.GONE);

                if (waitingtoStart) {
                    mHasSelectedOfficials = true;
                    filterAvailableOfficials(mAvailableOfficialsMaster);
                }
                refreshOfficialsData();
            }
        });
    }

    private void filterAvailableOfficials(final List<ShootingTestOfficial> availableOfficials) {
        if (mHasSelectedOfficials && mHasAvailableOfficials) {
            final List<ShootingTestOfficial> availableAndNotSelected = new ArrayList<>(availableOfficials);

            mAvailableOfficials.clear();

            for (final ShootingTestOfficial selected : mSelectedOfficials) {
                for (final ShootingTestOfficial available : availableOfficials)
                    if (available.personId.equals(selected.personId)) {
                        availableAndNotSelected.remove(available);
                    }
            }

            mAvailableOfficials.addAll(availableAndNotSelected);
        }
    }

    private void refreshButtonStates() {
        mEditEventBtn.setEnabled(mHasAvailableOfficials && mHasSelectedOfficials);
        mStartEventBtn.setEnabled(mSelectedOfficials.size() >= 2);
        mSaveButton.setEnabled(mSelectedOfficials.size() >= 2);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_shooting_test_event, container, false);

        ViewAnnotations.apply(this, view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshEventData();
    }

    @ViewAnnotations.ViewOnClick(R.id.shooting_test_start_event_btn)
    protected void onStartClick(final View view) {
        final List<Long> officialIds = new ArrayList<>(mSelectedOfficials.size());
        for (final ShootingTestOfficial official : mSelectedOfficials) {
            officialIds.add(official.occupationId);
        }

        final Long testEventId = mModel.getTestEventId();
        if (officialIds.size() >= 2 && testEventId == null) {
            startEvent(officialIds, mModel.getCalendarEventId(), testEventId);
        }
    }

    @ViewAnnotations.ViewOnClick(R.id.shooting_test_edit_event_btn)
    protected void onEditClick(final View view) {
        setEditingOfficials(true);
    }

    @ViewAnnotations.ViewOnClick(R.id.shooting_test_finish_event_btn)
    protected void onCloseClick(final View view) {
        new AlertDialog.Builder(mActivity)
                .setMessage(mActivity.getString(R.string.confirm_operation_prompt))
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    if (mModel.getTestEventId() != null) {
                        closeEvent(mModel.getTestEventId());
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @ViewAnnotations.ViewOnClick(R.id.shooting_test_reopen_event_btn)
    protected void onReopenClick(final View view) {
        new AlertDialog.Builder(mActivity)
                .setMessage(mActivity.getString(R.string.confirm_operation_prompt))
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    if (mModel.getTestEventId() != null) {
                        reopenEvent(mModel.getTestEventId());
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @ViewAnnotations.ViewOnClick(R.id.cancel_btn)
    protected void onCancelEditOfficialsClick(final View view) {
        if (mIsEditing) {
            setEditingOfficials(false);

            mSelectedOfficials.clear();
            if (mSelectedOfficialsMaster != null) {
                mSelectedOfficials.addAll(mSelectedOfficialsMaster);
            }
            populateListView(mSelectedOfficialsView, mSelectedOfficials, true, mIsEditing);

            mAvailableOfficials.clear();
            if (mAvailableOfficialsMaster != null) {
                filterAvailableOfficials(mAvailableOfficialsMaster);
            }
            populateListView(mAvailableOfficialsView, mAvailableOfficials, false, mIsEditing);
        }
    }

    @ViewAnnotations.ViewOnClick(R.id.save_btn)
    protected void onSaveOfficialEditClick(final View view) {
        if (mIsEditing) {
            final List<Long> officialIds = new ArrayList<>(mSelectedOfficials.size());
            for (final ShootingTestOfficial official : mSelectedOfficials) {
                officialIds.add(official.occupationId);
            }

            final Long testEventId = mModel.getTestEventId();
            if (officialIds.size() >= 2 && testEventId != null) {
                updateEventOfficials(officialIds, mModel.getCalendarEventId(), testEventId);
            }
        }
    }

    private void startEvent(@NonNull final List<Long> officialIds,
                            @NonNull final Long calendarEventId,
                            final Long testEventId) {

        final StartShootingTestEventTask task =
                new StartShootingTestEventTask(mAppWorkContext, calendarEventId, testEventId, officialIds) {

            @Override
            protected void onFinishText(final String text) {
                refreshEventData();
            }

            @Override
            protected void onError() {
                super.onError();

                final String errorMsg = format(getString(R.string.error_operation_failed), getHttpStatusCode());

                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        };
        task.start();
    }

    private void updateEventOfficials(final List<Long> officialIds,
                                      @NonNull final Long calendarEventId,
                                      @NonNull final Long testEventId) {

        final UpdateShootingTestOfficialsTask task =
                new UpdateShootingTestOfficialsTask(mAppWorkContext, calendarEventId, testEventId, officialIds) {

            @Override
            protected void onFinishText(final String text) {
                setEditingOfficials(false);
                refreshEventData();
            }

            @Override
            protected void onError() {
                super.onError();

                final String errorMsg = format(getString(R.string.error_operation_failed), getHttpStatusCode());

                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        };
        task.start();
    }

    private void closeEvent(@NonNull final Long eventId) {
        final CloseShootingTestEventTask task = new CloseShootingTestEventTask(mAppWorkContext, eventId) {
            @Override
            protected void onFinishText(final String text) {
                refreshEventData();
            }

            @Override
            protected void onError() {
                super.onError();

                final String errorMsg = format(getString(R.string.error_operation_failed), getHttpStatusCode());

                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        };
        task.start();
    }

    private void reopenEvent(@NonNull final Long eventId) {
        final ReopenShootingTestEventTask task = new ReopenShootingTestEventTask(mAppWorkContext, eventId) {
            @Override
            protected void onFinishText(final String text) {
                refreshEventData();
            }

            @Override
            protected void onError() {
                super.onError();

                final String errorMsg = format(getString(R.string.error_operation_failed), getHttpStatusCode());

                Toast.makeText(mActivity, errorMsg, Toast.LENGTH_LONG).show();
            }
        };
        task.start();
    }

    private void updateTitle(final ShootingTestCalendarEvent event) {
        final String localisedDateText = DateTimeUtils.convertDateStringToFinnishFormat(event.date);

        mEventTitle.setText(format("%s %s\n%s %s %s",
                ShootingTestCalendarEvent.localisedEventTypeText(getContext(), event.calendarEventType).toUpperCase(),
                TextUtils.isEmpty(event.name) ? "" : event.name,
                localisedDateText,
                event.beginTime,
                TextUtils.isEmpty(event.endTime) ? "" : "- " + event.endTime));
        mEventDetails.setText(format("%s\n%s\n%s",
                event.venue.name != null ? event.venue.name : "",
                event.venue.address.streetAddress != null ? event.venue.address.streetAddress : "",
                event.venue.address.city != null ? event.venue.address.city : ""));
        mEventSumOfPayments.setText(format(getString(R.string.shooting_test_event_sum_of_payments), event.totalPaidAmount));
    }

    private void updateButtonVisibility(final ShootingTestCalendarEvent event) {
        final Boolean isUserSelectedAsOfficial =
                mModel.isUserSelectedOfficial() != null ? mModel.isUserSelectedOfficial().getValue() : false;
        final Boolean isUserCoordinator =
                mModel.isUserCoordinator() != null ? mModel.isUserCoordinator().getValue() : false;

        mStartEventBtn.setVisibility(event.isWaitingToStart() ? View.VISIBLE : View.GONE);
        mCloseEventBtn.setVisibility(!mIsEditing && event.isReadyToClose() && (Boolean.TRUE.equals(isUserSelectedAsOfficial) || Boolean.TRUE.equals(isUserCoordinator)) ? View.VISIBLE : View.GONE);
        mReopenEventBtn.setVisibility(!mIsEditing && event.isClosed() ? View.VISIBLE : View.GONE);
        mEditEventBtn.setVisibility(!mIsEditing && event.isOngoing() ? View.VISIBLE : View.GONE);
    }

    private void setEditingOfficials(final boolean enabled) {
        mIsEditing = enabled;
        mEditButtonView.setVisibility(mIsEditing ? View.VISIBLE : View.GONE);

        populateListView(mSelectedOfficialsView, mSelectedOfficials, true, enabled);
        populateListView(mAvailableOfficialsView, mAvailableOfficials, false, enabled);

        mAvailableOfficialsView.setVisibility(mIsEditing ? View.VISIBLE : View.GONE);
        updateButtonVisibility(mEvent);
    }

    private void refreshEventData() {
        if (mModel != null) {
            mModel.refreshCalendarEvent();
            mModel.refreshParticipants();
        }
    }

    private void refreshOfficialsData() {
        mModel.refreshSelectedOfficials();
        mModel.refreshAvailableOfficials();
    }

    protected void populateListView(final ViewGroup parent,
                                    final List<ShootingTestOfficial> data,
                                    final boolean isSelected,
                                    final boolean isEdit) {
        parent.removeAllViews();

        for (final ShootingTestOfficial item : data) {
            parent.addView(createOfficialView(item, parent, isSelected, isEdit || (mEvent != null && mEvent.isWaitingToStart())));
        }
    }

    protected View createOfficialView(final ShootingTestOfficial item,
                                      final ViewGroup parent,
                                      final boolean isSelected,
                                      final boolean isEdit) {

        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.view_shooting_official_item, parent, false);

        final TextView nameLabel = view.findViewById(R.id.official_name);
        nameLabel.setText(format("%s %s", item.lastName, item.firstName));

        if (isEdit) {
            if (isSelected) {
                final Button removeButton = view.findViewById(R.id.remove_official_btn);
                removeButton.setOnClickListener(mOnRemoveClickListener);
                removeButton.setVisibility(View.VISIBLE);
                removeButton.setTag(item.personId);
            } else {
                final Button addButton = view.findViewById(R.id.add_official_btn);
                addButton.setOnClickListener(mOnAddClickListener);
                addButton.setVisibility(View.VISIBLE);
                addButton.setTag(item.personId);
            }
        }

        return view;
    }

    @Override
    public void onTabSelected() {
        refreshEventData();
    }

    @Override
    public void onTabDeselected() {
        setEditingOfficials(false);
    }
}
