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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;

import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.RiistaApplication;
import fi.riista.mobile.activity.ShootingTestEditAttemptActivity;
import fi.riista.mobile.activity.ShootingTestUserAttemptsActivity;
import fi.riista.mobile.models.shootingTest.ShootingTestAttemptDetailed;
import fi.riista.mobile.network.shootingTest.DeleteAttemptForParticipantTask;

import static fi.riista.mobile.activity.ShootingTestEditAttemptActivity.EXTRA_ATTEMPT;
import static fi.riista.mobile.activity.ShootingTestEditAttemptActivity.EXTRA_ATTEMPT_ID;
import static fi.riista.mobile.activity.ShootingTestEditAttemptActivity.EXTRA_PARTICIPANT_ID;
import static fi.riista.mobile.activity.ShootingTestEditAttemptActivity.EXTRA_PARTICIPANT_NAME;
import static fi.riista.mobile.activity.ShootingTestEditAttemptActivity.EXTRA_PARTICIPANT_REV;

public class ShootingTestUserAttempsAdapter extends ArrayAdapter<ShootingTestAttemptDetailed> {

    private ShootingTestUserAttemptsActivity mContext;

    private long mParticipantId;
    private int mParticipantRev;
    private String mParticipantName;
    private boolean canEdit;

    private View.OnClickListener mOnEditClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            final int position = (int) v.getTag();
            final ShootingTestAttemptDetailed clickedItem = getItem(position);

            if (clickedItem != null) {
                final Intent intent = new Intent(mContext, ShootingTestEditAttemptActivity.class);
                intent.putExtra(EXTRA_ATTEMPT, clickedItem);
                intent.putExtra(EXTRA_ATTEMPT_ID, clickedItem.id);
                intent.putExtra(EXTRA_PARTICIPANT_ID, mParticipantId);
                intent.putExtra(EXTRA_PARTICIPANT_REV, mParticipantRev);
                intent.putExtra(EXTRA_PARTICIPANT_NAME, mParticipantName);

                mContext.setEditTypeLimitsTo(intent, clickedItem.type);

                mContext.startActivity(intent);
            }
        }
    };
    private View.OnClickListener mOnDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            final int position = (int) v.getTag();
            final ShootingTestAttemptDetailed clickedItem = getItem(position);

            if (clickedItem != null) {
                new AlertDialog.Builder(mContext)
                        .setMessage(mContext.getString(R.string.confirm_delete_attempt))
                        .setPositiveButton(R.string.yes, (dialog, which) -> deleteAttempt(clickedItem.id))
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
        }
    };

    public ShootingTestUserAttempsAdapter(final List<ShootingTestAttemptDetailed> data,
                                          final long participantId,
                                          final boolean completed,
                                          @NonNull final ShootingTestUserAttemptsActivity context) {

        super(context, R.layout.view_shooting_test_attempt_item, data);

        mContext = context;
        mParticipantId = participantId;
        canEdit = !completed;
    }

    public void setParticipant(final long participantId,
                               final int participantRev,
                               final String participantName,
                               final boolean completed) {

        mParticipantId = participantId;
        mParticipantRev = participantRev;
        mParticipantName = participantName;
        canEdit = !completed;
    }

    private void deleteAttempt(final long attemptId) {
        final DeleteAttemptForParticipantTask task = new DeleteAttemptForParticipantTask(
                RiistaApplication.getInstance().getWorkContext(), attemptId) {

            @Override
            protected void onFinishText(final String text) {
                mContext.refreshData();
            }

            @Override
            protected void onError() {
                super.onError();

                final String errorMsg =
                        String.format(mContext.getString(R.string.error_operation_failed), getHttpStatusCode());

                Toast.makeText(mContext, errorMsg, Toast.LENGTH_LONG).show();
            }
        };
        task.start();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
        final Context context = getContext();
        final ViewHolder viewHolder;

        if (convertView == null) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.view_shooting_test_attempt_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.attemptType = convertView.findViewById(R.id.attempt_type);
            viewHolder.result = convertView.findViewById(R.id.attempt_result);
            viewHolder.hits = convertView.findViewById(R.id.attempt_hits);

            viewHolder.editButton = convertView.findViewById(R.id.edit_button);
            viewHolder.deleteButton = convertView.findViewById(R.id.delete_button);

            viewHolder.editButton.setOnClickListener(mOnEditClickListener);
            viewHolder.deleteButton.setOnClickListener(mOnDeleteClickListener);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ShootingTestAttemptDetailed item = getItem(position);

        if (item != null) {
            viewHolder.attemptType.setText(ShootingTestAttemptDetailed.localizedTypeText(context, item.type));
            viewHolder.result.setText(ShootingTestAttemptDetailed.localisedResultText(context, item.result));
            viewHolder.hits.setText(String.valueOf(item.hits));

            viewHolder.editButton.setEnabled(canEdit);
            viewHolder.editButton.setTag(position);

            viewHolder.deleteButton.setEnabled(canEdit);
            viewHolder.deleteButton.setTag(position);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView attemptType;
        TextView result;
        TextView hits;
        AppCompatImageButton editButton;
        AppCompatImageButton deleteButton;
    }
}
