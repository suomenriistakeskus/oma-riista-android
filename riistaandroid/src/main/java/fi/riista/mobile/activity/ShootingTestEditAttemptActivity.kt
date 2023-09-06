package fi.riista.mobile.activity

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatSpinner
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.OperationResult
import fi.riista.common.domain.OperationResultWithData
import fi.riista.common.domain.model.ShootingTestType
import fi.riista.common.domain.shootingTest.model.ShootingTestResult
import fi.riista.mobile.R
import fi.riista.mobile.models.shootingTest.ShootingTestAttemptDetailed
import fi.riista.mobile.riistaSdkHelpers.toShootingTestAttemptDetailed
import fi.riista.mobile.utils.isResumed
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

// TODO: Do not allow edits for completed event
class ShootingTestEditAttemptActivity : BaseActivity() {
    private var enableBear = true
    private var enableMoose = true
    private var enableRoeDeer = true
    private var enableBow = true
    private var attemptId: Long = 0
    private var participantId: Long = 0
    private var participantRev = 0
    private var attempt: ShootingTestAttemptDetailed? = null
    private lateinit var resultAdapter: ArrayAdapter<CharSequence>

    private val shootingTestContext = RiistaSDK.shootingTestContext

    private lateinit var participantNameTextView: TextView
    private lateinit var typeButtonMoose: AppCompatButton
    private lateinit var typeButtonBear: AppCompatButton
    private lateinit var typeButtonRoeDeer: AppCompatButton
    private lateinit var typeButtonBow: AppCompatButton
    private lateinit var hitsButton0: AppCompatButton
    private lateinit var hitsButton1: AppCompatButton
    private lateinit var hitsButton2: AppCompatButton
    private lateinit var hitsButton3: AppCompatButton
    private lateinit var hitsButton4: AppCompatButton
    private lateinit var resultSpinner: AppCompatSpinner
    private lateinit var resultNoteTitleTextView: TextView
    private lateinit var resultNoteEditText: EditText
    private lateinit var saveButton: AppCompatButton
    private lateinit var cancelButton: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_shooting_test_edit_attempt)
        setCustomTitle(getString(R.string.shooting_test_participant_attemps))

        participantNameTextView = findViewById(R.id.attempt_participant_name)
        typeButtonMoose = findViewById(R.id.moose_type_button)
        typeButtonBear = findViewById(R.id.bear_type_button)
        typeButtonRoeDeer = findViewById(R.id.roe_deer_type_button)
        typeButtonBow = findViewById(R.id.bow_type_button)
        hitsButton0 = findViewById(R.id.hits_0_button)
        hitsButton1 = findViewById(R.id.hits_1_button)
        hitsButton2 = findViewById(R.id.hits_2_button)
        hitsButton3 = findViewById(R.id.hits_3_button)
        hitsButton4 = findViewById(R.id.hits_4_button)
        resultSpinner = findViewById(R.id.attempt_result_spinner)
        resultNoteTitleTextView = findViewById(R.id.attempt_result_note_title)
        resultNoteEditText = findViewById(R.id.attempt_result_note)
        saveButton = findViewById(R.id.save_button)
        saveButton.setOnClickListener { onSaveClicked() }
        cancelButton = findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener { onCancelClicked() }

        val intent = intent
        attempt = intent.getSerializableExtra(EXTRA_ATTEMPT) as ShootingTestAttemptDetailed?
        attemptId = intent.getLongExtra(EXTRA_ATTEMPT_ID, -1)
        participantId = intent.getLongExtra(EXTRA_PARTICIPANT_ID, -1)
        participantRev = intent.getIntExtra(EXTRA_PARTICIPANT_REV, -1)
        participantNameTextView.text = intent.getStringExtra(EXTRA_PARTICIPANT_NAME)
        enableBear = intent.getBooleanExtra(EXTRA_ENABLE_BEAR, true)
        enableMoose = intent.getBooleanExtra(EXTRA_ENABLE_MOOSE, true)
        enableRoeDeer = intent.getBooleanExtra(EXTRA_ENABLE_ROEDEER, true)
        enableBow = intent.getBooleanExtra(EXTRA_ENABLE_BOW, true)
        initTypeButton(typeButtonMoose, ShootingTestType.MOOSE)
        initTypeButton(typeButtonBear, ShootingTestType.BEAR)
        initTypeButton(typeButtonRoeDeer, ShootingTestType.ROE_DEER)
        initTypeButton(typeButtonBow, ShootingTestType.BOW)
        initHitsButton(hitsButton0)
        initHitsButton(hitsButton1)
        initHitsButton(hitsButton2)
        initHitsButton(hitsButton3)
        initHitsButton(hitsButton4)
        setupResultSpinner()
        if (attempt != null) {
            updateViewsFromPreviouslySavedAttempt()
        } else {
            // Disable some fields.
            resetInputFields()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupResultSpinner() {
        resultAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        resultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        resultSpinner.adapter = resultAdapter
        resultSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                setNoteVisible(isRebated(position))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setResultChoices(qualified: Boolean) {
        val first = if (qualified) ShootingTestAttemptDetailed.localisedResultText(
            this,
            ShootingTestResult.QUALIFIED
        ) else ShootingTestAttemptDetailed.localisedResultText(this, ShootingTestResult.UNQUALIFIED)
        val values = listOf(
            first,
            ShootingTestAttemptDetailed.localisedResultText(this, ShootingTestResult.TIMED_OUT),
            ShootingTestAttemptDetailed.localisedResultText(this, ShootingTestResult.REBATED)
        )
        resultAdapter.clear()
        resultAdapter.addAll(values)
        resultAdapter.notifyDataSetChanged()
    }

    private fun refreshData() {
        // Fetch attempt from backend only if it is remote persistent.
        if (attemptId >= 0) {
            MainScope().launch {
                val response = shootingTestContext.fetchShootingTestAttempt(attemptId)

                if (!lifecycle.isResumed()) {
                    return@launch
                }

                when (response) {
                    is OperationResultWithData.Success -> {
                        val testAttempt = attempt
                        if (testAttempt == null || response.data.rev > testAttempt.rev) {
                            attempt = response.data.toShootingTestAttemptDetailed()
                            updateViewsFromPreviouslySavedAttempt()
                        }
                    }
                    is OperationResultWithData.Failure -> {
                        val errorMsg = if (response.statusCode != null) {
                            getString(R.string.error_operation_failed, response.statusCode)
                        } else {
                            getString(R.string.operation_failed)
                        }
                        showToast(errorMsg)
                    }
                }
            }
        }
    }

    private fun updateViewsFromPreviouslySavedAttempt() {
        resetInputFields()
        if (attempt?.type == null) {
            return
        }
        val selectedTypeButton: AppCompatButton?
        when (attempt?.type) {
            ShootingTestType.MOOSE -> selectedTypeButton = typeButtonMoose
            ShootingTestType.BEAR -> selectedTypeButton = typeButtonBear
            ShootingTestType.ROE_DEER -> selectedTypeButton = typeButtonRoeDeer
            ShootingTestType.BOW -> {
                selectedTypeButton = typeButtonBow
                hide4HitsButtonIfBowTypeSelected(true)
            }
            else -> selectedTypeButton = null
        }
        if (selectedTypeButton != null) {
            selectedTypeButton.isSelected = true
            enableAllHitsButtons()
        }
        val selectedHitsButton: AppCompatButton? = when (attempt?.hits) {
            4 -> hitsButton4
            3 -> hitsButton3
            2 -> hitsButton2
            1 -> hitsButton1
            0 -> hitsButton0
            else -> null
        }
        if (selectedHitsButton != null) {
            selectedHitsButton.isSelected = true
            resultSpinner.isEnabled = true
            saveButton.isEnabled = true
        }
        val result = attempt?.result
        var resultSpinnerSelection = -1
        var qualified = false
        if (result != null) {
            when (result) {
                ShootingTestResult.QUALIFIED -> {
                    qualified = true
                    resultSpinnerSelection = SPINNER_PASS_FAIL_INDEX
                }
                ShootingTestResult.UNQUALIFIED -> {
                    qualified = false
                    resultSpinnerSelection = SPINNER_PASS_FAIL_INDEX
                }
                ShootingTestResult.TIMED_OUT -> {
                    qualified = isQualifiedByNumberOfHits
                    resultSpinnerSelection = SPINNER_OVERTIME_INDEX
                }
                ShootingTestResult.REBATED -> {
                    qualified = isQualifiedByNumberOfHits
                    resultSpinnerSelection = SPINNER_REBATED_INDEX
                }
            }
        }
        setResultChoices(qualified)
        resultSpinner.setSelection(resultSpinnerSelection)
        setNoteVisible(ShootingTestResult.REBATED === result)
        resultNoteEditText.setText(attempt?.note ?: "")
    }

    private val isQualifiedByNumberOfHits: Boolean
        get() = typeButtonMoose.isSelected && hitsButton4.isSelected ||
                typeButtonBear.isSelected && hitsButton4.isSelected ||
                typeButtonRoeDeer.isSelected && hitsButton4.isSelected ||
                typeButtonBow.isSelected && hitsButton3.isSelected

    private fun initTypeButton(button: AppCompatButton, testType: ShootingTestType) {
        val enabled = ShootingTestType.BEAR === testType && enableBear ||
                ShootingTestType.MOOSE === testType && enableMoose ||
                ShootingTestType.ROE_DEER === testType && enableRoeDeer ||
                ShootingTestType.BOW === testType && enableBow
        button.isEnabled = enabled
        button.setOnClickListener { btn: View ->
            if (!btn.isSelected) {
                deselectAllTypeButtons()
                btn.isSelected = true
                deselectAllHitsButtons()
                enableAllHitsButtons()

                // Bow test only has 3 attempts.
                hide4HitsButtonIfBowTypeSelected(btn === typeButtonBow)
                clearResultAndHideNote()
            }
        }
    }

    private fun hide4HitsButtonIfBowTypeSelected(isBowTypeSelected: Boolean) {
        hitsButton4.visibility = if (isBowTypeSelected) View.GONE else View.VISIBLE
    }

    private fun initHitsButton(button: AppCompatButton) {
        button.setOnClickListener { btn: View ->
            if (!btn.isSelected) {
                deselectAllHitsButtons()
                btn.isSelected = true
                val qualified = btn === hitsButton4 || typeButtonBow.isSelected && hitsButton3.isSelected
                setResultChoices(qualified)

                // Currently not changing existing UI logic significantly
                // => hence "false" parameter
                // => the first entry is selected from the result spinner (pass/fail) and note is hidden
                handleResultAndNoteOnHitsChanged(false)
                resultSpinner.isEnabled = true
                saveButton.isEnabled = true
            }
        }
    }

    private fun handleResultAndNoteOnHitsChanged(memorizeResult: Boolean) {
        if (memorizeResult) {
            setNoteVisible(isRebated(resultSpinner.selectedItemPosition))
        } else {
            resultSpinner.setSelection(SPINNER_PASS_FAIL_INDEX)
            setNoteVisible(false)
        }
    }

    private fun resetInputFields() {
        deselectAllTypeButtons()
        deselectAllHitsButtons()
        clearResultAndHideNote()
    }

    private fun deselectAllTypeButtons() {
        typeButtonMoose.isSelected = false
        typeButtonBear.isSelected = false
        typeButtonRoeDeer.isSelected = false
        typeButtonBow.isSelected = false

        // Hits buttons cannot be selected before test type is selected.
        disableAllHitsButtons()

        // form must go invalid
        saveButton.isEnabled = false
    }

    private fun deselectAllHitsButtons() {
        hitsButton0.isSelected = false
        hitsButton1.isSelected = false
        hitsButton2.isSelected = false
        hitsButton3.isSelected = false
        hitsButton4.isSelected = false

        // form must go invalid
        saveButton.isEnabled = false
    }

    private fun enableAllHitsButtons() {
        toggleAllHitsButtons(true)
    }

    private fun disableAllHitsButtons() {
        toggleAllHitsButtons(false)
    }

    private fun toggleAllHitsButtons(enabled: Boolean) {
        hitsButton0.isEnabled = enabled
        hitsButton1.isEnabled = enabled
        hitsButton2.isEnabled = enabled
        hitsButton3.isEnabled = enabled
        hitsButton4.isEnabled = enabled
    }

    private fun clearResultAndHideNote() {
        resultSpinner.setSelection(-1)
        resultSpinner.isEnabled = false
        setNoteVisible(false)
    }

    private fun setNoteVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        resultNoteEditText.visibility = visibility
        resultNoteTitleTextView.visibility = visibility
    }

    private fun onCancelClicked() {
        onBackPressed()
    }

    private fun onSaveClicked() {
        storeInputs()
        attempt?.let { attempt ->
            if (attempt.validateData()) {
                if (attempt.id >= 0) {
                    saveAndUpdateAttempt()
                } else {
                    saveAndAddAttempt()
                }
            } else {
                showToast("Validating input failed")
            }
        }
    }

    private fun storeInputs() {
        if (typeButtonMoose.isSelected) {
            attempt?.type = ShootingTestType.MOOSE
        } else if (typeButtonBear.isSelected) {
            attempt?.type = ShootingTestType.BEAR
        } else if (typeButtonRoeDeer.isSelected) {
            attempt?.type = ShootingTestType.ROE_DEER
        } else if (typeButtonBow.isSelected) {
            attempt?.type = ShootingTestType.BOW
        } else {
            attempt?.type = null
        }
        if (hitsButton0.isSelected) {
            attempt?.hits = 0
        } else if (hitsButton1.isSelected) {
            attempt?.hits = 1
        } else if (hitsButton2.isSelected) {
            attempt?.hits = 2
        } else if (hitsButton3.isSelected) {
            attempt?.hits = 3
        } else if (hitsButton4.isSelected) {
            attempt?.hits = 4
        }

        // Null by default, overridden if rebate selected.
        attempt?.note = null
        when (resultSpinner.selectedItemPosition) {
            SPINNER_PASS_FAIL_INDEX -> attempt?.result =
                if (isQualifiedByNumberOfHits) ShootingTestResult.QUALIFIED else ShootingTestResult.UNQUALIFIED
            SPINNER_OVERTIME_INDEX -> attempt?.result = ShootingTestResult.TIMED_OUT
            SPINNER_REBATED_INDEX -> {
                attempt?.result = ShootingTestResult.REBATED
                attempt?.note = resultNoteEditText.text.toString()
            }
            else -> attempt?.result = null
        }
    }

    private fun saveAndAddAttempt() {
        val testAttempt = attempt ?: return
        val attemptType = testAttempt.type ?: return // These shouldn't be null at this point
        val attemptResult = testAttempt.result ?: return // as save is possible only when these are set

        MainScope().launch {
            val response = shootingTestContext.addShootingTestAttempt(
                participantId = participantId,
                participantRev = participantRev,
                type = attemptType,
                result = attemptResult,
                hits = testAttempt.hits,
                note = testAttempt.note,
            )

            if (!lifecycle.isResumed()) {
                return@launch
            }

            when (response) {
                is OperationResult.Success -> {
                    onBackPressed()
                }
                is OperationResult.Failure -> {
                    val errorMsg = if (response.statusCode != null) {
                        getString(R.string.error_operation_failed, response.statusCode)
                    } else {
                        getString(R.string.operation_failed)
                    }
                    showToast(errorMsg)
                }
            }
        }
    }

    private fun saveAndUpdateAttempt() {
        val testAttempt = attempt ?: return
        val attemptType = testAttempt.type ?: return // These shouldn't be null at this point
        val attemptResult = testAttempt.result ?: return // as save is possible only when these are set

        MainScope().launch {
            val response = shootingTestContext.updateShootingTestAttempt(
                id = testAttempt.id,
                rev = testAttempt.rev,
                participantId = participantId,
                participantRev = participantRev,
                type = attemptType,
                result = attemptResult,
                hits = testAttempt.hits,
                note = testAttempt.note,
            )

            if (!lifecycle.isResumed()) {
                return@launch
            }

            when (response) {
                is OperationResult.Success -> {
                    onBackPressed()
                }
                is OperationResult.Failure -> {
                    val errorMsg = if (response.statusCode != null) {
                        getString(R.string.error_operation_failed, response.statusCode)
                    } else {
                        getString(R.string.operation_failed)
                    }
                    showToast(errorMsg)
                }
                else -> {
                }
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val EXTRA_ATTEMPT = "extra_attempt"
        const val EXTRA_ATTEMPT_ID = "extra_attempt_id"
        const val EXTRA_PARTICIPANT_NAME = "extra_participant_name"
        const val EXTRA_PARTICIPANT_ID = "extra_participant_id"
        const val EXTRA_PARTICIPANT_REV = "extra_participant_rev"
        const val EXTRA_ENABLE_BEAR = "extra_enable_bear"
        const val EXTRA_ENABLE_MOOSE = "extra_enable_moose"
        const val EXTRA_ENABLE_ROEDEER = "extra_enable_roedeer"
        const val EXTRA_ENABLE_BOW = "extra_enable_bow"
        private const val SPINNER_PASS_FAIL_INDEX = 0
        private const val SPINNER_OVERTIME_INDEX = 1
        private const val SPINNER_REBATED_INDEX = 2
        private fun isRebated(resultSpinnerPosition: Int): Boolean {
            return resultSpinnerPosition == SPINNER_REBATED_INDEX
        }
    }
}
