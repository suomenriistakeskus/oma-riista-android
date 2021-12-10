package fi.riista.mobile.pages;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.lifecycle.ViewModelProvider;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.android.support.AndroidSupportInjection;
import fi.riista.mobile.R;
import fi.riista.mobile.models.shootingTest.ShootingTestSearchPersonResult;
import fi.riista.mobile.network.shootingTest.AddShootingTestParticipantTask;
import fi.riista.mobile.network.shootingTest.SearchPersonWithHunterNumberTask;
import fi.riista.mobile.network.shootingTest.SearchPersonWithSsnTask;
import fi.riista.mobile.utils.DateTimeUtils;
import fi.riista.mobile.utils.KeyboardUtils;
import fi.riista.mobile.viewmodel.ShootingTestMainViewModel;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.util.ViewAnnotations;

import static fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME;
import static fi.riista.mobile.models.shootingTest.ShootingTestSearchPersonResult.REGISTRATION_STATUS_COMPLETED;
import static fi.riista.mobile.models.shootingTest.ShootingTestSearchPersonResult.REGISTRATION_STATUS_FOREIGN_HUNTER;
import static fi.riista.mobile.models.shootingTest.ShootingTestSearchPersonResult.REGISTRATION_STATUS_HUNTING_BAN;
import static fi.riista.mobile.models.shootingTest.ShootingTestSearchPersonResult.REGISTRATION_STATUS_HUNTING_PAYMENT_DONE;
import static fi.riista.mobile.models.shootingTest.ShootingTestSearchPersonResult.REGISTRATION_STATUS_HUNTING_PAYMENT_NOT_DONE;
import static fi.riista.mobile.models.shootingTest.ShootingTestSearchPersonResult.REGISTRATION_STATUS_IN_PROGRESS;
import static fi.riista.mobile.models.shootingTest.ShootingTestSearchPersonResult.REGISTRATION_STATUS_NOT_HUNTER;
import static fi.riista.mobile.models.shootingTest.ShootingTestSearchPersonResult.REGISTRATION_STATUS_OFFICIAL;
import static java.lang.String.format;

public class ShootingTestRegisterFragment extends ShootingTestTabContentFragment {

    private static final Pattern HUNTING_LICENSE_PATTERN = Pattern.compile("^.*;.*;.*;\\d*;(\\d{8});\\d*;\\d*;.*$");
    private static final Pattern SSN_PATTERN = Pattern.compile("^\\d{6}[A+-]\\d{3}[0-9A-FHJ-NPR-Y]$");

    @Inject
    @Named(APPLICATION_WORK_CONTEXT_NAME)
    WorkContext mAppWorkContext;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private ShootingTestMainViewModel mModel;
    private ShootingTestSearchPersonResult mSearchResult;

    @ViewAnnotations.ViewId(R.id.hunter_number_input)
    private EditText mNumberInput;

    @ViewAnnotations.ViewId(R.id.hunter_number_search_button)
    private AppCompatImageButton mSearchButton;

    @ViewAnnotations.ViewId(R.id.hunter_number_read_qr_button)
    private Button mReadQrCodeButton;

    @ViewAnnotations.ViewId(R.id.result_details_view)
    private LinearLayout mResultView;

    @ViewAnnotations.ViewId(R.id.hunter_name)
    private TextView mHunterName;

    @ViewAnnotations.ViewId(R.id.hunter_number)
    private TextView mHunterNumber;

    @ViewAnnotations.ViewId(R.id.hunter_birth_date)
    private TextView mHunterBirthDate;

    @ViewAnnotations.ViewId(R.id.hunter_state_message)
    private TextView mHunterState;

    @ViewAnnotations.ViewId(R.id.test_type_moose_check)
    private AppCompatCheckBox mMooseCheckedText;

    @ViewAnnotations.ViewId(R.id.test_type_bear_check)
    private AppCompatCheckBox mBearCheckedText;

    @ViewAnnotations.ViewId(R.id.test_type_roedeer_check)
    private AppCompatCheckBox mRoeDeerChecked;

