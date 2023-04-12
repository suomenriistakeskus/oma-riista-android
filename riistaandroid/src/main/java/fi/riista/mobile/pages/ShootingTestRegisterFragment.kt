package fi.riista.mobile.pages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageButton
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.integration.android.IntentIntegrator
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.OperationResult
import fi.riista.common.domain.OperationResultWithData
import fi.riista.common.domain.shootingTest.model.CommonShootingTestPerson
import fi.riista.common.domain.shootingTest.model.ShootingTestRegistrationStatus
import fi.riista.mobile.R
import fi.riista.mobile.RemoteConfig.ssnPattern
import fi.riista.mobile.di.DependencyQualifiers.APPLICATION_WORK_CONTEXT_NAME
import fi.riista.mobile.riistaSdkHelpers.toJodaLocalDate
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.ui.registerAlertDialogFragmentResultListener
import fi.riista.mobile.utils.DateTimeUtils
import fi.riista.mobile.utils.KeyboardUtils
import fi.riista.mobile.viewmodel.ShootingTestMainViewModel
import fi.vincit.androidutilslib.context.WorkContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Named

class ShootingTestRegisterFragment : ShootingTestTabContentFragment() {
    @JvmField
    @Inject
    @Named(APPLICATION_WORK_CONTEXT_NAME)
    var appWorkContext: WorkContext? = null

    @JvmField
    @Inject
    var viewModelFactory: ViewModelProvider.Factory? = null

    private lateinit var viewModel: ShootingTestMainViewModel
    private var searchResult: CommonShootingTestPerson? = null
    private val shootingTestContext = RiistaSDK.shootingTestContext

    private lateinit var numberInputEditTest: EditText
    private lateinit var searchButton: AppCompatImageButton
    private lateinit var readQrCodeButton: Button
    private lateinit var resultView: LinearLayout
    private lateinit var hunterNameTextView: TextView
    private lateinit var hunterNumberTextView: TextView
    private lateinit var hunterBirthDateTextView: TextView
    private lateinit var hunterStateTextView: TextView
    private lateinit var mooseCheckBox: AppCompatCheckBox
    private lateinit var bearCheckBox: AppCompatCheckBox
    private lateinit var roeDeerCheckBox: AppCompatCheckBox
    private lateinit var bowCheckBox: AppCompatCheckBox
    private lateinit var resultButtonView: LinearLayout
    private lateinit var cancelButton: Button
    private lateinit var addButton: Button

