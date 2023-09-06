package fi.riista.mobile.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatSpinner
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.OperationResult
import fi.riista.common.domain.OperationResultWithData
import fi.riista.mobile.R
import fi.riista.mobile.models.shootingTest.ShootingTestParticipant
import fi.riista.mobile.riistaSdkHelpers.toShootingTestParticipant
import fi.riista.mobile.utils.isResumed
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

// TODO: Do not allow edits for completed events
class ShootingTestEditPaymentActivity : BaseActivity() {
    private var participantId: Long = 0
    private var participant: ShootingTestParticipant? = null

    private lateinit var participantNameTextView: TextView
    private lateinit var participantHunterNumberTextView: TextView
    private lateinit var paymentsTotalAmountTextView: TextView
    private lateinit var paymentsPaidAmountSpinner: AppCompatSpinner
    private lateinit var paymentsRemainingAmountTextView: TextView
    private lateinit var testFinishedCheckBox: AppCompatCheckBox
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var paidAmountAdapter: ArrayAdapter<String?>

    private val shootingTestContext = RiistaSDK.shootingTestContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shooting_test_edit_payment)
        setCustomTitle(getString(R.string.shooting_test))

        participantId = intent.getLongExtra(EXTRA_PARTICIPANT_ID, -1)
        paidAmountAdapter = AmountPaidSpinnerAdapter(this, android.R.layout.simple_spinner_item)
        paidAmountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        participantNameTextView = findViewById(R.id.participant_name)
        participantHunterNumberTextView = findViewById(R.id.participant_hunter_number)
        paymentsTotalAmountTextView = findViewById(R.id.payments_total_amount)
        paymentsPaidAmountSpinner = findViewById(R.id.payments_paid_amount)
        paymentsRemainingAmountTextView = findViewById(R.id.payments_remaining_amount)
        testFinishedCheckBox = findViewById(R.id.test_finished)
        cancelButton = findViewById(R.id.cancel_btn)
        saveButton = findViewById(R.id.save_btn)

        paymentsPaidAmountSpinner.adapter = paidAmountAdapter
        paymentsPaidAmountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("DefaultLocale")
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                participant?.let { participant ->
                    val totalDueAmount: Int = participant.totalDueAmount
                    val paymentAmountChoice: Int = i * 20
                    paymentsRemainingAmountTextView.text = String.format("%d €", totalDueAmount - paymentAmountChoice)
                    testFinishedCheckBox.isChecked = paymentAmountChoice == totalDueAmount
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        cancelButton.setOnClickListener { onBackPressed() }
        saveButton.setOnClickListener {
            participant?.let { participant ->
                MainScope().launch {
                    val response = shootingTestContext.updatePaymentStateForParticipant(
                        participantId = participant.id,
                        participantRev = participant.rev,
                        paidAttempts = paymentsPaidAmountSpinner.selectedItemPosition,
                        testFinishedCheckBox.isChecked,
                    )

                    if (!lifecycle.isResumed()) {
                        return@launch
                    }

                    when (response) {
                        is OperationResult.Success -> onBackPressed()
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
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        if (participantId >= 0) {
            MainScope().launch {
                val response = shootingTestContext.fetchShootingTestParticipant(participantId)

                if (!lifecycle.isResumed()) {
                    return@launch
                }

                when (response) {
                    is OperationResultWithData.Success -> {
                        participant = response.data.toShootingTestParticipant()
                        updateUiFields()
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

    private fun updateUiFields() {
        participant?.let { participant ->
            participantNameTextView.text = String.format("%s %s", participant.firstName, participant.lastName)
            participantHunterNumberTextView.text = participant.hunterNumber
            paymentsTotalAmountTextView.text = String.format("%s €", participant.totalDueAmount)
            paymentsRemainingAmountTextView.text = String.format("%s €", participant.remainingAmount)
            testFinishedCheckBox.isSelected = participant.paidAmount == participant.totalDueAmount
            val values: MutableList<String?> = ArrayList()
            var i = 0
            while (i <= participant.totalDueAmount) {
                values.add("$i €")
                i += 20
            }
            paidAmountAdapter.clear()
            paidAmountAdapter.addAll(values)
            paidAmountAdapter.notifyDataSetChanged()
            paymentsPaidAmountSpinner.setSelection(participant.paidAmount / 20)
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    private class AmountPaidSpinnerAdapter(context: Context, resource: Int) :
        ArrayAdapter<String?>(context, resource)

    companion object {
        const val EXTRA_PARTICIPANT_ID = "extra_participant_id"
    }
}