    @ViewAnnotations.ViewId(R.id.test_type_bow_check)
    private AppCompatCheckBox mBowCheckedText;

    @ViewAnnotations.ViewId(R.id.result_button_view)
    private LinearLayout mResultButtonView;

    @ViewAnnotations.ViewId(R.id.cancel_participant_btn)
    private Button mCancelButton;

    @ViewAnnotations.ViewId(R.id.add_participant_btn)
    private Button mAddButton;

    public static ShootingTestRegisterFragment newInstance() {
        final ShootingTestRegisterFragment fragment = new ShootingTestRegisterFragment();
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_shooting_test_register, container, false);

        ViewAnnotations.apply(view);

        mNumberInput = view.findViewById(R.id.hunter_number_input);
        mNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {
            }

            @Override
            public void onTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {
                final boolean enabled = charSequence.toString().trim().length() == 8;
                mSearchButton.setEnabled(enabled);
            }

            @Override
            public void afterTextChanged(final Editable editable) {
            }
        });

        mResultView = view.findViewById(R.id.result_details_view);
        mHunterName = view.findViewById(R.id.hunter_name);
        mHunterNumber = view.findViewById(R.id.hunter_number);
        mHunterBirthDate = view.findViewById(R.id.hunter_birth_date);
        mHunterState = view.findViewById(R.id.hunter_state_message);

        mMooseCheckedText = view.findViewById(R.id.test_type_moose_check);
        mMooseCheckedText.setOnCheckedChangeListener((buttonView, isChecked) -> refreshAddButtonState());

        mBearCheckedText = view.findViewById(R.id.test_type_bear_check);
        mBearCheckedText.setOnCheckedChangeListener((buttonView, isChecked) -> refreshAddButtonState());

        mRoeDeerChecked = view.findViewById(R.id.test_type_roedeer_check);
        mRoeDeerChecked.setOnCheckedChangeListener((buttonView, isChecked) -> refreshAddButtonState());

        mBowCheckedText = view.findViewById(R.id.test_type_bow_check);
        mBowCheckedText.setOnCheckedChangeListener((buttonView, isChecked) -> refreshAddButtonState());

        mSearchButton = view.findViewById(R.id.hunter_number_search_button);
        mSearchButton.setEnabled(false);
        mSearchButton.setOnClickListener(this::onSearchClicked);

        mReadQrCodeButton = view.findViewById(R.id.hunter_number_read_qr_button);
        mReadQrCodeButton.setOnClickListener(this::onReadQrCodeClicked);

        mResultButtonView = view.findViewById(R.id.result_button_view);

        mAddButton = view.findViewById(R.id.add_participant_btn);
        mAddButton.setOnClickListener(this::onAddClick);

        mCancelButton = view.findViewById(R.id.cancel_participant_btn);
        mCancelButton.setOnClickListener(this::onCancelClick);

        return view;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            final String content = result.getContents();

            if (content != null) {
                final Matcher huntingLicenseMatcher = HUNTING_LICENSE_PATTERN.matcher(content);

                if (huntingLicenseMatcher.find()) {
                    final String huntingNumber = huntingLicenseMatcher.group(1);
                    confirmSearchWithQrCodeResult(huntingNumber);
                } else {
                    final Matcher ssnMatcher = SSN_PATTERN.matcher(content);

                    if (ssnMatcher.find()) {
                        final String ssn = ssnMatcher.group(0);
                        confirmSearchWithSsn(ssn);
                    } else {
                        final String errorMsg = getString(R.string.hunting_licence_qr_code_invalid_format);
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void confirmSearchWithQrCodeResult(final String huntingNumber) {
        final DialogInterface.OnClickListener dialogListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                mNumberInput.setText(huntingNumber);
                onSearchClicked(null);
            }
        };

        new AlertDialog.Builder(requireContext())
                .setMessage(format(getString(R.string.hunting_licence_search_with_scanned_number), huntingNumber))
                .setPositiveButton(R.string.yes, dialogListener)
                .setNegativeButton(R.string.no, dialogListener)
                .show();
    }

    private void confirmSearchWithSsn(final String ssn) {
        final DialogInterface.OnClickListener dialogListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                onSearchWithSsn(ssn);
            }
        };

        new AlertDialog.Builder(requireContext())
                .setMessage(format(getString(R.string.hunting_licence_search_with_scanned_ssn), ssn))
                .setPositiveButton(R.string.yes, dialogListener)
                .setNegativeButton(R.string.no, dialogListener)
                .show();
    }

    private void resetResultView() {
        mHunterName.setText("");
        mHunterNumber.setText("");
        mHunterBirthDate.setText("");

        mHunterState.setText("");
        mHunterState.setVisibility(View.GONE);

        mMooseCheckedText.setChecked(false);
        mBearCheckedText.setChecked(false);
        mRoeDeerChecked.setChecked(false);
        mBowCheckedText.setChecked(false);

        mResultView.setVisibility(View.GONE);
        mResultButtonView.setVisibility(View.GONE);
        mNumberInput.setText("");

        mNumberInput.setEnabled(true);
        mSearchButton.setEnabled(false);
        mReadQrCodeButton.setEnabled(true);
        mReadQrCodeButton.setVisibility(View.VISIBLE);
    }

    private void displayFromResponse(final ShootingTestSearchPersonResult result) {
        mNumberInput.setEnabled(false);
        mSearchButton.setEnabled(false);
        mReadQrCodeButton.setEnabled(false);
        mReadQrCodeButton.setVisibility(View.GONE);

        final String localisedDate = DateTimeUtils.convertDateStringToFinnishFormat(result.dateOfBirth);

        mHunterName.setText(format("%s %s", result.lastName, result.firstName));
        mHunterNumber.setText(result.hunterNumber);
        mHunterBirthDate.setText(localisedDate);

        @StringRes final Integer hunterStateResId;

        switch (result.registrationStatus) {
            case REGISTRATION_STATUS_IN_PROGRESS:
                hunterStateResId = R.string.shooting_test_register_already_registered;
                break;
            case REGISTRATION_STATUS_COMPLETED:
                hunterStateResId = R.string.shooting_test_register_already_completed;
                break;
            case REGISTRATION_STATUS_HUNTING_PAYMENT_NOT_DONE:
                hunterStateResId = R.string.shooting_test_register_hunting_payment_not_done;
                break;
            case REGISTRATION_STATUS_OFFICIAL:
                hunterStateResId = R.string.shooting_test_register_already_official;
                break;
            case REGISTRATION_STATUS_HUNTING_BAN:
                hunterStateResId = R.string.shooting_test_register_hunting_ban;
                break;
            case REGISTRATION_STATUS_NOT_HUNTER:
                hunterStateResId = R.string.shooting_test_register_no_hunter_number;
                break;
            case REGISTRATION_STATUS_FOREIGN_HUNTER:
                hunterStateResId = R.string.shooting_test_register_foreign_hunter;
                break;
            case REGISTRATION_STATUS_HUNTING_PAYMENT_DONE:
                // Fall through to default.
            default:
                hunterStateResId = null;
                break;
        }

        if (hunterStateResId != null) {
            mHunterState.setText(hunterStateResId);
            mHunterState.setVisibility(View.VISIBLE);
        } else {
            mHunterState.setText("");
            mHunterState.setVisibility(View.GONE);
        }

        mMooseCheckedText.setChecked(result.selectedShootingTestTypes.mooseTestIntended);
        mBearCheckedText.setChecked(result.selectedShootingTestTypes.bearTestIntended);
        mRoeDeerChecked.setChecked(result.selectedShootingTestTypes.roeDeerTestIntended);
        mBowCheckedText.setChecked(result.selectedShootingTestTypes.bowTestIntended);

        refreshAddButtonState();

        mResultView.setVisibility(View.VISIBLE);
        mResultButtonView.setVisibility(View.VISIBLE);
    }

    private void refreshAddButtonState() {
        final boolean enabled = mSearchResult != null
                && !TextUtils.isEmpty(mSearchResult.hunterNumber) // hunter number must be present
                && isValidRegistrationStatus()
                && isTestTypeSelected();

        mAddButton.setEnabled(enabled);
    }

    private boolean isValidRegistrationStatus() {
        final String status = mSearchResult != null ? mSearchResult.registrationStatus : null;

        if (status == null) {
            return false;
        }

        return status.equals(REGISTRATION_STATUS_HUNTING_PAYMENT_DONE)
                || status.equals(REGISTRATION_STATUS_HUNTING_PAYMENT_NOT_DONE)
                || status.equals(REGISTRATION_STATUS_COMPLETED)
                || status.equals(REGISTRATION_STATUS_FOREIGN_HUNTER);
    }

    private boolean isTestTypeSelected() {
        return mMooseCheckedText.isChecked()
                || mBearCheckedText.isChecked()
                || mRoeDeerChecked.isChecked()
                || mBowCheckedText.isChecked();
    }

    protected void onSearchWithSsn(final String input) {
        final Long testEventId = mModel.getTestEventId();

        if (testEventId == null) {
            return;
        }

        final SearchPersonWithSsnTask task = new SearchPersonWithSsnTask(mAppWorkContext, testEventId, input) {
            @Override
            protected void onFinishObject(final ShootingTestSearchPersonResult result) {
                mSearchResult = result;
                displayFromResponse(result);
            }

            @Override
            protected void onError() {
                super.onError();

                final int statusCode = getHttpStatusCode();

                final String errorMsg = statusCode == 404
                        ? getString(R.string.shooting_test_search_failed_not_found)
                        : format(getString(R.string.error_operation_failed), statusCode);

                Toast.makeText(mActivity, errorMsg, Toast.LENGTH_SHORT).show();
            }
        };
        task.start();
    }

    @ViewAnnotations.ViewOnClick(R.id.hunter_number_search_button)
    protected void onSearchClicked(final View view) {
        KeyboardUtils.hideKeyboard(requireContext(), getView());

        final Long testEventId = mModel.getTestEventId();

        if (testEventId == null) {
            return;
        }

        final String input = mNumberInput.getText().toString();

        final SearchPersonWithHunterNumberTask task =
                new SearchPersonWithHunterNumberTask(mAppWorkContext, testEventId, input) {

            @Override
            protected void onFinishObject(final ShootingTestSearchPersonResult result) {
                mSearchResult = result;
                displayFromResponse(result);
            }

            @Override
            protected void onError() {
                super.onError();

                if (isAdded()) {
                    final int statusCode = getHttpStatusCode();

                    final String errorMsg = statusCode == 404
                            ? getString(R.string.shooting_test_search_failed_not_found)
                            : format(getString(R.string.error_operation_failed), statusCode);

                    Toast.makeText(mActivity, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.start();
    }

    @ViewAnnotations.ViewOnClick(R.id.hunter_number_read_qr_button)
    protected void onReadQrCodeClicked(final View view) {
        // Request CAMERA permission in scanner component
        final IntentIntegrator intentIntegrator = IntentIntegrator.forSupportFragment(this);
        intentIntegrator.setBarcodeImageEnabled(true);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.initiateScan();
    }

    @ViewAnnotations.ViewOnClick(R.id.cancel_participant_btn)
    protected void onCancelClick(final View view) {
        resetResultView();
    }

    @ViewAnnotations.ViewOnClick(R.id.add_participant_btn)
    protected void onAddClick(final View view) {
        final AddShootingTestParticipantTask task = new AddShootingTestParticipantTask(
                mAppWorkContext,
                mModel.getTestEventId(),
                mSearchResult.hunterNumber,
                mMooseCheckedText.isChecked(),
                mBearCheckedText.isChecked(),
                mRoeDeerChecked.isChecked(),
                mBowCheckedText.isChecked()
        ) {
            @Override
            protected void onFinishText(final String text) {
                resetResultView();
                refreshData();
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
