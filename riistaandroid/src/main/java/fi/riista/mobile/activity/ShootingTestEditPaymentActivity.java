package fi.riista.mobile.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSpinner;

import java.util.ArrayList;
import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.models.shootingTest.ShootingTestParticipant;
import fi.riista.mobile.network.shootingTest.GetShootingTestParticipantSummaryTask;
import fi.riista.mobile.network.shootingTest.UpdatePaymentStateForParticipantTask;
import fi.vincit.androidutilslib.util.ViewAnnotations;

import static java.lang.String.format;

// TODO: Do not allow edits for completed events
public class ShootingTestEditPaymentActivity extends BaseActivity {

    public static final String EXTRA_PARTICIPANT_ID = "extra_participant_id";

    private long mParticipantId;
    private ShootingTestParticipant mParticipant;

    @ViewAnnotations.ViewId(R.id.participant_name)
    private TextView mParticipantName;

    @ViewAnnotations.ViewId(R.id.participant_hunter_number)
    private TextView mParticipantHunterNumber;

    @ViewAnnotations.ViewId(R.id.payments_total_amount)
    private TextView mPaymentsTotalAmount;

    @ViewAnnotations.ViewId(R.id.payments_paid_amount)
    private AppCompatSpinner mPaymentsPaidAmount;

    @ViewAnnotations.ViewId(R.id.payments_remaining_amount)
    private TextView mPaymentsRemainingAmount;

    @ViewAnnotations.ViewId(R.id.test_finished)
    private AppCompatCheckBox mTestFinished;

    @ViewAnnotations.ViewId(R.id.cancel_btn)
    private Button mCancelButton;

    @ViewAnnotations.ViewId(R.id.save_btn)
    private Button mSaveButton;

    private ArrayAdapter<String> mPaidAmountAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shooting_test_edit_payment);
        setCustomTitle(getString(R.string.shooting_test));

        ViewAnnotations.apply(this);

        final Intent intent = getIntent();
        mParticipantId = intent.getLongExtra(EXTRA_PARTICIPANT_ID, -1);

        mPaidAmountAdapter = new AmountPaidSpinnerAdapter(this, android.R.layout.simple_spinner_item);
        mPaidAmountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mPaymentsPaidAmount.setAdapter(mPaidAmountAdapter);
        mPaymentsPaidAmount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onItemSelected(final AdapterView<?> adapterView, final View view, final int i, final long l) {
                if (mParticipant != null) {
                    final int totalDueAmount = mParticipant.totalDueAmount;
                    final int paymentAmountChoice = i * 20;

                    mPaymentsRemainingAmount.setText(format("%d €", totalDueAmount - paymentAmountChoice));
                    mTestFinished.setChecked(paymentAmountChoice == totalDueAmount);
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> adapterView) {
            }
        });

        mCancelButton.setOnClickListener(v -> onBackPressed());

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mParticipant != null) {
                    final UpdatePaymentStateForParticipantTask task = new UpdatePaymentStateForParticipantTask(
                            getWorkContext(),
                            mParticipant.id,
                            mParticipant.rev,
                            mPaymentsPaidAmount.getSelectedItemPosition(),
                            mTestFinished.isChecked()) {

                        @Override
                        protected void onFinishText(final String text) {
                            onBackPressed();
                        }

                        @Override
                        protected void onError() {
                            super.onError();

                            final String errorMsg = format(getString(R.string.error_operation_failed), getHttpStatusCode());

                            showToast(errorMsg);
                        }
                    };
                    task.start();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshData();
    }

    private void refreshData() {
        if (mParticipantId >= 0) {
            final GetShootingTestParticipantSummaryTask task = new GetShootingTestParticipantSummaryTask(getWorkContext(), mParticipantId) {
                @Override
                protected void onFinishObject(final ShootingTestParticipant result) {
                    mParticipant = result;
                    updateUiFields();
                }

                @Override
                protected void onError() {
                    super.onError();

                    // TODO Commented out in order to not show ordinary error message in case session is expired.
                    //  Fix by initiating a new session.
                    //final String errorMsg = format(getString(R.string.error_operation_failed), getHttpStatusCode());
                    //showToast(errorMsg);
                }
            };
            task.start();
        }
    }

    private void updateUiFields() {
        mParticipantName.setText(format("%s %s", mParticipant.firstName, mParticipant.lastName));
        mParticipantHunterNumber.setText(mParticipant.hunterNumber);
        mPaymentsTotalAmount.setText(format("%s €", mParticipant.totalDueAmount));
        mPaymentsRemainingAmount.setText(format("%s €", mParticipant.remainingAmount));
        mTestFinished.setSelected(mParticipant.paidAmount == mParticipant.totalDueAmount);

        final List<String> values = new ArrayList<>();
        for (int i = 0; i <= mParticipant.totalDueAmount; i += 20) {
            values.add(i + " €");
        }

        mPaidAmountAdapter.clear();
        mPaidAmountAdapter.addAll(values);
        mPaidAmountAdapter.notifyDataSetChanged();
        mPaymentsPaidAmount.setSelection(mParticipant.paidAmount / 20);
    }


    private void showToast(final String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private static class AmountPaidSpinnerAdapter extends ArrayAdapter<String> {
        AmountPaidSpinnerAdapter(@NonNull final Context context, final int resource) {
            super(context, resource);
        }
    }
}
