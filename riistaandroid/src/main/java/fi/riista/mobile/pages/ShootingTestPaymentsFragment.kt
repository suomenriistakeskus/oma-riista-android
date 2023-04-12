package fi.riista.mobile.pages

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.OperationResult
import fi.riista.mobile.R
import fi.riista.mobile.adapter.ShootingTestPaymentsAdapter
import fi.riista.mobile.models.shootingTest.ShootingTestCalendarEvent
import fi.riista.mobile.models.shootingTest.ShootingTestParticipant
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.ui.registerAlertDialogFragmentResultListener
import fi.riista.mobile.utils.JsonUtils
import fi.riista.mobile.viewmodel.ShootingTestMainViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShootingTestPaymentsFragment : ShootingTestTabContentFragment() {
    @JvmField
    @Inject
    var viewModelFactory: ViewModelProvider.Factory? = null

    private lateinit var viewModel: ShootingTestMainViewModel
    private lateinit var shootingTestPaymentsAdapter: ShootingTestPaymentsAdapter
    private lateinit var sumOfPaymentsTextView: TextView

    private val shootingTestContext = RiistaSDK.shootingTestContext

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
        return inflater.inflate(R.layout.fragment_shooting_test_payments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sumOfPaymentsTextView = view.findViewById(R.id.shooting_test_payments_sum)

        viewModel = ViewModelProvider(requireActivity(), viewModelFactory!!)[ShootingTestMainViewModel::class.java]

        shootingTestPaymentsAdapter = ShootingTestPaymentsAdapter(
            requireActivity(), ArrayList(), viewModel, ::onCompleteAllPaymentsClicked,
        )
        val listView = view.findViewById<ListView>(R.id.shooting_test_payments)
        listView.adapter = shootingTestPaymentsAdapter

        viewModel.participants.observe(viewLifecycleOwner) { shootingTestParticipants: List<ShootingTestParticipant?>? ->
            shootingTestPaymentsAdapter.clear()
            if (shootingTestParticipants != null) {
                shootingTestPaymentsAdapter.addAll(shootingTestParticipants)
            }
            shootingTestPaymentsAdapter.sort { o1: ShootingTestParticipant?, o2: ShootingTestParticipant? ->
                if (o1 == null) {
                    return@sort -1
                } else if (o2 == null) {
                    return@sort 1
                } else if (o1.completed != o2.completed) {
                    return@sort if (o1.completed) 1 else -1
                } else if (o1.attempts.isEmpty() != o2.attempts.isEmpty()) {
                    return@sort if (o1.attempts.isEmpty()) -1 else 1
                } else {
                    return@sort o1.registrationTime?.compareTo(o2.registrationTime ?: "") ?: -1
                }
            }
            shootingTestPaymentsAdapter.notifyDataSetChanged()
        }
        viewModel.calendarEvent.observe(viewLifecycleOwner) { calendarEvent: ShootingTestCalendarEvent? ->
            if (calendarEvent != null) {
                shootingTestPaymentsAdapter.setEditEnabled(!calendarEvent.isClosed)
                sumOfPaymentsTextView.text = String.format(
                    getString(R.string.shooting_test_event_sum_of_payments),
                    calendarEvent.totalPaidAmount
                )
            }
        }

        requireActivity().registerAlertDialogFragmentResultListener(
            dialogId = AlertDialogId.SHOOTING_TEST_PAYMENTS_FRAGMENT_PAYMENT_CONFIRM,
            onPositive = ::onCompleteAllPayments
        )
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun onCompleteAllPaymentsClicked(participant: ShootingTestParticipant) {
        val msg = requireContext().getString(
            R.string.shooting_test_payment_confirm_done,
            participant.lastName, participant.firstName, participant.hunterNumber
        )
        AlertDialogFragment.Builder(requireContext(), AlertDialogId.SHOOTING_TEST_PAYMENTS_FRAGMENT_PAYMENT_CONFIRM)
            .setMessage(msg)
            .setPositiveButton(R.string.yes, JsonUtils.objectToJson(participant))
            .setNegativeButton(R.string.no)
            .build()
            .show(requireActivity().supportFragmentManager)
    }

    private fun onCompleteAllPayments(s: String?) {
        val participant = JsonUtils.jsonToObject(s, ShootingTestParticipant::class.java)
        completeAllPayments(participant)
    }

    private fun completeAllPayments(participant: ShootingTestParticipant) {
        MainScope().launch {
            val response = shootingTestContext.completeAllPaymentsForParticipant(
                participantId = participant.id,
                participantRev = participant.rev,
            )

            if (!isResumed) {
                return@launch
            }

            when (response) {
                is OperationResult.Success -> {
                    viewModel.refreshCalendarEvent()
                    viewModel.refreshParticipants()
                }
                is OperationResult.Failure -> {
                    val errorMsg = if (response.statusCode != null) {
                        requireContext().getString(R.string.error_operation_failed, response.statusCode)
                    } else {
                        requireContext().getString(R.string.operation_failed)
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun refreshData() {
        viewModel.refreshCalendarEvent()
        viewModel.refreshParticipants()
    }
}
