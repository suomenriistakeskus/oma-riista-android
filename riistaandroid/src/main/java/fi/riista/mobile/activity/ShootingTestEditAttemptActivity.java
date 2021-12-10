package fi.riista.mobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;

import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.models.shootingTest.ShootingTestAttemptDetailed;
import fi.riista.mobile.models.shootingTest.ShootingTestResult;
import fi.riista.mobile.models.shootingTest.ShootingTestType;
import fi.riista.mobile.network.shootingTest.AddAttemptForParticipantTask;
import fi.riista.mobile.network.shootingTest.GetShootingTestAttempt;
import fi.riista.mobile.network.shootingTest.UpdateAttemptForParticipantTask;
import fi.vincit.androidutilslib.util.ViewAnnotations;

import static fi.riista.mobile.models.shootingTest.ShootingTestResult.QUALIFIED;
import static fi.riista.mobile.models.shootingTest.ShootingTestResult.REBATED;
import static fi.riista.mobile.models.shootingTest.ShootingTestResult.TIMED_OUT;
import static fi.riista.mobile.models.shootingTest.ShootingTestResult.UNQUALIFIED;
import static fi.riista.mobile.models.shootingTest.ShootingTestType.BEAR;
import static fi.riista.mobile.models.shootingTest.ShootingTestType.BOW;
import static fi.riista.mobile.models.shootingTest.ShootingTestType.MOOSE;
import static fi.riista.mobile.models.shootingTest.ShootingTestType.ROE_DEER;
import static java.lang.String.format;
import static java.util.Arrays.asList;

// TODO: Do not allow edits for completed event

public class ShootingTestEditAttemptActivity extends BaseActivity {

    public static final String EXTRA_ATTEMPT = "extra_attempt";
    public static final String EXTRA_ATTEMPT_ID = "extra_attempt_id";
    public static final String EXTRA_PARTICIPANT_NAME = "extra_participant_name";
    public static final String EXTRA_PARTICIPANT_ID = "extra_participant_id";
    public static final String EXTRA_PARTICIPANT_REV = "extra_participant_rev";
    public static final String EXTRA_ENABLE_BEAR = "extra_enable_bear";
    public static final String EXTRA_ENABLE_MOOSE = "extra_enable_moose";
    public static final String EXTRA_ENABLE_ROEDEER = "extra_enable_roedeer";
    public static final String EXTRA_ENABLE_BOW = "extra_enable_bow";

    private static final int SPINNER_PASS_FAIL_INDEX = 0;
    private static final int SPINNER_OVERTIME_INDEX = 1;
    private static final int SPINNER_REBATED_INDEX = 2;

    private boolean mEnableBear = true;
    private boolean mEnableMoose = true;
    private boolean mEnableRoeDeer = true;
    private boolean mEnableBow = true;

    private long mAttemptId;
    private long mParticipantId;
    private int mParticipantRev;
    private ShootingTestAttemptDetailed mAttempt;

    private ArrayAdapter<CharSequence> mResultAdapter;

    @ViewAnnotations.ViewId(R.id.attempt_participant_name)
    private TextView mParticipantName;

    @ViewAnnotations.ViewId(R.id.moose_type_button)
    private AppCompatButton mTypeButtonMoose;

    @ViewAnnotations.ViewId(R.id.bear_type_button)
    private AppCompatButton mTypeButtonBear;

    @ViewAnnotations.ViewId(R.id.roe_deer_type_button)
    private AppCompatButton mTypeButtonRoeDeer;

    @ViewAnnotations.ViewId(R.id.bow_type_button)
    private AppCompatButton mTypeButtonBow;

    @ViewAnnotations.ViewId(R.id.hits_0_button)
    private AppCompatButton mHitsButton0;

    @ViewAnnotations.ViewId(R.id.hits_1_button)
    private AppCompatButton mHitsButton1;

    @ViewAnnotations.ViewId(R.id.hits_2_button)
    private AppCompatButton mHitsButton2;

    @ViewAnnotations.ViewId(R.id.hits_3_button)
    private AppCompatButton mHitsButton3;

    @ViewAnnotations.ViewId(R.id.hits_4_button)
    private AppCompatButton mHitsButton4;

    @ViewAnnotations.ViewId(R.id.attempt_result_spinner)
    private AppCompatSpinner mResultSpinner;

    @ViewAnnotations.ViewId(R.id.attempt_result_note_title)
    private TextView mResultNoteTitle;

