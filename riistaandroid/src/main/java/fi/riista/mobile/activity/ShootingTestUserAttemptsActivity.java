package fi.riista.mobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import java.util.ArrayList;

import fi.riista.mobile.R;
import fi.riista.mobile.RiistaApplication;
import fi.riista.mobile.adapter.ShootingTestUserAttempsAdapter;
import fi.riista.mobile.models.shootingTest.ShootingTestAttemptDetailed;
import fi.riista.mobile.models.shootingTest.ShootingTestParticipantDetailed;
import fi.riista.mobile.models.shootingTest.ShootingTestType;
import fi.riista.mobile.network.shootingTest.GetShootingTestParticipantDetailedTask;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.vincit.androidutilslib.util.ViewAnnotations;

import static fi.riista.mobile.activity.ShootingTestEditAttemptActivity.EXTRA_ATTEMPT;
import static fi.riista.mobile.activity.ShootingTestEditAttemptActivity.EXTRA_ENABLE_BEAR;
import static fi.riista.mobile.activity.ShootingTestEditAttemptActivity.EXTRA_ENABLE_BOW;
import static fi.riista.mobile.activity.ShootingTestEditAttemptActivity.EXTRA_ENABLE_MOOSE;
import static fi.riista.mobile.activity.ShootingTestEditAttemptActivity.EXTRA_ENABLE_ROEDEER;
import static fi.riista.mobile.activity.ShootingTestEditAttemptActivity.EXTRA_PARTICIPANT_NAME;
import static fi.riista.mobile.activity.ShootingTestEditAttemptActivity.EXTRA_PARTICIPANT_REV;
import static fi.riista.mobile.models.shootingTest.ShootingTestType.BEAR;
import static fi.riista.mobile.models.shootingTest.ShootingTestType.BOW;
import static fi.riista.mobile.models.shootingTest.ShootingTestType.MOOSE;
import static fi.riista.mobile.models.shootingTest.ShootingTestType.ROE_DEER;
import static java.lang.String.format;

public class ShootingTestUserAttemptsActivity extends BaseActivity {

    public static final String EXTRA_PARTICIPANT_ID = "extra_participant_id";
    public static final String EXTRA_TEST_COMPLETED = "extra_test_finished";

    private ShootingTestParticipantDetailed mParticipant;
    private long mParticipantId;
    private boolean mTestCompleted;
    private ShootingTestUserAttempsAdapter mAdapter;

    @ViewAnnotations.ViewId(R.id.participant_name)
    private TextView mParticipantName;

    @ViewAnnotations.ViewId(R.id.participant_hunter_number)
    private TextView mParticipantHunterNumber;

    @ViewAnnotations.ViewId(R.id.participant_date_of_birth)
    private TextView mParticipantDateOfBirth;

    @ViewAnnotations.ViewId(R.id.list_participant_attempts)
    private ListView mAttemptsListView;

    @ViewAnnotations.ViewId(R.id.add_attempt_button)
    private AppCompatButton mAddAttemptButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shooting_test_user_attempts);

        ViewAnnotations.apply(this);

        mParticipantId = getIntent().getLongExtra(EXTRA_PARTICIPANT_ID, -1);
        mTestCompleted = getIntent().getBooleanExtra(EXTRA_TEST_COMPLETED, false);

        mAdapter = new ShootingTestUserAttempsAdapter(new ArrayList<>(), mParticipantId, mTestCompleted, this);
        mAttemptsListView.setAdapter(mAdapter);

        mAddAttemptButton.setEnabled(!mTestCompleted);

        setCustomTitle(getString(R.string.shooting_test_participant_attemps));
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshData();
    }

    public void refreshData() {
        final GetShootingTestParticipantDetailedTask task =
                new GetShootingTestParticipantDetailedTask(RiistaApplication.getInstance().getWorkContext(), mParticipantId) {

            @Override
            protected void onFinishObject(final ShootingTestParticipantDetailed result) {
                mParticipant = result;

                mParticipantName.setText(format("%s %s", mParticipant.lastName, mParticipant.firstName));
                mParticipantHunterNumber.setText(mParticipant.hunterNumber);
                mParticipantDateOfBirth.setText(DateTimeUtils.convertDateStringToFinnishFormat(mParticipant.dateOfBirth));

                mAdapter.setParticipant(mParticipant.id, mParticipant.rev, format("%s %s", mParticipant.lastName, mParticipant.firstName), mTestCompleted);

                mAdapter.clear();
                mAdapter.addAll(mParticipant.attempts);
                mAdapter.notifyDataSetChanged();

                mAddAttemptButton.setEnabled(true);
            }

            @Override
            protected void onError() {
                super.onError();
            }
        };
        task.start();
    }

    @ViewAnnotations.ViewOnClick(R.id.add_attempt_button)
    protected void onCreateAttemptClicked(final View view) {
        if (!mTestCompleted && mParticipant != null) {

            final Intent intent = new Intent(this, ShootingTestEditAttemptActivity.class);
            intent.putExtra(EXTRA_ATTEMPT, new ShootingTestAttemptDetailed());
            intent.putExtra(EXTRA_PARTICIPANT_ID, mParticipant.id);
            intent.putExtra(EXTRA_PARTICIPANT_REV, mParticipant.rev);
            intent.putExtra(EXTRA_PARTICIPANT_NAME, format("%s %s", mParticipant.lastName, mParticipant.firstName));

            setEditTypeLimitsTo(intent, null);

            startActivity(intent);
        }
    }

    public void setEditTypeLimitsTo(final Intent intent, final ShootingTestType testType) {
        int bearCount = 0;
        int mooseCount = 0;
        int roeDeerCount = 0;
        int bowCount = 0;

        for (final ShootingTestAttemptDetailed item : mParticipant.attempts) {
            if (BEAR == item.type) {
                bearCount++;
            } else if (MOOSE == item.type) {
                mooseCount++;
            } else if (ROE_DEER == item.type) {
                roeDeerCount++;
            } else if (BOW == item.type) {
                bowCount++;
            }
        }

        intent.putExtra(EXTRA_ENABLE_BEAR, bearCount < 5 || bearCount == 5 && BEAR == testType);
        intent.putExtra(EXTRA_ENABLE_MOOSE, mooseCount < 5 || mooseCount == 5 && MOOSE == testType);
        intent.putExtra(EXTRA_ENABLE_ROEDEER, roeDeerCount < 5 || roeDeerCount == 5 && ROE_DEER == testType);
        intent.putExtra(EXTRA_ENABLE_BOW, bowCount < 5 || bowCount == 5 && BOW == testType);
    }
}