    private val zxingActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onIntegratorActivityResult(result.resultCode, result.data)
    }

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shooting_test_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory!!)[ShootingTestMainViewModel::class.java]

        numberInputEditTest = view.findViewById(R.id.hunter_number_input)
        numberInputEditTest.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val enabled = charSequence.toString().trim { it <= ' ' }.length == 8
                searchButton.isEnabled = enabled
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        resultView = view.findViewById(R.id.result_details_view)
        hunterNameTextView = view.findViewById(R.id.hunter_name)
        hunterNumberTextView = view.findViewById(R.id.hunter_number)
        hunterBirthDateTextView = view.findViewById(R.id.hunter_birth_date)
        hunterStateTextView = view.findViewById(R.id.hunter_state_message)
        mooseCheckBox = view.findViewById(R.id.test_type_moose_check)
        mooseCheckBox.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean -> refreshAddButtonState() }
        bearCheckBox = view.findViewById(R.id.test_type_bear_check)
        bearCheckBox.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean -> refreshAddButtonState() }
        roeDeerCheckBox = view.findViewById(R.id.test_type_roedeer_check)
        roeDeerCheckBox.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean -> refreshAddButtonState() }
        bowCheckBox = view.findViewById(R.id.test_type_bow_check)
        bowCheckBox.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean -> refreshAddButtonState() }
        searchButton = view.findViewById(R.id.hunter_number_search_button)
        searchButton.isEnabled = false
        searchButton.setOnClickListener { onSearchClicked() }
        readQrCodeButton = view.findViewById(R.id.hunter_number_read_qr_button)
        readQrCodeButton.setOnClickListener { onReadQrCodeClicked() }
        resultButtonView = view.findViewById(R.id.result_button_view)
        addButton = view.findViewById(R.id.add_participant_btn)
        addButton.setOnClickListener { onAddClick() }
        cancelButton = view.findViewById(R.id.cancel_participant_btn)
        cancelButton.setOnClickListener { onCancelClick() }

        requireActivity().registerAlertDialogFragmentResultListener(
            dialogId = AlertDialogId.SHOOTING_TEST_REGISTER_FRAGMENT_SEARCH_WITH_HUNTER_NUMBER,
            onPositive = ::searchWithHunterNumber
        )
        requireActivity().registerAlertDialogFragmentResultListener(
            dialogId = AlertDialogId.SHOOTING_TEST_REGISTER_FRAGMENT_SEARCH_WITH_SSN,
            onPositive = ::searchWithSsn
        )
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun onIntegratorActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val intentResult = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data)
            val content = intentResult?.contents
            if (content != null) {
                val huntingLicenseMatcher = HUNTING_LICENSE_PATTERN.matcher(content)
                if (huntingLicenseMatcher.find()) {
                    val huntingNumber = huntingLicenseMatcher.group(1)
                    if (huntingNumber != null) {
                        confirmSearchWithQrCodeResult(huntingNumber)
                    }
                } else {
                    val ssnMatcher = SSN_PATTERN.matcher(content)
                    if (ssnMatcher.find()) {
                        val ssn = ssnMatcher.group(0)
                        if (ssn != null) {
                            confirmSearchWithSsn(ssn)
                        }
                    } else {
                        val errorMsg = getString(R.string.hunting_licence_qr_code_invalid_format)
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun confirmSearchWithQrCodeResult(huntingNumber: String) {
        AlertDialogFragment.Builder(
            requireContext(),
            AlertDialogId.SHOOTING_TEST_REGISTER_FRAGMENT_SEARCH_WITH_HUNTER_NUMBER,
        )
            .setMessage(String.format(getString(R.string.hunting_licence_search_with_scanned_number), huntingNumber))
            .setPositiveButton(R.string.yes, huntingNumber)
            .setNegativeButton(R.string.no)
            .build()
            .show(requireActivity().supportFragmentManager)
    }

    private fun searchWithHunterNumber(huntingNumber: String?) {
        numberInputEditTest.setText(huntingNumber)
        onSearchClicked()
    }

    private fun confirmSearchWithSsn(ssn: String) {
        AlertDialogFragment.Builder(requireContext(), AlertDialogId.SHOOTING_TEST_REGISTER_FRAGMENT_SEARCH_WITH_SSN)
            .setMessage(String.format(getString(R.string.hunting_licence_search_with_scanned_ssn), ssn))
            .setPositiveButton(R.string.yes, ssn)
            .setNegativeButton(R.string.no)
            .build()
            .show(requireActivity().supportFragmentManager)
    }

    private fun searchWithSsn(ssn: String?) {
        onSearchWithSsn(ssn)
    }

    private fun resetResultView() {
        hunterNameTextView.text = ""
        hunterNumberTextView.text = ""
        hunterBirthDateTextView.text = ""
        hunterStateTextView.text = ""
        hunterStateTextView.visibility = View.GONE
        mooseCheckBox.isChecked = false
        bearCheckBox.isChecked = false
        roeDeerCheckBox.isChecked = false
        bowCheckBox.isChecked = false
        resultView.visibility = View.GONE
        resultButtonView.visibility = View.GONE
        numberInputEditTest.setText("")
        numberInputEditTest.isEnabled = true
        searchButton.isEnabled = false
        readQrCodeButton.isEnabled = true
        readQrCodeButton.visibility = View.VISIBLE
    }

    private fun displayFromResponse(result: CommonShootingTestPerson) {
        numberInputEditTest.isEnabled = false
        searchButton.isEnabled = false
        readQrCodeButton.isEnabled = false
        readQrCodeButton.visibility = View.GONE
        val localisedDate = DateTimeUtils.formatLocalDateUsingLongFinnishFormat(result.dateOfBirth?.toJodaLocalDate())
        hunterNameTextView.text = String.format("%s %s", result.lastName, result.firstName)
        hunterNumberTextView.text = result.hunterNumber
        hunterBirthDateTextView.text = localisedDate
        @StringRes val hunterStateResId: Int? = when (result.registrationStatus.value) {
            ShootingTestRegistrationStatus.IN_PROGRESS -> R.string.shooting_test_register_already_registered
            ShootingTestRegistrationStatus.COMPLETED -> R.string.shooting_test_register_already_completed
            ShootingTestRegistrationStatus.HUNTING_PAYMENT_NOT_DONE -> R.string.shooting_test_register_hunting_payment_not_done
            ShootingTestRegistrationStatus.DISQUALIFIED_AS_OFFICIAL -> R.string.shooting_test_register_already_official
            ShootingTestRegistrationStatus.HUNTING_BAN -> R.string.shooting_test_register_hunting_ban
            ShootingTestRegistrationStatus.NO_HUNTER_NUMBER -> R.string.shooting_test_register_no_hunter_number
            ShootingTestRegistrationStatus.FOREIGN_HUNTER -> R.string.shooting_test_register_foreign_hunter
            ShootingTestRegistrationStatus.HUNTING_PAYMENT_DONE -> null
            else -> null
        }
        if (hunterStateResId != null) {
            hunterStateTextView.setText(hunterStateResId)
            hunterStateTextView.visibility = View.VISIBLE
        } else {
            hunterStateTextView.text = ""
            hunterStateTextView.visibility = View.GONE
        }
        mooseCheckBox.isChecked = result.selectedShootingTestTypes.mooseTestIntended
        bearCheckBox.isChecked = result.selectedShootingTestTypes.bearTestIntended
        roeDeerCheckBox.isChecked = result.selectedShootingTestTypes.roeDeerTestIntended
        bowCheckBox.isChecked = result.selectedShootingTestTypes.bowTestIntended
        refreshAddButtonState()
        resultView.visibility = View.VISIBLE
        resultButtonView.visibility = View.VISIBLE
    }

    private fun refreshAddButtonState() {
        val enabled =
            (searchResult != null && !TextUtils.isEmpty(searchResult?.hunterNumber) // hunter number must be present
                    && isValidRegistrationStatus
                    && isTestTypeSelected)
        addButton.isEnabled = enabled
    }

    private val isValidRegistrationStatus: Boolean
        get() {
            val status = searchResult?.registrationStatus?.value ?: return false
            return status == ShootingTestRegistrationStatus.HUNTING_PAYMENT_DONE ||
                    status == ShootingTestRegistrationStatus.HUNTING_PAYMENT_NOT_DONE ||
                    status == ShootingTestRegistrationStatus.COMPLETED ||
                    status == ShootingTestRegistrationStatus.FOREIGN_HUNTER
        }

    private val isTestTypeSelected: Boolean
        get() = (mooseCheckBox.isChecked
                || bearCheckBox.isChecked
                || roeDeerCheckBox.isChecked
                || bowCheckBox.isChecked)

    private fun onSearchWithSsn(input: String?) {
        val testEventId = viewModel.testEventId ?: return
        val ssnText = input ?: return

        MainScope().launch {
            val response = shootingTestContext.searchPersonBySsn(testEventId, ssnText)

            if (!isResumed) {
                return@launch
            }

            when (response) {
                is OperationResultWithData.Success -> {
                    searchResult = response.data
                    displayFromResponse(response.data)
                }
                is OperationResultWithData.Failure -> {
                    val statusCode = response.statusCode
                    val errorMsg =
                        if (statusCode == 404) {
                            getString(R.string.shooting_test_search_failed_not_found)
                        } else if (statusCode != null) {
                            getString(R.string.error_operation_failed, statusCode)
                        } else {
                            getString(R.string.error_operation_failed)
                        }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun onSearchClicked() {
        KeyboardUtils.hideKeyboard(requireContext(), view)
        val testEventId = viewModel.testEventId ?: return
        val input = numberInputEditTest.text.toString()

        MainScope().launch {
            val response = shootingTestContext.searchPersonByHunterNumber(testEventId, input)

            if (!isResumed) {
                return@launch
            }

            when (response) {
                is OperationResultWithData.Success -> {
                    searchResult = response.data
                    displayFromResponse(response.data)
                }
                is OperationResultWithData.Failure -> {
                    val statusCode = response.statusCode
                    val errorMsg =
                        if (statusCode == 404) {
                            getString(R.string.shooting_test_search_failed_not_found)
                        } else if (statusCode != null) {
                            getString(R.string.error_operation_failed, statusCode)
                        } else {
                            getString(R.string.error_operation_failed)
                        }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(requireContext(), getString(R.string.error_operation_failed), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun onReadQrCodeClicked() {
        // Request CAMERA permission in scanner component
        val intent = IntentIntegrator(requireActivity())
            .setBarcodeImageEnabled(true)
            .setOrientationLocked(false)
            .createScanIntent()
        zxingActivityResultLauncher.launch(intent)
    }

    private fun onCancelClick() {
        resetResultView()
    }

    private fun onAddClick() {
        val testEventId = viewModel.testEventId
        val hunterNumber = searchResult?.hunterNumber
        if (testEventId != null && hunterNumber != null) {

            MainScope().launch {
                val response = shootingTestContext.addShootingTestParticipant(
                    shootingTestEventId = testEventId,
                    hunterNumber = hunterNumber,
                    mooseTestIntended = mooseCheckBox.isChecked,
                    bearTestIntended = bearCheckBox.isChecked,
                    roeDeerTestIntended = roeDeerCheckBox.isChecked,
                    bowTestIntended = bowCheckBox.isChecked,
                )

                if (!isResumed) {
                    return@launch
                }

                when (response) {
                    is OperationResult.Success -> {
                        resetResultView()
                        refreshData()
                    }
                    is OperationResult.Failure -> {
                        val statusCode = response.statusCode
                        val errorMsg = statusCode?.let {
                            getString(R.string.error_operation_failed, statusCode)
                        } ?: getString(R.string.operation_failed)
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun refreshData() {
        viewModel.refreshCalendarEvent()
        viewModel.refreshParticipants()
    }

    companion object {
        private val HUNTING_LICENSE_PATTERN = Pattern.compile("^.*;.*;.*;\\d*;(\\d{8});\\d*;\\d*;.*$")
        private val SSN_PATTERN = Pattern.compile(ssnPattern)
    }
}
