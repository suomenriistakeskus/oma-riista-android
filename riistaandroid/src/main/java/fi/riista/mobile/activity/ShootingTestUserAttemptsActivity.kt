package fi.riista.mobile.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.OperationResult
import fi.riista.common.domain.OperationResultWithData
import fi.riista.common.domain.model.ShootingTestType
import fi.riista.mobile.R
import fi.riista.mobile.adapter.ShootingTestUserAttempsAdapter
import fi.riista.mobile.models.shootingTest.ShootingTestAttemptDetailed
import fi.riista.mobile.models.shootingTest.ShootingTestParticipantDetailed
import fi.riista.mobile.riistaSdkHelpers.toShootingTestParticipantDetailed
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.ui.registerAlertDialogFragmentResultListener
import fi.riista.mobile.utils.DateTimeUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ShootingTestUserAttemptsActivity : BaseActivity() {
    private var participant: ShootingTestParticipantDetailed? = null
    private var participantId: Long = 0
    private var testCompleted = false

    private lateinit var adapter: ShootingTestUserAttempsAdapter
    private lateinit var participantNameTextView: TextView
    private lateinit var participantHunterNumberTextView: TextView
    private lateinit var participantDateOfBirthTextView: TextView
    private lateinit var attemptsListView: ListView
    private lateinit var addAttemptButton: AppCompatButton

    private val shootingTestContext = RiistaSDK.shootingTestContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shooting_test_user_attempts)
        setCustomTitle(getString(R.string.shooting_test_participant_attemps))

        participantNameTextView = findViewById(R.id.participant_name)
        participantHunterNumberTextView = findViewById(R.id.participant_hunter_number)
        participantDateOfBirthTextView = findViewById(R.id.participant_date_of_birth)
        attemptsListView = findViewById(R.id.list_participant_attempts)
        addAttemptButton = findViewById(R.id.add_attempt_button)
        addAttemptButton.setOnClickListener { onCreateAttemptClicked() }

        participantId = intent.getLongExtra(EXTRA_PARTICIPANT_ID, -1)
        testCompleted = intent.getBooleanExtra(EXTRA_TEST_COMPLETED, false)
        adapter = ShootingTestUserAttempsAdapter(ArrayList(), participantId, testCompleted, this, ::onDeleteAttemptClicked)
        attemptsListView.adapter = adapter
        addAttemptButton.isEnabled = !testCompleted

        registerAlertDialogFragmentResultListener(
            dialogId = AlertDialogId.SHOOTING_TEST_USER_ATTEMPTS_ADAPTER_REMOVE_ATTEMPT_QUESTION,
            onPositive = ::deleteAttempt
        )
    }

    public override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        MainScope().launch {
            val response = shootingTestContext.fetchShootingTestParticipantDetailed(participantId)

            if (isFinishing) {
                return@launch
            }

            when (response) {
                is OperationResultWithData.Success -> {
                    participant = response.data.toShootingTestParticipantDetailed()
                    participant?.let { participant ->
                        participantNameTextView.text =
                            String.format("%s %s", participant.lastName, participant.firstName)
                        participantHunterNumberTextView.text = participant.hunterNumber
                        participantDateOfBirthTextView.text = DateTimeUtils.convertDateStringToFinnishFormat(
                            participant.dateOfBirth
                        )
                        adapter.setParticipant(
                            participant.id,
                            participant.rev,
                            String.format("%s %s", participant.lastName, participant.firstName),
                            testCompleted
                        )
                        adapter.clear()
                        adapter.addAll(participant.attempts)
                        adapter.notifyDataSetChanged()
                        addAttemptButton.isEnabled = true
                    }
                }
                else -> {
                    // no-op
                }
            }
        }
    }

    private fun onCreateAttemptClicked() {
        if (!testCompleted && participant != null) {
            val intent = Intent(this, ShootingTestEditAttemptActivity::class.java)
            intent.putExtra(ShootingTestEditAttemptActivity.EXTRA_ATTEMPT, ShootingTestAttemptDetailed())
            intent.putExtra(EXTRA_PARTICIPANT_ID, participant!!.id)
            intent.putExtra(ShootingTestEditAttemptActivity.EXTRA_PARTICIPANT_REV, participant!!.rev)
            intent.putExtra(
                ShootingTestEditAttemptActivity.EXTRA_PARTICIPANT_NAME,
                String.format("%s %s", participant!!.lastName, participant!!.firstName)
            )
            setEditTypeLimitsTo(intent, null)
            startActivity(intent)
        }
    }

    private fun onDeleteAttemptClicked(attemptId: Long) {
        AlertDialogFragment.Builder(
            this,
            AlertDialogId.SHOOTING_TEST_USER_ATTEMPTS_ADAPTER_REMOVE_ATTEMPT_QUESTION,
        )
            .setMessage(getString(R.string.confirm_delete_attempt))
            .setPositiveButton(R.string.yes, attemptId.toString())
            .setNegativeButton(R.string.no)
            .build()
            .show(supportFragmentManager)
    }

    private fun deleteAttempt(attemptIdAsString: String?) {
        val attemptId = attemptIdAsString?.toLong() ?: return

        MainScope().launch {
            val response = shootingTestContext.removeShootingTestAttempt(attemptId)

            if (isFinishing) {
                return@launch
            }

            when (response) {
                is OperationResult.Success -> refreshData()
                is OperationResult.Failure -> {
                    val errorMsg = if (response.statusCode != null) {
                        getString(R.string.error_operation_failed, response.statusCode)
                    } else {
                        getString(R.string.operation_failed)
                    }
                    Toast.makeText(this@ShootingTestUserAttemptsActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun setEditTypeLimitsTo(intent: Intent, testType: ShootingTestType?) {
        var bearCount = 0
        var mooseCount = 0
        var roeDeerCount = 0
        var bowCount = 0
        for (item in participant!!.attempts) {
            if (ShootingTestType.BEAR === item.type) {
                bearCount++
            } else if (ShootingTestType.MOOSE === item.type) {
                mooseCount++
            } else if (ShootingTestType.ROE_DEER === item.type) {
                roeDeerCount++
            } else if (ShootingTestType.BOW === item.type) {
                bowCount++
            }
        }
        intent.putExtra(
            ShootingTestEditAttemptActivity.EXTRA_ENABLE_BEAR,
            bearCount < 5 || bearCount == 5 && ShootingTestType.BEAR === testType
        )
        intent.putExtra(
            ShootingTestEditAttemptActivity.EXTRA_ENABLE_MOOSE,
            mooseCount < 5 || mooseCount == 5 && ShootingTestType.MOOSE === testType
        )
        intent.putExtra(
            ShootingTestEditAttemptActivity.EXTRA_ENABLE_ROEDEER,
            roeDeerCount < 5 || roeDeerCount == 5 && ShootingTestType.ROE_DEER === testType
        )
        intent.putExtra(
            ShootingTestEditAttemptActivity.EXTRA_ENABLE_BOW,
            bowCount < 5 || bowCount == 5 && ShootingTestType.BOW === testType
        )
    }

    companion object {
        const val EXTRA_PARTICIPANT_ID = "extra_participant_id"
        const val EXTRA_TEST_COMPLETED = "extra_test_finished"
    }
}
