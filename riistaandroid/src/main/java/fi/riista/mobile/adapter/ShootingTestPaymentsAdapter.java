package fi.riista.mobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;

import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.RiistaApplication;
import fi.riista.mobile.activity.ShootingTestEditPaymentActivity;
import fi.riista.mobile.models.shootingTest.ShootingTestAttemptSummary;
import fi.riista.mobile.models.shootingTest.ShootingTestParticipant;
import fi.riista.mobile.models.shootingTest.ShootingTestType;
import fi.riista.mobile.network.shootingTest.CompleteAllPaymentsForParticipantTask;
import fi.riista.mobile.ui.ShootingTestAttemptStateView;
import fi.riista.mobile.viewmodel.ShootingTestMainViewModel;

import static fi.riista.mobile.activity.ShootingTestEditPaymentActivity.EXTRA_PARTICIPANT_ID;
import static fi.riista.mobile.models.shootingTest.ShootingTestType.BEAR;
import static fi.riista.mobile.models.shootingTest.ShootingTestType.BOW;
import static fi.riista.mobile.models.shootingTest.ShootingTestType.MOOSE;
import static fi.riista.mobile.models.shootingTest.ShootingTestType.ROE_DEER;
import static fi.riista.mobile.ui.ShootingTestAttemptStateView.AttemptState;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class ShootingTestPaymentsAdapter extends ArrayAdapter<ShootingTestParticipant> {

    private final ShootingTestMainViewModel mViewModel;

    private boolean mCanEdit;

    private final View.OnClickListener mOnEditClickListener = v -> {
        final int position = (int) v.getTag();
        final ShootingTestParticipant participant = getItem(position);

        if (participant != null) {
            final Context context = getContext();
            final Intent intent = new Intent(context, ShootingTestEditPaymentActivity.class);
            intent.putExtra(EXTRA_PARTICIPANT_ID, participant.id);

            context.startActivity(intent);
        }
    };

    private final View.OnClickListener mOnDoneClickListener = v -> {
        final int position = (int) v.getTag();
        final ShootingTestParticipant participant = getItem(position);

        if (participant != null) {
            final Context context = getContext();
            final String msg = context.getString(R.string.shooting_test_payment_confirm_done,
                    participant.lastName, participant.firstName, participant.hunterNumber);

            new AlertDialog.Builder(context)
                    .setMessage(msg)
                    .setPositiveButton(R.string.yes, (dialog, which) -> completeAllPayments(participant))
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
    };

    public ShootingTestPaymentsAdapter(@NonNull final Context context,
                                       @NonNull final List<ShootingTestParticipant> data,
                                       @NonNull final ShootingTestMainViewModel viewModel) {

        super(context, R.layout.view_shooting_test_payment_item, data);

        mViewModel = requireNonNull(viewModel);
    }

    public void setEditEnabled(final boolean canEdit) {
        mCanEdit = canEdit;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.view_shooting_test_payment_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.participantTitle = convertView.findViewById(R.id.item_title_text);
            viewHolder.participantStatus = convertView.findViewById(R.id.participant_status);

            viewHolder.mooseAttemptsView = convertView.findViewById(R.id.attempts_moose_view);
            viewHolder.bearAttemptsView = convertView.findViewById(R.id.attempts_bear_view);
            viewHolder.roeDeerAttemptsView = convertView.findViewById(R.id.attempts_roedeer_view);
            viewHolder.bowAttemptsView = convertView.findViewById(R.id.attempts_bow_view);

            viewHolder.paymentTotal = convertView.findViewById(R.id.payment_total_amount);
            viewHolder.paymentPaid = convertView.findViewById(R.id.payment_paid_amount);
            viewHolder.paymentRemaining = convertView.findViewById(R.id.payment_remaining_amount);

            viewHolder.markDoneButton = convertView.findViewById(R.id.finish_payment_btn);
            viewHolder.markDoneButton.setOnClickListener(mOnDoneClickListener);

            viewHolder.editPaymentButton = convertView.findViewById(R.id.edit_payment_btn);
            viewHolder.editPaymentButton.setOnClickListener(mOnEditClickListener);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ShootingTestParticipant item = getItem(position);

        if (item != null) {
            viewHolder.participantTitle.setText(format("%s %s, %s", item.lastName, item.firstName, item.hunterNumber));
            viewHolder.participantStatus.setVisibility(item.completed ? View.GONE : View.VISIBLE);

            setupAttemptsView(viewHolder.mooseAttemptsView, item, R.string.shooting_test_type_moose_short, MOOSE);
            setupAttemptsView(viewHolder.bearAttemptsView, item, R.string.shooting_test_type_bear_short, BEAR);
            setupAttemptsView(viewHolder.roeDeerAttemptsView, item, R.string.shooting_test_type_roe_deer_short, ROE_DEER);
            setupAttemptsView(viewHolder.bowAttemptsView, item, R.string.shooting_test_type_bow_short, BOW);

            viewHolder.paymentTotal.setText(format("%S €", item.totalDueAmount));
            viewHolder.paymentPaid.setText(format("%s €", item.paidAmount));
            viewHolder.paymentRemaining.setText(format("%s €", item.remainingAmount));

            viewHolder.markDoneButton.setTag(position);
            viewHolder.markDoneButton.setEnabled(mCanEdit && !item.completed);

            viewHolder.editPaymentButton.setTag(position);
            viewHolder.editPaymentButton.setEnabled(mCanEdit);
        }

        return convertView;
    }

    private void setupAttemptsView(@NonNull final ShootingTestAttemptStateView view,
                                   @NonNull final ShootingTestParticipant item,
                                   @StringRes final int stringId,
                                   @NonNull final ShootingTestType attemptType) {

        final ShootingTestAttemptSummary attemptSummary = item.getAttemptSummaryFor(attemptType);

        final AttemptState state;
        final int numberOfAttempts;

        if (attemptSummary != null) {
            state = attemptSummary.qualified ? AttemptState.PASS : AttemptState.FAIL;
            numberOfAttempts = attemptSummary.attemptCount;
        } else {
            state = AttemptState.NONE;
            numberOfAttempts = 0;
        }

        view.setState(state, numberOfAttempts, getContext().getString(stringId));
    }

    private void completeAllPayments(@NonNull final ShootingTestParticipant participant) {
        final CompleteAllPaymentsForParticipantTask task = new CompleteAllPaymentsForParticipantTask(
                RiistaApplication.getInstance().getWorkContext(), participant.id, participant.rev) {

            @Override
            protected void onFinishText(final String text) {
                mViewModel.refreshCalendarEvent();
                mViewModel.refreshParticipants();
            }

            @Override
            protected void onError() {
                super.onError();

                final Context context = getContext();
                final String errorMsg = format(context.getString(R.string.error_operation_failed), getHttpStatusCode());

                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
            }
        };
        task.start();
    }

    private static class ViewHolder {
        TextView participantTitle;
        TextView participantStatus;
        ShootingTestAttemptStateView mooseAttemptsView;
        ShootingTestAttemptStateView bearAttemptsView;
        ShootingTestAttemptStateView roeDeerAttemptsView;
        ShootingTestAttemptStateView bowAttemptsView;
        TextView paymentTotal;
        TextView paymentPaid;
        TextView paymentRemaining;

        AppCompatImageButton markDoneButton;
        AppCompatImageButton editPaymentButton;
    }
}