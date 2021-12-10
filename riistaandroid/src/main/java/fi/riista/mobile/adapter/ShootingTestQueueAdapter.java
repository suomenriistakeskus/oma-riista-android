package fi.riista.mobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.models.shootingTest.ShootingTestAttemptSummary;
import fi.riista.mobile.models.shootingTest.ShootingTestParticipant;
import fi.riista.mobile.models.shootingTest.ShootingTestType;
import fi.riista.mobile.ui.ShootingTestAttemptStateView;

import static fi.riista.mobile.models.shootingTest.ShootingTestType.BEAR;
import static fi.riista.mobile.models.shootingTest.ShootingTestType.BOW;
import static fi.riista.mobile.models.shootingTest.ShootingTestType.MOOSE;
import static fi.riista.mobile.models.shootingTest.ShootingTestType.ROE_DEER;
import static fi.riista.mobile.ui.ShootingTestAttemptStateView.AttemptState;
import static java.lang.String.format;

public class ShootingTestQueueAdapter extends ArrayAdapter<ShootingTestParticipant> {

    public ShootingTestQueueAdapter(final List<ShootingTestParticipant> data, final Context context) {
        super(context, R.layout.view_shooting_test_participant, data);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.view_shooting_test_participant, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.participantTitle = convertView.findViewById(R.id.participant_title);
            viewHolder.mooseTestState = convertView.findViewById(R.id.participant_test_type_moose_short);
            viewHolder.bearTestState = convertView.findViewById(R.id.participant_test_type_bear_short);
            viewHolder.roeDeerTestState = convertView.findViewById(R.id.participant_test_type_roedeer_short);
            viewHolder.bowTestState = convertView.findViewById(R.id.participant_test_type_bow_short);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ShootingTestParticipant item = getItem(position);

        if (item != null) {
            viewHolder.participantTitle.setText(format("%s %s, %s", item.lastName, item.firstName, item.hunterNumber));

            setAttemptsView(viewHolder.mooseTestState, item, R.string.shooting_test_type_moose_short, MOOSE);
            setAttemptsView(viewHolder.bearTestState, item, R.string.shooting_test_type_bear_short, BEAR);
            setAttemptsView(viewHolder.roeDeerTestState, item, R.string.shooting_test_type_roe_deer_short, ROE_DEER);
            setAttemptsView(viewHolder.bowTestState, item, R.string.shooting_test_type_bow_short, BOW);
        }

        return convertView;
    }

    private void setAttemptsView(@NonNull final ShootingTestAttemptStateView view,
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
            final boolean intended = MOOSE == attemptType && item.mooseTestIntended
                    || BEAR == attemptType && item.bearTestIntended
                    || ROE_DEER == attemptType && item.deerTestIntended
                    || BOW == attemptType && item.bowTestIntended;

            state = intended ? AttemptState.INTENDED : AttemptState.NONE;
            numberOfAttempts = 0;
        }

        view.setState(state, numberOfAttempts, getContext().getString(stringId));
    }

    private static class ViewHolder {
        TextView participantTitle;
        ShootingTestAttemptStateView mooseTestState;
        ShootingTestAttemptStateView bearTestState;
        ShootingTestAttemptStateView roeDeerTestState;
        ShootingTestAttemptStateView bowTestState;
    }
}