    @ViewAnnotations.ViewId(R.id.attempt_result_note)
    private EditText mResultNote;

    @ViewAnnotations.ViewId(R.id.save_button)
    private AppCompatButton mSaveButton;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState, @Nullable final PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onPostCreate(@Nullable final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setContentView(R.layout.activity_shooting_test_edit_attempt);
        setCustomTitle(getString(R.string.shooting_test_participant_attemps));

        ViewAnnotations.apply(this);

        final Intent intent = getIntent();

        mAttempt = (ShootingTestAttemptDetailed) intent.getSerializableExtra(EXTRA_ATTEMPT);
        mAttemptId = intent.getLongExtra(EXTRA_ATTEMPT_ID, -1);
        mParticipantId = intent.getLongExtra(EXTRA_PARTICIPANT_ID, -1);
        mParticipantRev = intent.getIntExtra(EXTRA_PARTICIPANT_REV, -1);
        mParticipantName.setText(intent.getStringExtra(EXTRA_PARTICIPANT_NAME));

        mEnableBear = intent.getBooleanExtra(EXTRA_ENABLE_BEAR, true);
        mEnableMoose = intent.getBooleanExtra(EXTRA_ENABLE_MOOSE, true);
        mEnableRoeDeer = intent.getBooleanExtra(EXTRA_ENABLE_ROEDEER, true);
        mEnableBow = intent.getBooleanExtra(EXTRA_ENABLE_BOW, true);

        initTypeButton(mTypeButtonMoose, MOOSE);
        initTypeButton(mTypeButtonBear, BEAR);
        initTypeButton(mTypeButtonRoeDeer, ROE_DEER);
        initTypeButton(mTypeButtonBow, BOW);

        initHitsButton(mHitsButton0);
        initHitsButton(mHitsButton1);
        initHitsButton(mHitsButton2);
        initHitsButton(mHitsButton3);
        initHitsButton(mHitsButton4);

        setupResultSpinner();

        if (mAttempt != null) {
            updateViewsFromPreviouslySavedAttempt();
        } else {
            // Disable some fields.
            resetInputFields();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshData();
    }

    private void setupResultSpinner() {
        mResultAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        mResultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mResultSpinner.setAdapter(mResultAdapter);
        mResultSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent,
                                       final View view,
                                       final int position,
                                       final long id) {

                setNoteVisible(isRebated(position));
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
            }
        });
    }

    private static boolean isRebated(final int resultSpinnerPosition) {
        return resultSpinnerPosition == SPINNER_REBATED_INDEX;
    }

    private void setResultChoices(final boolean qualified) {
        final String first = qualified
                ? ShootingTestAttemptDetailed.localisedResultText(this, QUALIFIED)
                : ShootingTestAttemptDetailed.localisedResultText(this, UNQUALIFIED);

        final List<String> values = asList(
                first,
                ShootingTestAttemptDetailed.localisedResultText(this, TIMED_OUT),
                ShootingTestAttemptDetailed.localisedResultText(this, REBATED));

        mResultAdapter.clear();
        mResultAdapter.addAll(values);
        mResultAdapter.notifyDataSetChanged();
    }

    private void refreshData() {
        // Fetch attempt from backend only if it is remote persistent.
        if (mAttemptId >= 0) {
            final GetShootingTestAttempt task = new GetShootingTestAttempt(getWorkContext(), mAttemptId) {
                @Override
                protected void onFinishObject(final ShootingTestAttemptDetailed result) {
                    if (mAttempt == null || result.rev > mAttempt.rev) {
                        mAttempt = result;
                        updateViewsFromPreviouslySavedAttempt();
                    }
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

    private void updateViewsFromPreviouslySavedAttempt() {
        resetInputFields();

        if (mAttempt.type == null) {
            return;
        }

        final AppCompatButton selectedTypeButton;

        switch (mAttempt.type) {
            case MOOSE:
                selectedTypeButton = mTypeButtonMoose;
                break;
            case BEAR:
                selectedTypeButton = mTypeButtonBear;
                break;
            case ROE_DEER:
                selectedTypeButton = mTypeButtonRoeDeer;
                break;
            case BOW:
                selectedTypeButton = mTypeButtonBow;
                hide4HitsButtonIfBowTypeSelected(true);
                break;
            default:
                selectedTypeButton = null;
                break;
        }

        if (selectedTypeButton != null) {
            selectedTypeButton.setSelected(true);
            enableAllHitsButtons();
        }

        final AppCompatButton selectedHitsButton;

        switch (mAttempt.hits) {
            case 4:
                selectedHitsButton = mHitsButton4;
                break;
            case 3:
                selectedHitsButton = mHitsButton3;
                break;
            case 2:
                selectedHitsButton = mHitsButton2;
                break;
            case 1:
                selectedHitsButton = mHitsButton1;
                break;
            case 0:
                selectedHitsButton = mHitsButton0;
                break;
            default:
                selectedHitsButton = null;
                break;
        }

        if (selectedHitsButton != null) {
            selectedHitsButton.setSelected(true);

            mResultSpinner.setEnabled(true);
            mSaveButton.setEnabled(true);
        }

        final ShootingTestResult result = mAttempt.result;

        int resultSpinnerSelection = -1;
        boolean qualified = false;

        if (result != null) {
            switch (result) {
                case QUALIFIED:
                    qualified = true;
                    resultSpinnerSelection = SPINNER_PASS_FAIL_INDEX;
                    break;
                case UNQUALIFIED:
                    qualified = false;
                    resultSpinnerSelection = SPINNER_PASS_FAIL_INDEX;
                    break;
                case TIMED_OUT:
                    qualified = isQualifiedByNumberOfHits();
                    resultSpinnerSelection = SPINNER_OVERTIME_INDEX;
                    break;
                case REBATED:
                    qualified = isQualifiedByNumberOfHits();
                    resultSpinnerSelection = SPINNER_REBATED_INDEX;
                    break;
            }
        }

        setResultChoices(qualified);
        mResultSpinner.setSelection(resultSpinnerSelection);

        setNoteVisible(REBATED == result);
        mResultNote.setText(mAttempt.note);
    }

    private boolean isQualifiedByNumberOfHits() {
        return mTypeButtonMoose.isSelected() && mHitsButton4.isSelected()
                || mTypeButtonBear.isSelected() && mHitsButton4.isSelected()
                || mTypeButtonRoeDeer.isSelected() && mHitsButton4.isSelected()
                || mTypeButtonBow.isSelected() && mHitsButton3.isSelected();
    }

    private void initTypeButton(final AppCompatButton button, final ShootingTestType testType) {
        final boolean enabled =
                BEAR == testType && mEnableBear
                || MOOSE == testType && mEnableMoose
                || ROE_DEER == testType && mEnableRoeDeer
                || BOW == testType && mEnableBow;

        button.setEnabled(enabled);

        button.setOnClickListener(btn -> {
            if (!btn.isSelected()) {
                deselectAllTypeButtons();
                btn.setSelected(true);

                deselectAllHitsButtons();
                enableAllHitsButtons();

                // Bow test only has 3 attempts.
                hide4HitsButtonIfBowTypeSelected(btn == mTypeButtonBow);

                clearResultAndHideNote();
            }
        });
    }

    private void hide4HitsButtonIfBowTypeSelected(final boolean isBowTypeSelected) {
        mHitsButton4.setVisibility(isBowTypeSelected ? View.GONE : View.VISIBLE);
    }

    private void initHitsButton(final AppCompatButton button) {
        button.setOnClickListener(btn -> {
            if (!btn.isSelected()) {
                deselectAllHitsButtons();
                btn.setSelected(true);

                final boolean qualified =
                        btn == mHitsButton4 || mTypeButtonBow.isSelected() && mHitsButton3.isSelected();
                setResultChoices(qualified);

                // Currently not changing existing UI logic significantly
                // => hence "false" parameter
                // => the first entry is selected from the result spinner (pass/fail) and note is hidden
                handleResultAndNoteOnHitsChanged(false);

                mResultSpinner.setEnabled(true);
                mSaveButton.setEnabled(true);
            }
        });
    }

    private void handleResultAndNoteOnHitsChanged(final boolean memorizeResult) {
        if (memorizeResult) {
            setNoteVisible(isRebated(mResultSpinner.getSelectedItemPosition()));
        } else {
            mResultSpinner.setSelection(SPINNER_PASS_FAIL_INDEX);
            setNoteVisible(false);
        }
    }

    private void resetInputFields() {
        deselectAllTypeButtons();
        deselectAllHitsButtons();

        clearResultAndHideNote();
    }

    private void deselectAllTypeButtons() {
        mTypeButtonMoose.setSelected(false);
        mTypeButtonBear.setSelected(false);
        mTypeButtonRoeDeer.setSelected(false);
        mTypeButtonBow.setSelected(false);

        // Hits buttons cannot be selected before test type is selected.
        disableAllHitsButtons();

        // form must go invalid
        mSaveButton.setEnabled(false);
    }

    private void deselectAllHitsButtons() {
        mHitsButton0.setSelected(false);
        mHitsButton1.setSelected(false);
        mHitsButton2.setSelected(false);
        mHitsButton3.setSelected(false);
        mHitsButton4.setSelected(false);

        // form must go invalid
        mSaveButton.setEnabled(false);
    }

    private void enableAllHitsButtons() {
        toggleAllHitsButtons(true);
    }

    private void disableAllHitsButtons() {
        toggleAllHitsButtons(false);
    }

    private void toggleAllHitsButtons(final boolean enabled) {
        mHitsButton0.setEnabled(enabled);
        mHitsButton1.setEnabled(enabled);
        mHitsButton2.setEnabled(enabled);
        mHitsButton3.setEnabled(enabled);
        mHitsButton4.setEnabled(enabled);
    }

    private void clearResultAndHideNote() {
        mResultSpinner.setSelection(-1);
        mResultSpinner.setEnabled(false);

        setNoteVisible(false);
    }

    private void setNoteVisible(final boolean visible) {
        final int visibility = visible ? View.VISIBLE : View.GONE;

        mResultNote.setVisibility(visibility);
        mResultNoteTitle.setVisibility(visibility);
    }

    @ViewAnnotations.ViewOnClick(R.id.cancel_button)
    protected void onCancelClicked(final View view) {
        onBackPressed();
    }

    @ViewAnnotations.ViewOnClick(R.id.save_button)
    protected void onSaveClicked(final View view) {
        storeInputs();

        if (mAttempt.validateData()) {
            if (mAttempt.id >= 0) {
                saveAndUpdateAttempt();
            } else {
                saveAndAddAttempt();
            }
        } else {
            showToast("Validating input failed");
        }
    }

    private void storeInputs() {
        if (mTypeButtonMoose.isSelected()) {
            mAttempt.type = MOOSE;
        } else if (mTypeButtonBear.isSelected()) {
            mAttempt.type = BEAR;
        } else if (mTypeButtonRoeDeer.isSelected()) {
            mAttempt.type = ROE_DEER;
        } else if (mTypeButtonBow.isSelected()) {
            mAttempt.type = BOW;
        } else {
            mAttempt.type = null;
        }

        if (mHitsButton0.isSelected()) {
            mAttempt.hits = 0;
        } else if (mHitsButton1.isSelected()) {
            mAttempt.hits = 1;
        } else if (mHitsButton2.isSelected()) {
            mAttempt.hits = 2;
        } else if (mHitsButton3.isSelected()) {
            mAttempt.hits = 3;
        } else if (mHitsButton4.isSelected()) {
            mAttempt.hits = 4;
        }

        // Null by default, overridden if rebate selected.
        mAttempt.note = null;

        switch (mResultSpinner.getSelectedItemPosition()) {
            case SPINNER_PASS_FAIL_INDEX:
                mAttempt.result = isQualifiedByNumberOfHits() ? QUALIFIED : UNQUALIFIED;
                break;
            case SPINNER_OVERTIME_INDEX:
                mAttempt.result = TIMED_OUT;
                break;
            case SPINNER_REBATED_INDEX:
                mAttempt.result = REBATED;
                mAttempt.note = mResultNote.getText().toString();
                break;
            default:
                mAttempt.result = null;
                break;
        }
    }

    private void saveAndAddAttempt() {
        final AddAttemptForParticipantTask task = new AddAttemptForParticipantTask(
                getWorkContext(),
                mParticipantId, mParticipantRev,
                mAttempt.type, mAttempt.result, mAttempt.hits, mAttempt.note) {

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

    private void saveAndUpdateAttempt() {
        final UpdateAttemptForParticipantTask task = new UpdateAttemptForParticipantTask(
                getWorkContext(),
                mAttempt.id, mAttempt.rev,
                mParticipantId, mParticipantRev,
                mAttempt.type, mAttempt.result, mAttempt.hits, mAttempt.note) {

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

    private void showToast(final String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}
